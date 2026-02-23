package com.sourcegraph.demo.bigbadmonolith.service.impl;

import com.sourcegraph.demo.bigbadmonolith.dao.UserDAO;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.exception.ResourceNotFoundException;
import com.sourcegraph.demo.bigbadmonolith.exception.ValidationException;
import com.sourcegraph.demo.bigbadmonolith.service.UserService;

import java.util.List;

/**
 * T052: UserService implementation delegating to UserDAO.
 */
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    public UserServiceImpl() {
        this.userDAO = new UserDAO();
    }

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User save(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new ValidationException("User name is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ValidationException("User email is required");
        }
        return userDAO.save(user);
    }

    @Override
    public User findById(Long id) {
        User user = userDAO.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User", id);
        }
        return user;
    }

    @Override
    public User findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userDAO.findAll();
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("User ID is required for update");
        }
        return userDAO.update(user);
    }

    @Override
    public boolean delete(Long id) {
        return userDAO.delete(id);
    }
}
