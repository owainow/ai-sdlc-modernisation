package com.bigbadmonolith.reporting.repository;

import com.bigbadmonolith.reporting.model.ReportBillingCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportBillingCategoryRepository extends JpaRepository<ReportBillingCategory, UUID> {
}
