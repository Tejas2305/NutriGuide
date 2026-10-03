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
 * NutritionGoalDAO: Handles JDBC database operations for the 'nutrition_goal' relation.
 */
public class NutritionGoalDAO {

    /**
     * Adds a new nutrition goal and returns the generated goal_id.
     *
     * @param goal NutritionGoal entity
     * @return Generated goal_id
     * @throws DatabaseOperationException If database operation fails
     */
    public int addGoal(NutritionGoal goal) throws DatabaseOperationException {
        String sql = "INSERT INTO nutrition_goal (user_id, target_calories, target_protein, " +
                     "target_carbs, target_fats, is_active) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, goal.getUserId());
            stmt.setBigDecimal(2, goal.getTargetCalories());
            stmt.setBigDecimal(3, goal.getTargetProtein());
            stmt.setBigDecimal(4, goal.getTargetCarbs());
            stmt.setBigDecimal(5, goal.getTargetFats());
            stmt.setBoolean(6, goal.getIsActive() != null ? goal.getIsActive() : true);

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    goal.setGoalId(id);
                    return id;
                } else {
                    throw new DatabaseOperationException("Goal insertion succeeded but failed to obtain ID.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add nutrition goal for user ID: " + goal.getUserId(), e);
        }
    }

    /**
     * Adds a new nutrition goal using an existing Connection (for transactional support).
     */
    public int addGoal(Connection conn, NutritionGoal goal) throws SQLException, DatabaseOperationException {
        String sql = "INSERT INTO nutrition_goal (user_id, target_calories, target_protein, " +
                     "target_carbs, target_fats, is_active) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, goal.getUserId());
            stmt.setBigDecimal(2, goal.getTargetCalories());
            stmt.setBigDecimal(3, goal.getTargetProtein());
            stmt.setBigDecimal(4, goal.getTargetCarbs());
            stmt.setBigDecimal(5, goal.getTargetFats());
            stmt.setBoolean(6, goal.getIsActive() != null ? goal.getIsActive() : true);

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    goal.setGoalId(id);
                    return id;
                } else {
                    throw new DatabaseOperationException("Goal insertion succeeded but failed to obtain ID.");
                }
            }
        }
    }

    /**
     * Deactivates all currently active nutrition goals for a user using an existing Connection.
     */
    public int deactivateActiveGoals(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE nutrition_goal SET is_active = FALSE WHERE user_id = ? AND is_active = TRUE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate();
        }
    }

    /**
     * Deactivates all currently active nutrition goals for a user.
     */
    public int deactivateActiveGoals(int userId) throws DatabaseOperationException {
        try (Connection conn = DBConnectionFactory.getConnection()) {
            return deactivateActiveGoals(conn, userId);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to deactivate active goals for user ID: " + userId, e);
        }
    }

    /**
     * Retrieves a nutrition goal by its primary key goal_id.
     *
     * @param goalId Primary key ID
     * @return NutritionGoal object or null if not found
     * @throws DatabaseOperationException If database operation fails
     */
    public NutritionGoal getGoalById(int goalId) throws DatabaseOperationException {
        String sql = "SELECT goal_id, user_id, target_calories, target_protein, target_carbs, " +
                     "target_fats, is_active, created_at FROM nutrition_goal WHERE goal_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, goalId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get goal by ID: " + goalId, e);
        }
    }

    /**
     * Retrieves all nutrition goals for a given user (historical and active).
     *
     * @param userId User ID
     * @return ArrayList of NutritionGoal objects
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<NutritionGoal> getGoalsByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT goal_id, user_id, target_calories, target_protein, target_carbs, " +
                     "target_fats, is_active, created_at FROM nutrition_goal WHERE user_id = ? ORDER BY created_at DESC";

        ArrayList<NutritionGoal> list = new ArrayList<NutritionGoal>();

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get goals for user ID: " + userId, e);
        }
    }

    /**
     * Retrieves the currently active goal for a user.
     *
     * @param userId User ID
     * @return Active NutritionGoal or null if none
     * @throws DatabaseOperationException If database operation fails
     */
    public NutritionGoal getActiveGoalByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT goal_id, user_id, target_calories, target_protein, target_carbs, " +
                     "target_fats, is_active, created_at FROM nutrition_goal WHERE user_id = ? AND is_active = TRUE LIMIT 1";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get active goal for user ID: " + userId, e);
        }
    }

    /**
     * Updates an existing nutrition goal.
     *
     * @param goal NutritionGoal with updated values
     * @return true if updated, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean updateGoal(NutritionGoal goal) throws DatabaseOperationException {
        String sql = "UPDATE nutrition_goal SET target_calories = ?, target_protein = ?, " +
                     "target_carbs = ?, target_fats = ?, is_active = ? WHERE goal_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
        }
    }

    /**
     * Deletes a nutrition goal by ID.
     *
     * @param goalId Primary key ID
     * @return true if deleted, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean deleteGoal(int goalId) throws DatabaseOperationException {
        String sql = "DELETE FROM nutrition_goal WHERE goal_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, goalId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete goal ID: " + goalId, e);
        }
    }

    private NutritionGoal mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = (timestamp != null) ? timestamp.toLocalDateTime() : null;

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
}
