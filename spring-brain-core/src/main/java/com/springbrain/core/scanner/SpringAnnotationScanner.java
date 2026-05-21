package com.springbrain.core.scanner;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.springbrain.core.model.ConfigPropertyUsageModel;
import com.springbrain.core.model.ControllerModel;
import com.springbrain.core.model.EntityModel;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.model.RepositoryModel;
import com.springbrain.core.model.RouteModel;
import com.springbrain.core.model.ServiceModel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class SpringAnnotationScanner {

    private static final Set<String> CONTROLLER_ANNOTATIONS =
            Set.of("RestController", "Controller");

    private static final Set<String> SERVICE_ANNOTATIONS =
            Set.of("Service");

    private static final Set<String> REPOSITORY_BASE_TYPES =
            Set.of("JpaRepository", "CrudRepository", "PagingAndSortingRepository",
                    "ListCrudRepository");

    private static final Set<String> HTTP_METHOD_ANNOTATIONS =
            Set.of("GetMapping", "PostMapping", "PutMapping", "PatchMapping",
                    "DeleteMapping", "RequestMapping");

    private SpringAnnotationScanner() {
    }

    /**
     * Scans the given project root for Spring Boot components.
     * Returns a populated {@link ProjectModel}.
     */
    public static ProjectModel scan(Path projectRoot) {
        StaticJavaParser.getParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);

        List<Path> javaFiles = JavaFileDiscovery.discover(projectRoot);

        List<ControllerModel> controllers = new ArrayList<>();
        List<ServiceModel> services = new ArrayList<>();
        List<RepositoryModel> repositories = new ArrayList<>();
        List<EntityModel> entities = new ArrayList<>();
        List<ConfigPropertyUsageModel> configPropertyUsages = new ArrayList<>();

        for (Path absolutePath : javaFiles) {
            Path relativePath = projectRoot.relativize(absolutePath);
            try {
                CompilationUnit cu = StaticJavaParser.parse(absolutePath);
                String packageName = cu.getPackageDeclaration()
                        .map(pd -> pd.getName().asString())
                        .orElse("");

                for (ClassOrInterfaceDeclaration type : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                    processType(type, packageName, relativePath,
                            controllers, services, repositories, entities, configPropertyUsages);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to parse: " + absolutePath, e);
            }
        }

        return ProjectModel.builder(projectRoot)
                .controllers(controllers)
                .services(services)
                .repositories(repositories)
                .entities(entities)
                .configPropertyUsages(configPropertyUsages)
                .definedConfigKeys(PropertiesScanner.scan(projectRoot))
                .build();
    }

    private static void processType(ClassOrInterfaceDeclaration type,
                                    String packageName,
                                    Path relativePath,
                                    List<ControllerModel> controllers,
                                    List<ServiceModel> services,
                                    List<RepositoryModel> repositories,
                                    List<EntityModel> entities,
                                    List<ConfigPropertyUsageModel> configPropertyUsages) {

        String className = type.getNameAsString();
        String qualifiedName = packageName.isEmpty() ? className : packageName + "." + className;
        int line = type.getBegin().map(p -> p.line).orElse(0);

        // Collect @Value usages from fields regardless of class type
        for (FieldDeclaration field : type.getFields()) {
            extractValueAnnotations(field, relativePath, qualifiedName, configPropertyUsages);
        }

        List<AnnotationExpr> annotations = type.getAnnotations();

        if (hasAnnotation(annotations, CONTROLLER_ANNOTATIONS)) {
            String classLevelPath = extractMappingPath(annotations);
            List<RouteModel> routes = extractRoutes(type, qualifiedName, classLevelPath, relativePath);
            List<String> injected = extractInjectedTypeNames(type);
            controllers.add(new ControllerModel(
                    className, packageName, qualifiedName, relativePath, line, routes, injected));
            return;
        }

        if (hasAnnotation(annotations, SERVICE_ANNOTATIONS)) {
            List<String> injected = extractInjectedTypeNames(type);
            services.add(new ServiceModel(className, qualifiedName, relativePath, line, injected));
            return;
        }

        if (hasAnnotationNamed(annotations, "Entity")) {
            entities.add(new EntityModel(className, qualifiedName, relativePath, line));
            return;
        }

        // Repositories: interface extending JpaRepository / CrudRepository
        if (type.isInterface()) {
            tryExtractRepository(type, className, qualifiedName, relativePath, line)
                    .ifPresent(repositories::add);
        }
    }

    private static boolean hasAnnotation(List<AnnotationExpr> annotations, Set<String> names) {
        return annotations.stream().anyMatch(a -> names.contains(a.getNameAsString()));
    }

    private static boolean hasAnnotationNamed(List<AnnotationExpr> annotations, String name) {
        return annotations.stream().anyMatch(a -> a.getNameAsString().equals(name));
    }

    /**
     * Extracts the path value from the first mapping annotation found on a class.
     */
    private static String extractMappingPath(List<AnnotationExpr> annotations) {
        for (AnnotationExpr ann : annotations) {
            if (HTTP_METHOD_ANNOTATIONS.contains(ann.getNameAsString())) {
                return extractPathFromAnnotation(ann).orElse("");
            }
        }
        return "";
    }

    private static Optional<String> extractPathFromAnnotation(AnnotationExpr ann) {
        if (ann instanceof SingleMemberAnnotationExpr sma) {
            if (sma.getMemberValue() instanceof StringLiteralExpr str) {
                return Optional.of(str.asString());
            }
        } else if (ann instanceof NormalAnnotationExpr nae) {
            for (MemberValuePair pair : nae.getPairs()) {
                String pairName = pair.getNameAsString();
                if (pairName.equals("value") || pairName.equals("path")) {
                    if (pair.getValue() instanceof StringLiteralExpr str) {
                        return Optional.of(str.asString());
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static List<RouteModel> extractRoutes(ClassOrInterfaceDeclaration type,
                                                   String qualifiedClassName,
                                                   String classLevelPath,
                                                   Path relativePath) {
        List<RouteModel> routes = new ArrayList<>();
        for (MethodDeclaration method : type.getMethods()) {
            for (AnnotationExpr ann : method.getAnnotations()) {
                String httpMethod = toHttpMethod(ann.getNameAsString());
                if (httpMethod == null) {
                    continue;
                }
                String methodPath = extractPathFromAnnotation(ann).orElse("");
                String fullPath = joinPaths(classLevelPath, methodPath);
                int line = method.getBegin().map(p -> p.line).orElse(0);
                routes.add(new RouteModel(
                        httpMethod, fullPath, qualifiedClassName,
                        method.getNameAsString(), relativePath, line));
            }
        }
        return routes;
    }

    private static String toHttpMethod(String annotationName) {
        return switch (annotationName) {
            case "GetMapping" -> "GET";
            case "PostMapping" -> "POST";
            case "PutMapping" -> "PUT";
            case "PatchMapping" -> "PATCH";
            case "DeleteMapping" -> "DELETE";
            case "RequestMapping" -> "GET";
            default -> null;
        };
    }

    /**
     * Joins a class-level base path and a method-level path, normalizing slashes.
     */
    static String joinPaths(String classPath, String methodPath) {
        if (classPath.isEmpty() && methodPath.isEmpty()) return "/";
        if (classPath.isEmpty()) return ensureLeadingSlash(methodPath);
        if (methodPath.isEmpty()) return classPath;

        String base = classPath.endsWith("/") ? classPath.substring(0, classPath.length() - 1) : classPath;
        String suffix = methodPath.startsWith("/") ? methodPath : "/" + methodPath;
        return base + suffix;
    }

    private static String ensureLeadingSlash(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    private static Optional<RepositoryModel> tryExtractRepository(ClassOrInterfaceDeclaration type,
                                                                    String className,
                                                                    String qualifiedName,
                                                                    Path relativePath,
                                                                    int line) {
        NodeList<ClassOrInterfaceType> extended = type.getExtendedTypes();
        for (ClassOrInterfaceType parent : extended) {
            if (REPOSITORY_BASE_TYPES.contains(parent.getNameAsString())) {
                String entityType = "";
                String idType = "";
                if (parent.getTypeArguments().isPresent()) {
                    var typeArgs = parent.getTypeArguments().get();
                    if (!typeArgs.isEmpty()) {
                        entityType = typeArgs.get(0).asString();
                    }
                    if (typeArgs.size() > 1) {
                        idType = typeArgs.get(1).asString();
                    }
                }
                return Optional.of(new RepositoryModel(
                        className, qualifiedName, entityType, idType, relativePath, line));
            }
        }
        return Optional.empty();
    }

    private static void extractValueAnnotations(FieldDeclaration field,
                                                 Path relativePath,
                                                 String ownerQualifiedName,
                                                 List<ConfigPropertyUsageModel> usages) {
        for (AnnotationExpr ann : field.getAnnotations()) {
            if (!ann.getNameAsString().equals("Value")) {
                continue;
            }
            extractPathFromAnnotation(ann).ifPresent(raw -> {
                // Extract key from ${key} or ${key:default}
                if (raw.startsWith("${") && raw.contains("}")) {
                    String inner = raw.substring(2, raw.indexOf("}"));
                    String key = inner.contains(":") ? inner.substring(0, inner.indexOf(":")) : inner;
                    int line = field.getBegin().map(p -> p.line).orElse(0);
                    usages.add(new ConfigPropertyUsageModel(key, relativePath, line, ownerQualifiedName));
                }
            });
        }
    }

    private static List<String> extractInjectedTypeNames(ClassOrInterfaceDeclaration type) {
        List<String> result = new ArrayList<>();
        // Constructor injection (Spring's preferred style — no @Autowired needed)
        for (ConstructorDeclaration constructor : type.getConstructors()) {
            constructor.getParameters().forEach(p -> result.add(p.getTypeAsString()));
        }
        // Field injection via @Autowired
        for (FieldDeclaration field : type.getFields()) {
            if (hasAnnotationNamed(field.getAnnotations(), "Autowired")) {
                field.getVariables().forEach(v -> result.add(v.getTypeAsString()));
            }
        }
        return result;
    }
}
