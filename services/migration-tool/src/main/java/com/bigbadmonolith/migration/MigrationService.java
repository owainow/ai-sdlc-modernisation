package com.bigbadmonolith.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Migrates data from legacy Derby database to PostgreSQL microservice schemas.
 * Handles integer→UUID key mapping, rateSnapshot backfill, and idempotent upserts.
 */
@Service
public class MigrationService {

    private static final Logger log = LoggerFactory.getLogger(MigrationService.class);

    // Maps legacy BIGINT IDs to new UUIDs for cross-table FK resolution
    private final Map<String, Map<Long, UUID>> idMappings = new HashMap<>();

    public MigrationService() {
        idMappings.put("users", new HashMap<>());
        idMappings.put("customers", new HashMap<>());
        idMappings.put("billing_categories", new HashMap<>());
        idMappings.put("billable_hours", new HashMap<>());
    }

    /**
     * Runs the full migration: users, customers, billing_categories, billable_hours.
     * Order matters for FK resolution.
     */
    public MigrationResult migrate(Connection derbyConn, Connection pgUserConn,
                                    Connection pgCustomerConn, Connection pgBillingConn,
                                    Connection pgReportingConn) throws SQLException {
        MigrationResult result = new MigrationResult();

        log.info("Starting Derby → PostgreSQL migration...");

        int users = migrateUsers(derbyConn, pgUserConn, pgReportingConn);
        result.setUsersMigrated(users);
        log.info("Migrated {} users", users);

        int customers = migrateCustomers(derbyConn, pgCustomerConn, pgReportingConn);
        result.setCustomersMigrated(customers);
        log.info("Migrated {} customers", customers);

        int categories = migrateBillingCategories(derbyConn, pgBillingConn, pgReportingConn);
        result.setCategoriesMigrated(categories);
        log.info("Migrated {} billing categories", categories);

        int hours = migrateBillableHours(derbyConn, pgBillingConn, pgReportingConn);
        result.setHoursMigrated(hours);
        log.info("Migrated {} billable hours", hours);

        log.info("Migration complete: {} users, {} customers, {} categories, {} hours",
                users, customers, categories, hours);

        return result;
    }

    int migrateUsers(Connection derby, Connection pgUser, Connection pgReporting) throws SQLException {
        int count = 0;
        String selectSql = "SELECT id, email, name FROM users ORDER BY id";
        String upsertUserSql = "MERGE INTO users (id, name, email, created_at, updated_at) " +
                "KEY (id) VALUES (?, ?, ?, NOW(), NOW())";
        String upsertReportSql = "MERGE INTO report_users (id, name, email) " +
                "KEY (id) VALUES (?, ?, ?)";

        try (Statement stmt = derby.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql);
             PreparedStatement pgStmt = pgUser.prepareStatement(upsertUserSql);
             PreparedStatement reportStmt = pgReporting.prepareStatement(upsertReportSql)) {

            while (rs.next()) {
                long legacyId = rs.getLong("id");
                String email = rs.getString("email");
                String name = rs.getString("name");
                UUID newId = generateDeterministicUuid("user", legacyId);

                idMappings.get("users").put(legacyId, newId);

                pgStmt.setObject(1, newId);
                pgStmt.setString(2, name);
                pgStmt.setString(3, email);
                pgStmt.executeUpdate();

                reportStmt.setObject(1, newId);
                reportStmt.setString(2, name);
                reportStmt.setString(3, email);
                reportStmt.executeUpdate();

                count++;
            }
        }
        return count;
    }

    int migrateCustomers(Connection derby, Connection pgCustomer, Connection pgReporting) throws SQLException {
        int count = 0;
        String selectSql = "SELECT id, name, email, address, created_at FROM customers ORDER BY id";
        String upsertSql = "MERGE INTO customers (id, name, email, address, created_at, updated_at) " +
                "KEY (id) VALUES (?, ?, ?, ?, ?, NOW())";
        String upsertReportSql = "MERGE INTO report_customers (id, name, email, address) " +
                "KEY (id) VALUES (?, ?, ?, ?)";

        try (Statement stmt = derby.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql);
             PreparedStatement pgStmt = pgCustomer.prepareStatement(upsertSql);
             PreparedStatement reportStmt = pgReporting.prepareStatement(upsertReportSql)) {

            while (rs.next()) {
                long legacyId = rs.getLong("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String address = rs.getString("address");
                Timestamp createdAt = rs.getTimestamp("created_at");
                UUID newId = generateDeterministicUuid("customer", legacyId);

                idMappings.get("customers").put(legacyId, newId);

                pgStmt.setObject(1, newId);
                pgStmt.setString(2, name);
                pgStmt.setString(3, email);
                pgStmt.setString(4, address);
                pgStmt.setTimestamp(5, createdAt);
                pgStmt.executeUpdate();

                reportStmt.setObject(1, newId);
                reportStmt.setString(2, name);
                reportStmt.setString(3, email);
                reportStmt.setString(4, address);
                reportStmt.executeUpdate();

                count++;
            }
        }
        return count;
    }

    int migrateBillingCategories(Connection derby, Connection pgBilling, Connection pgReporting) throws SQLException {
        int count = 0;
        String selectSql = "SELECT id, name, description, hourly_rate FROM billing_categories ORDER BY id";
        String upsertSql = "MERGE INTO billing_categories (id, name, description, hourly_rate, created_at, updated_at) " +
                "KEY (id) VALUES (?, ?, ?, ?, NOW(), NOW())";
        String upsertReportSql = "MERGE INTO report_billing_categories (id, name, hourly_rate) " +
                "KEY (id) VALUES (?, ?, ?)";

        try (Statement stmt = derby.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql);
             PreparedStatement pgStmt = pgBilling.prepareStatement(upsertSql);
             PreparedStatement reportStmt = pgReporting.prepareStatement(upsertReportSql)) {

            while (rs.next()) {
                long legacyId = rs.getLong("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                BigDecimal hourlyRate = rs.getBigDecimal("hourly_rate");
                UUID newId = generateDeterministicUuid("category", legacyId);

                idMappings.get("billing_categories").put(legacyId, newId);

                pgStmt.setObject(1, newId);
                pgStmt.setString(2, name);
                pgStmt.setString(3, description);
                pgStmt.setBigDecimal(4, hourlyRate);
                pgStmt.executeUpdate();

                reportStmt.setObject(1, newId);
                reportStmt.setString(2, name);
                reportStmt.setBigDecimal(3, hourlyRate);
                reportStmt.executeUpdate();

                count++;
            }
        }
        return count;
    }

    int migrateBillableHours(Connection derby, Connection pgBilling, Connection pgReporting) throws SQLException {
        int count = 0;

        // Build a lookup for category hourly rates for rateSnapshot backfill
        Map<Long, BigDecimal> categoryRates = new HashMap<>();
        try (Statement stmt = derby.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, hourly_rate FROM billing_categories")) {
            while (rs.next()) {
                categoryRates.put(rs.getLong("id"), rs.getBigDecimal("hourly_rate"));
            }
        }

        String selectSql = "SELECT id, customer_id, user_id, category_id, hours, note, date_logged, created_at " +
                "FROM billable_hours ORDER BY id";
        String upsertSql = "MERGE INTO billable_hours (id, customer_id, user_id, category_id, hours, " +
                "rate_snapshot, date_logged, note, created_at, updated_at) " +
                "KEY (id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        String upsertReportSql = "MERGE INTO report_billable_hours (id, customer_id, user_id, category_id, " +
                "hours, rate_snapshot, date_logged, note, created_at) " +
                "KEY (id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Statement stmt = derby.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql);
             PreparedStatement pgStmt = pgBilling.prepareStatement(upsertSql);
             PreparedStatement reportStmt = pgReporting.prepareStatement(upsertReportSql)) {

            while (rs.next()) {
                long legacyId = rs.getLong("id");
                long customerId = rs.getLong("customer_id");
                long userId = rs.getLong("user_id");
                long categoryId = rs.getLong("category_id");
                BigDecimal hours = rs.getBigDecimal("hours");
                String note = rs.getString("note");
                Date dateLogged = rs.getDate("date_logged");
                Timestamp createdAt = rs.getTimestamp("created_at");

                UUID newId = generateDeterministicUuid("billable_hour", legacyId);
                UUID newCustomerId = idMappings.get("customers").get(customerId);
                UUID newUserId = idMappings.get("users").get(userId);
                UUID newCategoryId = idMappings.get("billing_categories").get(categoryId);

                if (newCustomerId == null || newUserId == null || newCategoryId == null) {
                    log.warn("Skipping billable hour {} — missing FK mapping (customer={}, user={}, category={})",
                            legacyId, customerId, userId, categoryId);
                    continue;
                }

                // rateSnapshot backfill: use the category's current hourly rate
                BigDecimal rateSnapshot = categoryRates.getOrDefault(categoryId, BigDecimal.ZERO);

                pgStmt.setObject(1, newId);
                pgStmt.setObject(2, newCustomerId);
                pgStmt.setObject(3, newUserId);
                pgStmt.setObject(4, newCategoryId);
                pgStmt.setBigDecimal(5, hours);
                pgStmt.setBigDecimal(6, rateSnapshot);
                pgStmt.setDate(7, dateLogged);
                pgStmt.setString(8, note);
                pgStmt.setTimestamp(9, createdAt);
                pgStmt.executeUpdate();

                reportStmt.setObject(1, newId);
                reportStmt.setObject(2, newCustomerId);
                reportStmt.setObject(3, newUserId);
                reportStmt.setObject(4, newCategoryId);
                reportStmt.setBigDecimal(5, hours);
                reportStmt.setBigDecimal(6, rateSnapshot);
                reportStmt.setDate(7, dateLogged);
                reportStmt.setString(8, note);
                reportStmt.setTimestamp(9, createdAt);
                reportStmt.executeUpdate();

                count++;
            }
        }
        return count;
    }

    /**
     * Generates a deterministic UUID from entity type + legacy ID.
     * Ensures idempotent migrations — same input always produces same UUID.
     */
    UUID generateDeterministicUuid(String entityType, long legacyId) {
        return UUID.nameUUIDFromBytes((entityType + ":" + legacyId).getBytes());
    }

    /**
     * Returns the ID mapping for a given table (for testing/validation).
     */
    public Map<Long, UUID> getIdMapping(String table) {
        return idMappings.getOrDefault(table, Map.of());
    }
}
