package com.sourcegraph.demo.billing.repository;

import com.sourcegraph.demo.billing.entity.BillingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BillingCategoryRepository extends JpaRepository<BillingCategory, UUID> {
    boolean existsByName(String name);
}
