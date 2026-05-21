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
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.springbrain.core.model.BeanModel;
import com.springbrain.core.model.ConfigPropertyUsageModel;
import com.springbrain.core.model.ControllerModel;
import com.springbrain.core.model.EntityModel;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.model.RepositoryModel;
import com.springbrain.core.model.RouteModel;
import com.springbrain.core.model.SecurityAnnotationModel;
import com.springbrain.core.model.SecurityRuleModel;
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

    private static final Set<String> EXTRA_BEAN_ANNOTATIONS =
            Set.of("Component", "Configuration", "ControllerAdvice");

    private static final Set<String> REPOSITORY_BASE_TYPES =
            Set.of("JpaRepository", "CrudRepository", "PagingAndSortingRepository",
                    "ListCrudRepository");

    private static final Set<String> HTTP_METHOD_ANNOTATIONS =
            Set.of("GetMapping", "PostMapping", "PutMapping", "PatchMapping",
                    "DeleteMapping", "RequestMapping");

    private static final Set<String> SECURITY_ANNOTATIONS =
            Set.of("PreAuthorize", "Secured", "RolesAllowed");

    private static final Set<String> SECURITY_ACCESS_METHODS =
            Set.of("permitAll", "authenticated", "hasRole", "hasAnyRole");

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
        List<BeanModel> beans = new ArrayList<>();
        List<SecurityAnnotationModel> securityAnnotations = new ArrayList<>();
        List<SecurityRuleModel> securityRules = new ArrayList<>();

        for (Path absolutePath : javaFiles) {
            Path relativePath = projectRoot.relativize(absolutePath);
            try {
                CompilationUnit cu = StaticJavaParser.parse(absolutePath);
                String packageName = cu.getPackageDeclaration()
                        .map(pd -> pd.getName().asString())
                        .orElse("");

                for (ClassOrInterfaceDeclaration type : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                    processType(type, packageName, relativePath,
                            controllers, services, repositories, entities, configPropertyUsages, beans,
                            securityAnnotations, securityRules);
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
                .beans(beans)
                .securityAnnotations(securityAnnotations)
                .securityRules(securityRules)
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
                                    List<ConfigPropertyUsageModel> configPropertyUsages,
                                    List<BeanModel> beans,
                                    List<SecurityAnnotationModel> securityAnnotations,
                                    List<SecurityRuleModel> securityRules) {

        String className = type.getNameAsString();
        String qualifiedName = packageName.isEmpty() ? className : packageName + "." + className;
        int line = type.getBegin().map(p -> p.line).orElse(0);

        // Collect @Value usages from fields regardless of class type
        for (FieldDeclaration field : type.getFields()) {
            extractValueAnnotations(field, relativePath, qualifiedName, configPropertyUsages);
        }

        List<AnnotationExpr> annotations = type.getAnnotations();
        extractSecurityAnnotations(annotations, qualifiedName, "", relativePath, securityAnnotations);
        for (MethodDeclaration method : type.getMethods()) {
            extractSecurityAnnotations(method.getAnnotations(), qualifiedName,
                    method.getNameAsString(), relativePath, securityAnnotations);
            extractSecurityFilterChainRules(method, relativePath, securityRules);
        }

        if (hasAnnotation(annotations, CONTROLLER_ANNOTATIONS)) {
            String classLevelPath = extractMappingPath(annotations);
            List<RouteModel> routes = extractRoutes(type, qualifiedName, classLevelPath, relativePath);
            List<String> injected = extractInjectedTypeNames(type);
            controllers.add(new ControllerModel(
                    className, packageName, qualifiedName, relativePath, line, routes, injected));
            beans.add(new BeanModel(className, qualifiedName, "controller", relativePath, line, injected));
            return;
        }

        if (hasAnnotation(annotations, SERVICE_ANNOTATIONS)) {
            List<String> injected = extractInjectedTypeNames(type);
            List<String> interfaces = type.getImplementedTypes().stream()
                    .map(ClassOrInterfaceType::getNameAsString)
                    .toList();
            services.add(new ServiceModel(className, qualifiedName, relativePath, line, injected, interfaces));
            beans.add(new BeanModel(className, qualifiedName, "service", relativePath, line, injected));
            return;
        }

        if (hasAnnotationNamed(annotations, "Entity")) {
            entities.add(new EntityModel(className, qualifiedName, relativePath, line));
            return;
        }

        // Repositories: interface extending JpaRepository / CrudRepository
        if (type.isInterface()) {
            tryExtractRepository(type, className, qualifiedName, relativePath, line)
                    .ifPresent(repo -> {
                        repositories.add(repo);
                        beans.add(new BeanModel(className, qualifiedName, "repository", relativePath, line, List.of()));
                    });
            return;
        }

        String beanType = extraBeanType(annotations);
        if (beanType != null) {
            beans.add(new BeanModel(className, qualifiedName, beanType, relativePath, line, extractInjectedTypeNames(type)));
        }
    }

    private static boolean hasAnnotation(List<AnnotationExpr> annotations, Set<String> names) {
        return annotations.stream().anyMatch(a -> names.contains(a.getNameAsString()));
    }

    private static boolean hasAnnotationNamed(List<AnnotationExpr> annotations, String name) {
        return annotations.stream().anyMatch(a -> a.getNameAsString().equals(name));
    }

    private static String extraBeanType(List<AnnotationExpr> annotations) {
        for (AnnotationExpr annotation : annotations) {
            String name = annotation.getNameAsString();
            if (EXTRA_BEAN_ANNOTATIONS.contains(name)) {
                return switch (name) {
                    case "Component" -> "component";
                    case "Configuration" -> "configuration";
                    case "ControllerAdvice" -> "controller_advice";
                    default -> null;
                };
            }
        }
        return null;
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

    private static void extractSecurityAnnotations(List<AnnotationExpr> annotations,
                                                   String ownerQualifiedName,
                                                   String methodName,
                                                   Path relativePath,
                                                   List<SecurityAnnotationModel> securityAnnotations) {
        for (AnnotationExpr annotation : annotations) {
            String annotationName = simpleName(annotation.getNameAsString());
            if (!SECURITY_ANNOTATIONS.contains(annotationName)) {
                continue;
            }
            int line = annotation.getBegin().map(p -> p.line).orElse(0);
            securityAnnotations.add(new SecurityAnnotationModel(
                    ownerQualifiedName,
                    methodName,
                    annotationName,
                    extractAnnotationExpression(annotation),
                    relativePath,
                    line));
        }
    }

    private static String extractAnnotationExpression(AnnotationExpr annotation) {
        if (annotation instanceof SingleMemberAnnotationExpr single) {
            return expressionToSecurityValue(single.getMemberValue());
        }
        if (annotation instanceof NormalAnnotationExpr normal) {
            for (MemberValuePair pair : normal.getPairs()) {
                if (pair.getNameAsString().equals("value")) {
                    return expressionToSecurityValue(pair.getValue());
                }
            }
        }
        return "";
    }

    private static String expressionToSecurityValue(Expression expression) {
        if (expression instanceof StringLiteralExpr stringLiteral) {
            return stringLiteral.asString();
        }
        if (expression instanceof ArrayInitializerExpr array) {
            return array.getValues().stream()
                    .map(SpringAnnotationScanner::expressionToSecurityValue)
                    .filter(value -> !value.isEmpty())
                    .reduce((left, right) -> left + "," + right)
                    .orElse("");
        }
        return expression.toString();
    }

    private static void extractSecurityFilterChainRules(MethodDeclaration method,
                                                        Path relativePath,
                                                        List<SecurityRuleModel> securityRules) {
        if (!method.getTypeAsString().contains("SecurityFilterChain")) {
            return;
        }
        for (MethodCallExpr accessCall : method.findAll(MethodCallExpr.class)) {
            String accessMethod = accessCall.getNameAsString();
            if (!SECURITY_ACCESS_METHODS.contains(accessMethod)) {
                continue;
            }
            Optional<MethodCallExpr> matcherCall = accessCall.getScope()
                    .filter(Expression::isMethodCallExpr)
                    .map(Expression::asMethodCallExpr)
                    .filter(call -> call.getNameAsString().equals("requestMatchers"));
            if (matcherCall.isEmpty()) {
                continue;
            }
            String accessType = accessMethod.equals("permitAll") ? "PUBLIC" : "PROTECTED";
            String detail = accessDetail(accessCall);
            int line = matcherCall.get().getBegin()
                    .or(() -> accessCall.getBegin())
                    .map(p -> p.line)
                    .orElse(0);
            for (String pathPattern : extractStringArguments(matcherCall.get())) {
                securityRules.add(new SecurityRuleModel(
                        pathPattern,
                        accessType,
                        "SecurityFilterChain",
                        detail,
                        relativePath,
                        line));
            }
        }
    }

    private static List<String> extractStringArguments(MethodCallExpr methodCall) {
        List<String> values = new ArrayList<>();
        for (Expression argument : methodCall.getArguments()) {
            if (argument instanceof StringLiteralExpr stringLiteral) {
                values.add(stringLiteral.asString());
            }
        }
        return values;
    }

    private static String accessDetail(MethodCallExpr accessCall) {
        if (accessCall.getArguments().isEmpty()) {
            return accessCall.getNameAsString();
        }
        String arguments = accessCall.getArguments().stream()
                .map(Expression::toString)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        return accessCall.getNameAsString() + "(" + arguments + ")";
    }

    private static String simpleName(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : name;
    }
}
