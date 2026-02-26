package com.bigbadmonolith.reporting.service;

import com.bigbadmonolith.reporting.dto.RevenueSummaryByCategory;
import com.bigbadmonolith.reporting.dto.RevenueSummaryByCustomer;
import com.bigbadmonolith.reporting.dto.RevenueSummaryResponse;
import com.bigbadmonolith.reporting.model.ReportBillableHour;
import com.bigbadmonolith.reporting.model.ReportBillingCategory;
import com.bigbadmonolith.reporting.model.ReportCustomer;
import com.bigbadmonolith.reporting.repository.ReportBillableHourRepository;
import com.bigbadmonolith.reporting.repository.ReportBillingCategoryRepository;
import com.bigbadmonolith.reporting.repository.ReportCustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RevenueSummaryService {

    private final ReportBillableHourRepository billableHourRepository;
    private final ReportCustomerRepository customerRepository;
    private final ReportBillingCategoryRepository categoryRepository;

    public RevenueSummaryService(ReportBillableHourRepository billableHourRepository,
                                 ReportCustomerRepository customerRepository,
                                 ReportBillingCategoryRepository categoryRepository) {
        this.billableHourRepository = billableHourRepository;
        this.customerRepository = customerRepository;
        this.categoryRepository = categoryRepository;
    }

    public RevenueSummaryResponse getRevenueSummary() {
        List<ReportBillableHour> allHours = billableHourRepository.findAll();
        List<ReportCustomer> allCustomers = customerRepository.findAll();
        List<ReportBillingCategory> allCategories = categoryRepository.findAll();

        Map<UUID, List<ReportBillableHour>> hoursByCustomer = allHours.stream()
                .collect(Collectors.groupingBy(ReportBillableHour::getCustomerId));

        List<RevenueSummaryByCustomer> byCustomer = allCustomers.stream()
                .map(customer -> {
                    List<ReportBillableHour> customerHours = hoursByCustomer.getOrDefault(customer.getId(), List.of());

                    BigDecimal totalHours = customerHours.stream()
                            .map(ReportBillableHour::getHours)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalRevenue = customerHours.stream()
                            .map(h -> h.getHours().multiply(h.getRateSnapshot()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal averageRate = totalHours.compareTo(BigDecimal.ZERO) > 0
                            ? totalRevenue.divide(totalHours, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return new RevenueSummaryByCustomer(
                            customer.getId(),
                            customer.getName(),
                            totalHours,
                            totalRevenue,
                            averageRate
                    );
                })
                .toList();

        Map<UUID, List<ReportBillableHour>> hoursByCategory = allHours.stream()
                .collect(Collectors.groupingBy(ReportBillableHour::getCategoryId));

        List<RevenueSummaryByCategory> byCategory = allCategories.stream()
                .map(category -> {
                    List<ReportBillableHour> categoryHours = hoursByCategory.getOrDefault(category.getId(), List.of());

                    BigDecimal totalHours = categoryHours.stream()
                            .map(ReportBillableHour::getHours)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalRevenue = categoryHours.stream()
                            .map(h -> h.getHours().multiply(h.getRateSnapshot()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new RevenueSummaryByCategory(
                            category.getId(),
                            category.getName(),
                            category.getHourlyRate(),
                            totalHours,
                            totalRevenue
                    );
                })
                .toList();

        return new RevenueSummaryResponse(byCustomer, byCategory);
    }
}
