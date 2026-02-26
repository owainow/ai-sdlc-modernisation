package com.bigbadmonolith.reporting.service;

import com.bigbadmonolith.reporting.dto.MonthlySummaryResponse;
import com.bigbadmonolith.reporting.dto.MonthlySummaryRow;
import com.bigbadmonolith.reporting.model.ReportBillableHour;
import com.bigbadmonolith.reporting.model.ReportCustomer;
import com.bigbadmonolith.reporting.repository.ReportBillableHourRepository;
import com.bigbadmonolith.reporting.repository.ReportCustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MonthlySummaryService {

    private final ReportBillableHourRepository billableHourRepository;
    private final ReportCustomerRepository customerRepository;

    public MonthlySummaryService(ReportBillableHourRepository billableHourRepository,
                                 ReportCustomerRepository customerRepository) {
        this.billableHourRepository = billableHourRepository;
        this.customerRepository = customerRepository;
    }

    public MonthlySummaryResponse getMonthlySummary(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<ReportBillableHour> hours = billableHourRepository.findByDateRange(startDate, endDate);

        Map<UUID, List<ReportBillableHour>> byCustomer = hours.stream()
                .collect(Collectors.groupingBy(ReportBillableHour::getCustomerId));

        Set<UUID> customerIds = byCustomer.keySet();
        Map<UUID, ReportCustomer> customersMap = customerRepository.findAllById(customerIds).stream()
                .collect(Collectors.toMap(ReportCustomer::getId, Function.identity()));

        List<MonthlySummaryRow> rows = byCustomer.entrySet().stream()
                .map(entry -> {
                    UUID customerId = entry.getKey();
                    List<ReportBillableHour> customerHours = entry.getValue();
                    ReportCustomer customer = customersMap.get(customerId);

                    BigDecimal totalHours = customerHours.stream()
                            .map(ReportBillableHour::getHours)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalRevenue = customerHours.stream()
                            .map(h -> h.getHours().multiply(h.getRateSnapshot()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new MonthlySummaryRow(
                            customerId,
                            customer != null ? customer.getName() : "Unknown",
                            totalHours,
                            totalRevenue
                    );
                })
                .sorted(Comparator.comparing(MonthlySummaryRow::totalRevenue).reversed())
                .toList();

        BigDecimal grandTotalHours = rows.stream()
                .map(MonthlySummaryRow::totalHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grandTotalRevenue = rows.stream()
                .map(MonthlySummaryRow::totalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlySummaryResponse(year, month, rows, grandTotalHours, grandTotalRevenue);
    }
}
