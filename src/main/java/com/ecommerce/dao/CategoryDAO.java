package com.ecommerce.dao;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.config.DatabaseConnection;
import com.ecommerce.models.Category;

/**
 * Data Access Object for Category operations
 */
public class CategoryDAO {
    private Connection connection;

    public CategoryDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Categories ORDER BY category_name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setCategoryName(rs.getString("category_name"));
                category.setDescription(rs.getString("description"));
                category.setParentCategoryId(rs.getObject("category_id", Integer.class));
//                category.setCreatedAt(rs.getTimestamp("created_at"));
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            e.printStackTrace();
        }
        return categories;
    }

    /**
     * Add new category
     */
    public boolean addCategory(Category category) {
        String sql = "INSERT INTO Categories (category_name, description, category_id) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, category.getCategoryName());
            pstmt.setString(2, category.getDescription());

            if (category.getParentCategoryId() != null) {
                pstmt.setInt(3, category.getParentCategoryId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    category.setCategoryId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get category by ID
     */
    public Category getCategoryById(int categoryId) {
        String sql = "SELECT * FROM Categories WHERE category_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setCategoryName(rs.getString("category_name"));
                category.setDescription(rs.getString("description"));
                category.setParentCategoryId(rs.getObject("category_id", Integer.class));
//                category.setCrqeatedAt(rs.getTimestamp("created_at"));
                return category;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching category: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
