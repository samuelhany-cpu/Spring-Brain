package com.springbrain.core.diagnostic.rules;

import com.springbrain.core.diagnostic.Diagnostic;
import com.springbrain.core.diagnostic.DiagnosticRule;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.EntityModel;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.model.RepositoryModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class RepositoryEntityMismatchRule implements DiagnosticRule {

    public static final String CODE = "SPRING_BRAIN_REPOSITORY_ENTITY_MISMATCH";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public List<Diagnostic> analyze(ProjectModel model, GraphDocument graph) {
        Set<String> entityClassNames = model.getEntities().stream()
                .map(EntityModel::getClassName)
                .collect(Collectors.toSet());

        List<Diagnostic> result = new ArrayList<>();
        for (RepositoryModel repo : model.getRepositories()) {
            String entityType = repo.getEntityType();
            if (!entityType.isEmpty() && !entityClassNames.contains(entityType)) {
                result.add(new Diagnostic(
                        DiagnosticSeverity.ERROR,
                        CODE,
                        repo.getInterfaceName() + " uses entity type " + entityType
                                + " which is not annotated with @Entity",
                        repo.getFile().toString(),
                        repo.getLine(),
                        List.of(),
                        List.of("Annotate " + entityType + " with @Entity",
                                "Change " + repo.getInterfaceName() + " to use a proper @Entity class")));
            }
        }
        return result;
    }
}
