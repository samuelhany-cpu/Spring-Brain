package com.springbrain.core.diagnostic.rules;

import com.springbrain.core.diagnostic.Diagnostic;
import com.springbrain.core.diagnostic.DiagnosticRule;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.security.EndpointSecurityModel;
import com.springbrain.core.security.EndpointSecurityResolver;

import java.util.List;
import java.util.Set;

public final class PublicRiskyEndpointRule implements DiagnosticRule {

    public static final String CODE = "SPRING_BRAIN_PUBLIC_RISKY_ENDPOINT";

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public List<Diagnostic> analyze(ProjectModel model, GraphDocument graph) {
        return EndpointSecurityResolver.resolve(model).stream()
                .filter(endpoint -> endpoint.getAccess().equals("PUBLIC"))
                .filter(endpoint -> MUTATING_METHODS.contains(endpoint.getHttpMethod()))
                .map(this::toDiagnostic)
                .toList();
    }

    private Diagnostic toDiagnostic(EndpointSecurityModel endpoint) {
        return new Diagnostic(
                DiagnosticSeverity.WARNING,
                CODE,
                endpoint.getHttpMethod() + " " + endpoint.getPath()
                        + " is public and mutates server state",
                endpoint.getFile().toString(),
                endpoint.getLine(),
                List.of(endpoint.routeId()),
                List.of(
                        "Require authentication or role checks for " + endpoint.getHttpMethod()
                                + " " + endpoint.getPath(),
                        "Move " + endpoint.getPath() + " out of permitAll request matchers if it changes state",
                        "Add a method-level security annotation such as @PreAuthorize"));
    }
}
