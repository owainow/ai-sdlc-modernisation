package com.sourcegraph.demo.reporting.service;

import com.sourcegraph.demo.reporting.entity.BillingReadModel;
import com.sourcegraph.demo.reporting.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportingServiceImpl implements ReportingService {

    private final ReportRepository reportRepository;

    public ReportingServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public Map<String, Object> getMonthlyReport(int year, int month, UUID customerId) {
        List<BillingReadModel> entries = reportRepository.findByYearAndMonth(year, month);
        if (customerId != null) {
            entries = entries.stream().filter(e -> e.getCustomerId().equals(customerId)).toList();
        }
        return buildReport(entries, Map.of("year", year, "month", month));
    }

    @Override
    public Map<String, Object> getRangeReport(LocalDate fromDate, LocalDate toDate, UUID customerId) {
        List<BillingReadModel> entries = reportRepository.findByDateRange(fromDate, toDate);
        if (customerId != null) {
            entries = entries.stream().filter(e -> e.getCustomerId().equals(customerId)).toList();
        }
        return buildReport(entries, Map.of("fromDate", fromDate.toString(), "toDate", toDate.toString()));
    }

    @Override
    public Map<String, Object> getUtilisationReport(int year, int month, UUID userId) {
        List<BillingReadModel> entries;
        if (userId != null) {
            entries = reportRepository.findByUserIdAndYearAndMonth(userId, year, month);
        } else {
            entries = reportRepository.findByYearAndMonth(year, month);
        }

        BigDecimal totalHours = entries.stream()
                .map(BillingReadModel::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by customer
        Map<UUID, List<BillingReadModel>> byCustomer = entries.stream()
                .collect(Collectors.groupingBy(BillingReadModel::getCustomerId));

        List<Map<String, Object>> customers = new ArrayList<>();
        for (Map.Entry<UUID, List<BillingReadModel>> entry : byCustomer.entrySet()) {
            Map<String, Object> customerData = new HashMap<>();
            customerData.put("customerName", entry.getValue().get(0).getCustomerName());
            BigDecimal custHours = entry.getValue().stream()
                    .map(BillingReadModel::getHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            customerData.put("totalHours", custHours);

            // Group by category within customer
            Map<UUID, List<BillingReadModel>> byCategory = entry.getValue().stream()
                    .collect(Collectors.groupingBy(BillingReadModel::getCategoryId));
            List<Map<String, Object>> categories = new ArrayList<>();
            for (Map.Entry<UUID, List<BillingReadModel>> catEntry : byCategory.entrySet()) {
                Map<String, Object> catData = new HashMap<>();
                BillingReadModel first = catEntry.getValue().get(0);
                catData.put("categoryName", first.getCategoryName());
                BigDecimal catHours = catEntry.getValue().stream()
                        .map(BillingReadModel::getHours)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                catData.put("totalHours", catHours);
                catData.put("totalAmount", catHours.multiply(first.getHourlyRate()));
                categories.add(catData);
            }
            customerData.put("categories", categories);
            customers.add(customerData);
        }

        Map<String, Object> report = new HashMap<>();
        if (userId != null) {
            report.put("userId", userId);
            if (!entries.isEmpty()) {
                report.put("userName", entries.get(0).getUserName());
            }
        }
        report.put("year", year);
        report.put("month", month);
        report.put("totalHours", totalHours);
        report.put("customers", customers);
        return report;
    }

    private Map<String, Object> buildReport(List<BillingReadModel> entries, Map<String, Object> metadata) {
        BigDecimal grandTotalHours = BigDecimal.ZERO;
        BigDecimal grandTotalAmount = BigDecimal.ZERO;

        Map<UUID, List<BillingReadModel>> byCustomer = entries.stream()
                .collect(Collectors.groupingBy(BillingReadModel::getCustomerId));

        List<Map<String, Object>> customers = new ArrayList<>();
        for (Map.Entry<UUID, List<BillingReadModel>> entry : byCustomer.entrySet()) {
            Map<String, Object> customerData = new HashMap<>();
            customerData.put("customerId", entry.getKey());
            customerData.put("customerName", entry.getValue().get(0).getCustomerName());

            BigDecimal custTotalHours = BigDecimal.ZERO;
            BigDecimal custTotalAmount = BigDecimal.ZERO;

            // Categories
            Map<UUID, List<BillingReadModel>> byCategory = entry.getValue().stream()
                    .collect(Collectors.groupingBy(BillingReadModel::getCategoryId));
            List<Map<String, Object>> categories = new ArrayList<>();
            for (Map.Entry<UUID, List<BillingReadModel>> catEntry : byCategory.entrySet()) {
                BillingReadModel first = catEntry.getValue().get(0);
                BigDecimal catHours = catEntry.getValue().stream()
                        .map(BillingReadModel::getHours)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal catAmount = catHours.multiply(first.getHourlyRate());

                Map<String, Object> catData = new HashMap<>();
                catData.put("categoryName", first.getCategoryName());
                catData.put("hourlyRate", first.getHourlyRate());
                catData.put("totalHours", catHours);
                catData.put("totalAmount", catAmount);
                categories.add(catData);

                custTotalHours = custTotalHours.add(catHours);
                custTotalAmount = custTotalAmount.add(catAmount);
            }

            // Users
            Map<UUID, List<BillingReadModel>> byUser = entry.getValue().stream()
                    .collect(Collectors.groupingBy(BillingReadModel::getUserId));
            List<Map<String, Object>> users = new ArrayList<>();
            for (Map.Entry<UUID, List<BillingReadModel>> userEntry : byUser.entrySet()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", userEntry.getKey());
                userData.put("userName", userEntry.getValue().get(0).getUserName());
                BigDecimal userHours = userEntry.getValue().stream()
                        .map(BillingReadModel::getHours)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                userData.put("totalHours", userHours);
                users.add(userData);
            }

            customerData.put("totalHours", custTotalHours);
            customerData.put("totalAmount", custTotalAmount);
            customerData.put("categories", categories);
            customerData.put("users", users);
            customers.add(customerData);

            grandTotalHours = grandTotalHours.add(custTotalHours);
            grandTotalAmount = grandTotalAmount.add(custTotalAmount);
        }

        Map<String, Object> report = new HashMap<>(metadata);
        report.put("customers", customers);
        report.put("grandTotalHours", grandTotalHours);
        report.put("grandTotalAmount", grandTotalAmount);
        return report;
    }
}
