package com.sourcegraph.demo.bigbadmonolith.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T027: Error page tests — verify custom error pages are configured
 * so that stack traces and internal details are not exposed to users.
 */
class ErrorPageTest {

    @Test
    void customErrorPagesExist() {
        Path error404 = Paths.get("src/main/webapp/WEB-INF/error/404.jsp");
        Path error500 = Paths.get("src/main/webapp/WEB-INF/error/500.jsp");

        assertThat(error404).as("Custom 404 error page should exist").exists();
        assertThat(error500).as("Custom 500 error page should exist").exists();
    }

    @Test
    void webXmlConfiguresErrorPages() throws IOException {
        Path webXml = Paths.get("src/main/webapp/WEB-INF/web.xml");
        String content = Files.readString(webXml);

        assertThat(content)
                .as("web.xml should configure error page for 404")
                .contains("404");
        assertThat(content)
                .as("web.xml should configure error page for 500")
                .contains("500");
        assertThat(content)
                .as("web.xml should configure error-page elements")
                .contains("error-page");
    }

    @Test
    void errorPagesDoNotExposeStackTraces() throws IOException {
        Path error500 = Paths.get("src/main/webapp/WEB-INF/error/500.jsp");
        String content = Files.readString(error500);

        assertThat(content)
                .as("500 error page should not display stack traces")
                .doesNotContain("printStackTrace")
                .doesNotContain("getStackTrace")
                .doesNotContain("e.getMessage()");
    }
}
