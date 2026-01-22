package com.ecommerce.service;

import com.ecommerce.dao.CategoryDAO;
import com.ecommerce.models.Category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Category operations
 * Implements business logic for category management
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class CategoryService {
    
    private static CategoryService instance;
    private CategoryDAO categoryDAO;
    
    // Cache for categories (changes infrequently)
    private List<Category> categoryCache;
    private long cacheTimestamp;
    private static final long CACHE_TTL_MS = 600000; // 10 minutes
    
    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
        this.categoryCache = null;
        this.cacheTimestamp = 0;
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized CategoryService getInstance() {
        if (instance == null) {
            instance = new CategoryService();
        }
        return instance;
    }
    
    // ============ CRUD OPERATIONS ============
    
    /**
     * Get all categories (with caching)
     */
    public List<Category> getAllCategories() {
        if (isCacheValid()) {
            System.out.println("[CategoryService] Returning cached categories");
            return new ArrayList<>(categoryCache);
        }
        
        System.out.println("[CategoryService] Fetching categories from database");
        categoryCache = categoryDAO.getAllCategories();
        cacheTimestamp = System.currentTimeMillis();
        return new ArrayList<>(categoryCache);
    }
    
    /**
     * Get category by ID
     */
    public Category getCategoryById(int categoryId) {
        return categoryDAO.getCategoryById(categoryId);
    }
    
    /**
     * Get category by name
     */
    public Category getCategoryByName(String name) {
        return getAllCategories().stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Add new category
     */
    public boolean addCategory(Category category) {
        // Validation
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            System.err.println("[CategoryService] Category name is required");
            return false;
        }
        
        // Check for duplicate name
        if (getCategoryByName(category.getCategoryName()) != null) {
            System.err.println("[CategoryService] Category name already exists");
            return false;
        }
        
        boolean success = categoryDAO.addCategory(category);
        if (success) {
            invalidateCache();
        }
        return success;
    }
    
    /**
     * Update category
     */
    public boolean updateCategory(Category category) {
        if (category == null || category.getCategoryId() <= 0) {
            System.err.println("[CategoryService] Invalid category for update");
            return false;
        }
        
        boolean success = categoryDAO.updateCategory(category);
        if (success) {
            invalidateCache();
        }
        return success;
    }
    
    /**
     * Delete category
     */
    public boolean deleteCategory(int categoryId) {
        if (categoryId <= 0) {
            System.err.println("[CategoryService] Invalid category ID");
            return false;
        }
        
        boolean success = categoryDAO.deleteCategory(categoryId);
        if (success) {
            invalidateCache();
        }
        return success;
    }
    
    // ============ HELPER METHODS ============
    
    /**
     * Get category names as list (for dropdowns)
     */
    public List<String> getCategoryNames() {
        return getAllCategories().stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toList());
    }
    
    /**
     * Get category names with "All Categories" option
     */
    public List<String> getCategoryNamesWithAll() {
        List<String> names = new ArrayList<>();
        names.add("All Categories");
        names.addAll(getCategoryNames());
        return names;
    }
    
    /**
     * Get category ID by name
     */
    public int getCategoryIdByName(String name) {
        Category category = getCategoryByName(name);
        return category != null ? category.getCategoryId() : -1;
    }
    
    /**
     * Get category name by ID
     */
    public String getCategoryNameById(int categoryId) {
        Category category = getCategoryById(categoryId);
        return category != null ? category.getCategoryName() : "Unknown";
    }
    
    /**
     * Map category ID to display name
     */
    public String mapCategoryIdToName(int categoryId) {
        return switch (categoryId) {
            case 1 -> "Electronics";
            case 2 -> "Clothing";
            case 3 -> "Home & Garden";
            case 4 -> "Sports & Outdoors";
            case 5 -> "Books & Media";
            default -> getCategoryNameById(categoryId);
        };
    }
    
    // ============ SORTING ============
    
    /**
     * Sort categories by name
     */
    public List<Category> sortByName(boolean ascending) {
        List<Category> categories = new ArrayList<>(getAllCategories());
        if (ascending) {
            categories.sort(Comparator.comparing(Category::getCategoryName));
        } else {
            categories.sort(Comparator.comparing(Category::getCategoryName).reversed());
        }
        return categories;
    }
    
    // ============ CACHE MANAGEMENT ============
    
    private boolean isCacheValid() {
        return categoryCache != null && 
               !categoryCache.isEmpty() &&
               (System.currentTimeMillis() - cacheTimestamp) < CACHE_TTL_MS;
    }
    
    public void invalidateCache() {
        categoryCache = null;
        cacheTimestamp = 0;
        System.out.println("[CategoryService] Cache invalidated");
    }
    
    public void refreshCategories() {
        invalidateCache();
        getAllCategories();
    }
}
