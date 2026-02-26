package com.bigbadmonolith.reporting.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "report_users")
public class ReportUser {

    @Id
    private UUID id;

    private String name;

    private String email;

    public ReportUser() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
