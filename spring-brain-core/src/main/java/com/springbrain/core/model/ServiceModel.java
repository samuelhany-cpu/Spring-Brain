package com.springbrain.core.model;

import java.nio.file.Path;

/**
 * Represents a Spring service class. Populated in Milestone 1.
 */
public final class ServiceModel {

    private final String className;
    private final String qualifiedName;
    private final Path file;
    private final int line;

    public ServiceModel(String className, String qualifiedName, Path file, int line) {
        this.className = className;
        this.qualifiedName = qualifiedName;
        this.file = file;
        this.line = line;
    }

    public String getClassName() { return className; }
    public String getQualifiedName() { return qualifiedName; }
    public Path getFile() { return file; }
    public int getLine() { return line; }
}
