package com.springbrain.core.diagnostic;

import java.util.List;

public record Diagnostic(
        DiagnosticSeverity severity,
        String code,
        String message,
        String file,
        int line,
        List<String> relatedNodeIds,
        List<String> suggestedFixes) {

    public Diagnostic {
        relatedNodeIds = List.copyOf(relatedNodeIds);
        suggestedFixes = List.copyOf(suggestedFixes);
    }
}
