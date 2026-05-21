package com.springbrain.core.model;

import java.nio.file.Path;
import java.util.List;

public final class BeanModel {

    private final String className;
    private final String qualifiedName;
    private final String beanType;
    private final Path file;
    private final int line;
    private final List<String> injectedTypeNames;

    public BeanModel(String className,
                     String qualifiedName,
                     String beanType,
                     Path file,
                     int line,
                     List<String> injectedTypeNames) {
        this.className = className;
        this.qualifiedName = qualifiedName;
        this.beanType = beanType;
        this.file = file;
        this.line = line;
        this.injectedTypeNames = List.copyOf(injectedTypeNames);
    }

    public String getClassName() { return className; }
    public String getQualifiedName() { return qualifiedName; }
    public String getBeanType() { return beanType; }
    public Path getFile() { return file; }
    public int getLine() { return line; }
    public List<String> getInjectedTypeNames() { return injectedTypeNames; }
}
