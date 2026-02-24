package com.sourcegraph.demo.reporting.repository;

import com.sourcegraph.demo.reporting.entity.BillingReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<BillingReadModel, UUID> {

    Optional<BillingReadModel> findByBillableHourId(UUID billableHourId);

    void deleteByBillableHourId(UUID billableHourId);

    @Query("SELECT r FROM BillingReadModel r WHERE YEAR(r.workDate) = :year AND MONTH(r.workDate) = :month")
    List<BillingReadModel> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT r FROM BillingReadModel r WHERE r.workDate BETWEEN :fromDate AND :toDate")
    List<BillingReadModel> findByDateRange(@Param("fromDate") LocalDate fromDate,
                                           @Param("toDate") LocalDate toDate);

    @Query("SELECT r FROM BillingReadModel r WHERE r.userId = :userId AND YEAR(r.workDate) = :year AND MONTH(r.workDate) = :month")
    List<BillingReadModel> findByUserIdAndYearAndMonth(@Param("userId") UUID userId,
                                                       @Param("year") int year,
                                                       @Param("month") int month);
}
