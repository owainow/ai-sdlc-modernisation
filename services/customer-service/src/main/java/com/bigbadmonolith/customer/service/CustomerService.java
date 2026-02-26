package com.bigbadmonolith.customer.service;

import com.bigbadmonolith.customer.dto.*;
import com.bigbadmonolith.customer.model.Customer;
import com.bigbadmonolith.customer.repository.CustomerRepository;
import com.bigbadmonolith.common.exception.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse create(CreateCustomerRequest request) {
        if (customerRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Customer with name '" + request.name() + "' already exists");
        }
        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        return toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAll(String search, Pageable pageable) {
        Page<Customer> page;
        if (search != null && !search.isBlank()) {
            page = customerRepository.searchByName(search, pageable);
        } else {
            page = customerRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        return customerRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    public CustomerResponse update(UUID id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        if (!customer.getName().equals(request.name()) && customerRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Customer with name '" + request.name() + "' already exists");
        }

        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        return toResponse(customerRepository.save(customer));
    }

    public void delete(UUID id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customerRepository.delete(customer);
    }

    @Transactional(readOnly = true)
    public long count() {
        return customerRepository.count();
    }

    @Transactional(readOnly = true)
    public boolean exists(UUID id) {
        return customerRepository.existsById(id);
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
            customer.getId(),
            customer.getName(),
            customer.getEmail(),
            customer.getAddress(),
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }
}
