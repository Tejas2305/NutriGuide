package com.nutriguide.dao;

import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.MealPlan;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * MealPlanDAO - handles all database operations for the 'meal_plan' table.
 */
public class MealPlanDAO {

    /**
     * Adds a new meal plan to the database.
     * Returns the auto-generated meal_plan_id.
     */
    public int addMealPlan(MealPlan plan) throws DatabaseOperationException {
        String sql = "INSERT INTO meal_plan (user_id, plan_name, start_date, end_date, is_active) " +
                     "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet keys = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, plan.getUserId());
            stmt.setString(2, plan.getPlanName());
            stmt.setDate(3, Date.valueOf(plan.getStartDate()));
            stmt.setDate(4, Date.valueOf(plan.getEndDate()));
            stmt.setBoolean(5, plan.getIsActive() != null ? plan.getIsActive() : true);

            stmt.executeUpdate();

            keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                plan.setMealPlanId(id);
                return id;
            } else {
                throw new DatabaseOperationException("Meal plan added but could not get generated ID.");
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add meal plan for user: " + plan.getUserId(), e);
        } finally {
            closeResultSet(keys);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets a meal plan by its meal_plan_id.
     * Returns null if not found.
     */
    public MealPlan getMealPlanById(int mealPlanId) throws DatabaseOperationException {
        String sql = "SELECT meal_plan_id, user_id, plan_name, start_date, end_date, " +
                     "is_active, created_at FROM meal_plan WHERE meal_plan_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mealPlanId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get meal plan by ID: " + mealPlanId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all meal plans for a specific user.
     */
    public ArrayList<MealPlan> getMealPlansByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT meal_plan_id, user_id, plan_name, start_date, end_date, " +
                     "is_active, created_at FROM meal_plan WHERE user_id = ? ORDER BY start_date DESC";

        ArrayList<MealPlan> list = new ArrayList<MealPlan>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get meal plans for user: " + userId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets the currently active meal plan for a user.
     * Returns null if no active plan found.
     */
    public MealPlan getActiveMealPlanByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT meal_plan_id, user_id, plan_name, start_date, end_date, " +
                     "is_active, created_at FROM meal_plan WHERE user_id = ? AND is_active = TRUE LIMIT 1";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get active meal plan for user: " + userId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Updates an existing meal plan.
     * Returns true if updated successfully.
     */
    public boolean updateMealPlan(MealPlan plan) throws DatabaseOperationException {
        String sql = "UPDATE meal_plan SET plan_name = ?, start_date = ?, end_date = ?, " +
                     "is_active = ? WHERE meal_plan_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, plan.getPlanName());
            stmt.setDate(2, Date.valueOf(plan.getStartDate()));
            stmt.setDate(3, Date.valueOf(plan.getEndDate()));
            stmt.setBoolean(4, plan.getIsActive() != null ? plan.getIsActive() : true);
            stmt.setInt(5, plan.getMealPlanId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update meal plan ID: " + plan.getMealPlanId(), e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Deletes a meal plan by its meal_plan_id.
     * Returns true if deleted successfully.
     */
    public boolean deleteMealPlan(int mealPlanId) throws DatabaseOperationException {
        String sql = "DELETE FROM meal_plan WHERE meal_plan_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mealPlanId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete meal plan ID: " + mealPlanId, e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Maps a database row to a MealPlan object.
     */
    private MealPlan mapRow(ResultSet rs) throws SQLException {
        Date startDate = rs.getDate("start_date");
        Date endDate = rs.getDate("end_date");
        Timestamp timestamp = rs.getTimestamp("created_at");

        LocalDate sDate = null;
        if (startDate != null) {
            sDate = startDate.toLocalDate();
        }

        LocalDate eDate = null;
        if (endDate != null) {
            eDate = endDate.toLocalDate();
        }

        LocalDateTime createdAt = null;
        if (timestamp != null) {
            createdAt = timestamp.toLocalDateTime();
        }

        return new MealPlan(
                rs.getInt("meal_plan_id"),
                rs.getInt("user_id"),
                rs.getString("plan_name"),
                sDate,
                eDate,
                rs.getBoolean("is_active"),
                createdAt
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
