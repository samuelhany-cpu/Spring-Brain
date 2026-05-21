package com.springbrain.core.graph;

public record GraphEdge(
        String id,
        String from,
        String to,
        String type
) {}
