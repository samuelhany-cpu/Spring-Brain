package com.springbrain.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ViewerServerTest {

    @TempDir
    Path outputDir;

    private ViewerServer server;
    private HttpClient http;
    private int port;  // set dynamically via server.getPort() after start(0, ...)

    @BeforeEach
    void setUp() throws Exception {
        Files.writeString(outputDir.resolve("graph.json"), "{\"schemaVersion\":\"1.0.0\",\"nodes\":[],\"edges\":[]}");
        Files.writeString(outputDir.resolve("diagnostics.json"), "{\"schemaVersion\":\"1.0.0\",\"diagnostics\":[]}");
        server = new ViewerServer();
        server.start(0, outputDir);
        port = server.getPort();
        http = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void graphEndpointReturnsGraphJson() throws Exception {
        HttpResponse<String> response = http.send(
            HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/graph")).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type").orElse(""))
            .contains("application/json");
        assertThat(response.body()).contains("schemaVersion");
    }

    @Test
    void diagnosticsEndpointReturnsDiagnosticsJson() throws Exception {
        HttpResponse<String> response = http.send(
            HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/diagnostics")).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type").orElse(""))
            .contains("application/json");
        assertThat(response.body()).contains("diagnostics");
    }

    @Test
    void eventsEndpointReturnsSseContentType() throws Exception {
        // ofInputStream() completes once headers arrive, without waiting for body close
        HttpResponse<InputStream> response = http.send(
            HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/events")).GET().build(),
            HttpResponse.BodyHandlers.ofInputStream()
        );
        try (InputStream body = response.body()) {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Content-Type").orElse(""))
                .contains("text/event-stream");
        }
    }

    @Test
    void rootRequestServesIndexHtml() throws Exception {
        HttpResponse<String> response = http.send(
            HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/")).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type").orElse(""))
            .contains("text/html");
    }
}
