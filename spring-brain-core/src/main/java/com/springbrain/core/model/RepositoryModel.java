package com.springbrain.core.model;

import java.nio.file.Path;

/**
 * Represents a Spring Data repository interface. Populated in Milestone 1.
 */
public final class RepositoryModel {

    private final String interfaceName;
    private final String qualifiedName;
    private final String entityType;
    private final String idType;
    private final Path file;
    private final int line;

    public RepositoryModel(String interfaceName, String qualifiedName, String entityType,
                           String idType, Path file, int line) {
        this.interfaceName = interfaceName;
        this.qualifiedName = qualifiedName;
        this.entityType = entityType;
        this.idType = idType;
        this.file = file;
        this.line = line;
    }

    public String getInterfaceName() { return interfaceName; }
    public String getQualifiedName() { return qualifiedName; }
    public String getEntityType() { return entityType; }
    public String getIdType() { return idType; }
    public Path getFile() { return file; }
    public int getLine() { return line; }
}
