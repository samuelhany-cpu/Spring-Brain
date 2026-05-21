package com.springbrain.core.model;

import java.nio.file.Path;
import java.util.List;

public final class ControllerModel {

    private final String className;
    private final String packageName;
    private final String qualifiedName;
    private final Path file;
    private final int line;
    private final List<RouteModel> routes;
    private final List<String> injectedTypeNames;

    public ControllerModel(String className, String packageName, String qualifiedName,
                           Path file, int line, List<RouteModel> routes,
                           List<String> injectedTypeNames) {
        this.className = className;
        this.packageName = packageName;
        this.qualifiedName = qualifiedName;
        this.file = file;
        this.line = line;
        this.routes = List.copyOf(routes);
        this.injectedTypeNames = List.copyOf(injectedTypeNames);
    }

    public String getClassName() { return className; }
    public String getPackageName() { return packageName; }
    public String getQualifiedName() { return qualifiedName; }
    public Path getFile() { return file; }
    public int getLine() { return line; }
    public List<RouteModel> getRoutes() { return routes; }
    public List<String> getInjectedTypeNames() { return injectedTypeNames; }
}
