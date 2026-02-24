package com.sourcegraph.demo.customer.controller;

import com.sourcegraph.demo.common.dto.ApiResponse;
import com.sourcegraph.demo.common.dto.PaginatedResponse;
import com.sourcegraph.demo.customer.entity.Customer;
import com.sourcegraph.demo.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<CustomerResponse>>> listCustomers(Pageable pageable) {
        Page<Customer> page = customerService.findAll(pageable);
        PaginatedResponse<CustomerResponse> paginatedResponse = new PaginatedResponse<>(
                page.getContent().stream().map(CustomerResponse::from).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()
        );
        return ResponseEntity.ok(ApiResponse.success(paginatedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable UUID id) {
        Customer customer = customerService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(CustomerResponse.from(customer)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = customerService.create(request.name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(CustomerResponse.from(customer)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = customerService.update(id, request.name());
        return ResponseEntity.ok(ApiResponse.success(CustomerResponse.from(customer)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record CreateCustomerRequest(String name) {}

    public record CustomerResponse(UUID id, String name, String createdAt, String updatedAt) {
        public static CustomerResponse from(Customer c) {
            return new CustomerResponse(c.getId(), c.getName(),
                    c.getCreatedAt() != null ? c.getCreatedAt().toString() : null,
                    c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null);
        }
    }
}
