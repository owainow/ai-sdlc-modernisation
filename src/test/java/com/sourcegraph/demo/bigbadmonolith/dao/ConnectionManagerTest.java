package com.sourcegraph.demo.bigbadmonolith.dao;

import com.sourcegraph.demo.bigbadmonolith.TestDatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T016: Characterisation tests for ConnectionManager — getConnection, lifecycle,
 * env-var-based credential configuration.
 */
class ConnectionManagerTest {

    @Test
    void getConnectionReturnsValidConnection() throws SQLException {
        TestDatabaseConfig.initialize();
        Connection conn = ConnectionManager.getConnection();
        assertThat(conn).isNotNull();
        assertThat(conn.isClosed()).isFalse();
        conn.close();
    }

    @Test
    void connectionPointsToDerbyDatabase() throws SQLException {
        TestDatabaseConfig.initialize();
        try (Connection conn = ConnectionManager.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            assertThat(meta.getDatabaseProductName()).containsIgnoringCase("Derby");
        }
    }

    @Test
    void databaseSchemaContainsExpectedTables() throws SQLException {
        TestDatabaseConfig.initialize();
        try (Connection conn = ConnectionManager.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            assertThat(tableExists(meta, "USERS")).isTrue();
            assertThat(tableExists(meta, "CUSTOMERS")).isTrue();
            assertThat(tableExists(meta, "BILLING_CATEGORIES")).isTrue();
            assertThat(tableExists(meta, "BILLABLE_HOURS")).isTrue();
        }
    }

    @Test
    void credentialsAreConfiguredViaEnvVarsWithDefaults() {
        // ConnectionManager reads DB_URL, DB_USER, DB_PASSWORD from env vars
        // with sensible defaults for local development (Derby embedded).
        // In production, these should be set via environment configuration.
        HikariDataSource ds = ConnectionManager.getDataSource();
        assertThat(ds).isNotNull();
        assertThat(ds.getJdbcUrl()).contains("derby");
    }

    private boolean tableExists(DatabaseMetaData meta, String tableName) throws SQLException {
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }
}
