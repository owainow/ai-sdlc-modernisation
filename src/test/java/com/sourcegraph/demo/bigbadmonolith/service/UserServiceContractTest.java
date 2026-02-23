package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.TestDataFactory;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.exception.ResourceNotFoundException;
import com.sourcegraph.demo.bigbadmonolith.exception.ValidationException;
import com.sourcegraph.demo.bigbadmonolith.integration.BaseIntegrationTest;
import com.sourcegraph.demo.bigbadmonolith.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * T042: Contract tests for UserService interface.
 */
class UserServiceContractTest extends BaseIntegrationTest {

    private final UserService userService = new UserServiceImpl();

    @Test
    void savePersistsValidUser() {
        User user = userService.save(TestDataFactory.createUser("contract@test.com", "Contract User"));
        assertThat(user.getId()).isNotNull();
    }

    @Test
    void saveRejectsEmptyName() {
        assertThatThrownBy(() -> userService.save(TestDataFactory.createUser("empty@test.com", "")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("name");
    }

    @Test
    void saveRejectsNullEmail() {
        assertThatThrownBy(() -> userService.save(TestDataFactory.createUser(null, "Name")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("email");
    }

    @Test
    void findByIdReturnsExistingUser() {
        User saved = userService.save(TestDataFactory.createUser("findme@test.com", "Find Me"));
        User found = userService.findById(saved.getId());
        assertThat(found.getEmail()).isEqualTo("findme@test.com");
    }

    @Test
    void findByIdThrowsForNonExistentUser() {
        assertThatThrownBy(() -> userService.findById(99999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllReturnsAllUsers() {
        userService.save(TestDataFactory.createUser("a@test.com", "User A"));
        userService.save(TestDataFactory.createUser("b@test.com", "User B"));
        List<User> users = userService.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void updateModifiesUser() {
        User saved = userService.save(TestDataFactory.createUser("old@test.com", "Old"));
        saved.setName("New");
        User updated = userService.update(saved);
        assertThat(updated.getName()).isEqualTo("New");
    }

    @Test
    void deleteRemovesUser() {
        User saved = userService.save(TestDataFactory.createUser("del@test.com", "Del"));
        boolean deleted = userService.delete(saved.getId());
        assertThat(deleted).isTrue();
    }
}
