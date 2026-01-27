package com.ecommerce.service;

import java.util.ArrayList;
import java.util.List;

import com.ecommerce.dao.CategoryDAO;
import com.ecommerce.models.Category;

public class CategoryService {
    private CategoryDAO categoryDAO;
    private List<Category> categoryCache;
    private long lastCacheUpdate;
    private static final long CACHE_VALIDITY = 600000; // 10 minutes

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
        this.categoryCache = new ArrayList<>();
        this.lastCacheUpdate = 0;
    }

    public List<Category> getAllCategories() {
        long now = System.currentTimeMillis();

        if (!categoryCache.isEmpty() && (now - lastCacheUpdate) < CACHE_VALIDITY) {
            return new ArrayList<>(categoryCache);
        }

        categoryCache = categoryDAO.getAllCategories();
        lastCacheUpdate = now;
        return new ArrayList<>(categoryCache);
    }

    public Category getCategoryById(int id) {
        return categoryDAO.getCategoryById(id);
    }
}
