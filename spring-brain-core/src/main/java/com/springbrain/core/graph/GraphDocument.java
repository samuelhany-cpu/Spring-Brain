package com.springbrain.core.graph;

import java.util.List;

public record GraphDocument(
        String schemaVersion,
        GraphMetadata metadata,
        List<GraphNode> nodes,
        List<GraphEdge> edges
) {
    public GraphDocument {
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
    }
}
