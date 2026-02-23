package com.sourcegraph.demo.bigbadmonolith.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionManager {
    private static final String DB_URL = System.getenv("DB_URL") != null 
            ? System.getenv("DB_URL") 
            : "jdbc:derby:./data/bigbadmonolith;create=true";
    private static final String DB_USER = System.getenv("DB_USER") != null 
            ? System.getenv("DB_USER") 
            : "app";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null 
            ? System.getenv("DB_PASSWORD") 
            : "app";

    private static final HikariDataSource dataSource;
    
    static {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Derby driver not found", e);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("BigBadMonolithPool");
        config.setRegisterMbeans(true);
        
        dataSource = new HikariDataSource(config);
        initializeDatabase();
    }
    
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
    
    private static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create users table if it doesn't exist
            String createUsersTableSQL =
                "CREATE TABLE users (" +
                "  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "  email VARCHAR(255) NOT NULL UNIQUE," +
                "  name VARCHAR(255) NOT NULL," +
                "  PRIMARY KEY (id)" +
                ")";
            
            // Create customers table if it doesn't exist
            String createCustomersTableSQL =
                "CREATE TABLE customers (" +
                "  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "  name VARCHAR(255) NOT NULL," +
                "  email VARCHAR(255) NOT NULL," +
                "  address VARCHAR(500)," +
                "  created_at TIMESTAMP NOT NULL," +
                "  PRIMARY KEY (id)" +
                ")";
            
            // Create billing_categories table if it doesn't exist
            String createBillingCategoriesTableSQL =
                "CREATE TABLE billing_categories (" +
                "  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "  name VARCHAR(255) NOT NULL," +
                "  description VARCHAR(500)," +
                "  hourly_rate DECIMAL(10,2) NOT NULL," +
                "  PRIMARY KEY (id)" +
                ")";
            
            // Create billable_hours table if it doesn't exist
            String createBillableHoursTableSQL =
                "CREATE TABLE billable_hours (" +
                "  id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                "  customer_id BIGINT NOT NULL," +
                "  user_id BIGINT NOT NULL," +
                "  category_id BIGINT NOT NULL," +
                "  hours DECIMAL(8,2) NOT NULL," +
                "  note VARCHAR(1000)," +
                "  date_logged DATE NOT NULL," +
                "  created_at TIMESTAMP NOT NULL," +
                "  PRIMARY KEY (id)," +
                "  FOREIGN KEY (customer_id) REFERENCES customers(id)," +
                "  FOREIGN KEY (user_id) REFERENCES users(id)," +
                "  FOREIGN KEY (category_id) REFERENCES billing_categories(id)" +
                ")";
            
            createTableIfNotExists(stmt, createUsersTableSQL);
            createTableIfNotExists(stmt, createCustomersTableSQL);
            createTableIfNotExists(stmt, createBillingCategoriesTableSQL);
            createTableIfNotExists(stmt, createBillableHoursTableSQL);
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    private static void createTableIfNotExists(Statement stmt, String createTableSQL) throws SQLException {
        try {
            stmt.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            // Table might already exist, ignore error
            if (!e.getSQLState().equals("X0Y32")) {
                throw e;
            }
        }
    }
    
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        try {
            java.sql.DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // Expected exception on shutdown
        }
    }
}
