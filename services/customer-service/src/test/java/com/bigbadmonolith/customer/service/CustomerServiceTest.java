package com.bigbadmonolith.customer.service;

import com.bigbadmonolith.customer.dto.*;
import com.bigbadmonolith.customer.model.Customer;
import com.bigbadmonolith.customer.repository.CustomerRepository;
import com.bigbadmonolith.common.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testCustomer = new Customer();
        testCustomer.setId(testId);
        testCustomer.setName("Acme Corp");
        testCustomer.setEmail("billing@acme.com");
        testCustomer.setAddress("123 Business St");
        testCustomer.setCreatedAt(Instant.now());
        testCustomer.setUpdatedAt(Instant.now());
    }

    @Test
    void create_shouldCreateCustomer() {
        when(customerRepository.existsByName("Acme Corp")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        var result = customerService.create(new CreateCustomerRequest("Acme Corp", "billing@acme.com", "123 Business St"));

        assertThat(result.name()).isEqualTo("Acme Corp");
        assertThat(result.email()).isEqualTo("billing@acme.com");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void create_shouldThrowOnDuplicateName() {
        when(customerRepository.existsByName("Acme Corp")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(new CreateCustomerRequest("Acme Corp", null, null)))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void findById_shouldReturnCustomer() {
        when(customerRepository.findById(testId)).thenReturn(Optional.of(testCustomer));

        var result = customerService.findById(testId);

        assertThat(result.id()).isEqualTo(testId);
        assertThat(result.name()).isEqualTo("Acme Corp");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(customerRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(testId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldDeleteCustomer() {
        when(customerRepository.findById(testId)).thenReturn(Optional.of(testCustomer));

        customerService.delete(testId);

        verify(customerRepository).delete(testCustomer);
    }

    @Test
    void count_shouldReturnCount() {
        when(customerRepository.count()).thenReturn(5L);

        assertThat(customerService.count()).isEqualTo(5L);
    }

    @Test
    void update_shouldUpdateCustomer() {
        when(customerRepository.findById(testId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        var result = customerService.update(testId, new UpdateCustomerRequest("Acme Corp", "new@acme.com", "456 New St"));

        assertThat(result.name()).isEqualTo("Acme Corp");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void update_shouldThrowOnDuplicateName() {
        when(customerRepository.findById(testId)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByName("Other Corp")).thenReturn(true);

        assertThatThrownBy(() -> customerService.update(testId, new UpdateCustomerRequest("Other Corp", null, null)))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        when(customerRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.update(testId, new UpdateCustomerRequest("Acme Corp", null, null)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(List.of(testCustomer), pageable, 1);
        when(customerRepository.findAll(pageable)).thenReturn(page);

        var result = customerService.findAll(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Acme Corp");
    }

    @Test
    void findAll_shouldSearchByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(List.of(testCustomer), pageable, 1);
        when(customerRepository.searchByName(eq("Acme"), eq(pageable))).thenReturn(page);

        var result = customerService.findAll("Acme", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(customerRepository).searchByName("Acme", pageable);
    }

    @Test
    void exists_shouldReturnTrue() {
        when(customerRepository.existsById(testId)).thenReturn(true);

        assertThat(customerService.exists(testId)).isTrue();
    }

    @Test
    void exists_shouldReturnFalse() {
        when(customerRepository.existsById(testId)).thenReturn(false);

        assertThat(customerService.exists(testId)).isFalse();
    }
}
