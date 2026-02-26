package com.bigbadmonolith.reporting.dto;

import java.util.List;

public record RevenueSummaryResponse(
    List<RevenueSummaryByCustomer> byCustomer,
    List<RevenueSummaryByCategory> byCategory
) {}
