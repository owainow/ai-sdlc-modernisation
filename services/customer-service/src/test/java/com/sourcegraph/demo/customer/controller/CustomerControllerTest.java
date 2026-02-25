package com.sourcegraph.demo.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcegraph.demo.customer.entity.Customer;
import com.sourcegraph.demo.customer.exception.DuplicateResourceException;
import com.sourcegraph.demo.customer.exception.ResourceNotFoundException;
import com.sourcegraph.demo.customer.service.CustomerService;
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

import java.time.Instant;
import java.util.List;
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
@WebMvcTest(CustomerController.class)
@ActiveProfiles("test")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer createTestCustomer() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("Acme Corp");
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
        return customer;
    }

    @Test
    void listCustomers_returnsPagedResponse() throws Exception {
        Customer customer = createTestCustomer();
        when(customerService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(customer)));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name", is("Acme Corp")));
    }

    @Test
    void getCustomerById_returnsCustomer() throws Exception {
        Customer customer = createTestCustomer();
        when(customerService.findById(customer.getId())).thenReturn(customer);

        mockMvc.perform(get("/api/v1/customers/{id}", customer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is("Acme Corp")));
    }

    @Test
    void getCustomerById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerService.findById(id)).thenThrow(new ResourceNotFoundException("Customer", id));

        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCustomer_returns201() throws Exception {
        Customer customer = createTestCustomer();
        when(customerService.create(eq("Acme Corp"))).thenReturn(customer);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme Corp\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is("Acme Corp")));
    }

    @Test
    void createCustomer_duplicateName_returns409() throws Exception {
        when(customerService.create(any()))
                .thenThrow(new DuplicateResourceException("Customer", "name", "Acme Corp"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme Corp\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void updateCustomer_returnsUpdated() throws Exception {
        Customer customer = createTestCustomer();
        when(customerService.update(eq(customer.getId()), eq("New Name"))).thenReturn(customer);

        mockMvc.perform(put("/api/v1/customers/{id}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));
    }

    @Test
    void deleteCustomer_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(customerService).delete(id);

        mockMvc.perform(delete("/api/v1/customers/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCustomer_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Customer", id)).when(customerService).delete(id);

        mockMvc.perform(delete("/api/v1/customers/{id}", id))
                .andExpect(status().isNotFound());
    }
}
