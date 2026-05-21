package com.springbrain.core.security;

import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.model.RouteModel;
import com.springbrain.core.model.SecurityAnnotationModel;
import com.springbrain.core.model.SecurityRuleModel;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class EndpointSecurityResolver {

    private EndpointSecurityResolver() {
    }

    public static List<EndpointSecurityModel> resolve(ProjectModel model) {
        return model.getControllers().stream()
                .flatMap(controller -> controller.getRoutes().stream())
                .sorted(Comparator.comparing(RouteModel::getPath)
                        .thenComparing(RouteModel::getHttpMethod))
                .map(route -> resolveRoute(route, model))
                .toList();
    }

    private static EndpointSecurityModel resolveRoute(RouteModel route, ProjectModel model) {
        Optional<SecurityAnnotationModel> methodAnnotation = model.getSecurityAnnotations().stream()
                .filter(annotation -> annotation.getOwnerQualifiedName().equals(route.getControllerClass()))
                .filter(annotation -> annotation.getMethodName().equals(route.getMethodName()))
                .findFirst();
        if (methodAnnotation.isPresent()) {
            return fromAnnotation(route, methodAnnotation.get(), "method_annotation");
        }

        Optional<SecurityAnnotationModel> classAnnotation = model.getSecurityAnnotations().stream()
                .filter(annotation -> annotation.getOwnerQualifiedName().equals(route.getControllerClass()))
                .filter(annotation -> annotation.getMethodName().isEmpty())
                .findFirst();
        if (classAnnotation.isPresent()) {
            return fromAnnotation(route, classAnnotation.get(), "class_annotation");
        }

        Optional<SecurityRuleModel> rule = model.getSecurityRules().stream()
                .filter(candidate -> matches(candidate.getPathPattern(), route.getPath()))
                .findFirst();
        if (rule.isPresent()) {
            return fromRule(route, rule.get());
        }

        return new EndpointSecurityModel(
                route.getHttpMethod(),
                route.getPath(),
                route.getControllerClass(),
                route.getMethodName(),
                "UNKNOWN",
                "none",
                "",
                route.getFile(),
                route.getLine());
    }

    private static EndpointSecurityModel fromAnnotation(RouteModel route,
                                                        SecurityAnnotationModel annotation,
                                                        String source) {
        return new EndpointSecurityModel(
                route.getHttpMethod(),
                route.getPath(),
                route.getControllerClass(),
                route.getMethodName(),
                "PROTECTED",
                source,
                annotation.getAnnotationName() + ": " + annotation.getExpression(),
                route.getFile(),
                route.getLine());
    }

    private static EndpointSecurityModel fromRule(RouteModel route, SecurityRuleModel rule) {
        return new EndpointSecurityModel(
                route.getHttpMethod(),
                route.getPath(),
                route.getControllerClass(),
                route.getMethodName(),
                rule.getAccessType(),
                "security_filter_chain",
                rule.getDetail(),
                route.getFile(),
                route.getLine());
    }

    static boolean matches(String pattern, String path) {
        if (pattern.equals(path) || pattern.equals("/**")) {
            return true;
        }
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
        if (pattern.contains("*")) {
            String regex = pattern.replace(".", "\\.").replace("**", ".*").replace("*", "[^/]*");
            return path.matches(regex);
        }
        return false;
    }
}
