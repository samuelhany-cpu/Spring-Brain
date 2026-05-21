package com.springbrain.core.diagnostic;

import com.springbrain.core.diagnostic.rules.ControllerDirectRepositoryRule;
import com.springbrain.core.diagnostic.rules.ControllerWithoutServiceRule;
import com.springbrain.core.diagnostic.rules.CircularDependencyRule;
import com.springbrain.core.diagnostic.rules.MissingConfigPropertyRule;
import com.springbrain.core.diagnostic.rules.MissingRepositoryBeanRule;
import com.springbrain.core.diagnostic.rules.PublicRiskyEndpointRule;
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

class DiagnosticEngineTest {

    @TempDir
    Path tempDir;

    private final List<DiagnosticRule> allRules = List.of(
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
        return GraphBuilder.build(m, "test", Instant.EPOCH);
    }

    @Test
    void cleanProjectProducesNoDiagnostics() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        DiagnosticsReport report = DiagnosticEngine.analyze(model, graph(model), allRules);

        assertThat(report.diagnostics()).isEmpty();
    }

    @Test
    void reportSchemaVersionIsSet() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        DiagnosticsReport report = DiagnosticEngine.analyze(model, graph(model), allRules);

        assertThat(report.schemaVersion()).isEqualTo("1.0.0");
    }

    @Test
    void diagnosticsAreSortedErrorBeforeWarning() throws Exception {
        ProjectModel model = scan("LonerController.java", "DtoRepository.java");
        DiagnosticsReport report = DiagnosticEngine.analyze(model, graph(model), allRules);

        assertThat(report.diagnostics()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(report.diagnostics().get(0).severity()).isEqualTo(DiagnosticSeverity.ERROR);

        boolean sawWarning = false;
        for (Diagnostic d : report.diagnostics()) {
            if (d.severity() == DiagnosticSeverity.WARNING) sawWarning = true;
            if (sawWarning) assertThat(d.severity()).isNotEqualTo(DiagnosticSeverity.ERROR);
        }
    }

    @Test
    void engineRunsAllRules() throws Exception {
        ProjectModel model = scan("LonerController.java", "DtoRepository.java");
        DiagnosticsReport report = DiagnosticEngine.analyze(model, graph(model), allRules);

        assertThat(report.diagnostics()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(report.diagnostics().stream().map(Diagnostic::code))
                .contains(ControllerWithoutServiceRule.CODE, RepositoryEntityMismatchRule.CODE);
    }

    @Test
    void defaultRulesIncludeCircularDependencyRule() {
        assertThat(DiagnosticEngine.defaultRules().stream().map(DiagnosticRule::code))
                .contains(CircularDependencyRule.CODE);
    }

    @Test
    void defaultRulesIncludePublicRiskyEndpointRule() {
        assertThat(DiagnosticEngine.defaultRules().stream().map(DiagnosticRule::code))
                .contains(PublicRiskyEndpointRule.CODE);
    }
}
