package com.springbrain.cli;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewerServer {

    private HttpServer httpServer;
    private ExecutorService executor;
    private Thread watchThread;
    private volatile boolean running;
    private final List<HttpExchange> sseClients = new CopyOnWriteArrayList<>();

    private static final Map<String, String> MIME = Map.of(
        ".html", "text/html; charset=utf-8",
        ".js",   "application/javascript; charset=utf-8",
        ".css",  "text/css; charset=utf-8",
        ".json", "application/json; charset=utf-8",
        ".svg",  "image/svg+xml",
        ".png",  "image/png",
        ".ico",  "image/x-icon"
    );

    public void start(int port, Path outputDir) throws IOException {
        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "spring-brain-http");
            t.setDaemon(true);
            return t;
        });
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.setExecutor(executor);

        httpServer.createContext("/api/graph", exchange -> serveFile(exchange, outputDir.resolve("graph.json"), "application/json; charset=utf-8"));
        httpServer.createContext("/api/diagnostics", exchange -> serveFile(exchange, outputDir.resolve("diagnostics.json"), "application/json; charset=utf-8"));
        httpServer.createContext("/api/events", this::handleSse);
        httpServer.createContext("/", this::serveStatic);

        httpServer.start();
        running = true;
        watchThread = new Thread(() -> watchOutputDir(outputDir), "spring-brain-watcher");
        watchThread.setDaemon(true);
        watchThread.start();
    }

    public int getPort() {
        return httpServer.getAddress().getPort();
    }

    public void stop() {
        running = false;
        if (watchThread != null) watchThread.interrupt();
        for (HttpExchange client : sseClients) {
            try { client.close(); } catch (Exception ignored) {}
        }
        sseClients.clear();
        if (httpServer != null) httpServer.stop(0);
        if (executor != null) executor.shutdownNow();
    }

    private void serveFile(HttpExchange exchange, Path file, String contentType) throws IOException {
        if (!Files.exists(file)) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        byte[] body = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private void handleSse(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, 0);
        sseClients.add(exchange);
        try {
            exchange.getResponseBody().write("data: connected\n\n".getBytes(StandardCharsets.UTF_8));
            exchange.getResponseBody().flush();
        } catch (IOException ignored) {
            sseClients.remove(exchange);
        }
    }

    private void broadcastRefresh() {
        byte[] event = "data: refresh\n\n".getBytes(StandardCharsets.UTF_8);
        List<HttpExchange> dead = new CopyOnWriteArrayList<>();
        for (HttpExchange client : sseClients) {
            try {
                client.getResponseBody().write(event);
                client.getResponseBody().flush();
            } catch (IOException e) {
                dead.add(client);
            }
        }
        sseClients.removeAll(dead);
    }

    private void watchOutputDir(Path outputDir) {
        try (var watcher = outputDir.getFileSystem().newWatchService()) {
            outputDir.register(watcher,
                java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY,
                java.nio.file.StandardWatchEventKinds.ENTRY_CREATE);
            while (running) {
                var key = watcher.take();
                boolean relevant = key.pollEvents().stream()
                    .map(e -> e.context().toString())
                    .anyMatch(name -> name.equals("graph.json") || name.equals("diagnostics.json"));
                if (relevant) broadcastRefresh();
                key.reset();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("[spring-brain] WatchService error: " + e.getMessage());
        }
    }

    private void serveStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.isEmpty()) path = "/index.html";
        String resource = "viewer" + path;
        try (InputStream in = ViewerServer.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }
            byte[] body = in.readAllBytes();
            String ext = path.contains(".") ? path.substring(path.lastIndexOf('.')) : "";
            String mime = MIME.getOrDefault(ext, "application/octet-stream");
            exchange.getResponseHeaders().set("Content-Type", mime);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }
    }
}
