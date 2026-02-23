package com.sourcegraph.demo.bigbadmonolith;

import com.sourcegraph.demo.bigbadmonolith.dao.LibertyConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Test database configuration helper that initialises an in-memory Derby database
 * with the same schema as production. Uses the existing ConnectionManager which
 * creates an embedded Derby at ./data/bigbadmonolith.
 */
public class TestDatabaseConfig {

    private static boolean initialized = false;

    /**
     * Ensures the database schema is initialized. Safe to call multiple times.
     * The ConnectionManager static initializer handles table creation.
     */
    public static synchronized void initialize() {
        if (!initialized) {
            // Trigger ConnectionManager static initializer by requesting a connection
            try (Connection conn = LibertyConnectionManager.getConnection()) {
                initialized = true;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to initialize test database", e);
            }
        }
    }

    /**
     * Cleans all data from tables in the correct order (respecting FK constraints).
     */
    public static void cleanAllTables() {
        initialize();
        try (Connection conn = LibertyConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            // Delete in order respecting foreign key constraints
            stmt.executeUpdate("DELETE FROM billable_hours");
            stmt.executeUpdate("DELETE FROM billing_categories");
            stmt.executeUpdate("DELETE FROM customers");
            stmt.executeUpdate("DELETE FROM users");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clean test tables", e);
        }
    }

    /**
     * Returns a connection to the test database.
     */
    public static Connection getConnection() throws SQLException {
        initialize();
        return LibertyConnectionManager.getConnection();
    }
}
