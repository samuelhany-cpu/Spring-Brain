package com.springbrain.core.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
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
                    .filter(p -> p.getFileName().toString().endsWith(".properties"))
                    .sorted()
                    .forEach(file -> {
                        Properties props = new Properties();
                        try (InputStream in = Files.newInputStream(file)) {
                            props.load(in);
                            props.stringPropertyNames().forEach(keys::add);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to walk resources: " + resourceDir, e);
        }
        return Set.copyOf(keys);
    }
}
