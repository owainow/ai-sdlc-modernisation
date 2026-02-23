package com.sourcegraph.demo.bigbadmonolith.service.impl;

import com.sourcegraph.demo.bigbadmonolith.dao.BillingCategoryDAO;
import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.exception.ResourceNotFoundException;
import com.sourcegraph.demo.bigbadmonolith.exception.ValidationException;
import com.sourcegraph.demo.bigbadmonolith.service.BillingCategoryService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * T054: BillingCategoryService implementation delegating to BillingCategoryDAO.
 */
public class BillingCategoryServiceImpl implements BillingCategoryService {

    private final BillingCategoryDAO categoryDAO;

    public BillingCategoryServiceImpl() {
        this.categoryDAO = new BillingCategoryDAO();
    }

    public BillingCategoryServiceImpl(BillingCategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    @Override
    public BillingCategory save(BillingCategory category) throws SQLException {
        validateRate(category);
        return categoryDAO.save(category);
    }

    @Override
    public BillingCategory findById(Long id) throws SQLException {
        BillingCategory category = categoryDAO.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("BillingCategory", id);
        }
        return category;
    }

    @Override
    public List<BillingCategory> findAll() throws SQLException {
        return categoryDAO.findAll();
    }

    @Override
    public boolean update(BillingCategory category) throws SQLException {
        validateRate(category);
        return categoryDAO.update(category);
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        return categoryDAO.delete(id);
    }

    private void validateRate(BillingCategory category) {
        if (category == null) {
            throw new ValidationException("Billing category must not be null");
        }
        if (category.getHourlyRate() == null) {
            throw new ValidationException("Hourly rate must not be null");
        }
        if (category.getHourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Hourly rate must be greater than zero");
        }
        if (category.getHourlyRate().compareTo(new BigDecimal("10000")) > 0) {
            throw new ValidationException("Hourly rate must not exceed 10,000");
        }
    }
}
