package com.sourcegraph.demo.bigbadmonolith.service;

import java.sql.SQLException;
import java.util.Map;

/**
 * T051: Service interface for Reporting operations.
 */
public interface ReportingService {
    Map<String, Object> generateCustomerBill(Long customerId) throws SQLException;
    Map<String, Object> generateMonthlyReport(int year, int month) throws SQLException;
}
