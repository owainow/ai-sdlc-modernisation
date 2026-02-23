package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.TestDatabaseConfig;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T018: Characterisation tests for DatabaseService — initializeDatabase, schema verification.
 */
class DatabaseServiceTest extends BaseIntegrationTest {

    private final DatabaseService databaseService = new DatabaseService();

    @Test
    void saveUserPersistsAndReturnsUser() {
        User user = new User("db@test.com", "DB User");
        User saved = databaseService.saveUser(user);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("db@test.com");
    }

    @Test
    void findUserByIdReturnsPersistedUser() {
        User saved = databaseService.saveUser(new User("findid@test.com", "FindId User"));

        User found = databaseService.findUserById(saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("findid@test.com");
    }

    @Test
    void findUserByIdReturnsNullForMissingUser() {
        User found = databaseService.findUserById(99999L);
        assertThat(found).isNull();
    }

    @Test
    void findAllUsersReturnsAllPersistedUsers() {
        databaseService.saveUser(new User("all1@test.com", "User 1"));
        databaseService.saveUser(new User("all2@test.com", "User 2"));

        List<User> users = databaseService.findAllUsers();
        assertThat(users).hasSize(2);
    }

    @Test
    void findUserByEmailReturnsMatchingUser() {
        databaseService.saveUser(new User("byemail@test.com", "Email User"));

        User found = databaseService.findUserByEmail("byemail@test.com");
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Email User");
    }

    @Test
    void deleteUserRemovesUser() {
        User saved = databaseService.saveUser(new User("deluser@test.com", "Del User"));

        boolean deleted = databaseService.deleteUser(saved.getId());
        assertThat(deleted).isTrue();
        assertThat(databaseService.findUserById(saved.getId())).isNull();
    }

    @Test
    void updateUserModifiesUser() {
        User saved = databaseService.saveUser(new User("updateuser@test.com", "Old Name"));
        saved.setName("New Name");

        User updated = databaseService.updateUser(saved);
        assertThat(updated.getName()).isEqualTo("New Name");

        User found = databaseService.findUserById(saved.getId());
        assertThat(found.getName()).isEqualTo("New Name");
    }

    @Test
    void databaseSchemaIsInitialized() throws SQLException {
        try (Connection conn = TestDatabaseConfig.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            assertThat(tableExists(meta, "USERS")).isTrue();
            assertThat(tableExists(meta, "CUSTOMERS")).isTrue();
            assertThat(tableExists(meta, "BILLING_CATEGORIES")).isTrue();
            assertThat(tableExists(meta, "BILLABLE_HOURS")).isTrue();
        }
    }

    private boolean tableExists(DatabaseMetaData meta, String tableName) throws SQLException {
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }
}
