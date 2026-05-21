package com.springbrain.cli;

import com.springbrain.core.diagnostic.DiagnosticEngine;
import com.springbrain.core.diagnostic.DiagnosticsReport;
import com.springbrain.core.diagnostic.DiagnosticSeverity;
import com.springbrain.core.diagnostic.rules.ControllerDirectRepositoryRule;
import com.springbrain.core.diagnostic.rules.ControllerWithoutServiceRule;
import com.springbrain.core.diagnostic.rules.MissingConfigPropertyRule;
import com.springbrain.core.diagnostic.rules.MissingRepositoryBeanRule;
import com.springbrain.core.diagnostic.rules.RepositoryEntityMismatchRule;
import com.springbrain.core.export.JsonExporter;
import com.springbrain.core.graph.GraphBuilder;
import com.springbrain.core.graph.GraphDocument;
import com.springbrain.core.model.ProjectModel;
import com.springbrain.core.report.SummaryReportGenerator;
import com.springbrain.core.scanner.SpringAnnotationScanner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
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

    private static final List RULES = List.of(
            new ControllerWithoutServiceRule(),
            new ControllerDirectRepositoryRule(),
            new MissingRepositoryBeanRule(),
            new RepositoryEntityMismatchRule(),
            new MissingConfigPropertyRule());

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

        try {
            System.out.println("Scanning sources...");
            ProjectModel model = SpringAnnotationScanner.scan(projectPath);

            System.out.println("Building graph...");
            String projectName = projectPath.toAbsolutePath().normalize().getFileName().toString();
            GraphDocument graph = GraphBuilder.build(model, projectName, Instant.now());

            System.out.println("Running diagnostics...");
            DiagnosticsReport diagnostics = DiagnosticEngine.analyze(model, graph, RULES);

            String summary = SummaryReportGenerator.generate(model, graph, diagnostics);

            Files.createDirectories(resolvedOutput);
            Files.writeString(resolvedOutput.resolve("graph.json"),
                    JsonExporter.toJson(graph), StandardCharsets.UTF_8);
            Files.writeString(resolvedOutput.resolve("diagnostics.json"),
                    JsonExporter.toJson(diagnostics), StandardCharsets.UTF_8);
            Files.writeString(resolvedOutput.resolve("summary.md"),
                    summary, StandardCharsets.UTF_8);

            long errors = diagnostics.diagnostics().stream()
                    .filter(d -> d.severity() == DiagnosticSeverity.ERROR).count();
            long warnings = diagnostics.diagnostics().stream()
                    .filter(d -> d.severity() == DiagnosticSeverity.WARNING).count();

            System.out.println();
            System.out.println("Results");
            System.out.println("-------");
            System.out.printf("  Controllers : %d%n", model.getControllers().size());
            System.out.printf("  Services    : %d%n", model.getServices().size());
            System.out.printf("  Repositories: %d%n", model.getRepositories().size());
            System.out.printf("  Entities    : %d%n", model.getEntities().size());
            System.out.printf("  Routes      : %d%n",
                    model.getControllers().stream().mapToInt(c -> c.getRoutes().size()).sum());
            System.out.printf("  Errors      : %d%n", errors);
            System.out.printf("  Warnings    : %d%n", warnings);
            System.out.println();
            System.out.println("Output written to: " + resolvedOutput.toAbsolutePath().normalize());
            System.out.println();

            if (failOnError && errors > 0) {
                System.err.println("Exiting with code 2: " + errors + " error(s) found.");
                return 2;
            }

            return 0;

        } catch (IOException e) {
            System.err.println("Error writing output: " + e.getMessage());
            return 1;
        }
    }
}
