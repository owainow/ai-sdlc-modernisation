package com.bigbadmonolith.customer.controller;

import com.bigbadmonolith.customer.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/customers")
public class InternalCustomerController {
    private final CustomerService customerService;

    public InternalCustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Map<String, Boolean>> exists(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("exists", customerService.exists(id)));
    }
}
