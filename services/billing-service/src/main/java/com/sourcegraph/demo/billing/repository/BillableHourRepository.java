package com.sourcegraph.demo.billing.repository;

import com.sourcegraph.demo.billing.entity.BillableHour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BillableHourRepository extends JpaRepository<BillableHour, UUID> {
    Page<BillableHour> findByUserId(UUID userId, Pageable pageable);
    Page<BillableHour> findByCustomerId(UUID customerId, Pageable pageable);
    boolean existsByCategoryId(UUID categoryId);
    boolean existsByCustomerId(UUID customerId);

    @Query("SELECT COALESCE(SUM(bh.hours), 0) FROM BillableHour bh WHERE bh.userId = :userId AND bh.workDate = :workDate")
    BigDecimal sumHoursByUserIdAndWorkDate(@Param("userId") UUID userId, @Param("workDate") LocalDate workDate);

    @Query("SELECT bh FROM BillableHour bh WHERE bh.customerId = :customerId AND bh.workDate BETWEEN :fromDate AND :toDate")
    List<BillableHour> findByCustomerIdAndDateRange(
            @Param("customerId") UUID customerId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
