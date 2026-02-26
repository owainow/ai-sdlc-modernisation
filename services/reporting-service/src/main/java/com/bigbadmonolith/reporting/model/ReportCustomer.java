package com.bigbadmonolith.reporting.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "report_customers")
public class ReportCustomer {

    @Id
    private UUID id;

    private String name;

    private String email;

    private String address;

    public ReportCustomer() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
