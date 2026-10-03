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
 * MealPlanItemDAO: Handles JDBC database operations for the 'meal_plan_item' relation.
 */
public class MealPlanItemDAO {

    /**
     * Inserts a new meal plan item and returns the generated meal_plan_item_id.
     *
     * @param item MealPlanItem entity
     * @return Generated meal_plan_item_id
     * @throws DatabaseOperationException If database operation fails
     */
    public int addMealPlanItem(MealPlanItem item) throws DatabaseOperationException {
        String sql = "INSERT INTO meal_plan_item (meal_plan_id, food_id, planned_meal_type, quantity) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, item.getMealPlanId());
            stmt.setInt(2, item.getFoodId());
            stmt.setString(3, item.getPlannedMealType());
            stmt.setBigDecimal(4, item.getQuantity());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    item.setMealPlanItemId(id);
                    return id;
                } else {
                    throw new DatabaseOperationException("MealPlanItem insertion succeeded but failed to obtain ID.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add meal plan item to plan ID: " + item.getMealPlanId(), e);
        }
    }

    /**
     * Retrieves a meal plan item by its primary key ID.
     *
     * @param mealPlanItemId Primary key ID
     * @return MealPlanItem object or null if not found
     * @throws DatabaseOperationException If database operation fails
     */
    public MealPlanItem getMealPlanItemById(int mealPlanItemId) throws DatabaseOperationException {
        String sql = "SELECT meal_plan_item_id, meal_plan_id, food_id, planned_meal_type, quantity " +
                     "FROM meal_plan_item WHERE meal_plan_item_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mealPlanItemId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get meal plan item by ID: " + mealPlanItemId, e);
        }
    }

    /**
     * Retrieves all items scheduled for a meal plan.
     *
     * @param mealPlanId Meal plan ID
     * @return ArrayList of MealPlanItem objects
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<MealPlanItem> getItemsByMealPlanId(int mealPlanId) throws DatabaseOperationException {
        String sql = "SELECT meal_plan_item_id, meal_plan_id, food_id, planned_meal_type, quantity " +
                     "FROM meal_plan_item WHERE meal_plan_id = ? ORDER BY meal_plan_item_id";

        ArrayList<MealPlanItem> list = new ArrayList<MealPlanItem>();

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mealPlanId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get items for meal plan ID: " + mealPlanId, e);
        }
    }

    /**
     * Updates an existing meal plan item.
     *
     * @param item MealPlanItem entity with updated fields
     * @return true if updated, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean updateMealPlanItem(MealPlanItem item) throws DatabaseOperationException {
        String sql = "UPDATE meal_plan_item SET meal_plan_id = ?, food_id = ?, planned_meal_type = ?, " +
                     "quantity = ? WHERE meal_plan_item_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item.getMealPlanId());
            stmt.setInt(2, item.getFoodId());
            stmt.setString(3, item.getPlannedMealType());
            stmt.setBigDecimal(4, item.getQuantity());
            stmt.setInt(5, item.getMealPlanItemId());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update meal plan item ID: " + item.getMealPlanItemId(), e);
        }
    }

    /**
     * Deletes a meal plan item by ID.
     *
     * @param mealPlanItemId Primary key ID
     * @return true if deleted, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean deleteMealPlanItem(int mealPlanItemId) throws DatabaseOperationException {
        String sql = "DELETE FROM meal_plan_item WHERE meal_plan_item_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mealPlanItemId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete meal plan item ID: " + mealPlanItemId, e);
        }
    }

    private MealPlanItem mapRow(ResultSet rs) throws SQLException {
        return new MealPlanItem(
                rs.getInt("meal_plan_item_id"),
                rs.getInt("meal_plan_id"),
                rs.getInt("food_id"),
                rs.getString("planned_meal_type"),
                rs.getBigDecimal("quantity")
        );
    }
}
