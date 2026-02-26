package com.bigbadmonolith.billing.controller;

import com.bigbadmonolith.billing.service.BillableHourService;
import com.bigbadmonolith.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/billing/dashboard")
public class DashboardController {
    private final BillableHourService billableHourService;

    public DashboardController(BillableHourService billableHourService) {
        this.billableHourService = billableHourService;
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getRevenue() {
        BigDecimal totalRevenue = billableHourService.calculateTotalRevenue();
        return ResponseEntity.ok(ApiResponse.success(Map.of("totalRevenue", totalRevenue)));
    }
}
