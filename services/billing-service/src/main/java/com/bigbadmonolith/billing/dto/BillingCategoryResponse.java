package com.bigbadmonolith.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BillingCategoryResponse(
    UUID id,
    String name,
    String description,
    BigDecimal hourlyRate,
    Instant createdAt,
    Instant updatedAt
) {}
