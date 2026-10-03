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
 * MealPlanDAO: Handles JDBC database operations for the 'meal_plan' relation.
 */
public class MealPlanDAO {

    /**
     * Adds a new meal plan and returns the generated meal_plan_id.
     *
     * @param plan MealPlan entity to insert
     * @return Generated meal_plan_id
     * @throws DatabaseOperationException If database operation fails
     */
    public int addMealPlan(MealPlan plan) throws DatabaseOperationException {
        String sql = "INSERT INTO meal_plan (user_id, plan_name, start_date, end_date, is_active) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, plan.getUserId());
            stmt.setString(2, plan.getPlanName());
            stmt.setDate(3, Date.valueOf(plan.getStartDate()));
            stmt.setDate(4, Date.valueOf(plan.getEndDate()));
            stmt.setBoolean(5, plan.getIsActive() != null ? plan.getIsActive() : true);

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    plan.setMealPlanId(id);
                    return id;
                } else {
                    throw new DatabaseOperationException("MealPlan insertion succeeded but failed to obtain ID.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add meal plan for user ID: " + plan.getUserId(), e);
        }
    }

    /**
     * Retrieves a meal plan by primary key ID.
     *
     * @param mealPlanId Primary key ID
     * @return MealPlan object or null if not found
     * @throws DatabaseOperationException If database operation fails
     */
    public MealPlan getMealPlanById(int mealPlanId) throws DatabaseOperationException {
        String sql = "SELECT meal_plan_id, user_id, plan_name, start_date, end_date, " +
                     "is_active, created_at FROM meal_plan WHERE meal_plan_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mealPlanId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get meal plan by ID: " + mealPlanId, e);
        }
    }

    /**
     * Retrieves all meal plans for a user.
     *
     * @param userId User ID
     * @return ArrayList of MealPlan objects
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<MealPlan> getMealPlansByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT meal_plan_id, user_id, plan_name, start_date, end_date, " +
                     "is_active, created_at FROM meal_plan WHERE user_id = ? ORDER BY start_date DESC";

        ArrayList<MealPlan> list = new ArrayList<MealPlan>();

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
            throw new DatabaseOperationException("Failed to get meal plans for user ID: " + userId, e);
        }
    }

    /**
     * Retrieves the currently active meal plan for a user.
     *
     * @param userId User ID
     * @return Active MealPlan or null if none
     * @throws DatabaseOperationException If database operation fails
     */
    public MealPlan getActiveMealPlanByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT meal_plan_id, user_id, plan_name, start_date, end_date, " +
                     "is_active, created_at FROM meal_plan WHERE user_id = ? AND is_active = TRUE LIMIT 1";

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
            throw new DatabaseOperationException("Failed to get active meal plan for user ID: " + userId, e);
        }
    }

    /**
     * Updates an existing meal plan.
     *
     * @param plan MealPlan entity with updated fields
     * @return true if updated, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean updateMealPlan(MealPlan plan) throws DatabaseOperationException {
        String sql = "UPDATE meal_plan SET plan_name = ?, start_date = ?, end_date = ?, " +
                     "is_active = ? WHERE meal_plan_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, plan.getPlanName());
            stmt.setDate(2, Date.valueOf(plan.getStartDate()));
            stmt.setDate(3, Date.valueOf(plan.getEndDate()));
            stmt.setBoolean(4, plan.getIsActive() != null ? plan.getIsActive() : true);
            stmt.setInt(5, plan.getMealPlanId());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update meal plan ID: " + plan.getMealPlanId(), e);
        }
    }

    /**
     * Deletes a meal plan by ID.
     *
     * @param mealPlanId Primary key ID
     * @return true if deleted, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean deleteMealPlan(int mealPlanId) throws DatabaseOperationException {
        String sql = "DELETE FROM meal_plan WHERE meal_plan_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mealPlanId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete meal plan ID: " + mealPlanId, e);
        }
    }

    private MealPlan mapRow(ResultSet rs) throws SQLException {
        Date startDate = rs.getDate("start_date");
        Date endDate = rs.getDate("end_date");
        Timestamp timestamp = rs.getTimestamp("created_at");

        LocalDate sDate = (startDate != null) ? startDate.toLocalDate() : null;
        LocalDate eDate = (endDate != null) ? endDate.toLocalDate() : null;
        LocalDateTime createdAt = (timestamp != null) ? timestamp.toLocalDateTime() : null;

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
}
