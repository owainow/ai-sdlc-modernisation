package com.bigbadmonolith.billing.controller;

import com.bigbadmonolith.billing.dto.*;
import com.bigbadmonolith.billing.service.BillableHourService;
import com.bigbadmonolith.billing.service.BillableHourService.BillableHourCreateResult;
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

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing/hours")
public class BillableHourController {
    private final BillableHourService billableHourService;

    public BillableHourController(BillableHourService billableHourService) {
        this.billableHourService = billableHourService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@Valid @RequestBody CreateBillableHourRequest request) {
        BillableHourCreateResult result = billableHourService.create(request);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("entry", result.response());
        if (result.warning() != null) {
            data.put("warning", result.warning());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BillableHourResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateLogged") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<BillableHourResponse> result = billableHourService.findAll(customerId, userId, categoryId, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(result.getContent(), PageMeta.from(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillableHourResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(billableHourService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BillableHourResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateBillableHourRequest request) {
        return ResponseEntity.ok(ApiResponse.success(billableHourService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        billableHourService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
