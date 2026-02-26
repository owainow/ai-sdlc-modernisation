package com.bigbadmonolith.reporting.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "report_billable_hours")
public class ReportBillableHour {

    @Id
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "category_id")
    private UUID categoryId;

    private BigDecimal hours;

    @Column(name = "rate_snapshot")
    private BigDecimal rateSnapshot;

    @Column(name = "date_logged")
    private LocalDate dateLogged;

    private String note;

    @Column(name = "created_at")
    private Instant createdAt;

    public ReportBillableHour() {}

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public BigDecimal getHours() { return hours; }
    public void setHours(BigDecimal hours) { this.hours = hours; }
    public BigDecimal getRateSnapshot() { return rateSnapshot; }
    public void setRateSnapshot(BigDecimal rateSnapshot) { this.rateSnapshot = rateSnapshot; }
    public LocalDate getDateLogged() { return dateLogged; }
    public void setDateLogged(LocalDate dateLogged) { this.dateLogged = dateLogged; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
