package com.sourcegraph.demo.reporting.controller;

import com.sourcegraph.demo.common.dto.ApiResponse;
import com.sourcegraph.demo.reporting.service.ReportingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    public ResponseEntity<?> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) UUID customerId) {
        if (month < 1 || month > 12) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST, "Month must be between 1 and 12");
            problem.setType(URI.create("https://api.example.com/problems/validation"));
            problem.setTitle("Validation Error");
            return ResponseEntity.badRequest().body(problem);
        }
        Map<String, Object> report = reportingService.getMonthlyReport(year, month, customerId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/range")
    public ResponseEntity<?> getRangeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID customerId) {
        if (fromDate.isAfter(toDate)) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST, "fromDate must not be after toDate");
            problem.setType(URI.create("https://api.example.com/problems/validation"));
            problem.setTitle("Validation Error");
            return ResponseEntity.badRequest().body(problem);
        }
        if (ChronoUnit.MONTHS.between(fromDate, toDate) > 12) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST, "Date range must not exceed 12 months");
            problem.setType(URI.create("https://api.example.com/problems/validation"));
            problem.setTitle("Validation Error");
            return ResponseEntity.badRequest().body(problem);
        }
        Map<String, Object> report = reportingService.getRangeReport(fromDate, toDate, customerId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/utilisation")
    public ResponseEntity<?> getUtilisationReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) UUID userId) {
        if (month < 1 || month > 12) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST, "Month must be between 1 and 12");
            problem.setType(URI.create("https://api.example.com/problems/validation"));
            problem.setTitle("Validation Error");
            return ResponseEntity.badRequest().body(problem);
        }
        Map<String, Object> report = reportingService.getUtilisationReport(year, month, userId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
