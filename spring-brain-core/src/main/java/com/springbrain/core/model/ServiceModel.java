package com.springbrain.core.model;

import java.nio.file.Path;
import java.util.List;

public final class ServiceModel {

    private final String className;
    private final String qualifiedName;
    private final Path file;
    private final int line;
    private final List<String> injectedTypeNames;

    public ServiceModel(String className, String qualifiedName, Path file, int line,
                        List<String> injectedTypeNames) {
        this.className = className;
        this.qualifiedName = qualifiedName;
        this.file = file;
        this.line = line;
        this.injectedTypeNames = List.copyOf(injectedTypeNames);
    }

    public String getClassName() { return className; }
    public String getQualifiedName() { return qualifiedName; }
    public Path getFile() { return file; }
    public int getLine() { return line; }
    public List<String> getInjectedTypeNames() { return injectedTypeNames; }
}
