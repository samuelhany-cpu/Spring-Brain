package com.springbrain.core.scanner;

import com.springbrain.core.model.ConfigPropertyUsageModel;
import com.springbrain.core.model.BeanModel;
import com.springbrain.core.model.ControllerModel;
import com.springbrain.core.model.EntityModel;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.model.RepositoryModel;
import com.springbrain.core.model.RouteModel;
import com.springbrain.core.model.ServiceModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpringAnnotationScannerTest {

    @TempDir
    Path tempDir;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Path fixtureFile(String name) throws URISyntaxException {
        return Paths.get(getClass().getClassLoader().getResource("fixtures/" + name).toURI());
    }

    private ProjectModel scanFixture(String... fileNames) throws Exception {
        Path srcMain = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcMain);
        for (String name : fileNames) {
            Path fixture = fixtureFile(name);
            Files.copy(fixture, srcMain.resolve(name));
        }
        return SpringAnnotationScanner.scan(tempDir);
    }

    // ── Controller detection ──────────────────────────────────────────────────

    @Test
    void detectsRestController() throws Exception {
        ProjectModel model = scanFixture("UserController.java");

        assertThat(model.getControllers()).hasSize(1);
        ControllerModel controller = model.getControllers().get(0);
        assertThat(controller.getClassName()).isEqualTo("UserController");
        assertThat(controller.getQualifiedName()).isEqualTo("com.example.user.UserController");
    }

    @Test
    void detectsControllerLineNumber() throws Exception {
        ProjectModel model = scanFixture("UserController.java");

        ControllerModel controller = model.getControllers().get(0);
        assertThat(controller.getLine()).isGreaterThan(0);
    }

    // ── Route detection ───────────────────────────────────────────────────────

    @Test
    void detectsGetMappingRoute() throws Exception {
        ProjectModel model = scanFixture("UserController.java");

        List<RouteModel> routes = model.getControllers().get(0).getRoutes();
        assertThat(routes).anyMatch(r ->
                r.getHttpMethod().equals("GET") && r.getPath().equals("/api/users/{id}"));
    }

    @Test
    void detectsPostMappingRoute() throws Exception {
        ProjectModel model = scanFixture("UserController.java");

        List<RouteModel> routes = model.getControllers().get(0).getRoutes();
        assertThat(routes).anyMatch(r ->
                r.getHttpMethod().equals("POST") && r.getPath().equals("/api/users"));
    }

    @Test
    void detectsDeleteMappingRoute() throws Exception {
        ProjectModel model = scanFixture("UserController.java");

        List<RouteModel> routes = model.getControllers().get(0).getRoutes();
        assertThat(routes).anyMatch(r ->
                r.getHttpMethod().equals("DELETE") && r.getPath().equals("/api/users/{id}"));
    }

    @Test
    void detectsPutMappingRoute() throws Exception {
        ProjectModel model = scanFixture("UserController.java");

        List<RouteModel> routes = model.getControllers().get(0).getRoutes();
        assertThat(routes).anyMatch(r ->
                r.getHttpMethod().equals("PUT") && r.getPath().equals("/api/users/{id}"));
    }

    @Test
    void joinsClassLevelAndMethodLevelPaths() throws Exception {
        // UserController has @RequestMapping("/api/users") + @GetMapping("/{id}")
        ProjectModel model = scanFixture("UserController.java");

        List<RouteModel> routes = model.getControllers().get(0).getRoutes();
        assertThat(routes).anyMatch(r ->
                r.getPath().equals("/api/users/{id}") && !r.getPath().contains("//"));
    }

    @Test
    void detectsGetAllRouteWithNoMethodPath() throws Exception {
        // @GetMapping with no value → just the class-level path /api/users
        ProjectModel model = scanFixture("UserController.java");

        List<RouteModel> routes = model.getControllers().get(0).getRoutes();
        assertThat(routes).anyMatch(r ->
                r.getHttpMethod().equals("GET") && r.getPath().equals("/api/users"));
    }

    @Test
    void routeContainsMethodName() throws Exception {
        ProjectModel model = scanFixture("UserController.java");

        List<RouteModel> routes = model.getControllers().get(0).getRoutes();
        assertThat(routes).anyMatch(r -> r.getMethodName().equals("getById"));
    }

    @Test
    void routeContainsControllerClass() throws Exception {
        ProjectModel model = scanFixture("UserController.java");

        List<RouteModel> routes = model.getControllers().get(0).getRoutes();
        assertThat(routes).allMatch(r ->
                r.getControllerClass().equals("com.example.user.UserController"));
    }

    // ── Service detection ─────────────────────────────────────────────────────

    @Test
    void detectsService() throws Exception {
        ProjectModel model = scanFixture("UserService.java");

        assertThat(model.getServices()).hasSize(1);
        ServiceModel service = model.getServices().get(0);
        assertThat(service.getClassName()).isEqualTo("UserService");
        assertThat(service.getQualifiedName()).isEqualTo("com.example.user.UserService");
    }

    @Test
    void detectsServiceLineNumber() throws Exception {
        ProjectModel model = scanFixture("UserService.java");

        assertThat(model.getServices().get(0).getLine()).isGreaterThan(0);
    }

    // ── Repository detection ──────────────────────────────────────────────────

    @Test
    void detectsRepository() throws Exception {
        ProjectModel model = scanFixture("UserRepository.java");

        assertThat(model.getRepositories()).hasSize(1);
        RepositoryModel repo = model.getRepositories().get(0);
        assertThat(repo.getInterfaceName()).isEqualTo("UserRepository");
        assertThat(repo.getQualifiedName()).isEqualTo("com.example.user.UserRepository");
    }

    @Test
    void extractsRepositoryEntityType() throws Exception {
        ProjectModel model = scanFixture("UserRepository.java");

        RepositoryModel repo = model.getRepositories().get(0);
        assertThat(repo.getEntityType()).isEqualTo("User");
    }

    @Test
    void extractsRepositoryIdType() throws Exception {
        ProjectModel model = scanFixture("UserRepository.java");

        RepositoryModel repo = model.getRepositories().get(0);
        assertThat(repo.getIdType()).isEqualTo("Long");
    }

    // ── Entity detection ──────────────────────────────────────────────────────

    @Test
    void detectsEntity() throws Exception {
        ProjectModel model = scanFixture("User.java");

        assertThat(model.getEntities()).hasSize(1);
        EntityModel entity = model.getEntities().get(0);
        assertThat(entity.getClassName()).isEqualTo("User");
        assertThat(entity.getQualifiedName()).isEqualTo("com.example.user.User");
    }

    @Test
    void detectsEntityLineNumber() throws Exception {
        ProjectModel model = scanFixture("User.java");

        assertThat(model.getEntities().get(0).getLine()).isGreaterThan(0);
    }

    // ── @Value property detection ─────────────────────────────────────────────

    @Test
    void detectsValueAnnotationProperties() throws Exception {
        ProjectModel model = scanFixture("AppConfig.java");

        List<ConfigPropertyUsageModel> props = model.getConfigPropertyUsages();
        assertThat(props).hasSize(3);
    }

    @Test
    void extractsValuePropertyKeys() throws Exception {
        ProjectModel model = scanFixture("AppConfig.java");

        List<String> keys = model.getConfigPropertyUsages().stream()
                .map(ConfigPropertyUsageModel::getPropertyKey)
                .toList();
        assertThat(keys).containsExactlyInAnyOrder("app.name", "app.timeout", "jwt.secret");
    }

    @Test
    void valuePropertyUsageContainsFileAndLine() throws Exception {
        ProjectModel model = scanFixture("AppConfig.java");

        assertThat(model.getConfigPropertyUsages()).allMatch(p ->
                p.getFile() != null && p.getLine() > 0);
    }

    // ── Multi-file scan ───────────────────────────────────────────────────────

    @Test
    void scansMultipleFilesIntoSingleProjectModel() throws Exception {
        ProjectModel model = scanFixture(
                "UserController.java", "UserService.java",
                "UserRepository.java", "User.java", "AppConfig.java");

        assertThat(model.getControllers()).hasSize(1);
        assertThat(model.getServices()).hasSize(1);
        assertThat(model.getRepositories()).hasSize(1);
        assertThat(model.getEntities()).hasSize(1);
        assertThat(model.getConfigPropertyUsages()).hasSize(3);
    }

    @Test
    void scannedFilesHaveRelativePaths() throws Exception {
        ProjectModel model = scanFixture("UserController.java");

        ControllerModel controller = model.getControllers().get(0);
        assertThat(controller.getFile().isAbsolute()).isFalse();
    }

    // ── Injected type detection ───────────────────────────────────────────────

    @Test
    void detectsControllerConstructorInjectedTypes() throws Exception {
        ProjectModel model = scanFixture("UserController.java");

        ControllerModel controller = model.getControllers().get(0);
        assertThat(controller.getInjectedTypeNames()).contains("UserService");
    }

    @Test
    void detectsServiceConstructorInjectedTypes() throws Exception {
        ProjectModel model = scanFixture("UserService.java");

        ServiceModel service = model.getServices().get(0);
        assertThat(service.getInjectedTypeNames()).contains("UserRepository");
    }

    // ── Config property owner ─────────────────────────────────────────────────

    @Test
    void configPropertyUsageHasOwnerQualifiedName() throws Exception {
        ProjectModel model = scanFixture("AppConfig.java");

        assertThat(model.getConfigPropertyUsages()).allMatch(p ->
                p.getOwnerQualifiedName().equals("com.example.config.AppConfig"));
    }

    // ── Service implemented interfaces ────────────────────────────────────────

    @Test
    void detectsImplementedInterfaceNamesForServiceImpl() throws Exception {
        ProjectModel model = scanFixture("UserServiceImpl.java");

        ServiceModel service = model.getServices().get(0);
        assertThat(service.getImplementedInterfaceNames()).containsExactly("UserService");
    }

    @Test
    void implementedInterfaceNamesEmptyWhenServiceImplementsNothing() throws Exception {
        ProjectModel model = scanFixture("UserService.java");

        ServiceModel service = model.getServices().get(0);
        assertThat(service.getImplementedInterfaceNames()).isEmpty();
    }

    // ── Java 15+ syntax (text blocks) ─────────────────────────────────────────

    @Test
    void scannerHandlesTextBlockLiterals() throws Exception {
        ProjectModel model = scanFixture("ServiceWithTextBlock.java");

        assertThat(model.getServices()).hasSize(1);
        assertThat(model.getServices().get(0).getClassName()).isEqualTo("ServiceWithTextBlock");
    }

    // ── Generic Spring bean detection ─────────────────────────────────────────────

    @Test
    void detectsGenericSpringBeans() throws Exception {
        ProjectModel model = scanFixture(
                "BillingComponent.java",
                "SecurityConfiguration.java",
                "ApiExceptionHandler.java",
                "UserRepository.java");

        assertThat(model.getBeans())
                .extracting(BeanModel::getClassName, BeanModel::getBeanType)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("BillingComponent", "component"),
                        org.assertj.core.groups.Tuple.tuple("SecurityConfiguration", "configuration"),
                        org.assertj.core.groups.Tuple.tuple("ApiExceptionHandler", "controller_advice"),
                        org.assertj.core.groups.Tuple.tuple("UserRepository", "repository"));
    }

    @Test
    void beanModelContainsLocationAndQualifiedName() throws Exception {
        ProjectModel model = scanFixture("BillingComponent.java");

        BeanModel bean = model.getBeans().get(0);
        assertThat(bean.getQualifiedName()).isEqualTo("com.example.billing.BillingComponent");
        assertThat(bean.getFile().isAbsolute()).isFalse();
        assertThat(bean.getLine()).isGreaterThan(0);
    }

    @Test
    void beanModelContainsInjectedTypeNames() throws Exception {
        ProjectModel model = scanFixture("BeanDependencyService.java", "BillingComponent.java");

        BeanModel bean = model.getBeans().stream()
                .filter(b -> b.getClassName().equals("BeanDependencyService"))
                .findFirst()
                .orElseThrow();
        assertThat(bean.getBeanType()).isEqualTo("service");
        assertThat(bean.getInjectedTypeNames()).containsExactly("BillingComponent");
    }
}
