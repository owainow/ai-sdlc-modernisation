package com.bigbadmonolith.billing.service;

import com.bigbadmonolith.billing.dto.*;
import com.bigbadmonolith.billing.model.BillingCategory;
import com.bigbadmonolith.billing.repository.BillingCategoryRepository;
import com.bigbadmonolith.billing.repository.BillableHourRepository;
import com.bigbadmonolith.common.exception.DeletionBlockedException;
import com.bigbadmonolith.common.exception.DuplicateResourceException;
import com.bigbadmonolith.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingCategoryServiceTest {

    @Mock
    private BillingCategoryRepository categoryRepository;

    @Mock
    private BillableHourRepository billableHourRepository;

    @InjectMocks
    private BillingCategoryService categoryService;

    private BillingCategory testCategory;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testCategory = new BillingCategory();
        testCategory.setId(testId);
        testCategory.setName("Consulting");
        testCategory.setDescription("General consulting services");
        testCategory.setHourlyRate(new BigDecimal("150.00"));
        testCategory.setCreatedAt(Instant.now());
        testCategory.setUpdatedAt(Instant.now());
    }

    @Test
    void create_shouldCreateCategory() {
        when(categoryRepository.existsByName("Consulting")).thenReturn(false);
        when(categoryRepository.save(any(BillingCategory.class))).thenReturn(testCategory);

        var result = categoryService.create(new CreateBillingCategoryRequest("Consulting", new BigDecimal("150.00"), "General consulting services"));

        assertThat(result.name()).isEqualTo("Consulting");
        assertThat(result.hourlyRate()).isEqualByComparingTo(new BigDecimal("150.00"));
        verify(categoryRepository).save(any(BillingCategory.class));
    }

    @Test
    void create_shouldThrowOnDuplicateName() {
        when(categoryRepository.existsByName("Consulting")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(new CreateBillingCategoryRequest("Consulting", new BigDecimal("150.00"), null)))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("Consulting");
    }

    @Test
    void findById_shouldReturnCategory() {
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));

        var result = categoryService.findById(testId);

        assertThat(result.id()).isEqualTo(testId);
        assertThat(result.name()).isEqualTo("Consulting");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(categoryRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(testId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldUpdateCategory() {
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(BillingCategory.class))).thenReturn(testCategory);

        var result = categoryService.update(testId, new UpdateBillingCategoryRequest("Consulting", new BigDecimal("200.00"), "Updated"));

        assertThat(result.name()).isEqualTo("Consulting");
        verify(categoryRepository).save(any(BillingCategory.class));
    }

    @Test
    void update_shouldThrowOnDuplicateName() {
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Development")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(testId, new UpdateBillingCategoryRequest("Development", new BigDecimal("200.00"), null)))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_shouldDeleteCategory() {
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        when(billableHourRepository.existsByCategoryId(testId)).thenReturn(false);

        categoryService.delete(testId);

        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void delete_shouldThrowWhenHoursExist() {
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        when(billableHourRepository.existsByCategoryId(testId)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(testId))
            .isInstanceOf(DeletionBlockedException.class);
    }

    @Test
    void findAll_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BillingCategory> page = new PageImpl<>(List.of(testCategory), pageable, 1);
        when(categoryRepository.findAll(pageable)).thenReturn(page);

        var result = categoryService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Consulting");
    }

    @Test
    void count_shouldReturnCount() {
        when(categoryRepository.count()).thenReturn(3L);

        assertThat(categoryService.count()).isEqualTo(3L);
    }
}
