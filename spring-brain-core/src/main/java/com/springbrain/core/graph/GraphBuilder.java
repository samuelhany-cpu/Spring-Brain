package com.springbrain.core.graph;

import com.springbrain.core.SpringBrainVersion;
import com.springbrain.core.model.ConfigPropertyUsageModel;
import com.springbrain.core.model.ControllerModel;
import com.springbrain.core.model.EntityModel;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.model.RepositoryModel;
import com.springbrain.core.model.RouteModel;
import com.springbrain.core.model.ServiceModel;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GraphBuilder {

    private GraphBuilder() {}

    public static GraphDocument build(ProjectModel model, String projectName, Instant generatedAt) {
        Map<String, GraphNode> nodes = new LinkedHashMap<>();
        Map<String, GraphEdge> edges = new LinkedHashMap<>();

        // Lookup maps for matching injected type names to known components
        // Index by class name AND any implemented interface names so controllers that
        // inject a service interface (e.g. TourService) still match the impl (TourServiceImpl).
        Map<String, ServiceModel> servicesByClassName = new HashMap<>();
        for (ServiceModel s : model.getServices()) {
            servicesByClassName.put(s.getClassName(), s);
            for (String iface : s.getImplementedInterfaceNames()) {
                servicesByClassName.putIfAbsent(iface, s);
            }
        }

        Map<String, RepositoryModel> reposByInterfaceName = new HashMap<>();
        for (RepositoryModel r : model.getRepositories()) {
            reposByInterfaceName.put(r.getInterfaceName(), r);
        }

        Map<String, EntityModel> entitiesByClassName = new HashMap<>();
        for (EntityModel e : model.getEntities()) {
            entitiesByClassName.put(e.getClassName(), e);
        }

        // Tracks which component qualified names map to node IDs (for uses_config edges)
        Map<String, String> qualifiedNameToNodeId = new HashMap<>();

        // 1. Route nodes + controller method nodes + maps_to edges + calls edges (ctrl→svc)
        for (ControllerModel controller : model.getControllers()) {
            for (RouteModel route : controller.getRoutes()) {
                String routeId = "route:" + route.getHttpMethod() + ":" + route.getPath();
                String ctrlId = "controller:" + route.getControllerClass() + "#" + route.getMethodName();
                String filePath = route.getFile().toString().replace('\\', '/');

                putNode(nodes, new GraphNode(
                        routeId, "route",
                        route.getHttpMethod() + " " + route.getPath(),
                        routeId,
                        filePath,
                        route.getLine()));

                putNode(nodes, new GraphNode(
                        ctrlId, "controller",
                        controller.getClassName() + "#" + route.getMethodName(),
                        route.getControllerClass() + "#" + route.getMethodName(),
                        filePath,
                        route.getLine()));

                putEdge(edges, new GraphEdge(
                        "edge:maps_to:" + routeId + "->" + ctrlId,
                        routeId, ctrlId, "maps_to"));

                // calls edges from each controller method to its injected services
                for (String injectedType : controller.getInjectedTypeNames()) {
                    ServiceModel svc = servicesByClassName.get(injectedType);
                    if (svc != null) {
                        String svcId = "service:" + svc.getQualifiedName();
                        putEdge(edges, new GraphEdge(
                                "edge:calls:" + ctrlId + "->" + svcId,
                                ctrlId, svcId, "calls"));
                    }
                }
            }
        }

        // 2. Service nodes + calls edges (svc→repo)
        for (ServiceModel svc : model.getServices()) {
            String svcId = "service:" + svc.getQualifiedName();
            putNode(nodes, new GraphNode(
                    svcId, "service",
                    svc.getClassName(),
                    svc.getQualifiedName(),
                    svc.getFile().toString().replace('\\', '/'),
                    svc.getLine()));
            qualifiedNameToNodeId.put(svc.getQualifiedName(), svcId);

            for (String injectedType : svc.getInjectedTypeNames()) {
                RepositoryModel repo = reposByInterfaceName.get(injectedType);
                if (repo != null) {
                    String repoId = "repository:" + repo.getQualifiedName();
                    putEdge(edges, new GraphEdge(
                            "edge:calls:" + svcId + "->" + repoId,
                            svcId, repoId, "calls"));
                }
            }
        }

        // 3. Repository nodes + manages edges (repo→entity)
        for (RepositoryModel repo : model.getRepositories()) {
            String repoId = "repository:" + repo.getQualifiedName();
            putNode(nodes, new GraphNode(
                    repoId, "repository",
                    repo.getInterfaceName(),
                    repo.getQualifiedName(),
                    repo.getFile().toString().replace('\\', '/'),
                    repo.getLine()));

            if (!repo.getEntityType().isEmpty()) {
                EntityModel entity = entitiesByClassName.get(repo.getEntityType());
                if (entity != null) {
                    String entityId = "entity:" + entity.getQualifiedName();
                    putEdge(edges, new GraphEdge(
                            "edge:manages:" + repoId + "->" + entityId,
                            repoId, entityId, "manages"));
                }
            }
        }

        // 4. Entity nodes
        for (EntityModel entity : model.getEntities()) {
            String entityId = "entity:" + entity.getQualifiedName();
            putNode(nodes, new GraphNode(
                    entityId, "entity",
                    entity.getClassName(),
                    entity.getQualifiedName(),
                    entity.getFile().toString().replace('\\', '/'),
                    entity.getLine()));
        }

        // 5. Config property nodes (deduplicated by key) + uses_config edges
        Set<String> seenKeys = new LinkedHashSet<>();
        for (ConfigPropertyUsageModel usage : model.getConfigPropertyUsages()) {
            String configId = "config:" + usage.getPropertyKey();
            if (seenKeys.add(usage.getPropertyKey())) {
                putNode(nodes, new GraphNode(
                        configId, "config_property",
                        usage.getPropertyKey(),
                        usage.getPropertyKey(),
                        usage.getFile().toString().replace('\\', '/'),
                        usage.getLine()));
            }
            String ownerNodeId = qualifiedNameToNodeId.get(usage.getOwnerQualifiedName());
            if (ownerNodeId != null) {
                putEdge(edges, new GraphEdge(
                        "edge:uses_config:" + ownerNodeId + "->" + configId,
                        ownerNodeId, configId, "uses_config"));
            }
        }

        List<GraphNode> sortedNodes = nodes.values().stream()
                .sorted(Comparator.comparing(GraphNode::type).thenComparing(GraphNode::id))
                .toList();

        List<GraphEdge> sortedEdges = edges.values().stream()
                .sorted(Comparator.comparing(GraphEdge::type)
                        .thenComparing(GraphEdge::from)
                        .thenComparing(GraphEdge::to))
                .toList();

        GraphMetadata metadata = new GraphMetadata(
                SpringBrainVersion.TOOL_NAME,
                SpringBrainVersion.VERSION,
                projectName,
                generatedAt.toString(),
                "static",
                "java",
                "spring-boot");

        return new GraphDocument("1.0.0", metadata, sortedNodes, sortedEdges);
    }

    private static void putNode(Map<String, GraphNode> nodes, GraphNode node) {
        nodes.putIfAbsent(node.id(), node);
    }

    private static void putEdge(Map<String, GraphEdge> edges, GraphEdge edge) {
        edges.putIfAbsent(edge.id(), edge);
    }
}
