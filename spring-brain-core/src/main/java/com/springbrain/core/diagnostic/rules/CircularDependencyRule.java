package com.springbrain.core.diagnostic.rules;

import com.springbrain.core.diagnostic.Diagnostic;
import com.springbrain.core.diagnostic.DiagnosticRule;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.BeanModel;
import com.springbrain.core.model.ProjectModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CircularDependencyRule implements DiagnosticRule {

    public static final String CODE = "SPRING_BRAIN_CIRCULAR_DEPENDENCY";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public List<Diagnostic> analyze(ProjectModel model, GraphDocument graph) {
        Map<String, BeanModel> beansByClassName = new HashMap<>();
        for (BeanModel bean : model.getBeans()) {
            beansByClassName.put(bean.getClassName(), bean);
        }

        List<BeanModel> beans = model.getBeans().stream()
                .sorted(Comparator.comparing(BeanModel::getQualifiedName))
                .toList();

        List<Diagnostic> diagnostics = new ArrayList<>();
        Set<String> emittedCycles = new HashSet<>();

        for (BeanModel bean : beans) {
            findCycles(bean, beansByClassName, new ArrayList<>(), emittedCycles, diagnostics);
        }

        diagnostics.sort(Comparator
                .comparing(Diagnostic::file)
                .thenComparingInt(Diagnostic::line)
                .thenComparing(Diagnostic::message));
        return diagnostics;
    }

    private void findCycles(BeanModel current,
                            Map<String, BeanModel> beansByClassName,
                            List<BeanModel> path,
                            Set<String> emittedCycles,
                            List<Diagnostic> diagnostics) {
        int existingIndex = path.indexOf(current);
        if (existingIndex >= 0) {
            List<BeanModel> cycle = new ArrayList<>(path.subList(existingIndex, path.size()));
            String key = canonicalCycleKey(cycle);
            if (emittedCycles.add(key)) {
                List<BeanModel> canonical = canonicalCycle(cycle);
                diagnostics.add(toDiagnostic(canonical));
            }
            return;
        }

        path.add(current);
        for (String injectedType : current.getInjectedTypeNames()) {
            BeanModel next = beansByClassName.get(injectedType);
            if (next != null) {
                findCycles(next, beansByClassName, path, emittedCycles, diagnostics);
            }
        }
        path.remove(path.size() - 1);
    }

    private Diagnostic toDiagnostic(List<BeanModel> cycle) {
        BeanModel first = cycle.get(0);
        List<String> classNames = cycle.stream().map(BeanModel::getClassName).toList();
        String message = "Circular dependency detected: "
                + String.join(" -> ", classNames)
                + " -> "
                + first.getClassName();

        List<String> relatedNodeIds = cycle.stream()
                .map(bean -> "bean:" + bean.getQualifiedName())
                .toList();

        return new Diagnostic(
                DiagnosticSeverity.ERROR,
                CODE,
                message,
                first.getFile().toString(),
                first.getLine(),
                relatedNodeIds,
                List.of(
                        "Introduce an interface or mediator to break the direct bean cycle.",
                        "Move shared behavior into a third bean that both cyclic beans can depend on.",
                        "Replace one constructor dependency with an event or callback if the dependency is not required during construction."));
    }

    private String canonicalCycleKey(List<BeanModel> cycle) {
        return String.join("->", canonicalCycle(cycle).stream()
                .map(BeanModel::getQualifiedName)
                .toList());
    }

    private List<BeanModel> canonicalCycle(List<BeanModel> cycle) {
        List<BeanModel> best = null;
        for (int index = 0; index < cycle.size(); index++) {
            List<BeanModel> rotated = rotate(cycle, index);
            if (best == null || compare(rotated, best) < 0) {
                best = rotated;
            }
        }
        return best == null ? List.of() : best;
    }

    private List<BeanModel> rotate(List<BeanModel> cycle, int start) {
        List<BeanModel> result = new ArrayList<>(cycle.size());
        for (int offset = 0; offset < cycle.size(); offset++) {
            result.add(cycle.get((start + offset) % cycle.size()));
        }
        return List.copyOf(result);
    }

    private int compare(List<BeanModel> left, List<BeanModel> right) {
        for (int index = 0; index < left.size(); index++) {
            int compared = left.get(index).getQualifiedName().compareTo(right.get(index).getQualifiedName());
            if (compared != 0) {
                return compared;
            }
        }
        return 0;
    }
}
