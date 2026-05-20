package com.springbrain.core.model;

import java.nio.file.Path;

/**
 * Represents a @Value("${...}") property usage in source code. Populated in Milestone 1.
 */
public final class ConfigPropertyUsageModel {

    private final String propertyKey;
    private final Path file;
    private final int line;

    public ConfigPropertyUsageModel(String propertyKey, Path file, int line) {
        this.propertyKey = propertyKey;
        this.file = file;
        this.line = line;
    }

    public String getPropertyKey() { return propertyKey; }
    public Path getFile() { return file; }
    public int getLine() { return line; }
}
