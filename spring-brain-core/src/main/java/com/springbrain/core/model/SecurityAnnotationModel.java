package com.springbrain.core.model;

import java.nio.file.Path;

public final class SecurityAnnotationModel {

    private final String ownerQualifiedName;
    private final String methodName;
    private final String annotationName;
    private final String expression;
    private final Path file;
    private final int line;

    public SecurityAnnotationModel(String ownerQualifiedName,
                                   String methodName,
                                   String annotationName,
                                   String expression,
                                   Path file,
                                   int line) {
        this.ownerQualifiedName = ownerQualifiedName;
        this.methodName = methodName;
        this.annotationName = annotationName;
        this.expression = expression;
        this.file = file;
        this.line = line;
    }

    public String getOwnerQualifiedName() { return ownerQualifiedName; }
    public String getMethodName() { return methodName; }
    public String getAnnotationName() { return annotationName; }
    public String getExpression() { return expression; }
    public Path getFile() { return file; }
    public int getLine() { return line; }
}
