package com.springbrain.core.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesScannerTest {

    @TempDir
    Path tempDir;

    // ── flattenYaml unit tests ────────────────────────────────────────────────

    @Test
    void flattenYaml_simpleNested() {
        List<String> lines = List.of(
                "jwt:",
                "  secret: abc",
                "  expiry: 3600"
        );
        Set<String> keys = PropertiesScanner.flattenYaml(lines);
        assertThat(keys).containsExactlyInAnyOrder("jwt.secret", "jwt.expiry");
    }

    @Test
    void flattenYaml_topLevelScalar() {
        List<String> lines = List.of("server.port: 8080");
        Set<String> keys = PropertiesScanner.flattenYaml(lines);
        assertThat(keys).containsExactly("server.port");
    }

    @Test
    void flattenYaml_deeplyNested() {
        List<String> lines = List.of(
                "spring:",
                "  datasource:",
                "    url: jdbc:h2:mem:test",
                "    username: sa"
        );
        Set<String> keys = PropertiesScanner.flattenYaml(lines);
        assertThat(keys).containsExactlyInAnyOrder("spring.datasource.url", "spring.datasource.username");
    }

    @Test
    void flattenYaml_ignoresBlankLinesAndComments() {
        List<String> lines = List.of(
                "# this is a comment",
                "",
                "app:",
                "  name: MyApp  # inline comment"
        );
        Set<String> keys = PropertiesScanner.flattenYaml(lines);
        assertThat(keys).containsExactly("app.name");
    }

    @Test
    void flattenYaml_ignoresListItems() {
        List<String> lines = List.of(
                "allowed-origins:",
                "  - http://localhost:3000",
                "  - http://localhost:8080"
        );
        Set<String> keys = PropertiesScanner.flattenYaml(lines);
        assertThat(keys).isEmpty();
    }

    @Test
    void flattenYaml_emptySetsForEmptyInput() {
        assertThat(PropertiesScanner.flattenYaml(List.of())).isEmpty();
    }

    // ── scan integration tests ────────────────────────────────────────────────

    @Test
    void scan_picksUpPropertiesKeys() throws IOException {
        Path resources = tempDir.resolve("src/main/resources");
        Files.createDirectories(resources);
        Files.writeString(resources.resolve("application.properties"),
                "server.port=8080\nspring.datasource.url=jdbc:h2:mem:test\n");

        Set<String> keys = PropertiesScanner.scan(tempDir);
        assertThat(keys).contains("server.port", "spring.datasource.url");
    }

    @Test
    void scan_picksUpYamlKeys() throws IOException {
        Path resources = tempDir.resolve("src/main/resources");
        Files.createDirectories(resources);
        Files.writeString(resources.resolve("application.yml"),
                "jwt:\n  secret: s3cr3t\n  expiry: 3600\n",
                StandardCharsets.UTF_8);

        Set<String> keys = PropertiesScanner.scan(tempDir);
        assertThat(keys).containsExactlyInAnyOrder("jwt.secret", "jwt.expiry");
    }

    @Test
    void scan_picksUpYamlAndPropertiesTogether() throws IOException {
        Path resources = tempDir.resolve("src/main/resources");
        Files.createDirectories(resources);
        Files.writeString(resources.resolve("application.properties"), "server.port=8080\n");
        Files.writeString(resources.resolve("application.yml"),
                "jwt:\n  secret: s3cr3t\n",
                StandardCharsets.UTF_8);

        Set<String> keys = PropertiesScanner.scan(tempDir);
        assertThat(keys).contains("server.port", "jwt.secret");
    }

    @Test
    void scan_returnsEmptyWhenNoResourceDir() {
        assertThat(PropertiesScanner.scan(tempDir)).isEmpty();
    }
}
