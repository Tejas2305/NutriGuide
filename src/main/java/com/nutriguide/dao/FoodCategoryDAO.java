package com.nutriguide.dao;

import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.FoodCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * FoodCategoryDAO: Handles JDBC database operations for the 'food_category' relation.
 */
public class FoodCategoryDAO {

    /**
     * Adds a new food category and returns the generated category_id.
     *
     * @param category The FoodCategory entity to persist
     * @return Generated category_id
     * @throws DatabaseOperationException If database operation fails
     */
    public int addCategory(FoodCategory category) throws DatabaseOperationException {
        String sql = "INSERT INTO food_category (category_name) VALUES (?)";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getCategoryName());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    category.setCategoryId(id);
                    return id;
                } else {
                    throw new DatabaseOperationException("Category insertion succeeded but failed to obtain ID.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add category: " + category.getCategoryName(), e);
        }
    }

    /**
     * Retrieves a food category by ID.
     *
     * @param categoryId Primary key ID
     * @return FoodCategory or null if not found
     * @throws DatabaseOperationException If database operation fails
     */
    public FoodCategory getCategoryById(int categoryId) throws DatabaseOperationException {
        String sql = "SELECT category_id, category_name FROM food_category WHERE category_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get category by ID: " + categoryId, e);
        }
    }

    /**
     * Retrieves all food categories.
     *
     * @return ArrayList of FoodCategory objects
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<FoodCategory> getAllCategories() throws DatabaseOperationException {
        String sql = "SELECT category_id, category_name FROM food_category ORDER BY category_id";

        ArrayList<FoodCategory> list = new ArrayList<FoodCategory>();

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get all categories", e);
        }
    }

    private FoodCategory mapRow(ResultSet rs) throws SQLException {
        return new FoodCategory(
                rs.getInt("category_id"),
                rs.getString("category_name")
        );
    }
}
