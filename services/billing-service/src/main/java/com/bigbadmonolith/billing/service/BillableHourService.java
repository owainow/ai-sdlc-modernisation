package com.bigbadmonolith.billing.service;

import com.bigbadmonolith.billing.dto.*;
import com.bigbadmonolith.billing.model.BillableHour;
import com.bigbadmonolith.billing.model.BillingCategory;
import com.bigbadmonolith.billing.repository.BillableHourRepository;
import com.bigbadmonolith.billing.repository.BillingCategoryRepository;
import com.bigbadmonolith.common.exception.BusinessValidationException;
import com.bigbadmonolith.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class BillableHourService {
    private final BillableHourRepository billableHourRepository;
    private final BillingCategoryRepository categoryRepository;

    public BillableHourService(BillableHourRepository billableHourRepository,
                               BillingCategoryRepository categoryRepository) {
        this.billableHourRepository = billableHourRepository;
        this.categoryRepository = categoryRepository;
    }

    public BillableHourCreateResult create(CreateBillableHourRequest request) {
        if (request.dateLogged().isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Date logged must not be in the future");
        }

        BillingCategory category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Billing category not found with id: " + request.categoryId()));

        BigDecimal existingHours = billableHourRepository.sumHoursForUserOnDateNew(request.userId(), request.dateLogged());
        if (existingHours.add(request.hours()).compareTo(new BigDecimal("24")) > 0) {
            throw new BusinessValidationException("Total hours for user on " + request.dateLogged() + " would exceed 24 (existing: " + existingHours + ", new: " + request.hours() + ")");
        }

        BillableHour entry = new BillableHour();
        entry.setCustomerId(request.customerId());
        entry.setUserId(request.userId());
        entry.setCategoryId(request.categoryId());
        entry.setHours(request.hours());
        entry.setRateSnapshot(category.getHourlyRate());
        entry.setDateLogged(request.dateLogged());
        entry.setNote(request.note());

        BillableHour saved = billableHourRepository.save(entry);

        String warning = null;
        DayOfWeek day = request.dateLogged().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            warning = "Hours logged on a weekend (" + day + ")";
        }

        return new BillableHourCreateResult(toResponse(saved), warning);
    }

    public BillableHourResponse update(UUID id, UpdateBillableHourRequest request) {
        BillableHour entry = billableHourRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Billable hour entry not found with id: " + id));

        if (request.dateLogged().isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Date logged must not be in the future");
        }

        BigDecimal existingHours = billableHourRepository.sumHoursForUserOnDate(request.userId(), request.dateLogged(), id);
        if (existingHours.add(request.hours()).compareTo(new BigDecimal("24")) > 0) {
            throw new BusinessValidationException("Total hours for user on " + request.dateLogged() + " would exceed 24 (existing: " + existingHours + ", new: " + request.hours() + ")");
        }

        entry.setCustomerId(request.customerId());
        entry.setUserId(request.userId());
        // categoryId is not changed on update; rateSnapshot is immutable
        entry.setHours(request.hours());
        entry.setDateLogged(request.dateLogged());
        entry.setNote(request.note());

        return toResponse(billableHourRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public Page<BillableHourResponse> findAll(UUID customerId, UUID userId, UUID categoryId,
                                               LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return billableHourRepository.findFiltered(customerId, userId, categoryId, fromDate, toDate, pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BillableHourResponse findById(UUID id) {
        return billableHourRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Billable hour entry not found with id: " + id));
    }

    public void delete(UUID id) {
        BillableHour entry = billableHourRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Billable hour entry not found with id: " + id));
        billableHourRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public boolean existsByCustomerId(UUID customerId) {
        return billableHourRepository.existsByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return billableHourRepository.existsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue() {
        return billableHourRepository.calculateTotalRevenue();
    }

    private BillableHourResponse toResponse(BillableHour entry) {
        BigDecimal lineTotal = entry.getHours().multiply(entry.getRateSnapshot()).setScale(2, RoundingMode.HALF_UP);
        return new BillableHourResponse(
            entry.getId(),
            entry.getCustomerId(),
            entry.getUserId(),
            entry.getCategoryId(),
            entry.getHours(),
            entry.getRateSnapshot(),
            entry.getDateLogged(),
            entry.getNote(),
            lineTotal,
            entry.getCreatedAt(),
            entry.getUpdatedAt()
        );
    }

    public record BillableHourCreateResult(BillableHourResponse response, String warning) {}
}
