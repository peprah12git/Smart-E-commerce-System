package com.ecommerce.dao;

import java.util.List;

import com.ecommerce.db.Category;

/**
 * CategoryDao Interface
 * Defines database operations for Category entity
 */
public interface CategoryDao {
    Category getCategoryById(int categoryId);
    List<Category> getAllCategories();
    int createCategory(Category category);
    boolean updateCategory(Category category);
    boolean deleteCategory(int categoryId);
}
