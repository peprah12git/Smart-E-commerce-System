package com.ecommerce.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.config.DatabaseConnection;
import com.ecommerce.models.Product;

public class ProductDAO {
    private Connection connection;

    public ProductDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
//adds a new product to the database
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO Products (name, description, price, category_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDescription());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getCategoryId());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    product.setProductId(rs.getInt(1));
                    createInventoryEntry(product.getProductId());
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
        return false;
    }
//creates an inventory entry for a new product
    private void createInventoryEntry(int productId) {
        String sql = "INSERT INTO Inventory (product_id, quantity_available) VALUES (?, 0)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating inventory: " + e.getMessage());
        }
    }
        //fetches all products from the database
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name, COALESCE(i.quantity_available, 0) as quantity " +
                "FROM Products p " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "ORDER BY p.product_id DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products: " + e.getMessage());
        }
        return products;
    }
//fetches one product using its product ID
    public Product getProductById(int id) {
        String sql = "SELECT p.*, c.category_name, COALESCE(i.quantity_available, 0) as quantity " +
                "FROM Products p " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "WHERE p.product_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractProduct(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product: " + e.getMessage());
        }
        return null;
    }
//updates product details in the database
    public boolean updateProduct(Product product) {
        String sql = "UPDATE Products SET name = ?, description = ?, price = ?, category_id = ? WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDescription());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getCategoryId());
            pstmt.setInt(5, product.getProductId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
        return false;
    }
//deletes a product from the database
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM Products WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
        }
        return false;
    }
//searches products by name with a LIKE query
    public List<Product> searchProductsByName(String term) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name, COALESCE(i.quantity_available, 0) as quantity " +
                "FROM Products p " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "WHERE p.name LIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + term + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching: " + e.getMessage());
        }
        return products;
    }
//extracts product data from ResultSet
    private Product extractProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setQuantityAvailable(rs.getInt("quantity"));
        return p;
    }
}