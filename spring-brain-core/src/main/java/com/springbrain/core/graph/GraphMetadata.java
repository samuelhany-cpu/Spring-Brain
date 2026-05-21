package com.springbrain.core.graph;

public record GraphMetadata(
        String tool,
        String toolVersion,
        String projectName,
        String generatedAt,
        String sourceMode,
        String language,
        String framework
) {}
