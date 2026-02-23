package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.entity.User;

import java.util.List;

/**
 * T047: Service interface for User operations.
 */
public interface UserService {
    User save(User user);
    User findById(Long id);
    User findByEmail(String email);
    List<User> findAll();
    User update(User user);
    boolean delete(Long id);
}
