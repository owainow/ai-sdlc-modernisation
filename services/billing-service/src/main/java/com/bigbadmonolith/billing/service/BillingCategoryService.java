package com.bigbadmonolith.billing.service;

import com.bigbadmonolith.billing.dto.*;
import com.bigbadmonolith.billing.model.BillingCategory;
import com.bigbadmonolith.billing.repository.BillingCategoryRepository;
import com.bigbadmonolith.billing.repository.BillableHourRepository;
import com.bigbadmonolith.common.exception.DeletionBlockedException;
import com.bigbadmonolith.common.exception.DuplicateResourceException;
import com.bigbadmonolith.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class BillingCategoryService {
    private final BillingCategoryRepository categoryRepository;
    private final BillableHourRepository billableHourRepository;

    public BillingCategoryService(BillingCategoryRepository categoryRepository,
                                  BillableHourRepository billableHourRepository) {
        this.categoryRepository = categoryRepository;
        this.billableHourRepository = billableHourRepository;
    }

    public BillingCategoryResponse create(CreateBillingCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Billing category with name '" + request.name() + "' already exists");
        }
        BillingCategory category = new BillingCategory();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setHourlyRate(request.hourlyRate());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public Page<BillingCategoryResponse> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BillingCategoryResponse findById(UUID id) {
        return categoryRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Billing category not found with id: " + id));
    }

    public BillingCategoryResponse update(UUID id, UpdateBillingCategoryRequest request) {
        BillingCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Billing category not found with id: " + id));

        if (!category.getName().equals(request.name()) && categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Billing category with name '" + request.name() + "' already exists");
        }

        category.setName(request.name());
        category.setDescription(request.description());
        category.setHourlyRate(request.hourlyRate());
        return toResponse(categoryRepository.save(category));
    }

    public void delete(UUID id) {
        BillingCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Billing category not found with id: " + id));

        if (billableHourRepository.existsByCategoryId(id)) {
            throw new DeletionBlockedException("Cannot delete billing category with existing billable hours");
        }

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public long count() {
        return categoryRepository.count();
    }

    private BillingCategoryResponse toResponse(BillingCategory category) {
        return new BillingCategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getHourlyRate(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
