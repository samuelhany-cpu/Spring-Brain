package com.springbrain.core.diagnostic;

import java.util.List;

public record DiagnosticsReport(String schemaVersion, List<Diagnostic> diagnostics) {
    public DiagnosticsReport {
        diagnostics = List.copyOf(diagnostics);
    }
}
