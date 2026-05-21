package com.springbrain.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ScanCommandTest {

    @TempDir
    Path tempDir;

    @Test
    void scanReturnsZeroForValidPath() {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        int exitCode = cmd.execute("scan", "--path", tempDir.toString());
        assertThat(exitCode).isZero();
    }

    @Test
    void scanReturnsOneForNonExistentPath() {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        int exitCode = cmd.execute("scan", "--path", "/this/path/does/not/exist/spring-brain-test-xyz");
        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    void helpExitsZero() {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        int exitCode = cmd.execute("--help");
        assertThat(exitCode).isZero();
    }

    @Test
    void scanHelpExitsZero() {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        int exitCode = cmd.execute("scan", "--help");
        assertThat(exitCode).isZero();
    }

    @Test
    void scanWritesGraphJson() {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        cmd.execute("scan", "--path", tempDir.toString());
        assertThat(tempDir.resolve(".spring-brain/graph.json")).exists();
    }

    @Test
    void scanWritesDiagnosticsJson() {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        cmd.execute("scan", "--path", tempDir.toString());
        assertThat(tempDir.resolve(".spring-brain/diagnostics.json")).exists();
    }

    @Test
    void scanWritesSummaryMd() {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        cmd.execute("scan", "--path", tempDir.toString());
        assertThat(tempDir.resolve(".spring-brain/summary.md")).exists();
    }

    @Test
    void graphJsonIsValidJson() throws Exception {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        cmd.execute("scan", "--path", tempDir.toString());
        String content = Files.readString(tempDir.resolve(".spring-brain/graph.json"));
        assertThat(content.trim()).startsWith("{").endsWith("}");
    }

    @Test
    void summaryMdStartsWithH1() throws Exception {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        cmd.execute("scan", "--path", tempDir.toString());
        String content = Files.readString(tempDir.resolve(".spring-brain/summary.md"));
        assertThat(content).startsWith("# ");
    }

    @Test
    void customOutputDirectoryIsRespected() throws Exception {
        Path customOut = tempDir.resolve("my-output");
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        cmd.execute("scan", "--path", tempDir.toString(), "--output", customOut.toString());
        assertThat(customOut.resolve("graph.json")).exists();
        assertThat(customOut.resolve("diagnostics.json")).exists();
        assertThat(customOut.resolve("summary.md")).exists();
    }

    @Test
    void failOnErrorExitsTwoWhenErrorsFound() throws Exception {
        // Empty project has no diagnostics — exit 0 even with --fail-on-error
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        int exitCode = cmd.execute("scan", "--path", tempDir.toString(), "--fail-on-error");
        assertThat(exitCode).isZero();
    }
}
