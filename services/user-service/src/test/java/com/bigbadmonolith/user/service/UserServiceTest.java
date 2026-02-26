package com.bigbadmonolith.user.service;

import com.bigbadmonolith.user.dto.*;
import com.bigbadmonolith.user.model.User;
import com.bigbadmonolith.user.repository.UserRepository;
import com.bigbadmonolith.common.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(testId);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setCreatedAt(Instant.now());
        testUser.setUpdatedAt(Instant.now());
    }

    @Test
    void create_shouldCreateUser() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        var result = userService.create(new CreateUserRequest("John Doe", "john@example.com"));

        assertThat(result.name()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_shouldThrowOnDuplicateEmail() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(new CreateUserRequest("John Doe", "john@example.com")))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void findById_shouldReturnUser() {
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));

        var result = userService.findById(testId);

        assertThat(result.id()).isEqualTo(testId);
        assertThat(result.name()).isEqualTo("John Doe");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(userRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(testId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldUpdateUser() {
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        var result = userService.update(testId, new UpdateUserRequest("John Doe", "john@example.com"));

        assertThat(result.name()).isEqualTo("John Doe");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_shouldThrowOnDuplicateEmail() {
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(testId, new UpdateUserRequest("John Doe", "other@example.com")))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        when(userRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(testId, new UpdateUserRequest("John Doe", "john@example.com")))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldDeleteUser() {
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));

        userService.delete(testId);

        verify(userRepository).delete(testUser);
    }

    @Test
    void count_shouldReturnCount() {
        when(userRepository.count()).thenReturn(5L);

        assertThat(userService.count()).isEqualTo(5L);
    }

    @Test
    void exists_shouldReturnTrue() {
        when(userRepository.existsById(testId)).thenReturn(true);

        assertThat(userService.exists(testId)).isTrue();
    }

    @Test
    void exists_shouldReturnFalse() {
        when(userRepository.existsById(testId)).thenReturn(false);

        assertThat(userService.exists(testId)).isFalse();
    }

    @Test
    void findAll_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(testUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(page);

        var result = userService.findAll(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("John Doe");
    }

    @Test
    void findAll_shouldSearchByNameOrEmail() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(testUser), pageable, 1);
        when(userRepository.searchByNameOrEmail(eq("John"), eq(pageable))).thenReturn(page);

        var result = userService.findAll("John", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).searchByNameOrEmail("John", pageable);
    }
}
