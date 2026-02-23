package com.sourcegraph.demo.bigbadmonolith.service.impl;

import com.sourcegraph.demo.bigbadmonolith.dao.BillableHourDAO;
import com.sourcegraph.demo.bigbadmonolith.entity.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.exception.ResourceNotFoundException;
import com.sourcegraph.demo.bigbadmonolith.service.BillableHourService;
import com.sourcegraph.demo.bigbadmonolith.service.BillingService;

import java.sql.SQLException;
import java.util.List;

/**
 * T055: BillableHourService implementation delegating to BillableHourDAO.
 */
public class BillableHourServiceImpl implements BillableHourService {

    private final BillableHourDAO billableHourDAO;
    private final BillingService billingService;

    public BillableHourServiceImpl() {
        this.billableHourDAO = new BillableHourDAO();
        this.billingService = new BillingService();
    }

    public BillableHourServiceImpl(BillableHourDAO billableHourDAO, BillingService billingService) {
        this.billableHourDAO = billableHourDAO;
        this.billingService = billingService;
    }

    @Override
    public BillableHour save(BillableHour hour) throws SQLException {
        return billableHourDAO.save(hour);
    }

    @Override
    public BillableHour findById(Long id) throws SQLException {
        BillableHour hour = billableHourDAO.findById(id);
        if (hour == null) {
            throw new ResourceNotFoundException("BillableHour", id);
        }
        return hour;
    }

    @Override
    public List<BillableHour> findAll() throws SQLException {
        return billableHourDAO.findAll();
    }

    @Override
    public List<BillableHour> findByCustomerId(Long customerId) throws SQLException {
        return billableHourDAO.findByCustomerId(customerId);
    }

    @Override
    public List<BillableHour> findByUserId(Long userId) throws SQLException {
        return billableHourDAO.findByUserId(userId);
    }

    @Override
    public boolean update(BillableHour hour) throws SQLException {
        return billableHourDAO.update(hour);
    }

    @Override
    public boolean delete(Long id) throws SQLException {
        return billableHourDAO.delete(id);
    }

    @Override
    public String validate(BillableHour hour) {
        return billingService.validateBillableHour(hour);
    }
}
