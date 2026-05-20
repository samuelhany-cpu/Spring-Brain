package com.springbrain.core.model;

import java.nio.file.Path;

/**
 * Represents a single HTTP route on a controller method. Populated in Milestone 1.
 */
public final class RouteModel {

    private final String httpMethod;
    private final String path;
    private final String controllerClass;
    private final String methodName;
    private final Path file;
    private final int line;

    public RouteModel(String httpMethod, String path, String controllerClass,
                      String methodName, Path file, int line) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.controllerClass = controllerClass;
        this.methodName = methodName;
        this.file = file;
        this.line = line;
    }

    public String getHttpMethod() { return httpMethod; }
    public String getPath() { return path; }
    public String getControllerClass() { return controllerClass; }
    public String getMethodName() { return methodName; }
    public Path getFile() { return file; }
    public int getLine() { return line; }

    public String routeId() {
        return "route:" + httpMethod.toUpperCase() + ":" + path;
    }
}
