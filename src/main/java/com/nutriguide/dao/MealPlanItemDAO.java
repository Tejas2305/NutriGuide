package com.nutriguide.dao;

import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.MealPlanItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * MealPlanItemDAO - handles all database operations for the 'meal_plan_item' table.
 * A meal plan item is a food item that is scheduled for a meal in a meal plan.
 */
public class MealPlanItemDAO {

    /**
     * Adds a new meal plan item to the database.
     * Returns the auto-generated meal_plan_item_id.
     */
    public int addMealPlanItem(MealPlanItem item) throws DatabaseOperationException {
        String sql = "INSERT INTO meal_plan_item (meal_plan_id, food_id, planned_meal_type, quantity) " +
                     "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet keys = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, item.getMealPlanId());
            stmt.setInt(2, item.getFoodId());
            stmt.setString(3, item.getPlannedMealType());
            stmt.setBigDecimal(4, item.getQuantity());

            stmt.executeUpdate();

            keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                item.setMealPlanItemId(id);
                return id;
            } else {
                throw new DatabaseOperationException("Meal plan item added but could not get generated ID.");
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add meal plan item to plan: " + item.getMealPlanId(), e);
        } finally {
            closeResultSet(keys);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets a meal plan item by its meal_plan_item_id.
     * Returns null if not found.
     */
    public MealPlanItem getMealPlanItemById(int mealPlanItemId) throws DatabaseOperationException {
        String sql = "SELECT meal_plan_item_id, meal_plan_id, food_id, planned_meal_type, quantity " +
                     "FROM meal_plan_item WHERE meal_plan_item_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mealPlanItemId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get meal plan item by ID: " + mealPlanItemId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all items in a specific meal plan.
     */
    public ArrayList<MealPlanItem> getItemsByMealPlanId(int mealPlanId) throws DatabaseOperationException {
        String sql = "SELECT meal_plan_item_id, meal_plan_id, food_id, planned_meal_type, quantity " +
                     "FROM meal_plan_item WHERE meal_plan_id = ? ORDER BY meal_plan_item_id";

        ArrayList<MealPlanItem> list = new ArrayList<MealPlanItem>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mealPlanId);

            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get items for meal plan: " + mealPlanId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Updates an existing meal plan item.
     * Returns true if updated successfully.
     */
    public boolean updateMealPlanItem(MealPlanItem item) throws DatabaseOperationException {
        String sql = "UPDATE meal_plan_item SET meal_plan_id = ?, food_id = ?, planned_meal_type = ?, " +
                     "quantity = ? WHERE meal_plan_item_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, item.getMealPlanId());
            stmt.setInt(2, item.getFoodId());
            stmt.setString(3, item.getPlannedMealType());
            stmt.setBigDecimal(4, item.getQuantity());
            stmt.setInt(5, item.getMealPlanItemId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update meal plan item ID: " + item.getMealPlanItemId(), e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Deletes a meal plan item by its ID.
     * Returns true if deleted successfully.
     */
    public boolean deleteMealPlanItem(int mealPlanItemId) throws DatabaseOperationException {
        String sql = "DELETE FROM meal_plan_item WHERE meal_plan_item_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mealPlanItemId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete meal plan item ID: " + mealPlanItemId, e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Maps a database row to a MealPlanItem object.
     */
    private MealPlanItem mapRow(ResultSet rs) throws SQLException {
        return new MealPlanItem(
                rs.getInt("meal_plan_item_id"),
                rs.getInt("meal_plan_id"),
                rs.getInt("food_id"),
                rs.getString("planned_meal_type"),
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
