package com.ecommerce.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.models.User;
import com.ecommerce.service.validation.UserValidator;

public class UserService {
    
    private static UserService instance;
    private final UserDAO userDAO;
    private final AuthenticationService authService;
    
    public UserService() {
        this.userDAO = new UserDAO();
        this.authService = new AuthenticationService(userDAO);
    }
    
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    public User authenticate(String email, String password) {
        return authService.authenticate(email, password);
    }

    public User authenticateAdmin(String email, String password) {
        return authService.authenticateAdmin(email, password);
    }

    public boolean isAdmin(User user) {
        return authService.isAdmin(user);
    }

    public RegisterResult registerUser(String name, String email, String password,
                                        String phone, String address) {
        if (!UserValidator.isValidName(name)) {
            return new RegisterResult(false, "Name is required");
        }
        if (!UserValidator.isValidEmail(email)) {
            return new RegisterResult(false, "Invalid email format");
        }
        if (!UserValidator.isValidPassword(password)) {
            return new RegisterResult(false, "Password must be at least 6 characters");
        }

        // Check if email already exists
        if (userDAO.getUserByEmail(email) != null) {
            return new RegisterResult(false, "Email already registered");
        }

        // Create user
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(password);
        user.setPhone(phone != null ? phone.trim() : "");
        user.setAddress(address != null ? address.trim() : "");
        user.setRole("user");

        if (userDAO.addUser(user)) {
            System.out.println("[UserService] User registered: " + user.getEmail());
            return new RegisterResult(true, "Registration successful", user);
        }

        return new RegisterResult(false, "Registration failed");
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }
    
    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return userDAO.getUserByEmail(email.trim().toLowerCase());
    }
    
    /**
     * Update user profile
     */
    public boolean updateUser(User user) {
        if (user == null || user.getUserId() <= 0) {
            System.err.println("[UserService] Invalid user for update");
            return false;
        }
        return userDAO.updateUser(user);
    }
    
    /**
     * Delete user
     */
    public boolean deleteUser(int userId) {
        if (userId <= 0) {
            System.err.println("[UserService] Invalid user ID");
            return false;
        }
        return userDAO.deleteUser(userId);
    }
    
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = userDAO.getUserById(userId);
        if (user == null || !user.getPassword().equals(oldPassword)) {
            return false;
        }
        if (!UserValidator.isValidPassword(newPassword)) {
            return false;
        }
        user.setPassword(newPassword);
        return userDAO.updateUser(user);
    }
    
    public boolean isEmailAvailable(String email) {
        return getUserByEmail(email) == null;
    }

    public static class RegisterResult {
        private final boolean success;
        private final String message;
        private final User user;

        public RegisterResult(boolean success, String message) {
            this(success, message, null);
        }

        public RegisterResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }
}
