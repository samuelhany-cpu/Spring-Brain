package com.springbrain.core.diagnostic.rules;

import com.springbrain.core.diagnostic.Diagnostic;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
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

class ControllerDirectRepositoryRuleTest {

    @TempDir
    Path tempDir;

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

    private final ControllerDirectRepositoryRule rule = new ControllerDirectRepositoryRule();

    // ── Positive ──────────────────────────────────────────────────────────────

    @Test
    void firesWhenControllerInjectsRepository() throws Exception {
        ProjectModel model = scan("DirectRepoController.java", "UserRepository.java", "User.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags).hasSize(1);
        assertThat(diags.get(0).severity()).isEqualTo(DiagnosticSeverity.WARNING);
        assertThat(diags.get(0).code()).isEqualTo(ControllerDirectRepositoryRule.CODE);
    }

    @Test
    void diagnosticMessageMentionsRepositoryName() throws Exception {
        ProjectModel model = scan("DirectRepoController.java", "UserRepository.java", "User.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags.get(0).message()).contains("UserRepository");
    }

    @Test
    void diagnosticHasRelatedNodeIds() throws Exception {
        ProjectModel model = scan("DirectRepoController.java", "UserRepository.java", "User.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags.get(0).relatedNodeIds()).isNotEmpty();
    }

    @Test
    void diagnosticHasAtLeastOneSuggestedFix() throws Exception {
        ProjectModel model = scan("DirectRepoController.java", "UserRepository.java", "User.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags.get(0).suggestedFixes()).isNotEmpty();
    }

    // ── Negative ──────────────────────────────────────────────────────────────

    @Test
    void doesNotFireWhenControllerInjectsService() throws Exception {
        ProjectModel model = scan("UserController.java", "UserService.java",
                "UserRepository.java", "User.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags).isEmpty();
    }

    // ── Code ─────────────────────────────────────────────────────────────────

    @Test
    void codeIsStable() {
        assertThat(rule.code()).isEqualTo("SPRING_BRAIN_CONTROLLER_DIRECT_REPOSITORY_ACCESS");
    }
}
