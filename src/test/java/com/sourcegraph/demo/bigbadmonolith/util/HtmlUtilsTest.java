package com.sourcegraph.demo.bigbadmonolith.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for HtmlUtils XSS escaping utility.
 */
class HtmlUtilsTest {

    @Test
    void htmlEscapeReturnsEmptyForNull() {
        assertThat(HtmlUtils.htmlEscape(null)).isEmpty();
    }

    @Test
    void htmlEscapeDoesNotModifySafeStrings() {
        assertThat(HtmlUtils.htmlEscape("Hello World")).isEqualTo("Hello World");
    }

    @Test
    void htmlEscapeEscapesAmpersand() {
        assertThat(HtmlUtils.htmlEscape("A&B")).isEqualTo("A&amp;B");
    }

    @Test
    void htmlEscapeEscapesLessThan() {
        assertThat(HtmlUtils.htmlEscape("<script>")).isEqualTo("&lt;script&gt;");
    }

    @Test
    void htmlEscapeEscapesGreaterThan() {
        assertThat(HtmlUtils.htmlEscape("a>b")).isEqualTo("a&gt;b");
    }

    @Test
    void htmlEscapeEscapesDoubleQuotes() {
        assertThat(HtmlUtils.htmlEscape("say \"hello\"")).isEqualTo("say &quot;hello&quot;");
    }

    @Test
    void htmlEscapeEscapesSingleQuotes() {
        assertThat(HtmlUtils.htmlEscape("it's")).isEqualTo("it&#39;s");
    }

    @Test
    void htmlEscapeHandlesXssPayload() {
        String xss = "<script>alert('xss')</script>";
        String escaped = HtmlUtils.htmlEscape(xss);
        assertThat(escaped).doesNotContain("<script>");
        assertThat(escaped).isEqualTo("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;");
    }

    @Test
    void htmlEscapeHandlesEmptyString() {
        assertThat(HtmlUtils.htmlEscape("")).isEmpty();
    }
}
