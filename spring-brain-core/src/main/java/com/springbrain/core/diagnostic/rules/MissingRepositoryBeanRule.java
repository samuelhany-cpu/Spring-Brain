package com.springbrain.core.diagnostic.rules;

import com.springbrain.core.diagnostic.Diagnostic;
import com.springbrain.core.diagnostic.DiagnosticRule;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.model.RepositoryModel;
import com.springbrain.core.model.ServiceModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class MissingRepositoryBeanRule implements DiagnosticRule {

    public static final String CODE = "SPRING_BRAIN_MISSING_REPOSITORY_BEAN";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public List<Diagnostic> analyze(ProjectModel model, GraphDocument graph) {
        Set<String> knownRepoNames = model.getRepositories().stream()
                .map(RepositoryModel::getInterfaceName)
                .collect(Collectors.toSet());

        List<Diagnostic> result = new ArrayList<>();
        for (ServiceModel svc : model.getServices()) {
            for (String injected : svc.getInjectedTypeNames()) {
                boolean looksLikeRepo = injected.endsWith("Repository") || injected.endsWith("Repo");
                if (looksLikeRepo && !knownRepoNames.contains(injected)) {
                    result.add(new Diagnostic(
                            DiagnosticSeverity.ERROR,
                            CODE,
                            svc.getClassName() + " injects " + injected
                                    + " but no matching @Repository bean was found",
                            svc.getFile().toString(),
                            svc.getLine(),
                            List.of(),
                            List.of("Create an interface " + injected + " extending JpaRepository",
                                    "Verify the repository class name matches the injected type")));
                }
            }
        }
        return result;
    }
}
