package com.springbrain.core.diagnostic.rules;

import com.springbrain.core.diagnostic.Diagnostic;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
import com.springbrain.core.graph.GraphBuilder;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.BeanModel;
import com.springbrain.core.model.ProjectModel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CircularDependencyRuleTest {

    private final CircularDependencyRule rule = new CircularDependencyRule();

    private ProjectModel model(BeanModel... beans) {
        return ProjectModel.builder(Path.of("."))
                .beans(List.of(beans))
                .build();
    }

    private BeanModel bean(String className, String... injectedTypes) {
        return new BeanModel(
                className,
                "com.example." + className,
                "service",
                Path.of("src/main/java/com/example/" + className + ".java"),
                10,
                List.of(injectedTypes));
    }

    private GraphDocument graph(ProjectModel model) {
        return GraphBuilder.build(model, "test", Instant.EPOCH);
    }

    @Test
    void firesForTwoBeanCircularDependency() {
        ProjectModel model = model(
                bean("AlphaService", "BetaService"),
                bean("BetaService", "AlphaService"));

        List<Diagnostic> diagnostics = rule.analyze(model, graph(model));

        assertThat(diagnostics).hasSize(1);
        Diagnostic diagnostic = diagnostics.get(0);
        assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.ERROR);
        assertThat(diagnostic.code()).isEqualTo(CircularDependencyRule.CODE);
        assertThat(diagnostic.message()).isEqualTo(
                "Circular dependency detected: AlphaService -> BetaService -> AlphaService");
        assertThat(diagnostic.relatedNodeIds()).containsExactly(
                "bean:com.example.AlphaService",
                "bean:com.example.BetaService");
        assertThat(diagnostic.suggestedFixes()).isNotEmpty();
    }

    @Test
    void firesForThreeBeanCircularDependency() {
        ProjectModel model = model(
                bean("AlphaService", "BetaService"),
                bean("BetaService", "GammaService"),
                bean("GammaService", "AlphaService"));

        List<Diagnostic> diagnostics = rule.analyze(model, graph(model));

        assertThat(diagnostics).hasSize(1);
        assertThat(diagnostics.get(0).message()).isEqualTo(
                "Circular dependency detected: AlphaService -> BetaService -> GammaService -> AlphaService");
    }

    @Test
    void doesNotFireForAcyclicDependencyChain() {
        ProjectModel model = model(
                bean("AlphaService", "BetaService"),
                bean("BetaService", "GammaService"),
                bean("GammaService"));

        List<Diagnostic> diagnostics = rule.analyze(model, graph(model));

        assertThat(diagnostics).isEmpty();
    }

    @Test
    void codeIsStable() {
        assertThat(rule.code()).isEqualTo("SPRING_BRAIN_CIRCULAR_DEPENDENCY");
    }
}
