package com.springbrain.cli;

import com.springbrain.core.diagnostic.DiagnosticEngine;
import com.springbrain.core.diagnostic.DiagnosticsReport;
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
        name = "serve",
        mixinStandardHelpOptions = true,
        description = "Scan a Spring Boot project and open the interactive architecture viewer."
)
public class ServeCommand implements Callable<Integer> {

    @Option(
            names = {"--path", "-p"},
            description = "Path to the Spring Boot project root.",
            required = true
    )
    private Path projectPath;

    @Option(
            names = {"--port"},
            description = "HTTP port for the viewer server. Defaults to 3000.",
            defaultValue = "3000"
    )
    private int port;

    @Option(
            names = {"--output", "-o"},
            description = "Output directory. Defaults to .spring-brain inside the project path.",
            required = false
    )
    private Path outputPath;

    @Option(
            names = {"--no-open"},
            description = "Do not open the browser after starting the server.",
            defaultValue = "false"
    )
    private boolean noOpen;

    @SuppressWarnings("rawtypes")
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

        try {
            System.out.println("Scanning sources...");
            ProjectModel model = SpringAnnotationScanner.scan(projectPath);
            String projectName = projectPath.toAbsolutePath().normalize().getFileName().toString();
            GraphDocument graph = GraphBuilder.build(model, projectName, Instant.now());
            DiagnosticsReport diagnostics = DiagnosticEngine.analyze(model, graph, RULES);
            String summary = SummaryReportGenerator.generate(model, graph, diagnostics);

            Files.createDirectories(resolvedOutput);
            Files.writeString(resolvedOutput.resolve("graph.json"), JsonExporter.toJson(graph), StandardCharsets.UTF_8);
            Files.writeString(resolvedOutput.resolve("diagnostics.json"), JsonExporter.toJson(diagnostics), StandardCharsets.UTF_8);
            Files.writeString(resolvedOutput.resolve("summary.md"), summary, StandardCharsets.UTF_8);

            if (noOpen) {
                System.out.println("Output written to: " + resolvedOutput.toAbsolutePath().normalize());
                return 0;
            }

            ViewerServer server = new ViewerServer();
            server.start(port, resolvedOutput);
            int actualPort = server.getPort();
            String url = "http://localhost:" + actualPort;

            System.out.println("Spring Brain viewer running at: " + url);
            openBrowser(url);
            System.out.println("Press Ctrl+C to stop.");

            Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "spring-brain-shutdown"));
            Thread.currentThread().join();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 0;
    }

    private void openBrowser(String url) {
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else {
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            }
        } catch (IOException ignored) {
        }
    }
}
