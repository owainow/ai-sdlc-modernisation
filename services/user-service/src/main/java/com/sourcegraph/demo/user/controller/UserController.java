package com.sourcegraph.demo.user.controller;

import com.sourcegraph.demo.common.dto.ApiResponse;
import com.sourcegraph.demo.common.dto.PaginatedResponse;
import com.sourcegraph.demo.user.entity.User;
import com.sourcegraph.demo.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> listUsers(Pageable pageable) {
        Page<User> page = userService.findAll(pageable);
        PaginatedResponse<UserResponse> paginatedResponse = new PaginatedResponse<>(
                page.getContent().stream().map(UserResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
        return ResponseEntity.ok(ApiResponse.success(paginatedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.create(
                request.username(), request.firstName(), request.lastName(), request.password()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(UserResponse.from(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.update(id, request.username(), request.firstName(), request.lastName());
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record CreateUserRequest(
            @jakarta.validation.constraints.NotBlank(message = "Username is required")
            @jakarta.validation.constraints.Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username,
            @jakarta.validation.constraints.NotBlank(message = "First name is required")
            @jakarta.validation.constraints.Size(max = 100, message = "First name must not exceed 100 characters")
            String firstName,
            @jakarta.validation.constraints.NotBlank(message = "Last name is required")
            @jakarta.validation.constraints.Size(max = 100, message = "Last name must not exceed 100 characters")
            String lastName,
            @jakarta.validation.constraints.NotBlank(message = "Password is required")
            @jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters")
            String password) {
    }

    public record UpdateUserRequest(
            @jakarta.validation.constraints.NotBlank(message = "Username is required")
            @jakarta.validation.constraints.Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username,
            @jakarta.validation.constraints.NotBlank(message = "First name is required")
            @jakarta.validation.constraints.Size(max = 100, message = "First name must not exceed 100 characters")
            String firstName,
            @jakarta.validation.constraints.NotBlank(message = "Last name is required")
            @jakarta.validation.constraints.Size(max = 100, message = "Last name must not exceed 100 characters")
            String lastName) {
    }

    public record UserResponse(
            UUID id,
            String username,
            String firstName,
            String lastName,
            String createdAt,
            String updatedAt) {

        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
                    user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null
            );
        }
    }
}
