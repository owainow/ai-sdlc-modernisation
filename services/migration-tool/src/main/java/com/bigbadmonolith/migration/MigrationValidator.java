package com.bigbadmonolith.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.*;

/**
 * Validates migration parity between Derby source and PostgreSQL targets.
 * Checks row counts, FK integrity, and revenue parity.
 */
@Service
public class MigrationValidator {

    private static final Logger log = LoggerFactory.getLogger(MigrationValidator.class);

    /**
     * Runs all validation checks and returns a summary.
     */
    public ValidationResult validate(Connection derbyConn, Connection pgUserConn,
                                      Connection pgCustomerConn, Connection pgBillingConn) throws SQLException {
        ValidationResult result = new ValidationResult();

        // Row count parity
        result.setSourceUserCount(countRows(derbyConn, "users"));
        result.setTargetUserCount(countRows(pgUserConn, "users"));
        result.setSourceCustomerCount(countRows(derbyConn, "customers"));
        result.setTargetCustomerCount(countRows(pgCustomerConn, "customers"));
        result.setSourceCategoryCount(countRows(derbyConn, "billing_categories"));
        result.setTargetCategoryCount(countRows(pgBillingConn, "billing_categories"));
        result.setSourceHourCount(countRows(derbyConn, "billable_hours"));
        result.setTargetHourCount(countRows(pgBillingConn, "billable_hours"));

        // Revenue parity
        result.setSourceRevenue(calculateRevenue(derbyConn));
        result.setTargetRevenue(calculateTargetRevenue(pgBillingConn));

        // FK integrity in target
        result.setOrphanedHours(countOrphanedHours(pgBillingConn));

        log.info("Validation results: {}", result);
        return result;
    }

    private long countRows(Connection conn, String table) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /**
     * Calculates total revenue from Derby: SUM(hours * category.hourly_rate)
     * since Derby doesn't have rate_snapshot.
     */
    private BigDecimal calculateRevenue(Connection derbyConn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(bh.hours * bc.hourly_rate), 0) " +
                "FROM billable_hours bh " +
                "JOIN billing_categories bc ON bh.category_id = bc.id";
        try (Statement stmt = derbyConn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    /**
     * Calculates total revenue from PostgreSQL: SUM(hours * rate_snapshot)
     */
    private BigDecimal calculateTargetRevenue(Connection pgBillingConn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(hours * rate_snapshot), 0) FROM billable_hours";
        try (Statement stmt = pgBillingConn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    /**
     * Checks for billable_hours referencing non-existent categories (FK integrity).
     */
    private long countOrphanedHours(Connection pgBillingConn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM billable_hours bh " +
                "LEFT JOIN billing_categories bc ON bh.category_id = bc.id " +
                "WHERE bc.id IS NULL";
        try (Statement stmt = pgBillingConn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
