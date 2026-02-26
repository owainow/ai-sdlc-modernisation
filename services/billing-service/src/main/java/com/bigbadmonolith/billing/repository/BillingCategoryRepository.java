package com.bigbadmonolith.billing.repository;

import com.bigbadmonolith.billing.model.BillingCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BillingCategoryRepository extends JpaRepository<BillingCategory, UUID> {
    boolean existsByName(String name);
    Optional<BillingCategory> findByName(String name);
}
