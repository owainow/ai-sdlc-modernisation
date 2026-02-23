package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;

import java.sql.SQLException;
import java.util.List;

/**
 * T049: Service interface for BillingCategory operations.
 */
public interface BillingCategoryService {
    BillingCategory save(BillingCategory category) throws SQLException;
    BillingCategory findById(Long id) throws SQLException;
    List<BillingCategory> findAll() throws SQLException;
    boolean update(BillingCategory category) throws SQLException;
    boolean delete(Long id) throws SQLException;
}
