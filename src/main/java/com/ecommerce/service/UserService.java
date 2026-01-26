package com.ecommerce.service;

import java.util.List;
import java.util.regex.Pattern;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.models.User;

/**
 * Service layer for User operations
 * Implements business logic for authentication and user management
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class UserService {
    
    private static UserService instance;
    private UserDAO userDAO;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    // ============ AUTHENTICATION ============
    
    /**
     * Authenticate user with email and password
     * @return User if credentials are valid, null otherwise
     */
    public User authenticate(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            System.err.println("[UserService] Email is required");
            return null;
        }
        if (password == null || password.trim().isEmpty()) {
            System.err.println("[UserService] Password is required");
            return null;
        }
        // Fetch user by email
        User user = userDAO.getUserByEmail(email.trim()); 
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("[UserService] User authenticated: " + user.getName());
            return user;
        }
        
        System.err.println("[UserService] Authentication failed for: " + email);
        return null;
    }
    
    /**
     * Authenticate admin user
     * @return User if admin credentials are valid, null otherwise
     */
    public User authenticateAdmin(String email, String password) {
        User user = authenticate(email, password);
        System.out.println(user);
        if (user != null && "admin".equalsIgnoreCase(user.getRole())) {
            System.out.println("[UserService] Admin authenticated: " + user.getName());
            return user;
        }
        System.err.println("[UserService] Admin authentication failed");
        return null;
    }
    
    /**
     * Check if user is admin
     */
    public boolean isAdmin(User user) {
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }
    
    // ============ REGISTRATION ============
    
    /**
     * Register new user
     * @return RegisterResult with success status and message
     */
    public RegisterResult registerUser(String name, String email, String password, 
                                        String phone, String address) {
        // Validation
        if (name == null || name.trim().isEmpty()) {
            return new RegisterResult(false, "Name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            return new RegisterResult(false, "Email is required");
        }
        if (!isValidEmail(email)) {
            return new RegisterResult(false, "Invalid email format");
        }
        if (password == null || password.length() < 6) {
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
    
    /**
     * Result class for registration
     */
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
    
    // ============ CRUD OPERATIONS ============
    
    /**
     * Get all users
     */
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
    
    // ============ PASSWORD MANAGEMENT ============
    
    /**
     * Change user password
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            return false;
        }
        
        if (!user.getPassword().equals(oldPassword)) {
            System.err.println("[UserService] Old password incorrect");
            return false;
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            System.err.println("[UserService] New password too short");
            return false;
        }
        
        user.setPassword(newPassword);
        return userDAO.updateUser(user);
    }
    
    // ============ VALIDATION ============
    
    /**
     * Validate email format
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Check if email is available (not registered)
     */
    public boolean isEmailAvailable(String email) {
        return getUserByEmail(email) == null;
    }
}
