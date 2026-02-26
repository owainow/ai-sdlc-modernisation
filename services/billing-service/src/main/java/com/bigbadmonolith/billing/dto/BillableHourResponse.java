package com.bigbadmonolith.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BillableHourResponse(
    UUID id,
    UUID customerId,
    UUID userId,
    UUID categoryId,
    BigDecimal hours,
    BigDecimal rateSnapshot,
    LocalDate dateLogged,
    String note,
    BigDecimal lineTotal,
    Instant createdAt,
    Instant updatedAt
) {}
