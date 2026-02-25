package com.sourcegraph.demo.customer.service;

import com.sourcegraph.demo.customer.entity.Customer;
import com.sourcegraph.demo.customer.exception.DuplicateResourceException;
import com.sourcegraph.demo.customer.exception.ResourceNotFoundException;
import com.sourcegraph.demo.customer.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    @Override
    public Customer create(String name) {
        if (customerRepository.existsByName(name)) {
            throw new DuplicateResourceException("Customer", "name", name);
        }
        Customer customer = new Customer();
        customer.setName(name);
        return customerRepository.save(customer);
    }

    @Override
    public Customer update(UUID id, String name) {
        Customer customer = findById(id);
        if (!customer.getName().equals(name) && customerRepository.existsByName(name)) {
            throw new DuplicateResourceException("Customer", "name", name);
        }
        customer.setName(name);
        return customerRepository.save(customer);
    }

    @Override
    public void delete(UUID id) {
        Customer customer = findById(id);
        customerRepository.delete(customer);
    }
}
