package com.springbrain.core.graph;

import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.scanner.SpringAnnotationScanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class GraphBuilderTest {

    @TempDir
    Path tempDir;

    private Path fixtureFile(String name) throws URISyntaxException {
        return Paths.get(getClass().getClassLoader().getResource("fixtures/" + name).toURI());
    }

    private ProjectModel scanFixtures(String... fileNames) throws Exception {
        Path srcMain = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcMain);
        for (String name : fileNames) {
            Files.copy(fixtureFile(name), srcMain.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        }
        return SpringAnnotationScanner.scan(tempDir);
    }

    private GraphDocument buildFullGraph() throws Exception {
        ProjectModel model = scanFixtures(
                "UserController.java", "UserService.java",
                "UserRepository.java", "User.java", "AppConfig.java");
        return GraphBuilder.build(model, "test-project", Instant.parse("2026-01-01T00:00:00Z"));
    }

    // ── Schema version ────────────────────────────────────────────────────────

    @Test
    void schemaVersionIsOnePointZero() throws Exception {
        assertThat(buildFullGraph().schemaVersion()).isEqualTo("1.0.0");
    }

    // ── Metadata ──────────────────────────────────────────────────────────────

    @Test
    void metadataContainsProjectName() throws Exception {
        assertThat(buildFullGraph().metadata().projectName()).isEqualTo("test-project");
    }

    @Test
    void metadataSourceModeIsStatic() throws Exception {
        assertThat(buildFullGraph().metadata().sourceMode()).isEqualTo("static");
    }

    @Test
    void metadataLanguageIsJava() throws Exception {
        assertThat(buildFullGraph().metadata().language()).isEqualTo("java");
    }

    @Test
    void metadataFrameworkIsSpringBoot() throws Exception {
        assertThat(buildFullGraph().metadata().framework()).isEqualTo("spring-boot");
    }

    // ── Route nodes ───────────────────────────────────────────────────────────

    @Test
    void buildsRouteNodes() throws Exception {
        List<String> routeIds = buildFullGraph().nodes().stream()
                .filter(n -> n.type().equals("route"))
                .map(GraphNode::id)
                .toList();
        assertThat(routeIds).containsExactlyInAnyOrder(
                "route:GET:/api/users",
                "route:GET:/api/users/{id}",
                "route:POST:/api/users",
                "route:PUT:/api/users/{id}",
                "route:DELETE:/api/users/{id}");
    }

    // ── Controller nodes ──────────────────────────────────────────────────────

    @Test
    void buildsControllerMethodNodes() throws Exception {
        List<String> ctrlIds = buildFullGraph().nodes().stream()
                .filter(n -> n.type().equals("controller"))
                .map(GraphNode::id)
                .toList();
        assertThat(ctrlIds).containsExactlyInAnyOrder(
                "controller:com.example.user.UserController#getAll",
                "controller:com.example.user.UserController#getById",
                "controller:com.example.user.UserController#create",
                "controller:com.example.user.UserController#update",
                "controller:com.example.user.UserController#delete");
    }

    // ── Service node ──────────────────────────────────────────────────────────

    @Test
    void buildsServiceNode() throws Exception {
        assertThat(buildFullGraph().nodes()).anyMatch(n ->
                n.id().equals("service:com.example.user.UserService")
                && n.type().equals("service"));
    }

    // ── Repository node ───────────────────────────────────────────────────────

    @Test
    void buildsRepositoryNode() throws Exception {
        assertThat(buildFullGraph().nodes()).anyMatch(n ->
                n.id().equals("repository:com.example.user.UserRepository")
                && n.type().equals("repository"));
    }

    // ── Entity node ───────────────────────────────────────────────────────────

    @Test
    void buildsEntityNode() throws Exception {
        assertThat(buildFullGraph().nodes()).anyMatch(n ->
                n.id().equals("entity:com.example.user.User")
                && n.type().equals("entity"));
    }

    // ── Config property nodes ─────────────────────────────────────────────────

    @Test
    void buildsConfigPropertyNodes() throws Exception {
        List<String> configIds = buildFullGraph().nodes().stream()
                .filter(n -> n.type().equals("config_property"))
                .map(GraphNode::id)
                .toList();
        assertThat(configIds).containsExactlyInAnyOrder(
                "config:app.name", "config:app.timeout", "config:jwt.secret");
    }

    @Test
    void configPropertyNodesAreDeduplicatedByKey() throws Exception {
        long count = buildFullGraph().nodes().stream()
                .filter(n -> n.id().equals("config:app.name"))
                .count();
        assertThat(count).isEqualTo(1);
    }

    // ── maps_to edges ─────────────────────────────────────────────────────────

    @Test
    void buildsFiveMapsToEdges() throws Exception {
        long count = buildFullGraph().edges().stream()
                .filter(e -> e.type().equals("maps_to"))
                .count();
        assertThat(count).isEqualTo(5);
    }

    @Test
    void mapsToEdgeConnectsRouteToControllerMethod() throws Exception {
        assertThat(buildFullGraph().edges()).anyMatch(e ->
                e.type().equals("maps_to")
                && e.from().equals("route:GET:/api/users/{id}")
                && e.to().equals("controller:com.example.user.UserController#getById"));
    }

    // ── manages edge ──────────────────────────────────────────────────────────

    @Test
    void buildsManagesEdgeFromRepositoryToEntity() throws Exception {
        assertThat(buildFullGraph().edges()).anyMatch(e ->
                e.type().equals("manages")
                && e.from().equals("repository:com.example.user.UserRepository")
                && e.to().equals("entity:com.example.user.User"));
    }

    // ── calls edges via interface injection (controller → service impl) ──────

    @Test
    void buildsCallsEdgeWhenControllerInjectsServiceByInterface() throws Exception {
        // UserController injects UserService (interface); UserServiceImpl implements UserService.
        // GraphBuilder must resolve the interface name to the impl node.
        ProjectModel model = scanFixtures(
                "UserController.java", "UserServiceImpl.java",
                "UserRepository.java", "User.java");
        GraphDocument graph = GraphBuilder.build(model, "test-project", Instant.parse("2026-01-01T00:00:00Z"));

        List<GraphEdge> calls = graph.edges().stream()
                .filter(e -> e.type().equals("calls")
                          && e.to().equals("service:com.example.user.UserServiceImpl"))
                .toList();
        assertThat(calls).isNotEmpty();
    }

    // ── calls edges (controller → service) ───────────────────────────────────

    @Test
    void buildsCallsEdgesFromEachControllerMethodToService() throws Exception {
        List<GraphEdge> calls = buildFullGraph().edges().stream()
                .filter(e -> e.type().equals("calls")
                          && e.to().equals("service:com.example.user.UserService"))
                .toList();
        assertThat(calls).hasSize(5);
    }

    // ── calls edge (service → repository) ────────────────────────────────────

    @Test
    void buildsCallsEdgeFromServiceToRepository() throws Exception {
        assertThat(buildFullGraph().edges()).anyMatch(e ->
                e.type().equals("calls")
                && e.from().equals("service:com.example.user.UserService")
                && e.to().equals("repository:com.example.user.UserRepository"));
    }

    // ── Graph integrity ───────────────────────────────────────────────────────

    @Test
    void nodeIdsAreUnique() throws Exception {
        List<String> ids = buildFullGraph().nodes().stream().map(GraphNode::id).toList();
        assertThat(ids).doesNotHaveDuplicates();
    }

    @Test
    void edgeIdsAreUnique() throws Exception {
        List<String> ids = buildFullGraph().edges().stream().map(GraphEdge::id).toList();
        assertThat(ids).doesNotHaveDuplicates();
    }

    @Test
    void allEdgeEndpointsExistAsNodes() throws Exception {
        GraphDocument graph = buildFullGraph();
        Set<String> nodeIds = graph.nodes().stream()
                .map(GraphNode::id)
                .collect(Collectors.toSet());
        for (GraphEdge edge : graph.edges()) {
            assertThat(nodeIds).as("edge.from=%s not in nodes", edge.from()).contains(edge.from());
            assertThat(nodeIds).as("edge.to=%s not in nodes", edge.to()).contains(edge.to());
        }
    }

    // ── Determinism & sorting ─────────────────────────────────────────────────

    @Test
    void graphIsDeterministic() throws Exception {
        assertThat(buildFullGraph().nodes()).isEqualTo(buildFullGraph().nodes());
        assertThat(buildFullGraph().edges()).isEqualTo(buildFullGraph().edges());
    }

    @Test
    void nodesSortedByTypeThenId() throws Exception {
        List<GraphNode> nodes = buildFullGraph().nodes();
        List<GraphNode> sorted = nodes.stream()
                .sorted(Comparator.comparing(GraphNode::type).thenComparing(GraphNode::id))
                .toList();
        assertThat(nodes).isEqualTo(sorted);
    }

    @Test
    void edgesSortedByTypeThenFromThenTo() throws Exception {
        List<GraphEdge> edges = buildFullGraph().edges();
        List<GraphEdge> sorted = edges.stream()
                .sorted(Comparator.comparing(GraphEdge::type)
                        .thenComparing(GraphEdge::from)
                        .thenComparing(GraphEdge::to))
                .toList();
        assertThat(edges).isEqualTo(sorted);
    }
}
