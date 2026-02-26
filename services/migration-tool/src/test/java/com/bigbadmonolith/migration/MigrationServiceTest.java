package com.bigbadmonolith.migration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.math.BigDecimal;
import java.sql.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for MigrationService using H2 in-memory databases
 * to simulate Derby (source) and PostgreSQL (target).
 */
@TestMethodOrder(OrderAnnotation.class)
class MigrationServiceTest {

    private static Connection sourceConn;
    private static Connection targetUserConn;
    private static Connection targetCustomerConn;
    private static Connection targetBillingConn;
    private static Connection targetReportingConn;

    private MigrationService migrationService;

    @BeforeAll
    static void setupDatabases() throws SQLException {
        // Source database (simulating Derby)
        sourceConn = DriverManager.getConnection("jdbc:h2:mem:source;DB_CLOSE_DELAY=-1");
        createSourceSchema(sourceConn);
        insertSourceData(sourceConn);

        // Target databases (simulating PostgreSQL schemas)
        targetUserConn = DriverManager.getConnection("jdbc:h2:mem:target_user;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        targetCustomerConn = DriverManager.getConnection("jdbc:h2:mem:target_customer;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        targetBillingConn = DriverManager.getConnection("jdbc:h2:mem:target_billing;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        targetReportingConn = DriverManager.getConnection("jdbc:h2:mem:target_reporting;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");

        createTargetUserSchema(targetUserConn);
        createTargetCustomerSchema(targetCustomerConn);
        createTargetBillingSchema(targetBillingConn);
        createTargetReportingSchema(targetReportingConn);
    }

    @BeforeEach
    void setUp() {
        migrationService = new MigrationService();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (sourceConn != null) sourceConn.close();
        if (targetUserConn != null) targetUserConn.close();
        if (targetCustomerConn != null) targetCustomerConn.close();
        if (targetBillingConn != null) targetBillingConn.close();
        if (targetReportingConn != null) targetReportingConn.close();
    }

    @Test
    @Order(1)
    void migrate_shouldTransferAllRecords() throws SQLException {
        MigrationResult result = migrationService.migrate(sourceConn, targetUserConn,
                targetCustomerConn, targetBillingConn, targetReportingConn);

        assertThat(result.getUsersMigrated()).isEqualTo(2);
        assertThat(result.getCustomersMigrated()).isEqualTo(3);
        assertThat(result.getCategoriesMigrated()).isEqualTo(3);
        assertThat(result.getHoursMigrated()).isEqualTo(4);
        assertThat(result.totalRecords()).isEqualTo(12);
    }

    @Test
    @Order(2)
    void migrate_shouldGenerateDeterministicUuids() {
        UUID id1 = migrationService.generateDeterministicUuid("user", 1);
        UUID id2 = migrationService.generateDeterministicUuid("user", 1);
        UUID id3 = migrationService.generateDeterministicUuid("user", 2);

        assertThat(id1).isEqualTo(id2); // Same input → same UUID
        assertThat(id1).isNotEqualTo(id3); // Different input → different UUID
    }

    @Test
    @Order(3)
    void migrate_shouldBeIdempotent() throws SQLException {
        // Reset and re-create migration service (fresh ID mappings)
        MigrationService freshService = new MigrationService();

        // Run migration again — ON CONFLICT DO NOTHING should make this safe
        MigrationResult result = freshService.migrate(sourceConn, targetUserConn,
                targetCustomerConn, targetBillingConn, targetReportingConn);

        // Should still report the counts (rows read from source)
        assertThat(result.getUsersMigrated()).isEqualTo(2);
        assertThat(result.getCustomersMigrated()).isEqualTo(3);

        // But target should still only have the original records
        assertThat(countRows(targetUserConn, "users")).isEqualTo(2);
        assertThat(countRows(targetCustomerConn, "customers")).isEqualTo(3);
        assertThat(countRows(targetBillingConn, "billing_categories")).isEqualTo(3);
        assertThat(countRows(targetBillingConn, "billable_hours")).isEqualTo(4);
    }

    @Test
    @Order(4)
    void migrate_shouldBackfillRateSnapshot() throws SQLException {
        // Verify all billable_hours have a non-zero rate_snapshot
        try (Statement stmt = targetBillingConn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT rate_snapshot FROM billable_hours")) {
            int count = 0;
            while (rs.next()) {
                BigDecimal rate = rs.getBigDecimal("rate_snapshot");
                assertThat(rate).isGreaterThan(BigDecimal.ZERO);
                count++;
            }
            assertThat(count).isEqualTo(4);
        }
    }

    @Test
    @Order(5)
    void migrate_shouldPopulateReportingReadModels() throws SQLException {
        assertThat(countRows(targetReportingConn, "report_users")).isEqualTo(2);
        assertThat(countRows(targetReportingConn, "report_customers")).isEqualTo(3);
        assertThat(countRows(targetReportingConn, "report_billing_categories")).isEqualTo(3);
        assertThat(countRows(targetReportingConn, "report_billable_hours")).isEqualTo(4);
    }

    @Test
    @Order(6)
    void migrate_shouldMaintainRevenueParity() throws SQLException {
        // Source revenue: calculated from hours * category rate
        BigDecimal sourceRevenue;
        try (Statement stmt = sourceConn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT SUM(bh.hours * bc.hourly_rate) FROM billable_hours bh " +
                             "JOIN billing_categories bc ON bh.category_id = bc.id")) {
            rs.next();
            sourceRevenue = rs.getBigDecimal(1);
        }

        // Target revenue: from rate_snapshot
        BigDecimal targetRevenue;
        try (Statement stmt = targetBillingConn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(hours * rate_snapshot) FROM billable_hours")) {
            rs.next();
            targetRevenue = rs.getBigDecimal(1);
        }

        assertThat(targetRevenue).isEqualByComparingTo(sourceRevenue);
    }

    @Test
    @Order(7)
    void validate_shouldPassAfterSuccessfulMigration() throws SQLException {
        MigrationValidator validator = new MigrationValidator();
        ValidationResult result = validator.validate(sourceConn, targetUserConn,
                targetCustomerConn, targetBillingConn);

        assertThat(result.isRowCountMatch()).isTrue();
        assertThat(result.isRevenueMatch()).isTrue();
        assertThat(result.isFkIntegrity()).isTrue();
        assertThat(result.isValid()).isTrue();
    }

    // --- Helper methods ---

    private long countRows(Connection conn, String table) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static void createSourceSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE users (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "name VARCHAR(255) NOT NULL)");
            stmt.executeUpdate("CREATE TABLE customers (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL, " +
                    "address VARCHAR(500), " +
                    "created_at TIMESTAMP NOT NULL)");
            stmt.executeUpdate("CREATE TABLE billing_categories (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "description VARCHAR(500), " +
                    "hourly_rate DECIMAL(10,2) NOT NULL)");
            stmt.executeUpdate("CREATE TABLE billable_hours (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "customer_id BIGINT NOT NULL, " +
                    "user_id BIGINT NOT NULL, " +
                    "category_id BIGINT NOT NULL, " +
                    "hours DECIMAL(8,2) NOT NULL, " +
                    "note VARCHAR(1000), " +
                    "date_logged DATE NOT NULL, " +
                    "created_at TIMESTAMP NOT NULL)");
        }
    }

    private static void insertSourceData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Users
            stmt.executeUpdate("INSERT INTO users (email, name) VALUES ('john.doe@example.com', 'John Doe')");
            stmt.executeUpdate("INSERT INTO users (email, name) VALUES ('jane.smith@example.com', 'Jane Smith')");

            // Customers
            stmt.executeUpdate("INSERT INTO customers (name, email, address, created_at) VALUES " +
                    "('Acme Corp', 'billing@acme.com', '123 Business St', CURRENT_TIMESTAMP)");
            stmt.executeUpdate("INSERT INTO customers (name, email, address, created_at) VALUES " +
                    "('TechStart Inc', 'finance@techstart.com', '456 Innovation Ave', CURRENT_TIMESTAMP)");
            stmt.executeUpdate("INSERT INTO customers (name, email, address, created_at) VALUES " +
                    "('MegaCorp Ltd', 'accounts@megacorp.com', '789 Enterprise Blvd', CURRENT_TIMESTAMP)");

            // Billing Categories
            stmt.executeUpdate("INSERT INTO billing_categories (name, description, hourly_rate) VALUES " +
                    "('Development', 'Software development services', 150.00)");
            stmt.executeUpdate("INSERT INTO billing_categories (name, description, hourly_rate) VALUES " +
                    "('Consulting', 'Business consulting services', 200.00)");
            stmt.executeUpdate("INSERT INTO billing_categories (name, description, hourly_rate) VALUES " +
                    "('Support', 'Technical support services', 100.00)");

            // Billable Hours
            stmt.executeUpdate("INSERT INTO billable_hours (customer_id, user_id, category_id, hours, note, date_logged, created_at) VALUES " +
                    "(1, 1, 1, 8.00, 'Backend development', '2025-01-15', CURRENT_TIMESTAMP)");
            stmt.executeUpdate("INSERT INTO billable_hours (customer_id, user_id, category_id, hours, note, date_logged, created_at) VALUES " +
                    "(1, 2, 2, 4.00, 'Architecture consulting', '2025-01-16', CURRENT_TIMESTAMP)");
            stmt.executeUpdate("INSERT INTO billable_hours (customer_id, user_id, category_id, hours, note, date_logged, created_at) VALUES " +
                    "(2, 1, 1, 6.00, 'Frontend development', '2025-01-17', CURRENT_TIMESTAMP)");
            stmt.executeUpdate("INSERT INTO billable_hours (customer_id, user_id, category_id, hours, note, date_logged, created_at) VALUES " +
                    "(3, 2, 3, 3.00, 'Technical support', '2025-01-18', CURRENT_TIMESTAMP)");
        }
    }

    private static void createTargetUserSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE users (" +
                    "id UUID PRIMARY KEY, " +
                    "name VARCHAR(200) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(), " +
                    "updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW())");
        }
    }

    private static void createTargetCustomerSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE customers (" +
                    "id UUID PRIMARY KEY, " +
                    "name VARCHAR(200) NOT NULL UNIQUE, " +
                    "email VARCHAR(255), " +
                    "address VARCHAR(500), " +
                    "created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(), " +
                    "updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW())");
        }
    }

    private static void createTargetBillingSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE billing_categories (" +
                    "id UUID PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL UNIQUE, " +
                    "description VARCHAR(500), " +
                    "hourly_rate DECIMAL(10,2) NOT NULL, " +
                    "created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(), " +
                    "updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW())");
            stmt.executeUpdate("CREATE TABLE billable_hours (" +
                    "id UUID PRIMARY KEY, " +
                    "customer_id UUID NOT NULL, " +
                    "user_id UUID NOT NULL, " +
                    "category_id UUID NOT NULL REFERENCES billing_categories(id), " +
                    "hours DECIMAL(5,2) NOT NULL, " +
                    "rate_snapshot DECIMAL(10,2) NOT NULL, " +
                    "date_logged DATE NOT NULL, " +
                    "note VARCHAR(500), " +
                    "created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(), " +
                    "updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW())");
        }
    }

    private static void createTargetReportingSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE report_users (" +
                    "id UUID PRIMARY KEY, " +
                    "name VARCHAR(200) NOT NULL, " +
                    "email VARCHAR(255))");
            stmt.executeUpdate("CREATE TABLE report_customers (" +
                    "id UUID PRIMARY KEY, " +
                    "name VARCHAR(200) NOT NULL, " +
                    "email VARCHAR(255), " +
                    "address VARCHAR(500))");
            stmt.executeUpdate("CREATE TABLE report_billing_categories (" +
                    "id UUID PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "hourly_rate DECIMAL(10,2) NOT NULL)");
            stmt.executeUpdate("CREATE TABLE report_billable_hours (" +
                    "id UUID PRIMARY KEY, " +
                    "customer_id UUID NOT NULL, " +
                    "user_id UUID NOT NULL, " +
                    "category_id UUID NOT NULL, " +
                    "hours DECIMAL(5,2) NOT NULL, " +
                    "rate_snapshot DECIMAL(10,2) NOT NULL, " +
                    "date_logged DATE NOT NULL, " +
                    "note VARCHAR(500), " +
                    "created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW())");
        }
    }
}
