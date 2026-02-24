package com.sourcegraph.demo.billing.controller;

import com.sourcegraph.demo.billing.entity.BillableHour;
import com.sourcegraph.demo.billing.entity.BillingCategory;
import com.sourcegraph.demo.billing.exception.ResourceNotFoundException;
import com.sourcegraph.demo.billing.exception.ValidationException;
import com.sourcegraph.demo.billing.service.BillingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(BillingController.class)
@ActiveProfiles("test")
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

    @Test
    void listCategories_returnsPagedResponse() throws Exception {
        BillingCategory cat = new BillingCategory();
        cat.setId(UUID.randomUUID());
        cat.setName("Development");
        cat.setHourlyRate(new BigDecimal("150.00"));
        cat.setCreatedAt(Instant.now());
        cat.setUpdatedAt(Instant.now());

        when(billingService.findAllCategories(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cat)));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("Development")));
    }

    @Test
    void createCategory_returns201() throws Exception {
        BillingCategory cat = new BillingCategory();
        cat.setId(UUID.randomUUID());
        cat.setName("Testing");
        cat.setHourlyRate(new BigDecimal("100.00"));
        cat.setCreatedAt(Instant.now());
        cat.setUpdatedAt(Instant.now());

        when(billingService.createCategory(eq("Testing"), any(BigDecimal.class))).thenReturn(cat);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Testing\",\"hourlyRate\":100.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Testing")));
    }

    @Test
    void deleteCategory_withLinkedHours_returns409() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ValidationException("Cannot delete category with associated billable hours"))
                .when(billingService).deleteCategory(id);

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
                .andExpect(status().isConflict());
    }

    @Test
    void listHours_returnsPagedResponse() throws Exception {
        BillableHour hour = new BillableHour();
        hour.setId(UUID.randomUUID());
        hour.setUserId(UUID.randomUUID());
        hour.setCustomerId(UUID.randomUUID());
        hour.setCategoryId(UUID.randomUUID());
        hour.setHours(new BigDecimal("8.00"));
        hour.setWorkDate(LocalDate.now());
        hour.setCreatedAt(Instant.now());
        hour.setUpdatedAt(Instant.now());

        when(billingService.findAllHours(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(hour)));

        mockMvc.perform(get("/api/v1/hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content", hasSize(1)));
    }

    @Test
    void createHour_returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        BillableHour hour = new BillableHour();
        hour.setId(UUID.randomUUID());
        hour.setUserId(userId);
        hour.setCustomerId(customerId);
        hour.setCategoryId(categoryId);
        hour.setHours(new BigDecimal("8.00"));
        hour.setWorkDate(LocalDate.of(2026, 2, 20));
        hour.setCreatedAt(Instant.now());
        hour.setUpdatedAt(Instant.now());

        when(billingService.createHour(any(), any(), any(), any(), any())).thenReturn(hour);

        String body = String.format(
                "{\"userId\":\"%s\",\"customerId\":\"%s\",\"categoryId\":\"%s\",\"hours\":8.00,\"workDate\":\"2026-02-20\"}",
                userId, customerId, categoryId);

        mockMvc.perform(post("/api/v1/hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("success")));
    }

    @Test
    void getBillingSummary_returnsData() throws Exception {
        UUID customerId = UUID.randomUUID();
        Map<String, Object> summary = Map.of(
                "customerId", customerId,
                "grandTotalHours", 40.0,
                "grandTotalAmount", 6000.00
        );
        when(billingService.getBillingSummary(eq(customerId), any(), any())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/billing/summary")
                        .param("customerId", customerId.toString())
                        .param("fromDate", "2026-01-01")
                        .param("toDate", "2026-02-23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));
    }
}
