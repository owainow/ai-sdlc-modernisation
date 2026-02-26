package com.bigbadmonolith.reporting.repository;

import com.bigbadmonolith.reporting.model.ReportUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportUserRepository extends JpaRepository<ReportUser, UUID> {
}
