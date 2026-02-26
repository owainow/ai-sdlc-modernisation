package com.bigbadmonolith.reporting.service;

import com.bigbadmonolith.reporting.dto.MonthlySummaryResponse;
import com.bigbadmonolith.reporting.model.ReportBillableHour;
import com.bigbadmonolith.reporting.model.ReportCustomer;
import com.bigbadmonolith.reporting.repository.ReportBillableHourRepository;
import com.bigbadmonolith.reporting.repository.ReportCustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlySummaryServiceTest {

    @Mock
    private ReportBillableHourRepository billableHourRepository;
    @Mock
    private ReportCustomerRepository customerRepository;

    @InjectMocks
    private MonthlySummaryService monthlySummaryService;

    private UUID customerId1;
    private UUID customerId2;
    private ReportCustomer customer1;
    private ReportCustomer customer2;

    @BeforeEach
    void setUp() {
        customerId1 = UUID.randomUUID();
        customerId2 = UUID.randomUUID();

        customer1 = new ReportCustomer();
        customer1.setId(customerId1);
        customer1.setName("Acme Corp");

        customer2 = new ReportCustomer();
        customer2.setId(customerId2);
        customer2.setName("Beta Inc");
    }

    @Test
    void getMonthlySummary_shouldReturnSummaryGroupedByCustomer() {
        ReportBillableHour hour1 = createHour(customerId1, new BigDecimal("8.00"), new BigDecimal("150.00"), LocalDate.of(2024, 1, 10));
        ReportBillableHour hour2 = createHour(customerId2, new BigDecimal("4.00"), new BigDecimal("100.00"), LocalDate.of(2024, 1, 15));

        when(billableHourRepository.findByDateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .thenReturn(List.of(hour1, hour2));
        when(customerRepository.findAllById(any())).thenReturn(List.of(customer1, customer2));

        MonthlySummaryResponse result = monthlySummaryService.getMonthlySummary(2024, 1);

        assertThat(result.year()).isEqualTo(2024);
        assertThat(result.month()).isEqualTo(1);
        assertThat(result.customers()).hasSize(2);
        // Sorted by revenue DESC: Acme (1200) > Beta (400)
        assertThat(result.customers().get(0).customerName()).isEqualTo("Acme Corp");
        assertThat(result.customers().get(0).totalRevenue()).isEqualByComparingTo(new BigDecimal("1200.00"));
        assertThat(result.customers().get(1).customerName()).isEqualTo("Beta Inc");
        assertThat(result.grandTotalHours()).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(result.grandTotalRevenue()).isEqualByComparingTo(new BigDecimal("1600.00"));
    }

    @Test
    void getMonthlySummary_shouldReturnEmptyForMonthWithNoHours() {
        when(billableHourRepository.findByDateRange(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29)))
                .thenReturn(List.of());

        MonthlySummaryResponse result = monthlySummaryService.getMonthlySummary(2024, 2);

        assertThat(result.year()).isEqualTo(2024);
        assertThat(result.month()).isEqualTo(2);
        assertThat(result.customers()).isEmpty();
        assertThat(result.grandTotalHours()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.grandTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getMonthlySummary_shouldHandleFebruaryLeapYear() {
        when(billableHourRepository.findByDateRange(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29)))
                .thenReturn(List.of());

        MonthlySummaryResponse result = monthlySummaryService.getMonthlySummary(2024, 2);

        assertThat(result.month()).isEqualTo(2);
        verify(billableHourRepository).findByDateRange(
                eq(LocalDate.of(2024, 2, 1)),
                eq(LocalDate.of(2024, 2, 29))
        );
    }

    @Test
    void getMonthlySummary_shouldHandleFebruaryNonLeapYear() {
        when(billableHourRepository.findByDateRange(LocalDate.of(2023, 2, 1), LocalDate.of(2023, 2, 28)))
                .thenReturn(List.of());

        MonthlySummaryResponse result = monthlySummaryService.getMonthlySummary(2023, 2);

        assertThat(result.month()).isEqualTo(2);
        verify(billableHourRepository).findByDateRange(
                eq(LocalDate.of(2023, 2, 1)),
                eq(LocalDate.of(2023, 2, 28))
        );
    }

    private ReportBillableHour createHour(UUID customerId, BigDecimal hours, BigDecimal rate, LocalDate date) {
        ReportBillableHour hour = new ReportBillableHour();
        hour.setId(UUID.randomUUID());
        hour.setCustomerId(customerId);
        hour.setUserId(UUID.randomUUID());
        hour.setCategoryId(UUID.randomUUID());
        hour.setHours(hours);
        hour.setRateSnapshot(rate);
        hour.setDateLogged(date);
        hour.setCreatedAt(Instant.now());
        return hour;
    }
}
