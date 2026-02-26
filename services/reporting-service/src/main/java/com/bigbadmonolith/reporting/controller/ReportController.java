package com.bigbadmonolith.reporting.controller;

import com.bigbadmonolith.common.dto.ApiResponse;
import com.bigbadmonolith.reporting.dto.CustomerBillResponse;
import com.bigbadmonolith.reporting.dto.MonthlySummaryResponse;
import com.bigbadmonolith.reporting.dto.RevenueSummaryResponse;
import com.bigbadmonolith.reporting.service.CustomerBillService;
import com.bigbadmonolith.reporting.service.MonthlySummaryService;
import com.bigbadmonolith.reporting.service.RevenueSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final CustomerBillService customerBillService;
    private final MonthlySummaryService monthlySummaryService;
    private final RevenueSummaryService revenueSummaryService;

    public ReportController(CustomerBillService customerBillService,
                            MonthlySummaryService monthlySummaryService,
                            RevenueSummaryService revenueSummaryService) {
        this.customerBillService = customerBillService;
        this.monthlySummaryService = monthlySummaryService;
        this.revenueSummaryService = revenueSummaryService;
    }

    @GetMapping("/customer-bill")
    public ResponseEntity<ApiResponse<CustomerBillResponse>> getCustomerBill(@RequestParam UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(customerBillService.getCustomerBill(customerId)));
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<ApiResponse<MonthlySummaryResponse>> getMonthlySummary(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success(monthlySummaryService.getMonthlySummary(year, month)));
    }

    @GetMapping("/revenue-summary")
    public ResponseEntity<ApiResponse<RevenueSummaryResponse>> getRevenueSummary() {
        return ResponseEntity.ok(ApiResponse.success(revenueSummaryService.getRevenueSummary()));
    }
}
