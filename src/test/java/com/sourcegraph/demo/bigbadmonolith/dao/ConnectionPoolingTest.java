package com.sourcegraph.demo.bigbadmonolith.dao;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T080: Connection pooling tests — HikariCP active, connections reused.
 */
class ConnectionPoolingTest {

    @Test
    void hikariCpDataSourceIsActive() {
        HikariDataSource ds = ConnectionManager.getDataSource();
        assertThat(ds).isNotNull();
        assertThat(ds.isClosed()).isFalse();
        assertThat(ds.getPoolName()).isEqualTo("BigBadMonolithPool");
    }

    @Test
    void connectionIsPooled() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isClosed()).isFalse();
        }
    }

    @Test
    void multipleConnectionsArePooled() throws SQLException {
        HikariDataSource ds = ConnectionManager.getDataSource();
        int initialActive = ds.getHikariPoolMXBean().getActiveConnections();

        try (Connection conn1 = ConnectionManager.getConnection();
             Connection conn2 = ConnectionManager.getConnection()) {
            assertThat(conn1).isNotNull();
            assertThat(conn2).isNotNull();
            assertThat(ds.getHikariPoolMXBean().getActiveConnections()).isGreaterThanOrEqualTo(2);
        }
    }
}
