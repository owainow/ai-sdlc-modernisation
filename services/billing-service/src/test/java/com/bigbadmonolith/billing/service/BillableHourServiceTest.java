package com.bigbadmonolith.billing.service;

import com.bigbadmonolith.billing.dto.*;
import com.bigbadmonolith.billing.model.BillableHour;
import com.bigbadmonolith.billing.model.BillingCategory;
import com.bigbadmonolith.billing.repository.BillableHourRepository;
import com.bigbadmonolith.billing.repository.BillingCategoryRepository;
import com.bigbadmonolith.billing.service.BillableHourService.BillableHourCreateResult;
import com.bigbadmonolith.common.exception.BusinessValidationException;
import com.bigbadmonolith.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillableHourServiceTest {

    @Mock
    private BillableHourRepository billableHourRepository;

    @Mock
    private BillingCategoryRepository categoryRepository;

    @InjectMocks
    private BillableHourService billableHourService;

    private BillingCategory testCategory;
    private BillableHour testEntry;
    private UUID testId;
    private UUID customerId;
    private UUID userId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        testCategory = new BillingCategory();
        testCategory.setId(categoryId);
        testCategory.setName("Consulting");
        testCategory.setHourlyRate(new BigDecimal("150.00"));
        testCategory.setCreatedAt(Instant.now());
        testCategory.setUpdatedAt(Instant.now());

        testEntry = new BillableHour();
        testEntry.setId(testId);
        testEntry.setCustomerId(customerId);
        testEntry.setUserId(userId);
        testEntry.setCategoryId(categoryId);
        testEntry.setHours(new BigDecimal("8.00"));
        testEntry.setRateSnapshot(new BigDecimal("150.00"));
        testEntry.setDateLogged(LocalDate.now().minusDays(1));
        testEntry.setNote("Test work");
        testEntry.setCreatedAt(Instant.now());
        testEntry.setUpdatedAt(Instant.now());
    }

    @Test
    void create_shouldCreateWithRateSnapshot() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        var request = new CreateBillableHourRequest(customerId, userId, categoryId, new BigDecimal("8.00"), yesterday, "Test");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(billableHourRepository.sumHoursForUserOnDateNew(userId, yesterday)).thenReturn(BigDecimal.ZERO);
        when(billableHourRepository.save(any(BillableHour.class))).thenReturn(testEntry);

        BillableHourCreateResult result = billableHourService.create(request);

        assertThat(result.response().rateSnapshot()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.response().lineTotal()).isEqualByComparingTo(new BigDecimal("1200.00"));
        verify(billableHourRepository).save(any(BillableHour.class));
    }

    @Test
    void create_shouldRejectWhenExceeding24Hours() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        var request = new CreateBillableHourRequest(customerId, userId, categoryId, new BigDecimal("10.00"), yesterday, null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(billableHourRepository.sumHoursForUserOnDateNew(userId, yesterday)).thenReturn(new BigDecimal("16.00"));

        assertThatThrownBy(() -> billableHourService.create(request))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("exceed 24");
    }

    @Test
    void create_shouldWarnOnWeekend() {
        LocalDate saturday = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.SATURDAY));
        var request = new CreateBillableHourRequest(customerId, userId, categoryId, new BigDecimal("4.00"), saturday, null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(billableHourRepository.sumHoursForUserOnDateNew(userId, saturday)).thenReturn(BigDecimal.ZERO);

        BillableHour weekendEntry = new BillableHour();
        weekendEntry.setId(UUID.randomUUID());
        weekendEntry.setCustomerId(customerId);
        weekendEntry.setUserId(userId);
        weekendEntry.setCategoryId(categoryId);
        weekendEntry.setHours(new BigDecimal("4.00"));
        weekendEntry.setRateSnapshot(new BigDecimal("150.00"));
        weekendEntry.setDateLogged(saturday);
        weekendEntry.setCreatedAt(Instant.now());
        weekendEntry.setUpdatedAt(Instant.now());

        when(billableHourRepository.save(any(BillableHour.class))).thenReturn(weekendEntry);

        BillableHourCreateResult result = billableHourService.create(request);

        assertThat(result.warning()).isNotNull();
        assertThat(result.warning()).contains("weekend");
    }

    @Test
    void create_shouldRejectFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        var request = new CreateBillableHourRequest(customerId, userId, categoryId, new BigDecimal("4.00"), futureDate, null);

        assertThatThrownBy(() -> billableHourService.create(request))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("future");
    }

    @Test
    void create_shouldRejectMissingCategory() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        var request = new CreateBillableHourRequest(customerId, userId, categoryId, new BigDecimal("4.00"), yesterday, null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billableHourService.create(request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldUpdateEntry() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        var request = new UpdateBillableHourRequest(customerId, userId, categoryId, new BigDecimal("6.00"), yesterday, "Updated");

        when(billableHourRepository.findById(testId)).thenReturn(Optional.of(testEntry));
        when(billableHourRepository.sumHoursForUserOnDate(userId, yesterday, testId)).thenReturn(BigDecimal.ZERO);
        when(billableHourRepository.save(any(BillableHour.class))).thenReturn(testEntry);

        var result = billableHourService.update(testId, request);

        assertThat(result).isNotNull();
        verify(billableHourRepository).save(any(BillableHour.class));
    }

    @Test
    void update_shouldRejectWhenExceeding24Hours() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        var request = new UpdateBillableHourRequest(customerId, userId, categoryId, new BigDecimal("10.00"), yesterday, null);

        when(billableHourRepository.findById(testId)).thenReturn(Optional.of(testEntry));
        when(billableHourRepository.sumHoursForUserOnDate(userId, yesterday, testId)).thenReturn(new BigDecimal("16.00"));

        assertThatThrownBy(() -> billableHourService.update(testId, request))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("exceed 24");
    }

    @Test
    void delete_shouldDeleteEntry() {
        when(billableHourRepository.findById(testId)).thenReturn(Optional.of(testEntry));

        billableHourService.delete(testId);

        verify(billableHourRepository).delete(testEntry);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(billableHourRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billableHourService.delete(testId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void existsByCustomerId_shouldReturnTrue() {
        when(billableHourRepository.existsByCustomerId(customerId)).thenReturn(true);

        assertThat(billableHourService.existsByCustomerId(customerId)).isTrue();
    }

    @Test
    void existsByUserId_shouldReturnTrue() {
        when(billableHourRepository.existsByUserId(userId)).thenReturn(true);

        assertThat(billableHourService.existsByUserId(userId)).isTrue();
    }

    @Test
    void calculateTotalRevenue_shouldReturnTotal() {
        when(billableHourRepository.calculateTotalRevenue()).thenReturn(new BigDecimal("5000.00"));

        assertThat(billableHourService.calculateTotalRevenue()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }
}
