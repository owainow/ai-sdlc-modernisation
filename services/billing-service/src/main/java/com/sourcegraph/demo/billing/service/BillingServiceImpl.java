package com.sourcegraph.demo.billing.service;

import com.sourcegraph.demo.billing.entity.BillableHour;
import com.sourcegraph.demo.billing.entity.BillingCategory;
import com.sourcegraph.demo.billing.event.BillingEventPublisher;
import com.sourcegraph.demo.billing.exception.DuplicateResourceException;
import com.sourcegraph.demo.billing.exception.ResourceNotFoundException;
import com.sourcegraph.demo.billing.exception.ValidationException;
import com.sourcegraph.demo.billing.repository.BillableHourRepository;
import com.sourcegraph.demo.billing.repository.BillingCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class BillingServiceImpl implements BillingService {

    private final BillingCategoryRepository categoryRepository;
    private final BillableHourRepository hourRepository;
    private final BillingEventPublisher eventPublisher;

    public BillingServiceImpl(BillingCategoryRepository categoryRepository,
                              BillableHourRepository hourRepository,
                              BillingEventPublisher eventPublisher) {
        this.categoryRepository = categoryRepository;
        this.hourRepository = hourRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillingCategory> findAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingCategory findCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillingCategory", id));
    }

    @Override
    public BillingCategory createCategory(String name, BigDecimal hourlyRate) {
        if (categoryRepository.existsByName(name)) {
            throw new DuplicateResourceException("BillingCategory", "name", name);
        }
        BillingCategory category = new BillingCategory();
        category.setName(name);
        category.setHourlyRate(hourlyRate);
        return categoryRepository.save(category);
    }

    @Override
    public BillingCategory updateCategory(UUID id, String name, BigDecimal hourlyRate) {
        BillingCategory category = findCategoryById(id);
        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new DuplicateResourceException("BillingCategory", "name", name);
        }
        category.setName(name);
        category.setHourlyRate(hourlyRate);
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(UUID id) {
        BillingCategory category = findCategoryById(id);
        if (hourRepository.existsByCategoryId(id)) {
            throw new ValidationException("Cannot delete category with associated billable hours");
        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillableHour> findAllHours(Pageable pageable) {
        return hourRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public BillableHour findHourById(UUID id) {
        return hourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillableHour", id));
    }

    @Override
    public BillableHour createHour(UUID userId, UUID customerId, UUID categoryId,
                                   BigDecimal hours, LocalDate workDate) {
        validateDailyHoursCap(userId, workDate, hours, null);
        findCategoryById(categoryId); // ensure category exists

        BillableHour hour = new BillableHour();
        hour.setUserId(userId);
        hour.setCustomerId(customerId);
        hour.setCategoryId(categoryId);
        hour.setHours(hours);
        hour.setWorkDate(workDate);
        BillableHour saved = hourRepository.save(hour);
        eventPublisher.publishHourCreated(saved);
        return saved;
    }

    @Override
    public BillableHour updateHour(UUID id, UUID userId, UUID customerId, UUID categoryId,
                                   BigDecimal hours, LocalDate workDate) {
        BillableHour hour = findHourById(id);
        validateDailyHoursCap(userId, workDate, hours, id);
        findCategoryById(categoryId);

        hour.setUserId(userId);
        hour.setCustomerId(customerId);
        hour.setCategoryId(categoryId);
        hour.setHours(hours);
        hour.setWorkDate(workDate);
        BillableHour saved = hourRepository.save(hour);
        eventPublisher.publishHourUpdated(saved);
        return saved;
    }

    @Override
    public void deleteHour(UUID id) {
        BillableHour hour = findHourById(id);
        hourRepository.delete(hour);
        eventPublisher.publishHourDeleted(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCustomerId(UUID customerId) {
        return hourRepository.existsByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBillingSummary(UUID customerId, LocalDate fromDate, LocalDate toDate) {
        List<BillableHour> hours = hourRepository.findByCustomerIdAndDateRange(customerId, fromDate, toDate);
        Map<UUID, List<BillableHour>> byCategory = new HashMap<>();
        for (BillableHour h : hours) {
            byCategory.computeIfAbsent(h.getCategoryId(), k -> new ArrayList<>()).add(h);
        }

        List<Map<String, Object>> categories = new ArrayList<>();
        BigDecimal grandTotalHours = BigDecimal.ZERO;
        BigDecimal grandTotalAmount = BigDecimal.ZERO;

        for (Map.Entry<UUID, List<BillableHour>> entry : byCategory.entrySet()) {
            BillingCategory category = findCategoryById(entry.getKey());
            BigDecimal totalHrs = entry.getValue().stream()
                    .map(BillableHour::getHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalAmt = totalHrs.multiply(category.getHourlyRate());

            Map<String, Object> catSummary = new HashMap<>();
            catSummary.put("categoryId", category.getId());
            catSummary.put("categoryName", category.getName());
            catSummary.put("hourlyRate", category.getHourlyRate());
            catSummary.put("totalHours", totalHrs);
            catSummary.put("totalAmount", totalAmt);
            categories.add(catSummary);

            grandTotalHours = grandTotalHours.add(totalHrs);
            grandTotalAmount = grandTotalAmount.add(totalAmt);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("customerId", customerId);
        summary.put("fromDate", fromDate.toString());
        summary.put("toDate", toDate.toString());
        summary.put("categories", categories);
        summary.put("grandTotalHours", grandTotalHours);
        summary.put("grandTotalAmount", grandTotalAmount);
        return summary;
    }

    private void validateDailyHoursCap(UUID userId, LocalDate workDate, BigDecimal newHours, UUID excludeId) {
        BigDecimal existing = hourRepository.sumHoursByUserIdAndWorkDate(userId, workDate);
        BigDecimal total;
        if (excludeId != null) {
            BigDecimal oldHours = hourRepository.findById(excludeId)
                    .filter(h -> h.getUserId().equals(userId) && h.getWorkDate().equals(workDate))
                    .map(BillableHour::getHours)
                    .orElse(BigDecimal.ZERO);
            total = existing.subtract(oldHours).add(newHours);
        } else {
            total = existing.add(newHours);
        }
        if (total.compareTo(new BigDecimal("24")) > 0) {
            throw new ValidationException("Total hours for user on " + workDate + " would exceed 24");
        }
    }
}
