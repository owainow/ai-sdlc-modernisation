package com.sourcegraph.demo.bigbadmonolith.service.impl;

import com.sourcegraph.demo.bigbadmonolith.service.BillingService;
import com.sourcegraph.demo.bigbadmonolith.service.ReportingService;

import java.sql.SQLException;
import java.util.Map;

/**
 * T056: ReportingService implementation delegating to existing BillingService.
 */
public class ReportingServiceImpl implements ReportingService {

    private final BillingService billingService;

    public ReportingServiceImpl() {
        this.billingService = new BillingService();
    }

    public ReportingServiceImpl(BillingService billingService) {
        this.billingService = billingService;
    }

    @Override
    public Map<String, Object> generateCustomerBill(Long customerId) throws SQLException {
        return billingService.generateCustomerBill(customerId);
    }

    @Override
    public Map<String, Object> generateMonthlyReport(int year, int month) throws SQLException {
        return billingService.generateMonthlyReport(year, month);
    }
}
