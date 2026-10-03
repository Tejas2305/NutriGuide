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
 * MealItemDAO: Handles JDBC database operations for the 'meal_item' relation.
 */
public class MealItemDAO {

    /**
     * Inserts a new meal item using an existing database connection (for transactions).
     *
     * @param conn Active Connection (participating in transaction)
     * @param item MealItem to insert
     * @return Generated meal_item_id
     * @throws SQLException If database operation fails
     */
    public int addMealItem(Connection conn, MealItem item) throws SQLException {
        String sql = "INSERT INTO meal_item (meal_id, food_id, quantity) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.getMealId());
            stmt.setInt(2, item.getFoodId());
            stmt.setBigDecimal(3, item.getQuantity());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    item.setMealItemId(id);
                    return id;
                } else {
                    throw new SQLException("MealItem insertion succeeded but failed to obtain ID.");
                }
            }
        }
    }

    /**
     * Inserts a new meal item and returns the generated meal_item_id.
     *
     * @param item MealItem to insert
     * @return Generated meal_item_id
     * @throws DatabaseOperationException If database operation fails
     */
    public int addMealItem(MealItem item) throws DatabaseOperationException {
        try (Connection conn = DBConnectionFactory.getConnection()) {
            return addMealItem(conn, item);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add meal item to meal ID: " + item.getMealId(), e);
        }
    }

    /**
     * Retrieves a meal item by its primary key meal_item_id.
     *
     * @param mealItemId Primary key ID
     * @return MealItem object or null if not found
     * @throws DatabaseOperationException If database operation fails
     */
    public MealItem getMealItemById(int mealItemId) throws DatabaseOperationException {
        String sql = "SELECT meal_item_id, meal_id, food_id, quantity FROM meal_item WHERE meal_item_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mealItemId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get meal item by ID: " + mealItemId, e);
        }
    }

    /**
     * Retrieves all items belonging to a meal.
     *
     * @param mealId Meal ID
     * @return ArrayList of MealItem objects
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<MealItem> getItemsByMealId(int mealId) throws DatabaseOperationException {
        String sql = "SELECT meal_item_id, meal_id, food_id, quantity FROM meal_item WHERE meal_id = ? ORDER BY meal_item_id";

        ArrayList<MealItem> list = new ArrayList<MealItem>();

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mealId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get items for meal ID: " + mealId, e);
        }
    }

    /**
     * Updates an existing meal item quantity.
     *
     * @param item MealItem entity with updated quantity
     * @return true if updated, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean updateMealItem(MealItem item) throws DatabaseOperationException {
        String sql = "UPDATE meal_item SET meal_id = ?, food_id = ?, quantity = ? WHERE meal_item_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item.getMealId());
            stmt.setInt(2, item.getFoodId());
            stmt.setBigDecimal(3, item.getQuantity());
            stmt.setInt(4, item.getMealItemId());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update meal item ID: " + item.getMealItemId(), e);
        }
    }

    /**
     * Deletes a specific meal item by ID.
     *
     * @param mealItemId Primary key ID
     * @return true if deleted, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean deleteMealItem(int mealItemId) throws DatabaseOperationException {
        String sql = "DELETE FROM meal_item WHERE meal_item_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mealItemId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete meal item ID: " + mealItemId, e);
        }
    }

    /**
     * Deletes all items for a given meal.
     *
     * @param mealId Meal ID
     * @return true if items were deleted, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean deleteItemsByMealId(int mealId) throws DatabaseOperationException {
        String sql = "DELETE FROM meal_item WHERE meal_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mealId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete items for meal ID: " + mealId, e);
        }
    }

    private MealItem mapRow(ResultSet rs) throws SQLException {
        return new MealItem(
                rs.getInt("meal_item_id"),
                rs.getInt("meal_id"),
                rs.getInt("food_id"),
                rs.getBigDecimal("quantity")
        );
    }
}
