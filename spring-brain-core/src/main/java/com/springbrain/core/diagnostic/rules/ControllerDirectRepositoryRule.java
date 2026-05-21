package com.springbrain.core.diagnostic.rules;

import com.springbrain.core.diagnostic.Diagnostic;
import com.springbrain.core.diagnostic.DiagnosticRule;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ControllerModel;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.model.RepositoryModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ControllerDirectRepositoryRule implements DiagnosticRule {

    public static final String CODE = "SPRING_BRAIN_CONTROLLER_DIRECT_REPOSITORY_ACCESS";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public List<Diagnostic> analyze(ProjectModel model, GraphDocument graph) {
        Map<String, RepositoryModel> reposByInterfaceName = new HashMap<>();
        for (RepositoryModel r : model.getRepositories()) {
            reposByInterfaceName.put(r.getInterfaceName(), r);
        }

        List<Diagnostic> result = new ArrayList<>();
        for (ControllerModel ctrl : model.getControllers()) {
            for (String injected : ctrl.getInjectedTypeNames()) {
                RepositoryModel repo = reposByInterfaceName.get(injected);
                if (repo != null) {
                    String repoNodeId = "repository:" + repo.getQualifiedName();
                    result.add(new Diagnostic(
                            DiagnosticSeverity.WARNING,
                            CODE,
                            ctrl.getClassName() + " directly injects " + repo.getInterfaceName()
                                    + " — repositories should be accessed through a service",
                            ctrl.getFile().toString(),
                            ctrl.getLine(),
                            List.of(repoNodeId),
                            List.of("Introduce a @Service that wraps " + repo.getInterfaceName()
                                    + " and inject it into " + ctrl.getClassName())));
                }
            }
        }
        return result;
    }
}
