package com.ecommerce.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.models.Product;

/**
 * Service layer for Product operations
 * Implements business logic and provides abstraction between Controller and DAO
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class ProductService {
    
    private static ProductService instance;
    private ProductDAO productDAO;
    
    /**
     * Private constructor for singleton pattern
     */
    public ProductService() {
        this.productDAO = new ProductDAO();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ProductService getInstance() {
        if (instance == null) {
            instance = new ProductService();
        }
        return instance;
    }
    
    // ============ CRUD OPERATIONS ============
    
    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }
    
    /**
     * Get product by ID
     */
    public Product getProductById(int productId) {
        return productDAO.getProductById(productId);
    }
    
    /**
     * Add new product
     */
    public boolean addProduct(Product product) {
        // Business validation
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            System.err.println("[ProductService] Product name is required");
            return false;
        }
        if (product.getPrice() == null || product.getPrice().doubleValue() <= 0) {
            System.err.println("[ProductService] Product price must be positive");
            return false;
        }
        return productDAO.addProduct(product);
    }
    
    /**
     * Update existing product
     */
    public boolean updateProduct(Product product) {
        if (product.getProductId() <= 0) {
            System.err.println(" Invalid product ID");
            return false;
        }
        return productDAO.updateProduct(product);
    }

    /**
     * Delete product
     */
    public boolean deleteProduct(int productId) {
        if (productId <= 0) {
            System.err.println(" Invalid product ID");
            return false;
        }
        return productDAO.deleteProduct(productId);
    }
    
    // ============ SEARCH & FILTER OPERATIONS ============
    
    /**
     * Search products by name
     */
    public List<Product> searchByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts();
        }
        return productDAO.searchProductsByName(searchTerm);
    }
    
    /**
     * Search products with multiple criteria (name and description)
     */
    public List<Product> searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProducts();
        }
        
        String term = searchTerm.toLowerCase();
        return getAllProducts().stream()
                .filter(p -> p.getProductName().toLowerCase().contains(term) ||
                           (p.getDescription() != null && p.getDescription().toLowerCase().contains(term)))
                .collect(Collectors.toList());
    }
    
    /**
     * Filter products by category
     */
    public List<Product> getProductsByCategory(int categoryId) {
        if (categoryId <= 0) {
            return getAllProducts();
        }
        return getAllProducts().stream()
                .filter(p -> p.getCategoryId() == categoryId)
                .collect(Collectors.toList());
    }
    
    /**
     * Filter products by category name
     */
    public List<Product> getProductsByCategoryName(String categoryName) {
        if (categoryName == null || categoryName.equals("All Categories")) {
            return getAllProducts();
        }
        return getAllProducts().stream()
                .filter(p -> categoryName.equalsIgnoreCase(p.getCategoryName()))
                .collect(Collectors.toList());
    }
    
    /**
     * Combined search and filter
     */
    public List<Product> searchAndFilter(String searchTerm, String categoryName) {
        List<Product> products = getAllProducts();
        
        // Filter by search term
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String term = searchTerm.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(term) ||
                               (p.getDescription() != null && p.getDescription().toLowerCase().contains(term)))
                    .collect(Collectors.toList());
        }
        
        // Filter by category
        if (categoryName != null && !categoryName.equals("All Categories")) {
            products = products.stream()
                    .filter(p -> categoryName.equalsIgnoreCase(p.getCategoryName()))
                    .collect(Collectors.toList());
        }
        
        return products;
    }
    
    /**
     * Get products by price range
     */
    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice) {
        return getAllProducts().stream()
                .filter(p -> p.getPrice().doubleValue() >= minPrice && 
                           p.getPrice().doubleValue() <= maxPrice)
                .collect(Collectors.toList());
    }
    
    // ============ SORTING OPERATIONS ============
    
    /**
     * Sort products by name
     */
    public List<Product> sortByName(boolean ascending) {
        List<Product> products = new ArrayList<>(getAllProducts());
        if (ascending) {
            products.sort(Comparator.comparing(Product::getProductName));
        } else {
            products.sort(Comparator.comparing(Product::getProductName).reversed());
        }
        return products;
    }
    
    /**
     * Sort products by price
     */
    public List<Product> sortByPrice(boolean ascending) {
        List<Product> products = new ArrayList<>(getAllProducts());
        if (ascending) {
            products.sort(Comparator.comparing(Product::getPrice));
        } else {
            products.sort(Comparator.comparing(Product::getPrice).reversed());
        }
        return products;
    }
    
    /**
     * Sort products by date created
     */
    public List<Product> sortByDate(boolean newest) {
        List<Product> products = new ArrayList<>(getAllProducts());
        if (newest) {
            products.sort(Comparator.comparing(Product::getCreatedAt).reversed());
        } else {
            products.sort(Comparator.comparing(Product::getCreatedAt));
        }
        return products;
    }
    
    // ============ BUSINESS LOGIC ============
    
    /**
     * Get products that are in stock
     */
    public List<Product> getInStockProducts() {
        return getAllProducts().stream()
                .filter(p -> p.getQuantityAvailable() > 0)
                .collect(Collectors.toList());
    }
    
    /**
     * Get featured products (example: top products by some criteria)
     */
    public List<Product> getFeaturedProducts(int limit) {
        return getAllProducts().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Check if product is available
     */
    public boolean isProductAvailable(int productId, int requestedQuantity) {
        Product product = getProductById(productId);
        return product != null && product.getQuantityAvailable() >= requestedQuantity;
    }
    
    /**
     * Invalidate cache (for admin operations)
     */
    public void refreshProducts() {
        productDAO.invalidateCache();
    }
    
    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        return ProductDAO.getCacheStats();
    }

    /**
     * Measure query time; optionally force cache miss.
     */
    public long measureQueryTime(boolean useCache) {
        if (!useCache) {
            productDAO.invalidateCache();
        }
        long start = System.currentTimeMillis();
        productDAO.getAllProducts();
        return System.currentTimeMillis() - start;
    }

    /**
     * Pass-through search by name used by controllers.
     */
    public List<Product> searchProductsByName(String term) {
        return productDAO.searchProductsByName(term);
    }

    /**
     * Pass-through category filter used by controllers.
     */
    public List<Product> filterByCategory(int categoryId) {
        return getProductsByCategory(categoryId);
    }
}
