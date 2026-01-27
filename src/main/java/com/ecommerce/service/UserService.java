package com.ecommerce.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.models.User;

public class UserService {
    private UserDAO userDAO;
    private Map<Integer, User> userCache;
    private List<User> allUsersCache;
    private long lastCacheUpdate;
    private static final long CACHE_VALIDITY = 300000; // 5 minutes

    public UserService() {
        this.userDAO = new UserDAO();
        this.userCache = new HashMap<>();
        this.allUsersCache = new ArrayList<>();
        this.lastCacheUpdate = 0;
    }

    public boolean addUser(User user) {
        boolean success = userDAO.addUser(user);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public List<User> getAllUsers() {
        long now = System.currentTimeMillis();

        if (!allUsersCache.isEmpty() && (now - lastCacheUpdate) < CACHE_VALIDITY) {
            System.out.println("✓ Users from cache");
            return new ArrayList<>(allUsersCache);
        }

        System.out.println("✗ Fetching users from database");
        allUsersCache = userDAO.getAllUsers();
        lastCacheUpdate = now;

        for (User u : allUsersCache) {
            userCache.put(u.getUserId(), u);
        }

        return new ArrayList<>(allUsersCache);
    }

    public User getUserById(int id) {
        if (userCache.containsKey(id)) {
            return userCache.get(id);
        }

        User user = userDAO.getUserById(id);
        if (user != null) {
            userCache.put(id, user);
        }
        return user;
    }

    public User getUserByEmail(String email) {
        return userDAO.getUserByEmail(email);
    }

    public boolean updateUser(User user) {
        boolean success = userDAO.updateUser(user);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public boolean deleteUser(int id) {
        boolean success = userDAO.deleteUser(id);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public boolean authenticateUser(String email, String password) {
        System.out.println("[AUTH DEBUG] Looking up user with email: " + email);
        User user = userDAO.getUserByEmail(email);
        
        if (user == null) {
            System.out.println("[AUTH DEBUG] ✗ No user found with email: " + email);
            return false;
        }
        
        System.out.println("[AUTH DEBUG] ✓ User found: " + user.getName());
        System.out.println("[AUTH DEBUG] Stored password: " + user.getPassword());
        System.out.println("[AUTH DEBUG] Input password: " + password);
        
        boolean match = user.getPassword().equals(password);
        System.out.println("[AUTH DEBUG] Password match: " + match);
        
        return match;
    }

    private void invalidateCache() {
        userCache.clear();
        allUsersCache.clear();
        lastCacheUpdate = 0;
    }
}
