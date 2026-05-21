package com.springbrain.core.security;

import java.nio.file.Path;

public final class EndpointSecurityModel {

    private final String httpMethod;
    private final String path;
    private final String controllerClass;
    private final String methodName;
    private final String access;
    private final String source;
    private final String detail;
    private final Path file;
    private final int line;

    public EndpointSecurityModel(String httpMethod,
                                 String path,
                                 String controllerClass,
                                 String methodName,
                                 String access,
                                 String source,
                                 String detail,
                                 Path file,
                                 int line) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.controllerClass = controllerClass;
        this.methodName = methodName;
        this.access = access;
        this.source = source;
        this.detail = detail;
        this.file = file;
        this.line = line;
    }

    public String getHttpMethod() { return httpMethod; }
    public String getPath() { return path; }
    public String getControllerClass() { return controllerClass; }
    public String getMethodName() { return methodName; }
    public String getAccess() { return access; }
    public String getSource() { return source; }
    public String getDetail() { return detail; }
    public Path getFile() { return file; }
    public int getLine() { return line; }

    public String routeId() {
        return "route:" + httpMethod.toUpperCase() + ":" + path;
    }
}
