package com.nutriguide.dao;

import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.MealItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * MealItemDAO - handles all database operations for the 'meal_item' table.
 * A meal item is a specific food added to a meal with its quantity.
 */
public class MealItemDAO {

    /**
     * Adds a meal item using an existing connection.
     * This version is used during transactions so we can rollback if something fails.
     */
    public int addMealItem(Connection conn, MealItem item) throws SQLException {
        String sql = "INSERT INTO meal_item (meal_id, food_id, quantity) VALUES (?, ?, ?)";

        PreparedStatement stmt = null;
        ResultSet keys = null;

        try {
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, item.getMealId());
            stmt.setInt(2, item.getFoodId());
            stmt.setBigDecimal(3, item.getQuantity());

            stmt.executeUpdate();

            keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                item.setMealItemId(id);
                return id;
            } else {
                throw new SQLException("MealItem added but could not get generated ID.");
            }

        } finally {
            // Don't close the connection here - caller manages it
            if (keys != null) { try { keys.close(); } catch (SQLException e) { /* ignore */ } }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignore */ } }
        }
    }

    /**
     * Adds a meal item and manages its own connection.
     */
    public int addMealItem(MealItem item) throws DatabaseOperationException {
        Connection conn = null;
        try {
            conn = DBConnectionFactory.getConnection();
            return addMealItem(conn, item);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add meal item to meal: " + item.getMealId(), e);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Gets a single meal item by its meal_item_id.
     * Returns null if not found.
     */
    public MealItem getMealItemById(int mealItemId) throws DatabaseOperationException {
        String sql = "SELECT meal_item_id, meal_id, food_id, quantity FROM meal_item WHERE meal_item_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mealItemId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get meal item by ID: " + mealItemId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all food items that belong to a specific meal.
     */
    public ArrayList<MealItem> getItemsByMealId(int mealId) throws DatabaseOperationException {
        String sql = "SELECT meal_item_id, meal_id, food_id, quantity FROM meal_item WHERE meal_id = ? ORDER BY meal_item_id";

        ArrayList<MealItem> list = new ArrayList<MealItem>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mealId);

            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get items for meal: " + mealId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Updates an existing meal item.
     * Returns true if updated successfully.
     */
    public boolean updateMealItem(MealItem item) throws DatabaseOperationException {
        String sql = "UPDATE meal_item SET meal_id = ?, food_id = ?, quantity = ? WHERE meal_item_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, item.getMealId());
            stmt.setInt(2, item.getFoodId());
            stmt.setBigDecimal(3, item.getQuantity());
            stmt.setInt(4, item.getMealItemId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update meal item ID: " + item.getMealItemId(), e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Deletes a single meal item by its ID.
     * Returns true if deleted successfully.
     */
    public boolean deleteMealItem(int mealItemId) throws DatabaseOperationException {
        String sql = "DELETE FROM meal_item WHERE meal_item_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mealItemId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete meal item ID: " + mealItemId, e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Deletes all food items that belong to a specific meal.
     * Returns true if any rows were deleted.
     */
    public boolean deleteItemsByMealId(int mealId) throws DatabaseOperationException {
        String sql = "DELETE FROM meal_item WHERE meal_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mealId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete items for meal: " + mealId, e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Maps a database row to a MealItem object.
     */
    private MealItem mapRow(ResultSet rs) throws SQLException {
        return new MealItem(
                rs.getInt("meal_item_id"),
                rs.getInt("meal_id"),
                rs.getInt("food_id"),
                rs.getBigDecimal("quantity")
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
