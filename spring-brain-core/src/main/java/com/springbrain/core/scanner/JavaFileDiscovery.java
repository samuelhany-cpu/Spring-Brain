package com.springbrain.core.scanner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class JavaFileDiscovery {

    private static final String SRC_MAIN_JAVA = "src/main/java";

    private JavaFileDiscovery() {
    }

    /**
     * Discovers all .java files under {@code projectRoot/src/main/java}.
     * Returns an empty list if that directory does not exist.
     * Results are sorted for deterministic output.
     */
    public static List<Path> discover(Path projectRoot) {
        Path sourceRoot = projectRoot.resolve(SRC_MAIN_JAVA);
        if (!Files.isDirectory(sourceRoot)) {
            return List.of();
        }
        try (var stream = Files.walk(sourceRoot)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".java"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to walk source root: " + sourceRoot, e);
        }
    }
}
