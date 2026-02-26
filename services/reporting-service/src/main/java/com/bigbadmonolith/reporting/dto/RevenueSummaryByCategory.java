package com.bigbadmonolith.reporting.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RevenueSummaryByCategory(
    UUID categoryId,
    String categoryName,
    BigDecimal hourlyRate,
    BigDecimal totalHours,
    BigDecimal totalRevenue
) {}
