package com.bigbadmonolith.reporting.service;

import com.bigbadmonolith.reporting.model.ReportBillableHour;
import com.bigbadmonolith.reporting.model.ReportBillingCategory;
import com.bigbadmonolith.reporting.model.ReportCustomer;
import com.bigbadmonolith.reporting.model.ReportUser;
import com.bigbadmonolith.reporting.repository.ReportBillableHourRepository;
import com.bigbadmonolith.reporting.repository.ReportBillingCategoryRepository;
import com.bigbadmonolith.reporting.repository.ReportCustomerRepository;
import com.bigbadmonolith.reporting.repository.ReportUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class EventHandlerService {

    private final ReportUserRepository userRepository;
    private final ReportCustomerRepository customerRepository;
    private final ReportBillingCategoryRepository categoryRepository;
    private final ReportBillableHourRepository billableHourRepository;

    public EventHandlerService(ReportUserRepository userRepository,
                               ReportCustomerRepository customerRepository,
                               ReportBillingCategoryRepository categoryRepository,
                               ReportBillableHourRepository billableHourRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.categoryRepository = categoryRepository;
        this.billableHourRepository = billableHourRepository;
    }

    public void syncUser(UUID id, String name, String email) {
        ReportUser user = userRepository.findById(id).orElse(new ReportUser());
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        userRepository.save(user);
    }

    public void syncCustomer(UUID id, String name, String email, String address) {
        ReportCustomer customer = customerRepository.findById(id).orElse(new ReportCustomer());
        customer.setId(id);
        customer.setName(name);
        customer.setEmail(email);
        customer.setAddress(address);
        customerRepository.save(customer);
    }

    public void syncCategory(UUID id, String name, BigDecimal hourlyRate) {
        ReportBillingCategory category = categoryRepository.findById(id).orElse(new ReportBillingCategory());
        category.setId(id);
        category.setName(name);
        category.setHourlyRate(hourlyRate);
        categoryRepository.save(category);
    }

    public void syncBillableHour(UUID id, UUID customerId, UUID userId, UUID categoryId,
                                  BigDecimal hours, BigDecimal rateSnapshot, LocalDate dateLogged, String note) {
        ReportBillableHour billableHour = billableHourRepository.findById(id).orElse(new ReportBillableHour());
        billableHour.setId(id);
        billableHour.setCustomerId(customerId);
        billableHour.setUserId(userId);
        billableHour.setCategoryId(categoryId);
        billableHour.setHours(hours);
        billableHour.setRateSnapshot(rateSnapshot);
        billableHour.setDateLogged(dateLogged);
        billableHour.setNote(note);
        billableHourRepository.save(billableHour);
    }

    public void removeUser(UUID id) {
        userRepository.deleteById(id);
    }

    public void removeCustomer(UUID id) {
        customerRepository.deleteById(id);
    }

    public void removeCategory(UUID id) {
        categoryRepository.deleteById(id);
    }

    public void removeBillableHour(UUID id) {
        billableHourRepository.deleteById(id);
    }
}
