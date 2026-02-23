package com.sourcegraph.demo.bigbadmonolith.dao;

import com.sourcegraph.demo.bigbadmonolith.TestDataFactory;
import com.sourcegraph.demo.bigbadmonolith.dto.PaginatedResponse;
import com.sourcegraph.demo.bigbadmonolith.dto.PaginationRequest;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T078: Pagination tests — page size, page number, total count, boundaries.
 */
class PaginationTest extends BaseIntegrationTest {

    private final UserDAO userDAO = new UserDAO();

    @Test
    void paginateReturnsFirstPage() {
        for (int i = 0; i < 5; i++) {
            userDAO.save(TestDataFactory.createUser("page" + i + "@test.com", "User " + i));
        }

        PaginatedResponse<User> page = userDAO.findAll(new PaginationRequest(0, 2));

        assertThat(page.getItems()).hasSize(2);
        assertThat(page.getPage()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getTotalItems()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.hasPrevious()).isFalse();
    }

    @Test
    void paginateReturnsLastPage() {
        for (int i = 0; i < 5; i++) {
            userDAO.save(TestDataFactory.createUser("last" + i + "@test.com", "User " + i));
        }

        PaginatedResponse<User> page = userDAO.findAll(new PaginationRequest(2, 2));

        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getPage()).isEqualTo(2);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void paginateReturnsEmptyForOutOfRange() {
        userDAO.save(TestDataFactory.createUser("only@test.com", "Only One"));

        PaginatedResponse<User> page = userDAO.findAll(new PaginationRequest(10, 10));

        assertThat(page.getItems()).isEmpty();
        assertThat(page.getTotalItems()).isEqualTo(1);
    }

    @Test
    void paginateCountMatchesFindAll() {
        for (int i = 0; i < 3; i++) {
            userDAO.save(TestDataFactory.createUser("count" + i + "@test.com", "User " + i));
        }

        assertThat(userDAO.count()).isEqualTo(3);
        assertThat(userDAO.findAll()).hasSize(3);
    }
}
