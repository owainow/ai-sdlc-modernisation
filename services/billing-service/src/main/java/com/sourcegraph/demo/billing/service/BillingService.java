package com.sourcegraph.demo.billing.service;

import com.sourcegraph.demo.billing.entity.BillableHour;
import com.sourcegraph.demo.billing.entity.BillingCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface BillingService {
    // Categories
    Page<BillingCategory> findAllCategories(Pageable pageable);
    BillingCategory findCategoryById(UUID id);
    BillingCategory createCategory(String name, java.math.BigDecimal hourlyRate);
    BillingCategory updateCategory(UUID id, String name, java.math.BigDecimal hourlyRate);
    void deleteCategory(UUID id);

    // Hours
    Page<BillableHour> findAllHours(Pageable pageable);
    BillableHour findHourById(UUID id);
    BillableHour createHour(UUID userId, UUID customerId, UUID categoryId,
                            java.math.BigDecimal hours, LocalDate workDate);
    BillableHour updateHour(UUID id, UUID userId, UUID customerId, UUID categoryId,
                            java.math.BigDecimal hours, LocalDate workDate);
    void deleteHour(UUID id);
    boolean existsByCustomerId(UUID customerId);

    // Summary
    Map<String, Object> getBillingSummary(UUID customerId, LocalDate fromDate, LocalDate toDate);
}
