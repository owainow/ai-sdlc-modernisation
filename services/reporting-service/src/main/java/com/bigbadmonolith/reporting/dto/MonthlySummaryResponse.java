package com.bigbadmonolith.reporting.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryResponse(
    int year,
    int month,
    List<MonthlySummaryRow> customers,
    BigDecimal grandTotalHours,
    BigDecimal grandTotalRevenue
) {}
