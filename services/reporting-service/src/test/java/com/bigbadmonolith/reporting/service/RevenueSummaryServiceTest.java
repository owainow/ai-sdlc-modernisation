package com.bigbadmonolith.reporting.service;

import com.bigbadmonolith.reporting.dto.RevenueSummaryResponse;
import com.bigbadmonolith.reporting.model.ReportBillableHour;
import com.bigbadmonolith.reporting.model.ReportBillingCategory;
import com.bigbadmonolith.reporting.model.ReportCustomer;
import com.bigbadmonolith.reporting.repository.ReportBillableHourRepository;
import com.bigbadmonolith.reporting.repository.ReportBillingCategoryRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevenueSummaryServiceTest {

    @Mock
    private ReportBillableHourRepository billableHourRepository;
    @Mock
    private ReportCustomerRepository customerRepository;
    @Mock
    private ReportBillingCategoryRepository categoryRepository;

    @InjectMocks
    private RevenueSummaryService revenueSummaryService;

    private UUID customerId1;
    private UUID customerId2;
    private UUID categoryId1;
    private UUID categoryId2;
    private ReportCustomer customer1;
    private ReportCustomer customer2;
    private ReportBillingCategory category1;
    private ReportBillingCategory category2;

    @BeforeEach
    void setUp() {
        customerId1 = UUID.randomUUID();
        customerId2 = UUID.randomUUID();
        categoryId1 = UUID.randomUUID();
        categoryId2 = UUID.randomUUID();

        customer1 = new ReportCustomer();
        customer1.setId(customerId1);
        customer1.setName("Acme Corp");

        customer2 = new ReportCustomer();
        customer2.setId(customerId2);
        customer2.setName("Beta Inc");

        category1 = new ReportBillingCategory();
        category1.setId(categoryId1);
        category1.setName("Development");
        category1.setHourlyRate(new BigDecimal("150.00"));

        category2 = new ReportBillingCategory();
        category2.setId(categoryId2);
        category2.setName("Consulting");
        category2.setHourlyRate(new BigDecimal("200.00"));
    }

    @Test
    void getRevenueSummary_shouldIncludeAllCustomersAndCategories() {
        ReportBillableHour hour = createHour(customerId1, categoryId1, new BigDecimal("8.00"), new BigDecimal("150.00"));

        when(billableHourRepository.findAll()).thenReturn(List.of(hour));
        when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2));
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        RevenueSummaryResponse result = revenueSummaryService.getRevenueSummary();

        // Both customers appear, even customer2 with zero hours
        assertThat(result.byCustomer()).hasSize(2);
        var acme = result.byCustomer().stream().filter(c -> c.customerId().equals(customerId1)).findFirst().orElseThrow();
        assertThat(acme.totalHours()).isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(acme.totalRevenue()).isEqualByComparingTo(new BigDecimal("1200.00"));
        assertThat(acme.averageRate()).isEqualByComparingTo(new BigDecimal("150.00"));

        var beta = result.byCustomer().stream().filter(c -> c.customerId().equals(customerId2)).findFirst().orElseThrow();
        assertThat(beta.totalHours()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(beta.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(beta.averageRate()).isEqualByComparingTo(BigDecimal.ZERO);

        // Both categories appear, even category2 with zero hours
        assertThat(result.byCategory()).hasSize(2);
        var dev = result.byCategory().stream().filter(c -> c.categoryId().equals(categoryId1)).findFirst().orElseThrow();
        assertThat(dev.totalHours()).isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(dev.totalRevenue()).isEqualByComparingTo(new BigDecimal("1200.00"));

        var consult = result.byCategory().stream().filter(c -> c.categoryId().equals(categoryId2)).findFirst().orElseThrow();
        assertThat(consult.totalHours()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(consult.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getRevenueSummary_shouldReturnEmptyWhenNoData() {
        when(billableHourRepository.findAll()).thenReturn(List.of());
        when(customerRepository.findAll()).thenReturn(List.of());
        when(categoryRepository.findAll()).thenReturn(List.of());

        RevenueSummaryResponse result = revenueSummaryService.getRevenueSummary();

        assertThat(result.byCustomer()).isEmpty();
        assertThat(result.byCategory()).isEmpty();
    }

    @Test
    void getRevenueSummary_shouldCalculateAverageRateCorrectly() {
        ReportBillableHour hour1 = createHour(customerId1, categoryId1, new BigDecimal("4.00"), new BigDecimal("100.00"));
        ReportBillableHour hour2 = createHour(customerId1, categoryId2, new BigDecimal("6.00"), new BigDecimal("200.00"));

        when(billableHourRepository.findAll()).thenReturn(List.of(hour1, hour2));
        when(customerRepository.findAll()).thenReturn(List.of(customer1));
        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        RevenueSummaryResponse result = revenueSummaryService.getRevenueSummary();

        var acme = result.byCustomer().get(0);
        assertThat(acme.totalHours()).isEqualByComparingTo(new BigDecimal("10.00"));
        // Revenue: 4*100 + 6*200 = 400 + 1200 = 1600
        assertThat(acme.totalRevenue()).isEqualByComparingTo(new BigDecimal("1600.00"));
        // Average rate: 1600 / 10 = 160.00
        assertThat(acme.averageRate()).isEqualByComparingTo(new BigDecimal("160.00"));
    }

    @Test
    void getRevenueSummary_shouldShowCustomersWithZeroHours() {
        when(billableHourRepository.findAll()).thenReturn(List.of());
        when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2));
        when(categoryRepository.findAll()).thenReturn(List.of(category1));

        RevenueSummaryResponse result = revenueSummaryService.getRevenueSummary();

        assertThat(result.byCustomer()).hasSize(2);
        result.byCustomer().forEach(c -> {
            assertThat(c.totalHours()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(c.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(c.averageRate()).isEqualByComparingTo(BigDecimal.ZERO);
        });
    }

    private ReportBillableHour createHour(UUID customerId, UUID categoryId, BigDecimal hours, BigDecimal rate) {
        ReportBillableHour hour = new ReportBillableHour();
        hour.setId(UUID.randomUUID());
        hour.setCustomerId(customerId);
        hour.setUserId(UUID.randomUUID());
        hour.setCategoryId(categoryId);
        hour.setHours(hours);
        hour.setRateSnapshot(rate);
        hour.setDateLogged(LocalDate.now());
        hour.setCreatedAt(Instant.now());
        return hour;
    }
}
