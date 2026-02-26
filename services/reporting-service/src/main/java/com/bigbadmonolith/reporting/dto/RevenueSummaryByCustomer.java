package com.bigbadmonolith.reporting.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RevenueSummaryByCustomer(
    UUID customerId,
    String customerName,
    BigDecimal totalHours,
    BigDecimal totalRevenue,
    BigDecimal averageRate
) {}
