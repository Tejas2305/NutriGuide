package com.nutriguide.dao;

import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.Food;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * FoodDAO: Handles JDBC database operations for the 'food' relation.
 *
 * Demonstrates:
 * - PreparedStatement for parameterized queries and SQL injection prevention
 * - executeQuery() and ResultSet iteration
 * - executeUpdate() for DML operations
 * - Returning ArrayList<Food>
 */
public class FoodDAO {

    /**
     * Adds a new food item and returns the generated food_id.
     *
     * @param food Food entity to insert
     * @return Generated food_id
     * @throws DatabaseOperationException If database operation fails
     */
    public int addFood(Food food) throws DatabaseOperationException {
        String sql = "INSERT INTO food (category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, food.getCategoryId());
            stmt.setString(2, food.getFoodName());
            stmt.setBigDecimal(3, food.getServingSize());
            stmt.setBigDecimal(4, food.getCalories());
            stmt.setBigDecimal(5, food.getProtein());
            stmt.setBigDecimal(6, food.getCarbohydrates());
            stmt.setBigDecimal(7, food.getFat());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    food.setFoodId(id);
                    return id;
                } else {
                    throw new DatabaseOperationException("Food insertion succeeded but failed to obtain ID.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add food: " + food.getFoodName(), e);
        }
    }

    /**
     * Retrieves a food item by its primary key food_id.
     *
     * @param foodId Primary key ID
     * @return Food object or null if not found
     * @throws DatabaseOperationException If database operation fails
     */
    public Food getFoodById(int foodId) throws DatabaseOperationException {
        String sql = "SELECT food_id, category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat FROM food WHERE food_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, foodId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get food by ID: " + foodId, e);
        }
    }

    /**
     * Retrieves a food item by its exact unique food name.
     *
     * @param foodName Unique name of food item
     * @return Food object or null if not found
     * @throws DatabaseOperationException If database operation fails
     */
    public Food getFoodByName(String foodName) throws DatabaseOperationException {
        String sql = "SELECT food_id, category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat FROM food WHERE food_name = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, foodName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get food by name: " + foodName, e);
        }
    }

    /**
     * Retrieves all food items in the database.
     *
     * @return ArrayList of Food objects
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<Food> getAllFoods() throws DatabaseOperationException {
        String sql = "SELECT food_id, category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat FROM food ORDER BY food_name";

        ArrayList<Food> list = new ArrayList<Food>();

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get all foods", e);
        }
    }

    /**
     * Searches for food items where food_name contains the given keyword (case-insensitive substring match).
     *
     * @param name Keyword to search for
     * @return ArrayList of matching Food items
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<Food> searchFoodByName(String name) throws DatabaseOperationException {
        String sql = "SELECT food_id, category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat FROM food WHERE food_name LIKE ? ORDER BY food_name";

        ArrayList<Food> list = new ArrayList<Food>();

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to search food with term: " + name, e);
        }
    }

    /**
     * Retrieves food items belonging to a specific category.
     *
     * @param categoryId Category ID
     * @return ArrayList of Food items
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<Food> getFoodsByCategoryId(int categoryId) throws DatabaseOperationException {
        String sql = "SELECT food_id, category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat FROM food WHERE category_id = ? ORDER BY food_name";

        ArrayList<Food> list = new ArrayList<Food>();

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get foods by category: " + categoryId, e);
        }
    }

    /**
     * Updates an existing food item.
     *
     * @param food Food item with updated values
     * @return true if updated, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean updateFood(Food food) throws DatabaseOperationException {
        String sql = "UPDATE food SET category_id = ?, food_name = ?, serving_size = ?, " +
                     "calories = ?, protein = ?, carbohydrates = ?, fat = ? WHERE food_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, food.getCategoryId());
            stmt.setString(2, food.getFoodName());
            stmt.setBigDecimal(3, food.getServingSize());
            stmt.setBigDecimal(4, food.getCalories());
            stmt.setBigDecimal(5, food.getProtein());
            stmt.setBigDecimal(6, food.getCarbohydrates());
            stmt.setBigDecimal(7, food.getFat());
            stmt.setInt(8, food.getFoodId());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update food ID: " + food.getFoodId(), e);
        }
    }

    /**
     * Deletes a food item by its ID.
     *
     * @param foodId Primary key of food to delete
     * @return true if deleted, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean deleteFood(int foodId) throws DatabaseOperationException {
        String sql = "DELETE FROM food WHERE food_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, foodId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete food ID: " + foodId, e);
        }
    }

    private Food mapRow(ResultSet rs) throws SQLException {
        return new Food(
                rs.getInt("food_id"),
                rs.getInt("category_id"),
                rs.getString("food_name"),
                rs.getBigDecimal("serving_size"),
                rs.getBigDecimal("calories"),
                rs.getBigDecimal("protein"),
                rs.getBigDecimal("carbohydrates"),
                rs.getBigDecimal("fat")
        );
    }
}
