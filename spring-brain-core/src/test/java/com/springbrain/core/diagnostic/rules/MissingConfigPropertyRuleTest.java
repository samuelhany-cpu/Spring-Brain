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

class MissingConfigPropertyRuleTest {

    @TempDir
    Path tempDir;

    private Path fixture(String name) throws URISyntaxException {
        return Paths.get(getClass().getClassLoader().getResource("fixtures/" + name).toURI());
    }

    private ProjectModel scan(String... names) throws Exception {
        Path srcDir = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcDir);
        for (String n : names) {
            Files.copy(fixture(n), srcDir.resolve(n), StandardCopyOption.REPLACE_EXISTING);
        }
        return SpringAnnotationScanner.scan(tempDir);
    }

    private GraphDocument graph(ProjectModel m) {
        return GraphBuilder.build(m, "test", Instant.EPOCH);
    }

    private final MissingConfigPropertyRule rule = new MissingConfigPropertyRule();

    @Test
    void firesForEachUndefinedPropertyKey() throws Exception {
        ProjectModel model = scan("AppConfig.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags).hasSize(3);
        assertThat(diags).allMatch(d -> d.severity() == DiagnosticSeverity.ERROR);
        assertThat(diags).allMatch(d -> d.code().equals(MissingConfigPropertyRule.CODE));
    }

    @Test
    void diagnosticMessageMentionsPropertyKey() throws Exception {
        ProjectModel model = scan("AppConfig.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        List<String> messages = diags.stream().map(Diagnostic::message).toList();
        assertThat(messages).anyMatch(m -> m.contains("app.name"));
    }

    @Test
    void diagnosticHasAtLeastOneSuggestedFix() throws Exception {
        ProjectModel model = scan("AppConfig.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags.get(0).suggestedFixes()).isNotEmpty();
    }

    @Test
    void doesNotFireWhenAllKeysAreDefined() throws Exception {
        Path resources = tempDir.resolve("src/main/resources");
        Files.createDirectories(resources);
        Files.writeString(resources.resolve("application.properties"),
                "app.name=SpringBrain\napp.timeout=30\njwt.secret=secret\n");

        ProjectModel model = scan("AppConfig.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags).isEmpty();
    }

    @Test
    void firesOnlyForUndefinedKeys() throws Exception {
        Path resources = tempDir.resolve("src/main/resources");
        Files.createDirectories(resources);
        Files.writeString(resources.resolve("application.properties"), "app.name=SpringBrain\n");

        ProjectModel model = scan("AppConfig.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        assertThat(diags).hasSize(2);
    }

    @Test
    void deduplicatesDiagnosticsByKey() throws Exception {
        ProjectModel model = scan("AppConfig.java");
        List<Diagnostic> diags = rule.analyze(model, graph(model));

        long uniqueMessages = diags.stream().map(Diagnostic::message).distinct().count();
        assertThat(uniqueMessages).isEqualTo(3);
    }

    @Test
    void codeIsStable() {
        assertThat(rule.code()).isEqualTo("SPRING_BRAIN_MISSING_CONFIG_PROPERTY");
    }
}
