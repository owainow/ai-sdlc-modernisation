package com.sourcegraph.demo.billing.controller;

import com.sourcegraph.demo.billing.entity.BillableHour;
import com.sourcegraph.demo.billing.entity.BillingCategory;
import com.sourcegraph.demo.billing.service.BillingService;
import com.sourcegraph.demo.common.dto.ApiResponse;
import com.sourcegraph.demo.common.dto.PaginatedResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    // --- Categories ---
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<PaginatedResponse<CategoryResponse>>> listCategories(Pageable pageable) {
        Page<BillingCategory> page = billingService.findAllCategories(pageable);
        PaginatedResponse<CategoryResponse> resp = new PaginatedResponse<>(
                page.getContent().stream().map(CategoryResponse::from).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
        );
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        BillingCategory cat = billingService.createCategory(request.name(), request.hourlyRate());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(CategoryResponse.from(cat)));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id, @Valid @RequestBody CreateCategoryRequest request) {
        BillingCategory cat = billingService.updateCategory(id, request.name(), request.hourlyRate());
        return ResponseEntity.ok(ApiResponse.success(CategoryResponse.from(cat)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        billingService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // --- Hours ---
    @GetMapping("/hours")
    public ResponseEntity<ApiResponse<PaginatedResponse<HourResponse>>> listHours(Pageable pageable) {
        Page<BillableHour> page = billingService.findAllHours(pageable);
        PaginatedResponse<HourResponse> resp = new PaginatedResponse<>(
                page.getContent().stream().map(HourResponse::from).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
        );
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PostMapping("/hours")
    public ResponseEntity<ApiResponse<HourResponse>> createHour(
            @Valid @RequestBody CreateHourRequest request) {
        BillableHour hour = billingService.createHour(
                request.userId(), request.customerId(), request.categoryId(),
                request.hours(), request.workDate());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HourResponse.from(hour)));
    }

    @PutMapping("/hours/{id}")
    public ResponseEntity<ApiResponse<HourResponse>> updateHour(
            @PathVariable UUID id, @Valid @RequestBody CreateHourRequest request) {
        BillableHour hour = billingService.updateHour(
                id, request.userId(), request.customerId(), request.categoryId(),
                request.hours(), request.workDate());
        return ResponseEntity.ok(ApiResponse.success(HourResponse.from(hour)));
    }

    @DeleteMapping("/hours/{id}")
    public ResponseEntity<Void> deleteHour(@PathVariable UUID id) {
        billingService.deleteHour(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hours/exists")
    public ResponseEntity<Boolean> existsByCustomerId(@RequestParam UUID customerId) {
        return ResponseEntity.ok(billingService.existsByCustomerId(customerId));
    }

    // --- Summary ---
    @GetMapping("/billing/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBillingSummary(
            @RequestParam UUID customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Map<String, Object> summary = billingService.getBillingSummary(customerId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // --- DTOs ---
    public record CreateCategoryRequest(
            @jakarta.validation.constraints.NotBlank(message = "Category name is required")
            @jakarta.validation.constraints.Size(max = 100, message = "Category name must not exceed 100 characters")
            String name,
            @jakarta.validation.constraints.NotNull(message = "Hourly rate is required")
            @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "Hourly rate must be greater than 0")
            @jakarta.validation.constraints.DecimalMax(value = "10000", message = "Hourly rate must not exceed 10000")
            BigDecimal hourlyRate) {}

    public record CreateHourRequest(
            @jakarta.validation.constraints.NotNull(message = "User ID is required")
            UUID userId,
            @jakarta.validation.constraints.NotNull(message = "Customer ID is required")
            UUID customerId,
            @jakarta.validation.constraints.NotNull(message = "Category ID is required")
            UUID categoryId,
            @jakarta.validation.constraints.NotNull(message = "Hours is required")
            @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "Hours must be greater than 0")
            @jakarta.validation.constraints.DecimalMax(value = "24", message = "Hours must not exceed 24")
            BigDecimal hours,
            @jakarta.validation.constraints.NotNull(message = "Work date is required")
            @jakarta.validation.constraints.PastOrPresent(message = "Work date must not be in the future")
            LocalDate workDate) {}

    public record CategoryResponse(UUID id, String name, BigDecimal hourlyRate,
                                   String createdAt, String updatedAt) {
        public static CategoryResponse from(BillingCategory c) {
            return new CategoryResponse(c.getId(), c.getName(), c.getHourlyRate(),
                    c.getCreatedAt() != null ? c.getCreatedAt().toString() : null,
                    c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null);
        }
    }

    public record HourResponse(UUID id, UUID userId, UUID customerId, UUID categoryId,
                                BigDecimal hours, String workDate, String createdAt, String updatedAt) {
        public static HourResponse from(BillableHour h) {
            return new HourResponse(h.getId(), h.getUserId(), h.getCustomerId(), h.getCategoryId(),
                    h.getHours(), h.getWorkDate() != null ? h.getWorkDate().toString() : null,
                    h.getCreatedAt() != null ? h.getCreatedAt().toString() : null,
                    h.getUpdatedAt() != null ? h.getUpdatedAt().toString() : null);
        }
    }
}
