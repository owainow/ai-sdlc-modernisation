package com.sourcegraph.demo.bigbadmonolith.service.impl;

import com.sourcegraph.demo.bigbadmonolith.dao.CustomerDAO;
import com.sourcegraph.demo.bigbadmonolith.entity.Customer;
import com.sourcegraph.demo.bigbadmonolith.exception.ResourceNotFoundException;
import com.sourcegraph.demo.bigbadmonolith.service.CustomerService;

import java.sql.SQLException;
import java.util.List;

/**
 * T053: CustomerService implementation delegating to CustomerDAO.
 */
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerServiceImpl() {
        this.customerDAO = new CustomerDAO();
    }

    public CustomerServiceImpl(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    @Override
    public Customer save(Customer customer) throws SQLException {
        return customerDAO.save(customer);
    }

    @Override
    public Customer findById(Long id) throws SQLException {
        Customer customer = customerDAO.findById(id);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer", id);
        }
        return customer;
    }

    @Override
    public List<Customer> findAll() throws SQLException {
        return customerDAO.findAll();
    }

    @Override
    public boolean update(Customer customer) throws SQLException {
        return customerDAO.update(customer);
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        return customerDAO.delete(id);
    }
}
