package com.bigbadmonolith.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerBillLineItem(
    UUID id,
    String userName,
    String categoryName,
    BigDecimal hours,
    BigDecimal rate,
    BigDecimal lineTotal,
    LocalDate dateLogged,
    String note
) {}
