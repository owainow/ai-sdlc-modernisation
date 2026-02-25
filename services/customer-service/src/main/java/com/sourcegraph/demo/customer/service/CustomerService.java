package com.sourcegraph.demo.customer.service;

import com.sourcegraph.demo.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {
    Page<Customer> findAll(Pageable pageable);
    Customer findById(UUID id);
    Customer create(String name);
    Customer update(UUID id, String name);
    void delete(UUID id);
}
