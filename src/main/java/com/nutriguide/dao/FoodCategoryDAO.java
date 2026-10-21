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
 * FoodCategoryDAO - handles all database operations for the 'food_category' table.
 */
public class FoodCategoryDAO {

    /**
     * Adds a new food category to the database.
     * Returns the auto-generated category_id.
     */
    public int addCategory(FoodCategory category) throws DatabaseOperationException {
        String sql = "INSERT INTO food_category (category_name) VALUES (?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet keys = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, category.getCategoryName());
            stmt.executeUpdate();

            keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                category.setCategoryId(id);
                return id;
            } else {
                throw new DatabaseOperationException("Category added but could not get generated ID.");
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add category: " + category.getCategoryName(), e);
        } finally {
            closeResultSet(keys);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets a food category by its category_id.
     * Returns null if not found.
     */
    public FoodCategory getCategoryById(int categoryId) throws DatabaseOperationException {
        String sql = "SELECT category_id, category_name FROM food_category WHERE category_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, categoryId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get category by ID: " + categoryId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all food categories from the database.
     */
    public ArrayList<FoodCategory> getAllCategories() throws DatabaseOperationException {
        String sql = "SELECT category_id, category_name FROM food_category ORDER BY category_id";

        ArrayList<FoodCategory> list = new ArrayList<FoodCategory>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get all categories", e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Maps a database row to a FoodCategory object.
     */
    private FoodCategory mapRow(ResultSet rs) throws SQLException {
        return new FoodCategory(
                rs.getInt("category_id"),
                rs.getString("category_name")
        );
    }

    // ---- Helper methods to safely close resources ----

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    private void closeStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
}
