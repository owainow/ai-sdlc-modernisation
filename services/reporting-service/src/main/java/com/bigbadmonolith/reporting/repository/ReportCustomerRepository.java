package com.bigbadmonolith.reporting.repository;

import com.bigbadmonolith.reporting.model.ReportCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportCustomerRepository extends JpaRepository<ReportCustomer, UUID> {
}
