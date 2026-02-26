package com.bigbadmonolith.billing.repository;

import com.bigbadmonolith.billing.model.BillableHour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface BillableHourRepository extends JpaRepository<BillableHour, UUID> {

    @Query("SELECT COALESCE(SUM(b.hours), 0) FROM BillableHour b WHERE b.userId = :userId AND b.dateLogged = :date AND b.id <> :excludeId")
    BigDecimal sumHoursForUserOnDate(@Param("userId") UUID userId, @Param("date") LocalDate date, @Param("excludeId") UUID excludeId);

    @Query("SELECT COALESCE(SUM(b.hours), 0) FROM BillableHour b WHERE b.userId = :userId AND b.dateLogged = :date")
    BigDecimal sumHoursForUserOnDateNew(@Param("userId") UUID userId, @Param("date") LocalDate date);

    boolean existsByCustomerId(UUID customerId);
    boolean existsByUserId(UUID userId);
    boolean existsByCategoryId(UUID categoryId);

    Page<BillableHour> findByCustomerId(UUID customerId, Pageable pageable);
    Page<BillableHour> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT b FROM BillableHour b WHERE " +
           "(:customerId IS NULL OR b.customerId = :customerId) AND " +
           "(:userId IS NULL OR b.userId = :userId) AND " +
           "(:categoryId IS NULL OR b.categoryId = :categoryId) AND " +
           "(:fromDate IS NULL OR b.dateLogged >= :fromDate) AND " +
           "(:toDate IS NULL OR b.dateLogged <= :toDate)")
    Page<BillableHour> findFiltered(
        @Param("customerId") UUID customerId,
        @Param("userId") UUID userId,
        @Param("categoryId") UUID categoryId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(b.hours * b.rateSnapshot), 0) FROM BillableHour b")
    BigDecimal calculateTotalRevenue();
}
