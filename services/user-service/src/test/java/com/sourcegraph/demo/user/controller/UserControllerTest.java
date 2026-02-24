package com.sourcegraph.demo.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sourcegraph.demo.user.entity.User;
import com.sourcegraph.demo.user.exception.DuplicateResourceException;
import com.sourcegraph.demo.user.exception.ResourceNotFoundException;
import com.sourcegraph.demo.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordHash("$2a$10$hashed");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    @Test
    void listUsers_returnsPagedResponse() throws Exception {
        User user = createTestUser();
        when(userService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].username", is("testuser")))
                .andExpect(jsonPath("$.data.totalElements", is(1)));
    }

    @Test
    void getUserById_returnsUser() throws Exception {
        User user = createTestUser();
        when(userService.findById(user.getId())).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.username", is("testuser")));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.findById(id)).thenThrow(new ResourceNotFoundException("User", id));

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Resource Not Found")));
    }

    @Test
    void createUser_returns201() throws Exception {
        User user = createTestUser();
        when(userService.create(eq("testuser"), eq("Test"), eq("User"), eq("Password1")))
                .thenReturn(user);

        String body = """
                {"username":"testuser","firstName":"Test","lastName":"User","password":"Password1"}
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.username", is("testuser")));
    }

    @Test
    void createUser_duplicateUsername_returns409() throws Exception {
        when(userService.create(any(), any(), any(), any()))
                .thenThrow(new DuplicateResourceException("User", "username", "testuser"));

        String body = """
                {"username":"testuser","firstName":"Test","lastName":"User","password":"Password1"}
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Resource Conflict")));
    }

    @Test
    void updateUser_returnsUpdatedUser() throws Exception {
        User user = createTestUser();
        when(userService.update(eq(user.getId()), eq("newname"), eq("New"), eq("Name")))
                .thenReturn(user);

        String body = """
                {"username":"newname","firstName":"New","lastName":"Name"}
                """;

        mockMvc.perform(put("/api/v1/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));
    }

    @Test
    void deleteUser_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(userService).delete(id);

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("User", id)).when(userService).delete(id);

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}
