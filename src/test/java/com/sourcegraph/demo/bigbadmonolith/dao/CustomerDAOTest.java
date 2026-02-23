package com.sourcegraph.demo.bigbadmonolith.dao;

import com.sourcegraph.demo.bigbadmonolith.TestDataFactory;
import com.sourcegraph.demo.bigbadmonolith.entity.Customer;
import com.sourcegraph.demo.bigbadmonolith.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * T013: Characterisation tests for CustomerDAO — CRUD against in-memory Derby.
 */
class CustomerDAOTest extends BaseIntegrationTest {

    private final CustomerDAO customerDAO = new CustomerDAO();

    @Test
    void saveSetsGeneratedId() throws SQLException {
        Customer customer = TestDataFactory.createCustomer("Save Corp", "save@corp.com", "123 St");
        Customer saved = customerDAO.save(customer);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Save Corp");
    }

    @Test
    void saveRejectsNullCustomer() {
        assertThatThrownBy(() -> customerDAO.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void saveRejectsEmptyName() {
        assertThatThrownBy(() -> customerDAO.save(new Customer("", "email@test.com", "addr")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void saveRejectsNullEmail() {
        assertThatThrownBy(() -> customerDAO.save(new Customer("Name", null, "addr")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }

    @Test
    void findByIdReturnsCustomerWhenExists() throws SQLException {
        Customer saved = customerDAO.save(TestDataFactory.createCustomer("Find Corp", "find@corp.com", "456 Ave"));

        Customer found = customerDAO.findById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Find Corp");
        assertThat(found.getEmail()).isEqualTo("find@corp.com");
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void findByIdReturnsNullWhenNotExists() throws SQLException {
        Customer found = customerDAO.findById(99999L);
        assertThat(found).isNull();
    }

    @Test
    void findAllReturnsAllCustomers() throws SQLException {
        customerDAO.save(TestDataFactory.createCustomer("Corp A", "a@corp.com", "A St"));
        customerDAO.save(TestDataFactory.createCustomer("Corp B", "b@corp.com", "B St"));

        List<Customer> customers = customerDAO.findAll();
        assertThat(customers).hasSize(2);
    }

    @Test
    void updateModifiesExistingCustomer() throws SQLException {
        Customer saved = customerDAO.save(TestDataFactory.createCustomer("Old Name", "old@corp.com", "Old Addr"));
        saved.setName("New Name");
        saved.setEmail("new@corp.com");
        saved.setAddress("New Addr");

        boolean updated = customerDAO.update(saved);
        assertThat(updated).isTrue();

        Customer found = customerDAO.findById(saved.getId());
        assertThat(found.getName()).isEqualTo("New Name");
        assertThat(found.getEmail()).isEqualTo("new@corp.com");
        assertThat(found.getAddress()).isEqualTo("New Addr");
    }

    @Test
    void updateRejectsNullCustomer() {
        assertThatThrownBy(() -> customerDAO.update(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteRemovesCustomer() throws SQLException {
        Customer saved = customerDAO.save(TestDataFactory.createCustomer("Delete Corp", "del@corp.com", "Del St"));

        boolean deleted = customerDAO.delete(saved.getId());
        assertThat(deleted).isTrue();
        assertThat(customerDAO.findById(saved.getId())).isNull();
    }

    @Test
    void deleteReturnsFalseForNonExistentCustomer() throws SQLException {
        boolean deleted = customerDAO.delete(99999L);
        assertThat(deleted).isFalse();
    }
}
