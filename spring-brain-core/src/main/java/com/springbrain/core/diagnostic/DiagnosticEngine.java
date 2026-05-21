package com.springbrain.core.diagnostic;

import com.springbrain.core.diagnostic.rules.CircularDependencyRule;
import com.springbrain.core.diagnostic.rules.ControllerDirectRepositoryRule;
import com.springbrain.core.diagnostic.rules.ControllerWithoutServiceRule;
import com.springbrain.core.diagnostic.rules.MissingConfigPropertyRule;
import com.springbrain.core.diagnostic.rules.MissingRepositoryBeanRule;
import com.springbrain.core.diagnostic.rules.PublicRiskyEndpointRule;
import com.springbrain.core.diagnostic.rules.RepositoryEntityMismatchRule;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ProjectModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DiagnosticEngine {

    private DiagnosticEngine() {}

    public static List<DiagnosticRule> defaultRules() {
        return List.of(
                new ControllerWithoutServiceRule(),
                new ControllerDirectRepositoryRule(),
                new MissingRepositoryBeanRule(),
                new RepositoryEntityMismatchRule(),
                new MissingConfigPropertyRule(),
                new CircularDependencyRule(),
                new PublicRiskyEndpointRule());
    }

    public static DiagnosticsReport analyze(ProjectModel model, GraphDocument graph, List<DiagnosticRule> rules) {
        List<Diagnostic> all = new ArrayList<>();
        for (DiagnosticRule rule : rules) {
            all.addAll(rule.analyze(model, graph));
        }
        all.sort(Comparator
                .comparingInt((Diagnostic d) -> d.severity().ordinal())
                .thenComparing(Diagnostic::file)
                .thenComparingInt(Diagnostic::line)
                .thenComparing(Diagnostic::code)
                .thenComparing(Diagnostic::message));
        return new DiagnosticsReport("1.0.0", all);
    }
}
