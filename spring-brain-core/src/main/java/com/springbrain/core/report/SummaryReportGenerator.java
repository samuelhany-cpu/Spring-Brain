package com.springbrain.core.report;

import com.springbrain.core.diagnostic.Diagnostic;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
import com.springbrain.core.diagnostic.DiagnosticsReport;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.model.RouteModel;
import com.springbrain.core.security.EndpointSecurityModel;
import com.springbrain.core.security.EndpointSecurityResolver;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SummaryReportGenerator {

    private SummaryReportGenerator() {}

    public static String generate(ProjectModel model, GraphDocument graph, DiagnosticsReport diagnostics) {
        StringBuilder sb = new StringBuilder();

        appendTitle(sb);
        appendOverview(sb, graph);
        appendComponents(sb, model);
        appendEndpoints(sb, model);
        appendEndpointSecurityMatrix(sb, model);
        appendGraphSummary(sb, graph);
        appendDiagnostics(sb, diagnostics);
        appendSuggestedActions(sb, diagnostics);

        return sb.toString();
    }

    private static void appendTitle(StringBuilder sb) {
        sb.append("# Spring Brain — Analysis Report\n\n");
    }

    private static void appendOverview(StringBuilder sb, GraphDocument graph) {
        sb.append("## Overview\n\n");
        sb.append("| Field | Value |\n");
        sb.append("|-------|-------|\n");
        sb.append("| Project | ").append(graph.metadata().projectName()).append(" |\n");
        sb.append("| Generated | ").append(graph.metadata().generatedAt()).append(" |\n");
        sb.append("| Tool | ").append(graph.metadata().tool())
                .append(" ").append(graph.metadata().toolVersion()).append(" |\n");
        sb.append("| Language | ").append(graph.metadata().language()).append(" |\n");
        sb.append("| Framework | ").append(graph.metadata().framework()).append(" |\n");
        sb.append("| Scan mode | ").append(graph.metadata().sourceMode()).append(" |\n");
        sb.append("\n");
    }

    private static void appendComponents(StringBuilder sb, ProjectModel model) {
        sb.append("## Components\n\n");
        sb.append("| Type | Count |\n");
        sb.append("|------|-------|\n");
        sb.append("| Controllers | ").append(model.getControllers().size()).append(" |\n");
        sb.append("| Services | ").append(model.getServices().size()).append(" |\n");
        sb.append("| Repositories | ").append(model.getRepositories().size()).append(" |\n");
        sb.append("| Entities | ").append(model.getEntities().size()).append(" |\n");
        sb.append("| Config properties | ").append(model.getConfigPropertyUsages().size()).append(" |\n");
        sb.append("\n");
    }

    private static void appendEndpoints(StringBuilder sb, ProjectModel model) {
        sb.append("## Endpoints\n\n");

        List<RouteModel> routes = model.getControllers().stream()
                .flatMap(c -> c.getRoutes().stream())
                .sorted((a, b) -> {
                    int cmp = a.getPath().compareTo(b.getPath());
                    return cmp != 0 ? cmp : a.getHttpMethod().compareTo(b.getHttpMethod());
                })
                .toList();

        if (routes.isEmpty()) {
            sb.append("_No endpoints found._\n\n");
            return;
        }

        sb.append("| Method | Path | Controller | Handler |\n");
        sb.append("|--------|------|------------|---------|\n");
        for (RouteModel r : routes) {
            sb.append("| ").append(r.getHttpMethod())
                    .append(" | ").append(r.getPath())
                    .append(" | ").append(simpleClassName(r.getControllerClass()))
                    .append(" | ").append(r.getMethodName())
                    .append(" |\n");
        }
        sb.append("\n");
    }

    private static void appendEndpointSecurityMatrix(StringBuilder sb, ProjectModel model) {
        sb.append("## Endpoint Security Matrix\n\n");

        List<EndpointSecurityModel> endpoints = EndpointSecurityResolver.resolve(model);
        if (endpoints.isEmpty()) {
            sb.append("_No endpoints found._\n\n");
            return;
        }

        sb.append("| Method | Path | Access | Source | Detail |\n");
        sb.append("|--------|------|--------|--------|--------|\n");
        for (EndpointSecurityModel endpoint : endpoints) {
            sb.append("| ").append(endpoint.getHttpMethod())
                    .append(" | ").append(endpoint.getPath())
                    .append(" | ").append(endpoint.getAccess())
                    .append(" | ").append(endpoint.getSource())
                    .append(" | ").append(escapePipe(endpoint.getDetail()))
                    .append(" |\n");
        }
        sb.append("\n");
    }

    private static void appendGraphSummary(StringBuilder sb, GraphDocument graph) {
        sb.append("## Graph\n\n");

        Map<String, Long> byType = graph.nodes().stream()
                .collect(Collectors.groupingBy(n -> n.type(), Collectors.counting()));

        sb.append("**Nodes:** ").append(graph.nodes().size())
                .append("  **Edges:** ").append(graph.edges().size()).append("\n\n");

        if (!byType.isEmpty()) {
            sb.append("| Node type | Count |\n");
            sb.append("|-----------|-------|\n");
            byType.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> sb.append("| ").append(e.getKey())
                            .append(" | ").append(e.getValue()).append(" |\n"));
            sb.append("\n");
        }
    }

    private static void appendDiagnostics(StringBuilder sb, DiagnosticsReport diagnostics) {
        sb.append("## Diagnostics\n\n");

        List<Diagnostic> diags = diagnostics.diagnostics();
        if (diags.isEmpty()) {
            sb.append("_No diagnostics — project looks clean._\n\n");
            return;
        }

        long errors = diags.stream().filter(d -> d.severity() == DiagnosticSeverity.ERROR).count();
        long warnings = diags.stream().filter(d -> d.severity() == DiagnosticSeverity.WARNING).count();

        sb.append("**").append(errors).append(" error(s)**, **").append(warnings).append(" warning(s)**\n\n");

        sb.append("| Severity | Code | File | Message |\n");
        sb.append("|----------|------|------|---------|\n");
        for (Diagnostic d : diags) {
            sb.append("| ").append(d.severity())
                    .append(" | ").append(d.code())
                    .append(" | ").append(shortPath(d.file())).append(":").append(d.line())
                    .append(" | ").append(escapePipe(d.message()))
                    .append(" |\n");
        }
        sb.append("\n");
    }

    private static void appendSuggestedActions(StringBuilder sb, DiagnosticsReport diagnostics) {
        List<String> fixes = diagnostics.diagnostics().stream()
                .flatMap(d -> d.suggestedFixes().stream())
                .distinct()
                .toList();

        if (fixes.isEmpty()) {
            return;
        }

        sb.append("## Suggested Actions\n\n");
        for (String fix : fixes) {
            sb.append("- ").append(fix).append("\n");
        }
        sb.append("\n");
    }

    private static String simpleClassName(String qualifiedName) {
        int dot = qualifiedName.lastIndexOf('.');
        return dot >= 0 ? qualifiedName.substring(dot + 1) : qualifiedName;
    }

    private static String shortPath(String filePath) {
        String normalized = filePath.replace('\\', '/');
        int idx = normalized.lastIndexOf('/');
        return idx >= 0 ? normalized.substring(idx + 1) : normalized;
    }

    private static String escapePipe(String s) {
        return s.replace("|", "\\|");
    }
}
