package com.springbrain.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
        name = "scan",
        mixinStandardHelpOptions = true,
        description = "Scan a Spring Boot project and generate architecture graph, diagnostics, and summary."
)
public class ScanCommand implements Callable<Integer> {

    @Option(
            names = {"--path", "-p"},
            description = "Path to the Spring Boot project root.",
            required = true
    )
    private Path projectPath;

    @Option(
            names = {"--output", "-o"},
            description = "Output directory for generated files. Defaults to .spring-brain inside the project path.",
            required = false
    )
    private Path outputPath;

    @Option(
            names = {"--fail-on-error"},
            description = "Exit with code 2 if any ERROR diagnostics are found.",
            defaultValue = "false"
    )
    private boolean failOnError;

    @Override
    public Integer call() {
        if (!Files.exists(projectPath)) {
            System.err.println("Error: Project path does not exist: " + projectPath.toAbsolutePath());
            return 1;
        }

        if (!Files.isDirectory(projectPath)) {
            System.err.println("Error: Project path is not a directory: " + projectPath.toAbsolutePath());
            return 1;
        }

        Path resolvedOutput = outputPath != null ? outputPath : projectPath.resolve(".spring-brain");

        System.out.println();
        System.out.println("Spring Brain — Static Analysis");
        System.out.println("================================");
        System.out.println("Project path : " + projectPath.toAbsolutePath().normalize());
        System.out.println("Output path  : " + resolvedOutput.toAbsolutePath().normalize());
        System.out.println();
        System.out.println("NOTE: Static scanner is planned for Milestone 1.");
        System.out.println("      No output files were generated in this milestone.");
        System.out.println();

        return 0;
    }
}
