package com.springbrain.core.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JavaFileDiscoveryTest {

    @TempDir
    Path tempDir;

    @Test
    void findsJavaFilesUnderSrcMainJava() throws IOException {
        Path srcMain = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcMain);
        Files.writeString(srcMain.resolve("Foo.java"), "class Foo {}");
        Files.writeString(srcMain.resolve("Bar.java"), "class Bar {}");

        List<Path> files = JavaFileDiscovery.discover(tempDir);

        assertThat(files).hasSize(2);
    }

    @Test
    void returnsEmptyWhenNoSrcMainJavaDirectory() {
        List<Path> files = JavaFileDiscovery.discover(tempDir);

        assertThat(files).isEmpty();
    }

    @Test
    void ignoresNonJavaFiles() throws IOException {
        Path srcMain = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcMain);
        Files.writeString(srcMain.resolve("Foo.java"), "class Foo {}");
        Files.writeString(srcMain.resolve("readme.txt"), "not java");
        Files.writeString(srcMain.resolve("config.yml"), "key: value");

        List<Path> files = JavaFileDiscovery.discover(tempDir);

        assertThat(files).hasSize(1);
        assertThat(files.get(0).getFileName().toString()).isEqualTo("Foo.java");
    }

    @Test
    void findsJavaFilesInNestedPackages() throws IOException {
        Path pkg1 = tempDir.resolve("src/main/java/com/example/user");
        Path pkg2 = tempDir.resolve("src/main/java/com/example/order");
        Files.createDirectories(pkg1);
        Files.createDirectories(pkg2);
        Files.writeString(pkg1.resolve("UserController.java"), "class UserController {}");
        Files.writeString(pkg2.resolve("OrderService.java"), "class OrderService {}");

        List<Path> files = JavaFileDiscovery.discover(tempDir);

        assertThat(files).hasSize(2);
    }

    @Test
    void returnsSortedResultsDeterministically() throws IOException {
        Path srcMain = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcMain);
        Files.writeString(srcMain.resolve("Zebra.java"), "class Zebra {}");
        Files.writeString(srcMain.resolve("Alpha.java"), "class Alpha {}");
        Files.writeString(srcMain.resolve("Middle.java"), "class Middle {}");

        List<Path> files = JavaFileDiscovery.discover(tempDir);
        List<Path> sortedFiles = files.stream().sorted().toList();

        assertThat(files).isEqualTo(sortedFiles);
    }
}
