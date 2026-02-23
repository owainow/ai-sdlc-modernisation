package com.sourcegraph.demo.bigbadmonolith.util;

/**
 * Utility class for HTML escaping to prevent XSS attacks.
 * Escapes the 5 critical HTML characters as per OWASP recommendations.
 */
public class HtmlUtils {

    private HtmlUtils() {
        // Utility class
    }

    /**
     * Escapes HTML special characters to prevent XSS.
     * Handles the 5 critical characters: &amp; &lt; &gt; &quot; &#39;
     *
     * @param input the raw string to escape
     * @return the HTML-escaped string, or empty string if input is null
     */
    public static String htmlEscape(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }
        return escaped.toString();
    }
}
