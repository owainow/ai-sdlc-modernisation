package com.bigbadmonolith.reporting.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "report_billing_categories")
public class ReportBillingCategory {

    @Id
    private UUID id;

    private String name;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    public ReportBillingCategory() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
}
