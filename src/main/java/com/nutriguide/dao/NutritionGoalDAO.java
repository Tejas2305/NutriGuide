package com.nutriguide.dao;

import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.NutritionGoal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * NutritionGoalDAO - handles all database operations for the 'nutrition_goal' table.
 */
public class NutritionGoalDAO {

    /**
     * Adds a new nutrition goal to the database.
     * Returns the auto-generated goal_id.
     */
    public int addGoal(NutritionGoal goal) throws DatabaseOperationException {
        String sql = "INSERT INTO nutrition_goal (user_id, target_calories, target_protein, " +
                     "target_carbs, target_fats, is_active) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet keys = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, goal.getUserId());
            stmt.setBigDecimal(2, goal.getTargetCalories());
            stmt.setBigDecimal(3, goal.getTargetProtein());
            stmt.setBigDecimal(4, goal.getTargetCarbs());
            stmt.setBigDecimal(5, goal.getTargetFats());
            stmt.setBoolean(6, goal.getIsActive() != null ? goal.getIsActive() : true);

            stmt.executeUpdate();

            keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                goal.setGoalId(id);
                return id;
            } else {
                throw new DatabaseOperationException("Goal added but could not get generated ID.");
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add nutrition goal for user: " + goal.getUserId(), e);
        } finally {
            closeResultSet(keys);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Adds a new nutrition goal using an existing connection (used for transactions).
     */
    public int addGoal(Connection conn, NutritionGoal goal) throws SQLException, DatabaseOperationException {
        String sql = "INSERT INTO nutrition_goal (user_id, target_calories, target_protein, " +
                     "target_carbs, target_fats, is_active) VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = null;
        ResultSet keys = null;

        try {
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, goal.getUserId());
            stmt.setBigDecimal(2, goal.getTargetCalories());
            stmt.setBigDecimal(3, goal.getTargetProtein());
            stmt.setBigDecimal(4, goal.getTargetCarbs());
            stmt.setBigDecimal(5, goal.getTargetFats());
            stmt.setBoolean(6, goal.getIsActive() != null ? goal.getIsActive() : true);

            stmt.executeUpdate();

            keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                goal.setGoalId(id);
                return id;
            } else {
                throw new DatabaseOperationException("Goal added but could not get generated ID.");
            }

        } finally {
            if (keys != null) { try { keys.close(); } catch (SQLException e) { /* ignore */ } }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignore */ } }
        }
    }

    /**
     * Deactivates all active goals for a user using an existing connection (used for transactions).
     */
    public int deactivateActiveGoals(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE nutrition_goal SET is_active = FALSE WHERE user_id = ? AND is_active = TRUE";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            return stmt.executeUpdate();
        } finally {
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignore */ } }
        }
    }

    /**
     * Deactivates all active goals for a user.
     */
    public int deactivateActiveGoals(int userId) throws DatabaseOperationException {
        Connection conn = null;
        try {
            conn = DBConnectionFactory.getConnection();
            return deactivateActiveGoals(conn, userId);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to deactivate goals for user: " + userId, e);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Gets a nutrition goal by its goal_id.
     * Returns null if not found.
     */
    public NutritionGoal getGoalById(int goalId) throws DatabaseOperationException {
        String sql = "SELECT goal_id, user_id, target_calories, target_protein, target_carbs, " +
                     "target_fats, is_active, created_at FROM nutrition_goal WHERE goal_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, goalId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get goal by ID: " + goalId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all nutrition goals for a user (active and past).
     */
    public ArrayList<NutritionGoal> getGoalsByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT goal_id, user_id, target_calories, target_protein, target_carbs, " +
                     "target_fats, is_active, created_at FROM nutrition_goal WHERE user_id = ? ORDER BY created_at DESC";

        ArrayList<NutritionGoal> list = new ArrayList<NutritionGoal>();
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
            throw new DatabaseOperationException("Failed to get goals for user: " + userId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets the currently active nutrition goal for a user.
     * Returns null if no active goal exists.
     */
    public NutritionGoal getActiveGoalByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT goal_id, user_id, target_calories, target_protein, target_carbs, " +
                     "target_fats, is_active, created_at FROM nutrition_goal WHERE user_id = ? AND is_active = TRUE LIMIT 1";

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
            throw new DatabaseOperationException("Failed to get active goal for user: " + userId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Updates an existing nutrition goal.
     * Returns true if updated successfully.
     */
    public boolean updateGoal(NutritionGoal goal) throws DatabaseOperationException {
        String sql = "UPDATE nutrition_goal SET target_calories = ?, target_protein = ?, " +
                     "target_carbs = ?, target_fats = ?, is_active = ? WHERE goal_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setBigDecimal(1, goal.getTargetCalories());
            stmt.setBigDecimal(2, goal.getTargetProtein());
            stmt.setBigDecimal(3, goal.getTargetCarbs());
            stmt.setBigDecimal(4, goal.getTargetFats());
            stmt.setBoolean(5, goal.getIsActive() != null ? goal.getIsActive() : true);
            stmt.setInt(6, goal.getGoalId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update goal ID: " + goal.getGoalId(), e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Deletes a nutrition goal by its goal_id.
     * Returns true if deleted successfully.
     */
    public boolean deleteGoal(int goalId) throws DatabaseOperationException {
        String sql = "DELETE FROM nutrition_goal WHERE goal_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, goalId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete goal ID: " + goalId, e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Maps a database row to a NutritionGoal object.
     */
    private NutritionGoal mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = null;
        if (timestamp != null) {
            createdAt = timestamp.toLocalDateTime();
        }

        return new NutritionGoal(
                rs.getInt("goal_id"),
                rs.getInt("user_id"),
                rs.getBigDecimal("target_calories"),
                rs.getBigDecimal("target_protein"),
                rs.getBigDecimal("target_carbs"),
                rs.getBigDecimal("target_fats"),
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
