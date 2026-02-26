package com.bigbadmonolith.common.dto;

public record PageMeta(
    int page,
    int pageSize,
    long totalItems,
    int totalPages
) {
    public static PageMeta from(org.springframework.data.domain.Page<?> page) {
        return new PageMeta(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}
