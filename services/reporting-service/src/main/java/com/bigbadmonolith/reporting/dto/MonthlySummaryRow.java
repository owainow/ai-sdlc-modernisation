package com.bigbadmonolith.reporting.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MonthlySummaryRow(
    UUID customerId,
    String customerName,
    BigDecimal totalHours,
    BigDecimal totalRevenue
) {}
