package com.ecommerce.dao;

import java.util.List;

import com.ecommerce.db.User;

/**
 * UserDao Interface
 * Defines database operations for User entity
 */
public interface UserDao {
    User getUserById(int userId);
    List<User> getAllUsers();
    int createUser(User user);
    boolean updateUser(User user);
    boolean deleteUser(int userId);
    User getUserByEmail(String email);
}
