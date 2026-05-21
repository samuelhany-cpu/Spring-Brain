package com.springbrain.core.model;

import java.nio.file.Path;

public final class ConfigPropertyUsageModel {

    private final String propertyKey;
    private final Path file;
    private final int line;
    private final String ownerQualifiedName;

    public ConfigPropertyUsageModel(String propertyKey, Path file, int line,
                                    String ownerQualifiedName) {
        this.propertyKey = propertyKey;
        this.file = file;
        this.line = line;
        this.ownerQualifiedName = ownerQualifiedName;
    }

    public String getPropertyKey() { return propertyKey; }
    public Path getFile() { return file; }
    public int getLine() { return line; }
    public String getOwnerQualifiedName() { return ownerQualifiedName; }
}
