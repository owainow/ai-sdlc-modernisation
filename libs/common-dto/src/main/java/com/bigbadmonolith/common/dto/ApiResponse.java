package com.bigbadmonolith.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    String status,
    T data,
    java.util.List<ProblemDetail> errors,
    PageMeta meta
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, PageMeta meta) {
        return new ApiResponse<>("success", data, null, meta);
    }

    public static <T> ApiResponse<T> error(java.util.List<ProblemDetail> errors) {
        return new ApiResponse<>("error", null, errors, null);
    }

    public static <T> ApiResponse<T> error(ProblemDetail error) {
        return new ApiResponse<>("error", null, java.util.List.of(error), null);
    }
}
