package com.sourcegraph.demo.bigbadmonolith.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T026: Credential exposure tests — scan source for hardcoded passwords,
 * assert zero hardcoded credentials in source code.
 */
class CredentialExposureTest {

    private static final Path SRC_ROOT = Paths.get("src/main/java");
    private static final Path WEBAPP_ROOT = Paths.get("src/main/webapp");

    @Test
    void noHardcodedPasswordsInJavaSource() throws IOException {
        List<String> violations = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(SRC_ROOT)) {
            paths.filter(p -> p.toString().endsWith(".java"))
                 .forEach(path -> {
                     try {
                         List<String> lines = Files.readAllLines(path);
                         for (int i = 0; i < lines.size(); i++) {
                             String line = lines.get(i).toLowerCase();
                             if ((line.contains("password") || line.contains("db_password"))
                                     && (line.contains("= \"") || line.contains("=\""))
                                     && !line.contains("getenv") && !line.contains("getproperty")
                                     && !line.trim().startsWith("//") && !line.trim().startsWith("*")) {
                                 violations.add(path + ":" + (i + 1) + " → " + lines.get(i).trim());
                             }
                         }
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 });
        }

        assertThat(violations)
                .as("Hardcoded credentials found in source code")
                .isEmpty();
    }

    @Test
    void noHardcodedDbUrlInJspFiles() throws IOException {
        List<String> violations = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(WEBAPP_ROOT)) {
            paths.filter(p -> p.toString().endsWith(".jsp"))
                 .forEach(path -> {
                     try {
                         List<String> lines = Files.readAllLines(path);
                         for (int i = 0; i < lines.size(); i++) {
                             String line = lines.get(i);
                             if (line.contains("jdbc:derby:")) {
                                 violations.add(path + ":" + (i + 1) + " → " + line.trim());
                             }
                         }
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 });
        }

        assertThat(violations)
                .as("Hardcoded DB connection strings found in JSP files")
                .isEmpty();
    }

    @Test
    void noDirectDriverManagerUsageInJsp() throws IOException {
        List<String> violations = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(WEBAPP_ROOT)) {
            paths.filter(p -> p.toString().endsWith(".jsp"))
                 .forEach(path -> {
                     try {
                         String content = Files.readString(path);
                         if (content.contains("DriverManager.getConnection")) {
                             violations.add(path.toString());
                         }
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                 });
        }

        assertThat(violations)
                .as("JSP files using DriverManager directly (should delegate to ConnectionManager)")
                .isEmpty();
    }
}
