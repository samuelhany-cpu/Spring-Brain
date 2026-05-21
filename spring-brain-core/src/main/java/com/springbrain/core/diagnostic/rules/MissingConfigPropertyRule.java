package com.springbrain.core.diagnostic.rules;

import com.springbrain.core.diagnostic.Diagnostic;
import com.springbrain.core.diagnostic.DiagnosticRule;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ConfigPropertyUsageModel;
import com.springbrain.core.model.ProjectModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MissingConfigPropertyRule implements DiagnosticRule {

    public static final String CODE = "SPRING_BRAIN_MISSING_CONFIG_PROPERTY";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public List<Diagnostic> analyze(ProjectModel model, GraphDocument graph) {
        Set<String> defined = model.getDefinedConfigKeys();
        Set<String> seenKeys = new LinkedHashSet<>();
        List<Diagnostic> result = new ArrayList<>();

        for (ConfigPropertyUsageModel usage : model.getConfigPropertyUsages()) {
            String key = usage.getPropertyKey();
            if (!defined.contains(key) && seenKeys.add(key)) {
                result.add(new Diagnostic(
                        DiagnosticSeverity.ERROR,
                        CODE,
                        "Property key '" + key + "' is used via @Value but not defined in any properties file",
                        usage.getFile().toString(),
                        usage.getLine(),
                        List.of(),
                        List.of("Add '" + key + "' to src/main/resources/application.properties")));
            }
        }
        return result;
    }
}
