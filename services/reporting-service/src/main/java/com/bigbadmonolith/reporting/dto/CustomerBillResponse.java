package com.bigbadmonolith.reporting.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CustomerBillResponse(
    UUID customerId,
    String customerName,
    List<CustomerBillLineItem> lineItems,
    BigDecimal totalHours,
    BigDecimal totalRevenue
) {}
