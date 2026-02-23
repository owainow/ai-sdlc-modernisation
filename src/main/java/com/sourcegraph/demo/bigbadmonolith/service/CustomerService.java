package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.entity.Customer;

import java.sql.SQLException;
import java.util.List;

/**
 * T048: Service interface for Customer operations.
 */
public interface CustomerService {
    Customer save(Customer customer) throws SQLException;
    Customer findById(Long id) throws SQLException;
    List<Customer> findAll() throws SQLException;
    boolean update(Customer customer) throws SQLException;
    boolean delete(Long id) throws SQLException;
}
