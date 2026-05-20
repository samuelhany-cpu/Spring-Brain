package com.springbrain.core.model;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents a Spring controller class. Populated in Milestone 1.
 */
public final class ControllerModel {

    private final String className;
    private final String packageName;
    private final String qualifiedName;
    private final Path file;
    private final int line;
    private final List<RouteModel> routes;

    public ControllerModel(String className, String packageName, String qualifiedName,
                           Path file, int line, List<RouteModel> routes) {
        this.className = className;
        this.packageName = packageName;
        this.qualifiedName = qualifiedName;
        this.file = file;
        this.line = line;
        this.routes = List.copyOf(routes);
    }

    public String getClassName() { return className; }
    public String getPackageName() { return packageName; }
    public String getQualifiedName() { return qualifiedName; }
    public Path getFile() { return file; }
    public int getLine() { return line; }
    public List<RouteModel> getRoutes() { return routes; }
}
