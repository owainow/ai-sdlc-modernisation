package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.entity.BillableHour;

import java.sql.SQLException;
import java.util.List;

/**
 * T050: Service interface for BillableHour operations.
 */
public interface BillableHourService {
    BillableHour save(BillableHour hour) throws SQLException;
    BillableHour findById(Long id) throws SQLException;
    List<BillableHour> findAll() throws SQLException;
    List<BillableHour> findByCustomerId(Long customerId) throws SQLException;
    List<BillableHour> findByUserId(Long userId) throws SQLException;
    boolean update(BillableHour hour) throws SQLException;
    boolean delete(Long id) throws SQLException;
    String validate(BillableHour hour);
}
