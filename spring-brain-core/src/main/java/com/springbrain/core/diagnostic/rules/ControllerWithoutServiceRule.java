package com.springbrain.core.diagnostic.rules;

import com.springbrain.core.diagnostic.Diagnostic;
import com.springbrain.core.diagnostic.DiagnosticRule;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ControllerModel;
import com.springbrain.core.model.ProjectModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ControllerWithoutServiceRule implements DiagnosticRule {

    public static final String CODE = "SPRING_BRAIN_CONTROLLER_WITHOUT_SERVICE";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public List<Diagnostic> analyze(ProjectModel model, GraphDocument graph) {
        Set<String> serviceClassNames = model.getServices().stream()
                .map(s -> s.getClassName())
                .collect(Collectors.toSet());

        List<Diagnostic> result = new ArrayList<>();
        for (ControllerModel ctrl : model.getControllers()) {
            boolean injectsService = ctrl.getInjectedTypeNames().stream()
                    .anyMatch(serviceClassNames::contains);
            if (!injectsService) {
                result.add(new Diagnostic(
                        DiagnosticSeverity.WARNING,
                        CODE,
                        ctrl.getClassName() + " does not inject any @Service — consider adding a service layer",
                        ctrl.getFile().toString(),
                        ctrl.getLine(),
                        List.of(),
                        List.of("Inject a @Service bean into " + ctrl.getClassName() + " to delegate business logic")));
            }
        }
        return result;
    }
}
