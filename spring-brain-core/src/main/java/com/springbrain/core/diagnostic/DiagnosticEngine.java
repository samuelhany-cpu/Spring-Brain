package com.springbrain.core.diagnostic;

import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ProjectModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DiagnosticEngine {

    private DiagnosticEngine() {}

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
