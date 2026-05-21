package com.springbrain.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

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
}
