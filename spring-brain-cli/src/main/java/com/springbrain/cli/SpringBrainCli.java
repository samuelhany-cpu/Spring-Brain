package com.springbrain.cli;

import com.springbrain.core.SpringBrainVersion;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = SpringBrainVersion.TOOL_NAME,
        version = SpringBrainVersion.VERSION,
        mixinStandardHelpOptions = true,
        subcommands = {ScanCommand.class, ServeCommand.class},
        description = "Spring Boot architecture intelligence tool. Scans a Spring Boot project and generates a graph, diagnostics, and summary.",
        header = {
                "",
                "  Spring Brain v" + SpringBrainVersion.VERSION,
                "  Architecture intelligence for Spring Boot",
                ""
        }
)
public class SpringBrainCli implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new SpringBrainCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
