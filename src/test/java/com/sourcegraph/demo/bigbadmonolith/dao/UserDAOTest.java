package com.sourcegraph.demo.bigbadmonolith.dao;

import com.sourcegraph.demo.bigbadmonolith.TestDataFactory;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * T012: Characterisation tests for UserDAO — CRUD against in-memory Derby.
 */
class UserDAOTest extends BaseIntegrationTest {

    private final UserDAO userDAO = new UserDAO();

    @Test
    void saveSetsGeneratedId() {
        User user = TestDataFactory.createUser("save@test.com", "Save User");
        User saved = userDAO.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("save@test.com");
        assertThat(saved.getName()).isEqualTo("Save User");
    }

    @Test
    void findByIdReturnsUserWhenExists() {
        User saved = userDAO.save(TestDataFactory.createUser("find@test.com", "Find User"));

        User found = userDAO.findById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getEmail()).isEqualTo("find@test.com");
        assertThat(found.getName()).isEqualTo("Find User");
    }

    @Test
    void findByIdReturnsNullWhenNotExists() {
        User found = userDAO.findById(99999L);
        assertThat(found).isNull();
    }

    @Test
    void findByEmailReturnsUserWhenExists() {
        userDAO.save(TestDataFactory.createUser("email@test.com", "Email User"));

        User found = userDAO.findByEmail("email@test.com");

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("email@test.com");
    }

    @Test
    void findByEmailReturnsNullWhenNotExists() {
        User found = userDAO.findByEmail("nonexistent@test.com");
        assertThat(found).isNull();
    }

    @Test
    void findAllReturnsAllUsers() {
        userDAO.save(TestDataFactory.createUser("one@test.com", "User One"));
        userDAO.save(TestDataFactory.createUser("two@test.com", "User Two"));

        List<User> users = userDAO.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void findAllReturnsEmptyListWhenNoUsers() {
        List<User> users = userDAO.findAll();
        assertThat(users).isEmpty();
    }

    @Test
    void updateModifiesExistingUser() {
        User saved = userDAO.save(TestDataFactory.createUser("before@test.com", "Before"));
        saved.setEmail("after@test.com");
        saved.setName("After");

        User updated = userDAO.update(saved);

        assertThat(updated.getEmail()).isEqualTo("after@test.com");
        assertThat(updated.getName()).isEqualTo("After");

        User found = userDAO.findById(saved.getId());
        assertThat(found.getEmail()).isEqualTo("after@test.com");
    }

    @Test
    void deleteRemovesUser() {
        User saved = userDAO.save(TestDataFactory.createUser("delete@test.com", "Delete User"));

        boolean deleted = userDAO.delete(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(userDAO.findById(saved.getId())).isNull();
    }

    @Test
    void deleteReturnsFalseForNonExistentUser() {
        boolean deleted = userDAO.delete(99999L);
        assertThat(deleted).isFalse();
    }

    @Test
    void saveDuplicateEmailThrowsException() {
        userDAO.save(TestDataFactory.createUser("dup@test.com", "User 1"));

        assertThatThrownBy(() -> userDAO.save(TestDataFactory.createUser("dup@test.com", "User 2")))
                .isInstanceOf(RuntimeException.class);
    }
}
