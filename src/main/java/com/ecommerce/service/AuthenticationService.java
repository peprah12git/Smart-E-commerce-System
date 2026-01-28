package com.ecommerce.service;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.models.User;

public class AuthenticationService {
    
    private final UserDAO userDAO;
    
    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    public User authenticate(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }
        
        User user = userDAO.getUserByEmail(email.trim());
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("[Auth] User authenticated: " + user.getName());
            return user;
        }
        
        System.err.println("[Auth] Authentication failed for: " + email);
        return null;
    }
    
    public User authenticateAdmin(String email, String password) {
        User user = authenticate(email, password);
        if (user != null && "admin".equalsIgnoreCase(user.getRole())) {
            System.out.println("[Auth] Admin authenticated: " + user.getName());
            return user;
        }
        System.err.println("[Auth] Admin authentication failed");
        return null;
    }
    
    public boolean isAdmin(User user) {
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }
}
