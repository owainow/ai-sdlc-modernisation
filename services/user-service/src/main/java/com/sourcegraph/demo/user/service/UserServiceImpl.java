package com.sourcegraph.demo.user.service;

import com.sourcegraph.demo.user.entity.User;
import com.sourcegraph.demo.user.exception.DuplicateResourceException;
import com.sourcegraph.demo.user.exception.ResourceNotFoundException;
import com.sourcegraph.demo.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    public User create(String username, String firstName, String lastName, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("User", "username", username);
        }
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPasswordHash(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    @Override
    public User update(UUID id, String username, String firstName, String lastName) {
        User user = findById(id);
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("User", "username", username);
        }
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return userRepository.save(user);
    }

    @Override
    public void delete(UUID id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}
