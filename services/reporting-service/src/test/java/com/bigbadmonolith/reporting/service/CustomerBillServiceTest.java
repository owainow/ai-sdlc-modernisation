package com.bigbadmonolith.reporting.service;

import com.bigbadmonolith.common.exception.ResourceNotFoundException;
import com.bigbadmonolith.reporting.dto.CustomerBillResponse;
import com.bigbadmonolith.reporting.model.ReportBillableHour;
import com.bigbadmonolith.reporting.model.ReportBillingCategory;
import com.bigbadmonolith.reporting.model.ReportCustomer;
import com.bigbadmonolith.reporting.model.ReportUser;
import com.bigbadmonolith.reporting.repository.ReportBillableHourRepository;
import com.bigbadmonolith.reporting.repository.ReportBillingCategoryRepository;
import com.bigbadmonolith.reporting.repository.ReportCustomerRepository;
import com.bigbadmonolith.reporting.repository.ReportUserRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerBillServiceTest {

    @Mock
    private ReportBillableHourRepository billableHourRepository;
    @Mock
    private ReportCustomerRepository customerRepository;
    @Mock
    private ReportUserRepository userRepository;
    @Mock
    private ReportBillingCategoryRepository categoryRepository;

    @InjectMocks
    private CustomerBillService customerBillService;

    private UUID customerId;
    private UUID userId;
    private UUID categoryId;
    private ReportCustomer customer;
    private ReportUser user;
    private ReportBillingCategory category;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        customer = new ReportCustomer();
        customer.setId(customerId);
        customer.setName("Acme Corp");
        customer.setEmail("acme@example.com");

        user = new ReportUser();
        user.setId(userId);
        user.setName("John Doe");
        user.setEmail("john@example.com");

        category = new ReportBillingCategory();
        category.setId(categoryId);
        category.setName("Development");
        category.setHourlyRate(new BigDecimal("150.00"));
    }

    @Test
    void getCustomerBill_shouldReturnBillWithLineItems() {
        ReportBillableHour hour = new ReportBillableHour();
        hour.setId(UUID.randomUUID());
        hour.setCustomerId(customerId);
        hour.setUserId(userId);
        hour.setCategoryId(categoryId);
        hour.setHours(new BigDecimal("8.00"));
        hour.setRateSnapshot(new BigDecimal("150.00"));
        hour.setDateLogged(LocalDate.of(2024, 1, 15));
        hour.setNote("Backend work");
        hour.setCreatedAt(Instant.now());

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(billableHourRepository.findByCustomerIdOrderByDateLoggedDesc(customerId)).thenReturn(List.of(hour));
        when(userRepository.findAllById(any())).thenReturn(List.of(user));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(category));

        CustomerBillResponse result = customerBillService.getCustomerBill(customerId);

        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.customerName()).isEqualTo("Acme Corp");
        assertThat(result.lineItems()).hasSize(1);
        assertThat(result.lineItems().get(0).userName()).isEqualTo("John Doe");
        assertThat(result.lineItems().get(0).categoryName()).isEqualTo("Development");
        assertThat(result.lineItems().get(0).hours()).isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(result.lineItems().get(0).lineTotal()).isEqualByComparingTo(new BigDecimal("1200.00"));
        assertThat(result.totalHours()).isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(result.totalRevenue()).isEqualByComparingTo(new BigDecimal("1200.00"));
    }

    @Test
    void getCustomerBill_shouldReturnEmptyBillWhenNoHours() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(billableHourRepository.findByCustomerIdOrderByDateLoggedDesc(customerId)).thenReturn(List.of());

        CustomerBillResponse result = customerBillService.getCustomerBill(customerId);

        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.lineItems()).isEmpty();
        assertThat(result.totalHours()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getCustomerBill_shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerBillService.getCustomerBill(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(customerId.toString());
    }

    @Test
    void getCustomerBill_shouldCalculateTotalsForMultipleLineItems() {
        ReportBillableHour hour1 = new ReportBillableHour();
        hour1.setId(UUID.randomUUID());
        hour1.setCustomerId(customerId);
        hour1.setUserId(userId);
        hour1.setCategoryId(categoryId);
        hour1.setHours(new BigDecimal("4.00"));
        hour1.setRateSnapshot(new BigDecimal("150.00"));
        hour1.setDateLogged(LocalDate.of(2024, 1, 15));
        hour1.setCreatedAt(Instant.now());

        ReportBillableHour hour2 = new ReportBillableHour();
        hour2.setId(UUID.randomUUID());
        hour2.setCustomerId(customerId);
        hour2.setUserId(userId);
        hour2.setCategoryId(categoryId);
        hour2.setHours(new BigDecimal("6.00"));
        hour2.setRateSnapshot(new BigDecimal("100.00"));
        hour2.setDateLogged(LocalDate.of(2024, 1, 16));
        hour2.setCreatedAt(Instant.now());

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(billableHourRepository.findByCustomerIdOrderByDateLoggedDesc(customerId)).thenReturn(List.of(hour1, hour2));
        when(userRepository.findAllById(any())).thenReturn(List.of(user));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(category));

        CustomerBillResponse result = customerBillService.getCustomerBill(customerId);

        assertThat(result.lineItems()).hasSize(2);
        assertThat(result.totalHours()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(result.totalRevenue()).isEqualByComparingTo(new BigDecimal("1200.00"));
    }
}
