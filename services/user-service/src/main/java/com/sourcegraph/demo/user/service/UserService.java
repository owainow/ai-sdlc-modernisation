package com.sourcegraph.demo.user.service;

import com.sourcegraph.demo.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    Page<User> findAll(Pageable pageable);
    User findById(UUID id);
    User create(String username, String firstName, String lastName, String password);
    User update(UUID id, String username, String firstName, String lastName);
    void delete(UUID id);
}
