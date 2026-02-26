package com.bigbadmonolith.reporting.repository;

import com.bigbadmonolith.reporting.model.ReportBillableHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReportBillableHourRepository extends JpaRepository<ReportBillableHour, UUID> {

    List<ReportBillableHour> findByCustomerIdOrderByDateLoggedDesc(UUID customerId);

    @Query("SELECT b FROM ReportBillableHour b WHERE b.dateLogged >= :startDate AND b.dateLogged <= :endDate ORDER BY b.dateLogged DESC")
    List<ReportBillableHour> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
