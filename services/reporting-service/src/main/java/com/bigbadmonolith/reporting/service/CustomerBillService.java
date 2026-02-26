package com.bigbadmonolith.reporting.service;

import com.bigbadmonolith.common.exception.ResourceNotFoundException;
import com.bigbadmonolith.reporting.dto.CustomerBillLineItem;
import com.bigbadmonolith.reporting.dto.CustomerBillResponse;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustomerBillService {

    private final ReportBillableHourRepository billableHourRepository;
    private final ReportCustomerRepository customerRepository;
    private final ReportUserRepository userRepository;
    private final ReportBillingCategoryRepository categoryRepository;

    public CustomerBillService(ReportBillableHourRepository billableHourRepository,
                               ReportCustomerRepository customerRepository,
                               ReportUserRepository userRepository,
                               ReportBillingCategoryRepository categoryRepository) {
        this.billableHourRepository = billableHourRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public CustomerBillResponse getCustomerBill(UUID customerId) {
        ReportCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        List<ReportBillableHour> hours = billableHourRepository.findByCustomerIdOrderByDateLoggedDesc(customerId);

        Map<UUID, ReportUser> usersMap = userRepository.findAllById(
                hours.stream().map(ReportBillableHour::getUserId).distinct().toList()
        ).stream().collect(Collectors.toMap(ReportUser::getId, Function.identity()));

        Map<UUID, ReportBillingCategory> categoriesMap = categoryRepository.findAllById(
                hours.stream().map(ReportBillableHour::getCategoryId).distinct().toList()
        ).stream().collect(Collectors.toMap(ReportBillingCategory::getId, Function.identity()));

        List<CustomerBillLineItem> lineItems = hours.stream()
                .map(h -> {
                    ReportUser user = usersMap.get(h.getUserId());
                    ReportBillingCategory category = categoriesMap.get(h.getCategoryId());
                    BigDecimal lineTotal = h.getHours().multiply(h.getRateSnapshot());
                    return new CustomerBillLineItem(
                            h.getId(),
                            user != null ? user.getName() : "Unknown",
                            category != null ? category.getName() : "Unknown",
                            h.getHours(),
                            h.getRateSnapshot(),
                            lineTotal,
                            h.getDateLogged(),
                            h.getNote()
                    );
                })
                .toList();

        BigDecimal totalHours = lineItems.stream()
                .map(CustomerBillLineItem::hours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = lineItems.stream()
                .map(CustomerBillLineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CustomerBillResponse(
                customer.getId(),
                customer.getName(),
                lineItems,
                totalHours,
                totalRevenue
        );
    }
}
