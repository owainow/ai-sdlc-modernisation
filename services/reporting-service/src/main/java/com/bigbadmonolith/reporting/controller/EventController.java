package com.bigbadmonolith.reporting.controller;

import com.bigbadmonolith.reporting.service.EventHandlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/events")
public class EventController {

    private final EventHandlerService eventHandlerService;

    public EventController(EventHandlerService eventHandlerService) {
        this.eventHandlerService = eventHandlerService;
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, String>> syncUser(@RequestBody Map<String, String> payload) {
        eventHandlerService.syncUser(
                UUID.fromString(payload.get("id")),
                payload.get("name"),
                payload.get("email")
        );
        return ResponseEntity.ok(Map.of("status", "synced"));
    }

    @PostMapping("/customers")
    public ResponseEntity<Map<String, String>> syncCustomer(@RequestBody Map<String, String> payload) {
        eventHandlerService.syncCustomer(
                UUID.fromString(payload.get("id")),
                payload.get("name"),
                payload.get("email"),
                payload.get("address")
        );
        return ResponseEntity.ok(Map.of("status", "synced"));
    }

    @PostMapping("/categories")
    public ResponseEntity<Map<String, String>> syncCategory(@RequestBody Map<String, String> payload) {
        eventHandlerService.syncCategory(
                UUID.fromString(payload.get("id")),
                payload.get("name"),
                new BigDecimal(payload.get("hourlyRate"))
        );
        return ResponseEntity.ok(Map.of("status", "synced"));
    }

    @PostMapping("/billable-hours")
    public ResponseEntity<Map<String, String>> syncBillableHour(@RequestBody Map<String, String> payload) {
        eventHandlerService.syncBillableHour(
                UUID.fromString(payload.get("id")),
                UUID.fromString(payload.get("customerId")),
                UUID.fromString(payload.get("userId")),
                UUID.fromString(payload.get("categoryId")),
                new BigDecimal(payload.get("hours")),
                new BigDecimal(payload.get("rateSnapshot")),
                LocalDate.parse(payload.get("dateLogged")),
                payload.get("note")
        );
        return ResponseEntity.ok(Map.of("status", "synced"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> removeUser(@PathVariable UUID id) {
        eventHandlerService.removeUser(id);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Map<String, String>> removeCustomer(@PathVariable UUID id) {
        eventHandlerService.removeCustomer(id);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, String>> removeCategory(@PathVariable UUID id) {
        eventHandlerService.removeCategory(id);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }

    @DeleteMapping("/billable-hours/{id}")
    public ResponseEntity<Map<String, String>> removeBillableHour(@PathVariable UUID id) {
        eventHandlerService.removeBillableHour(id);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }
}
