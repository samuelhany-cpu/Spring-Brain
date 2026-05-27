package com.springbrain.core.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public final class PropertiesScanner {

    private PropertiesScanner() {}

    public static Set<String> scan(Path projectRoot) {
        Path resourceDir = projectRoot.resolve("src/main/resources");
        if (!Files.isDirectory(resourceDir)) {
            return Set.of();
        }
        Set<String> keys = new LinkedHashSet<>();
        try (var stream = Files.walk(resourceDir)) {
            stream.filter(Files::isRegularFile)
                    .sorted()
                    .forEach(file -> {
                        String name = file.getFileName().toString();
                        if (name.endsWith(".properties")) {
                            loadProperties(file, keys);
                        } else if (name.endsWith(".yml") || name.endsWith(".yaml")) {
                            loadYaml(file, keys);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to walk resources: " + resourceDir, e);
        }
        return Set.copyOf(keys);
    }

    private static void loadProperties(Path file, Set<String> keys) {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
            props.stringPropertyNames().forEach(keys::add);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Flattens a simple YAML file into dot-separated keys.
     * Handles basic scalar key: value pairs and nested mappings.
     * Does not support YAML anchors, aliases, multi-document files, or block sequences.
     */
    static Set<String> flattenYaml(List<String> lines) {
        Set<String> keys = new LinkedHashSet<>();
        List<String> keyPath = new ArrayList<>();
        List<Integer> indentPath = new ArrayList<>();

        for (String raw : lines) {
            String stripped = raw.stripLeading();
            if (stripped.isBlank() || stripped.startsWith("#")) continue;
            // Skip list items
            if (stripped.startsWith("- ") || stripped.equals("-")) continue;

            int indent = raw.length() - stripped.length();
            int colon = stripped.indexOf(':');
            if (colon < 0) continue;

            String keyPart = stripped.substring(0, colon).strip();
            String valuePart = stripped.substring(colon + 1).strip();

            // Strip inline comments from value
            int commentIdx = valuePart.indexOf(" #");
            if (commentIdx >= 0) valuePart = valuePart.substring(0, commentIdx).strip();

            // Skip keys with spaces or quotes (complex YAML constructs)
            if (keyPart.isEmpty() || keyPart.contains(" ") || keyPart.startsWith("\"") || keyPart.startsWith("'")) {
                continue;
            }

            // Pop path entries at same or deeper indent
            while (!indentPath.isEmpty() && indentPath.get(indentPath.size() - 1) >= indent) {
                indentPath.remove(indentPath.size() - 1);
                keyPath.remove(keyPath.size() - 1);
            }

            String fullKey = keyPath.isEmpty() ? keyPart : String.join(".", keyPath) + "." + keyPart;

            if (valuePart.isEmpty()) {
                // Parent mapping key — push to path for children
                keyPath.add(keyPart);
                indentPath.add(indent);
            } else {
                // Leaf key with a scalar value
                keys.add(fullKey);
            }
        }
        return keys;
    }

    private static void loadYaml(Path file, Set<String> keys) {
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            keys.addAll(flattenYaml(lines));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
