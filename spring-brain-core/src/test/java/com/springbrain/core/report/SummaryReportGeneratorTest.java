package com.springbrain.core.report;

import com.springbrain.core.diagnostic.DiagnosticEngine;
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
import org.junit.jupiter.api.io.TempDir;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SummaryReportGeneratorTest {

    @TempDir
    Path tempDir;

    private final List rules = List.of(
            new ControllerWithoutServiceRule(),
            new ControllerDirectRepositoryRule(),
            new MissingRepositoryBeanRule(),
            new RepositoryEntityMismatchRule(),
            new MissingConfigPropertyRule());

    private Path fixture(String name) throws URISyntaxException {
        return Paths.get(getClass().getClassLoader().getResource("fixtures/" + name).toURI());
    }

    private ProjectModel scan(String... names) throws Exception {
        Path dir = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(dir);
        for (String n : names) {
            Files.copy(fixture(n), dir.resolve(n), StandardCopyOption.REPLACE_EXISTING);
        }
        return SpringAnnotationScanner.scan(tempDir);
    }

    private GraphDocument graph(ProjectModel m) {
        return GraphBuilder.build(m, "test-project", Instant.EPOCH);
    }

    private DiagnosticsReport diagnostics(ProjectModel m, GraphDocument g) {
        return DiagnosticEngine.analyze(m, g, rules);
    }

    // ── Section presence ─────────────────────────────────────────────────────

    @Test
    void reportContainsOverviewSection() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).contains("## Overview");
    }

    @Test
    void reportContainsComponentCountsSection() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).contains("## Components");
    }

    @Test
    void reportContainsEndpointsSection() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).contains("## Endpoints");
    }

    @Test
    void reportContainsDiagnosticsSection() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).contains("## Diagnostics");
    }

    // ── Overview content ─────────────────────────────────────────────────────

    @Test
    void overviewMentionsProjectName() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).contains("test-project");
    }

    @Test
    void overviewMentionsGeneratedAt() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).contains("1970-01-01");
    }

    // ── Component counts ─────────────────────────────────────────────────────

    @Test
    void componentCountsListControllers() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).containsPattern("(?i)controller[s]?.*1|1.*controller[s]?");
    }

    @Test
    void componentCountsListServices() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).containsPattern("(?i)service[s]?.*1|1.*service[s]?");
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    @Test
    void endpointsSectionListsRoutes() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).contains("/api/users");
        assertThat(report).containsAnyOf("GET", "POST", "PUT", "DELETE");
    }

    @Test
    void emptyProjectShowsNoEndpoints() throws Exception {
        ProjectModel model = SpringAnnotationScanner.scan(tempDir);
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).containsIgnoringCase("no endpoints");
    }

    // ── Diagnostics ───────────────────────────────────────────────────────────

    @Test
    void diagnosticsSectionShowsCleanWhenNone() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).containsIgnoringCase("no diagnostics");
    }

    @Test
    void diagnosticsSectionListsDiagnosticCodes() throws Exception {
        ProjectModel model = scan("LonerController.java", "DtoRepository.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).contains("SPRING_BRAIN_REPOSITORY_ENTITY_MISMATCH");
        assertThat(report).contains("SPRING_BRAIN_CONTROLLER_WITHOUT_SERVICE");
    }

    @Test
    void diagnosticsSectionShowsErrorCount() throws Exception {
        ProjectModel model = scan("LonerController.java", "DtoRepository.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).containsPattern("(?i)1.*error[s]?|error[s]?.*1");
    }

    // ── Suggested actions ─────────────────────────────────────────────────────

    @Test
    void reportContainsSuggestedActionsWhenDiagnosticsExist() throws Exception {
        ProjectModel model = scan("LonerController.java", "DtoRepository.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).containsIgnoringCase("suggested");
    }

    // ── Output format ─────────────────────────────────────────────────────────

    @Test
    void reportStartsWithH1Title() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        assertThat(report).startsWith("# ");
    }

    @Test
    void sectionsAreOrderedOverviewBeforeDiagnostics() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        GraphDocument g = graph(model);
        String report = SummaryReportGenerator.generate(model, g, diagnostics(model, g));

        int overviewIdx = report.indexOf("## Overview");
        int diagIdx = report.indexOf("## Diagnostics");
        assertThat(overviewIdx).isLessThan(diagIdx);
    }
}
