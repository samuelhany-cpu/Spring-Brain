package com.springbrain.core.graph;

public record GraphNode(
        String id,
        String type,
        String label,
        String qualifiedName,
        String file,
        int line
) {}
