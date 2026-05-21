package com.springbrain.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ServeCommandTest {

    @TempDir
    Path tempDir;

    @Test
    void serveHelpExitsZero() {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        int exitCode = cmd.execute("serve", "--help");
        assertThat(exitCode).isZero();
    }

    @Test
    void serveRequiresPath() {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        int exitCode = cmd.execute("serve");
        assertThat(exitCode).isNotZero();
    }

    @Test
    void serveRejectsNonExistentPath() {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        int exitCode = cmd.execute("serve", "--path", "/this/does/not/exist/spring-brain-xyz");
        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    void serveAcceptsCustomPort() {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        int exitCode = cmd.execute("serve", "--path", tempDir.toString(), "--port", "19876", "--no-open");
        assertThat(exitCode).isZero();
    }

    @Test
    void serveWritesOutputFiles() throws Exception {
        CommandLine cmd = new CommandLine(new SpringBrainCli());
        cmd.execute("serve", "--path", tempDir.toString(), "--no-open");
        assertThat(tempDir.resolve(".spring-brain/graph.json")).exists();
        assertThat(tempDir.resolve(".spring-brain/diagnostics.json")).exists();
    }
}
