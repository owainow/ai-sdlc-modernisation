package com.bigbadmonolith.billing.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateBillableHourRequest(
    @NotNull(message = "Customer ID is required")
    UUID customerId,

    @NotNull(message = "User ID is required")
    UUID userId,

    @NotNull(message = "Category ID is required")
    UUID categoryId,

    @NotNull(message = "Hours is required")
    @DecimalMin(value = "0.01", message = "Hours must be at least 0.01")
    @DecimalMax(value = "24", message = "Hours must not exceed 24")
    BigDecimal hours,

    @NotNull(message = "Date logged is required")
    @PastOrPresent(message = "Date logged must not be in the future")
    LocalDate dateLogged,

    String note
) {}
