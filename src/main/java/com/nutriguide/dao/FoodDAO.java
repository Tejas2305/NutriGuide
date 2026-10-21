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
 * FoodDAO - handles all database operations for the 'food' table.
 */
public class FoodDAO {

    /**
     * Adds a new food item to the database.
     * Returns the auto-generated food_id.
     */
    public int addFood(Food food) throws DatabaseOperationException {
        String sql = "INSERT INTO food (category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet keys = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, food.getCategoryId());
            stmt.setString(2, food.getFoodName());
            stmt.setBigDecimal(3, food.getServingSize());
            stmt.setBigDecimal(4, food.getCalories());
            stmt.setBigDecimal(5, food.getProtein());
            stmt.setBigDecimal(6, food.getCarbohydrates());
            stmt.setBigDecimal(7, food.getFat());

            stmt.executeUpdate();

            keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                food.setFoodId(id);
                return id;
            } else {
                throw new DatabaseOperationException("Food added but could not get generated ID.");
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add food: " + food.getFoodName(), e);
        } finally {
            closeResultSet(keys);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets a single food item by its food_id.
     * Returns null if not found.
     */
    public Food getFoodById(int foodId) throws DatabaseOperationException {
        String sql = "SELECT food_id, category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat FROM food WHERE food_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, foodId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get food by ID: " + foodId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets a food item by its exact name.
     * Returns null if not found.
     */
    public Food getFoodByName(String foodName) throws DatabaseOperationException {
        String sql = "SELECT food_id, category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat FROM food WHERE food_name = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, foodName);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get food by name: " + foodName, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all food items from the database, sorted by name.
     */
    public ArrayList<Food> getAllFoods() throws DatabaseOperationException {
        String sql = "SELECT food_id, category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat FROM food ORDER BY food_name";

        ArrayList<Food> list = new ArrayList<Food>();
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
            throw new DatabaseOperationException("Failed to get all foods", e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Searches for food items whose name contains the given keyword.
     * Uses SQL LIKE for partial matching (e.g., "chick" matches "Chicken").
     */
    public ArrayList<Food> searchFoodByName(String name) throws DatabaseOperationException {
        String sql = "SELECT food_id, category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat FROM food WHERE food_name LIKE ? ORDER BY food_name";

        ArrayList<Food> list = new ArrayList<Food>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + name + "%"); // % is wildcard in SQL

            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to search food: " + name, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all food items that belong to a specific food category.
     */
    public ArrayList<Food> getFoodsByCategoryId(int categoryId) throws DatabaseOperationException {
        String sql = "SELECT food_id, category_id, food_name, serving_size, calories, protein, " +
                     "carbohydrates, fat FROM food WHERE category_id = ? ORDER BY food_name";

        ArrayList<Food> list = new ArrayList<Food>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, categoryId);

            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get foods by category: " + categoryId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Updates an existing food item.
     * Returns true if updated successfully.
     */
    public boolean updateFood(Food food) throws DatabaseOperationException {
        String sql = "UPDATE food SET category_id = ?, food_name = ?, serving_size = ?, " +
                     "calories = ?, protein = ?, carbohydrates = ?, fat = ? WHERE food_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);

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
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Deletes a food item from the database.
     * Returns true if deleted successfully.
     */
    public boolean deleteFood(int foodId) throws DatabaseOperationException {
        String sql = "DELETE FROM food WHERE food_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, foodId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete food ID: " + foodId, e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Maps a database row (ResultSet) to a Food object.
     */
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
