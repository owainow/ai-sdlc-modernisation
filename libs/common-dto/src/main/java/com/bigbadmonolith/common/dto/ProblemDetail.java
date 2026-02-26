package com.bigbadmonolith.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetail(
    String type,
    String title,
    int status,
    String detail,
    String instance
) {
    public static ProblemDetail of(int status, String title, String detail) {
        return new ProblemDetail("about:blank", title, status, detail, null);
    }
}
