package com.bigbadmonolith.billing.controller;

import com.bigbadmonolith.billing.service.BillableHourService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/billing/hours")
public class InternalBillingController {
    private final BillableHourService billableHourService;

    public InternalBillingController(BillableHourService billableHourService) {
        this.billableHourService = billableHourService;
    }

    @GetMapping("/by-customer/{id}/exists")
    public ResponseEntity<Map<String, Boolean>> existsByCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("exists", billableHourService.existsByCustomerId(id)));
    }

    @GetMapping("/by-user/{id}/exists")
    public ResponseEntity<Map<String, Boolean>> existsByUser(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("exists", billableHourService.existsByUserId(id)));
    }
}
