package com.bigbadmonolith.user.service;

import com.bigbadmonolith.user.dto.*;
import com.bigbadmonolith.user.model.User;
import com.bigbadmonolith.user.repository.UserRepository;
import com.bigbadmonolith.common.exception.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User with email '" + request.email() + "' already exists");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(String search, Pageable pageable) {
        Page<User> page;
        if (search != null && !search.isBlank()) {
            page = userRepository.searchByNameOrEmail(search, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return userRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User with email '" + request.email() + "' already exists");
        }

        user.setName(request.name());
        user.setEmail(request.email());
        return toResponse(userRepository.save(user));
    }

    public void delete(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public boolean exists(UUID id) {
        return userRepository.existsById(id);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
