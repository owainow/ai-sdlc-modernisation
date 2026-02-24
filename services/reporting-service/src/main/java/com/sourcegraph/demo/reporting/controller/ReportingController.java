package com.sourcegraph.demo.reporting.controller;

import com.sourcegraph.demo.common.dto.ApiResponse;
import com.sourcegraph.demo.reporting.service.ReportingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) UUID customerId) {
        Map<String, Object> report = reportingService.getMonthlyReport(year, month, customerId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRangeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID customerId) {
        Map<String, Object> report = reportingService.getRangeReport(fromDate, toDate, customerId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/utilisation")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUtilisationReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) UUID userId) {
        Map<String, Object> report = reportingService.getUtilisationReport(year, month, userId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
