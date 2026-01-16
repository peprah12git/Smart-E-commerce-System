package com.ecommerce.dao;

import java.util.List;

import com.ecommerce.db.Product;

/**
 * ProductDao Interface
 * Defines database operations for Product entity
 */
public interface ProductDao {
    Product getProductById(int productId);
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(int categoryId);
    int createProduct(Product product);
    boolean updateProduct(Product product);
    boolean deleteProduct(int productId);
}
