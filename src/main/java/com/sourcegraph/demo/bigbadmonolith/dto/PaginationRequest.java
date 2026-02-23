package com.sourcegraph.demo.bigbadmonolith.dto;

/**
 * T087: Pagination request parameters.
 */
public class PaginationRequest {
    private final int page;
    private final int size;

    public PaginationRequest(int page, int size) {
        this.page = Math.max(0, page);
        this.size = Math.max(1, Math.min(size, 100));
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getOffset() {
        return page * size;
    }
}
