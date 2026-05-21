package com.springbrain.core.integration;

import com.springbrain.core.diagnostic.DiagnosticEngine;
import com.springbrain.core.diagnostic.DiagnosticRule;
import com.springbrain.core.diagnostic.DiagnosticsReport;
import com.springbrain.core.diagnostic.rules.ControllerDirectRepositoryRule;
import com.springbrain.core.diagnostic.rules.ControllerWithoutServiceRule;
import com.springbrain.core.diagnostic.rules.MissingConfigPropertyRule;
import com.springbrain.core.diagnostic.rules.MissingRepositoryBeanRule;
import com.springbrain.core.diagnostic.rules.RepositoryEntityMismatchRule;
import com.springbrain.core.graph.GraphBuilder;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.scanner.SpringAnnotationScanner;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SampleAppIntegrationTest {

    private static final Path SAMPLES_ROOT = Paths.get(
            System.getProperty("project.basedir", "."))
            .getParent()
            .resolve("spring-brain-samples");

    private static final List<DiagnosticRule> ALL_RULES = List.of(
            new ControllerWithoutServiceRule(),
            new ControllerDirectRepositoryRule(),
            new MissingRepositoryBeanRule(),
            new RepositoryEntityMismatchRule(),
            new MissingConfigPropertyRule());

    private DiagnosticsReport analyze(String appName) {
        Path appRoot = SAMPLES_ROOT.resolve(appName);
        ProjectModel model = SpringAnnotationScanner.scan(appRoot);
        GraphDocument graph = GraphBuilder.build(model, appName, Instant.EPOCH);
        return DiagnosticEngine.analyze(model, graph, ALL_RULES);
    }

    // ── clean-crud-app ────────────────────────────────────────────────────────

    @Test
    void cleanCrudAppProducesNoDiagnostics() {
        DiagnosticsReport report = analyze("clean-crud-app");
        assertThat(report.diagnostics()).isEmpty();
    }

    @Test
    void cleanCrudAppHasFourRoutes() {
        Path appRoot = SAMPLES_ROOT.resolve("clean-crud-app");
        ProjectModel model = SpringAnnotationScanner.scan(appRoot);
        int totalRoutes = model.getControllers().stream()
                .mapToInt(c -> c.getRoutes().size()).sum();
        assertThat(totalRoutes).isEqualTo(4);
    }

    // ── broken-controller-without-service-app ─────────────────────────────────

    @Test
    void brokenControllerWithoutServiceTriggersWarning() {
        DiagnosticsReport report = analyze("broken-controller-without-service-app");
        assertThat(report.diagnostics()).anySatisfy(d ->
                assertThat(d.code()).isEqualTo(ControllerWithoutServiceRule.CODE));
    }

    // ── broken-controller-direct-repository-app ───────────────────────────────

    @Test
    void brokenDirectRepositoryTriggersWarning() {
        DiagnosticsReport report = analyze("broken-controller-direct-repository-app");
        assertThat(report.diagnostics()).anySatisfy(d ->
                assertThat(d.code()).isEqualTo(ControllerDirectRepositoryRule.CODE));
    }

    // ── broken-missing-repository-app ─────────────────────────────────────────

    @Test
    void brokenMissingRepositoryTriggersError() {
        DiagnosticsReport report = analyze("broken-missing-repository-app");
        assertThat(report.diagnostics()).anySatisfy(d ->
                assertThat(d.code()).isEqualTo(MissingRepositoryBeanRule.CODE));
    }

    // ── broken-repository-entity-mismatch-app ────────────────────────────────

    @Test
    void brokenRepositoryEntityMismatchTriggersError() {
        DiagnosticsReport report = analyze("broken-repository-entity-mismatch-app");
        assertThat(report.diagnostics()).anySatisfy(d ->
                assertThat(d.code()).isEqualTo(RepositoryEntityMismatchRule.CODE));
    }

    // ── broken-config-property-app ────────────────────────────────────────────

    @Test
    void brokenConfigPropertyTriggersError() {
        DiagnosticsReport report = analyze("broken-config-property-app");
        assertThat(report.diagnostics()).anySatisfy(d ->
                assertThat(d.code()).isEqualTo(MissingConfigPropertyRule.CODE));
    }
}
