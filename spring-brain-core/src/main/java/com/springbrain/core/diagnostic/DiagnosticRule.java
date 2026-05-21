package com.springbrain.core.diagnostic;

import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ProjectModel;

import java.util.List;

public interface DiagnosticRule {
    String code();
    List<Diagnostic> analyze(ProjectModel model, GraphDocument graph);
}
