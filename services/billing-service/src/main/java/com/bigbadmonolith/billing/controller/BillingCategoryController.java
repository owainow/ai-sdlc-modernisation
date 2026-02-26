package com.bigbadmonolith.billing.controller;

import com.bigbadmonolith.billing.dto.*;
import com.bigbadmonolith.billing.service.BillingCategoryService;
import com.bigbadmonolith.common.dto.ApiResponse;
import com.bigbadmonolith.common.dto.PageMeta;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing/categories")
public class BillingCategoryController {
    private final BillingCategoryService categoryService;

    public BillingCategoryController(BillingCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BillingCategoryResponse>> create(@Valid @RequestBody CreateBillingCategoryRequest request) {
        BillingCategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BillingCategoryResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<BillingCategoryResponse> result = categoryService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), PageMeta.from(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillingCategoryResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BillingCategoryResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateBillingCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
