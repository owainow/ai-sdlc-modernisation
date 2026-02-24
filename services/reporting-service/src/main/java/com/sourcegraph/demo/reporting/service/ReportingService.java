package com.sourcegraph.demo.reporting.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface ReportingService {
    Map<String, Object> getMonthlyReport(int year, int month, UUID customerId);
    Map<String, Object> getRangeReport(LocalDate fromDate, LocalDate toDate, UUID customerId);
    Map<String, Object> getUtilisationReport(int year, int month, UUID userId);
}
