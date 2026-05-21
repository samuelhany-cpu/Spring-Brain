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

class MissingRepositoryBeanRuleTest {

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

    private final MissingRepositoryBeanRule rule = new MissingRepositoryBeanRule();

    @Test
    void firesWhenServiceInjectsMissingRepository() throws Exception {
        ProjectModel model = scan("BrokenService.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags).hasSize(1);
        assertThat(diags.get(0).severity()).isEqualTo(DiagnosticSeverity.ERROR);
        assertThat(diags.get(0).code()).isEqualTo(MissingRepositoryBeanRule.CODE);
    }

    @Test
    void diagnosticMessageMentionsMissingType() throws Exception {
        ProjectModel model = scan("BrokenService.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags.get(0).message()).contains("NonExistentRepository");
    }

    @Test
    void diagnosticHasAtLeastOneSuggestedFix() throws Exception {
        ProjectModel model = scan("BrokenService.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags.get(0).suggestedFixes()).isNotEmpty();
    }

    @Test
    void doesNotFireWhenRepositoryExists() throws Exception {
        ProjectModel model = scan("UserService.java", "UserRepository.java", "User.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags).isEmpty();
    }

    @Test
    void codeIsStable() {
        assertThat(rule.code()).isEqualTo("SPRING_BRAIN_MISSING_REPOSITORY_BEAN");
    }
}
