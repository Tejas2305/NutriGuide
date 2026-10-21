package com.nutriguide.dao;

import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.Meal;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * MealDAO - handles all database operations for the 'meal' table.
 * Also provides methods to query nutrition summaries using stored procedures and views.
 */
public class MealDAO {

    /**
     * Adds a meal using an existing connection.
     * This version is used when we need transactions (commit/rollback).
     * Returns the auto-generated meal_id.
     */
    public int addMeal(Connection conn, Meal meal) throws SQLException {
        String sql = "INSERT INTO meal (user_id, meal_type, meal_date) VALUES (?, ?, ?)";

        PreparedStatement stmt = null;
        ResultSet keys = null;

        try {
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, meal.getUserId());
            stmt.setString(2, meal.getMealType());
            stmt.setDate(3, Date.valueOf(meal.getMealDate()));

            stmt.executeUpdate();

            keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                meal.setMealId(id);
                return id;
            } else {
                throw new SQLException("Meal added but could not get generated ID.");
            }

        } finally {
            // Close statement and keys but NOT the connection (caller manages it)
            if (keys != null) { try { keys.close(); } catch (SQLException e) { /* ignore */ } }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignore */ } }
        }
    }

    /**
     * Adds a meal and handles its own connection.
     * Use this when you don't need transaction support.
     */
    public int addMeal(Meal meal) throws DatabaseOperationException {
        Connection conn = null;
        try {
            conn = DBConnectionFactory.getConnection();
            return addMeal(conn, meal);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add meal for user: " + meal.getUserId(), e);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Gets a single meal by its meal_id.
     * Returns null if not found.
     */
    public Meal getMealById(int mealId) throws DatabaseOperationException {
        String sql = "SELECT meal_id, user_id, meal_type, meal_date, created_at FROM meal WHERE meal_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mealId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get meal by ID: " + mealId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all meals logged by a specific user.
     */
    public ArrayList<Meal> getMealsByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT meal_id, user_id, meal_type, meal_date, created_at " +
                     "FROM meal WHERE user_id = ? ORDER BY meal_date DESC, created_at DESC";

        ArrayList<Meal> list = new ArrayList<Meal>();
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
            throw new DatabaseOperationException("Failed to get meals for user: " + userId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all meals for a user on a specific date.
     */
    public ArrayList<Meal> getMealsByUserIdAndDate(int userId, LocalDate date) throws DatabaseOperationException {
        String sql = "SELECT meal_id, user_id, meal_type, meal_date, created_at " +
                     "FROM meal WHERE user_id = ? AND meal_date = ? ORDER BY created_at ASC";

        ArrayList<Meal> list = new ArrayList<Meal>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));

            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get meals for user " + userId + " on date " + date, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Updates an existing meal's type and date.
     * Returns true if updated successfully.
     */
    public boolean updateMeal(Meal meal) throws DatabaseOperationException {
        String sql = "UPDATE meal SET meal_type = ?, meal_date = ? WHERE meal_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, meal.getMealType());
            stmt.setDate(2, Date.valueOf(meal.getMealDate()));
            stmt.setInt(3, meal.getMealId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update meal ID: " + meal.getMealId(), e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Deletes a meal by its meal_id.
     * Returns true if deleted successfully.
     */
    public boolean deleteMeal(int mealId) throws DatabaseOperationException {
        String sql = "DELETE FROM meal WHERE meal_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mealId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete meal ID: " + mealId, e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Calls the MySQL stored procedure 'get_daily_summary' to get nutrition totals for a day.
     * Uses CallableStatement to call a stored procedure with IN and OUT parameters.
     */
    public DailyNutritionSummary getDailySummaryProcedure(int userId, LocalDate date)
            throws DatabaseOperationException {
        String sql = "{CALL get_daily_summary(?, ?, ?, ?, ?, ?)}";

        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            cstmt = conn.prepareCall(sql);

            // Set input parameters
            cstmt.setInt(1, userId);
            cstmt.setDate(2, Date.valueOf(date));

            // Register output parameters (the procedure will fill these)
            cstmt.registerOutParameter(3, Types.DECIMAL);
            cstmt.registerOutParameter(4, Types.DECIMAL);
            cstmt.registerOutParameter(5, Types.DECIMAL);
            cstmt.registerOutParameter(6, Types.DECIMAL);

            cstmt.execute();

            return new DailyNutritionSummary(
                    userId,
                    date,
                    cstmt.getBigDecimal(3),
                    cstmt.getBigDecimal(4),
                    cstmt.getBigDecimal(5),
                    cstmt.getBigDecimal(6)
            );

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to call get_daily_summary procedure for user: " + userId, e);
        } finally {
            if (cstmt != null) { try { cstmt.close(); } catch (SQLException e) { /* ignore */ } }
            closeConnection(conn);
        }
    }

    /**
     * Queries the 'daily_nutrition_summary' database VIEW for a specific user and date.
     * A VIEW is a saved SELECT query in the database.
     */
    public DailyNutritionSummary getDailySummaryFromView(int userId, LocalDate date)
            throws DatabaseOperationException {
        String sql = "SELECT user_id, meal_date, daily_calories, daily_protein, daily_carbs, daily_fat " +
                     "FROM daily_nutrition_summary WHERE user_id = ? AND meal_date = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));

            rs = stmt.executeQuery();
            if (rs.next()) {
                return new DailyNutritionSummary(
                        rs.getInt("user_id"),
                        rs.getDate("meal_date").toLocalDate(),
                        rs.getBigDecimal("daily_calories"),
                        rs.getBigDecimal("daily_protein"),
                        rs.getBigDecimal("daily_carbs"),
                        rs.getBigDecimal("daily_fat")
                );
            }
            // Return empty summary (no meals logged for this date)
            return new DailyNutritionSummary(userId, date, null, null, null, null);

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query daily_nutrition_summary view for user: " + userId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets nutrition history from the view for a user within a date range.
     */
    public ArrayList<DailyNutritionSummary> getNutritionHistoryFromView(int userId, LocalDate fromDate, LocalDate toDate)
            throws DatabaseOperationException {
        String sql = "SELECT user_id, meal_date, daily_calories, daily_protein, daily_carbs, daily_fat " +
                     "FROM daily_nutrition_summary WHERE user_id = ? AND meal_date >= ? AND meal_date <= ? " +
                     "ORDER BY meal_date DESC";

        ArrayList<DailyNutritionSummary> list = new ArrayList<DailyNutritionSummary>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(fromDate));
            stmt.setDate(3, Date.valueOf(toDate));

            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new DailyNutritionSummary(
                        rs.getInt("user_id"),
                        rs.getDate("meal_date").toLocalDate(),
                        rs.getBigDecimal("daily_calories"),
                        rs.getBigDecimal("daily_protein"),
                        rs.getBigDecimal("daily_carbs"),
                        rs.getBigDecimal("daily_fat")
                ));
            }
            return list;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query nutrition history for user: " + userId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all nutrition history from the view for a user.
     */
    public ArrayList<DailyNutritionSummary> getAllNutritionHistoryFromView(int userId)
            throws DatabaseOperationException {
        String sql = "SELECT user_id, meal_date, daily_calories, daily_protein, daily_carbs, daily_fat " +
                     "FROM daily_nutrition_summary WHERE user_id = ? ORDER BY meal_date DESC";

        ArrayList<DailyNutritionSummary> list = new ArrayList<DailyNutritionSummary>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new DailyNutritionSummary(
                        rs.getInt("user_id"),
                        rs.getDate("meal_date").toLocalDate(),
                        rs.getBigDecimal("daily_calories"),
                        rs.getBigDecimal("daily_protein"),
                        rs.getBigDecimal("daily_carbs"),
                        rs.getBigDecimal("daily_fat")
                ));
            }
            return list;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query all nutrition history for user: " + userId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Maps a database row to a Meal object.
     */
    private Meal mapRow(ResultSet rs) throws SQLException {
        Date date = rs.getDate("meal_date");
        LocalDate mealDate = null;
        if (date != null) {
            mealDate = date.toLocalDate();
        }

        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = null;
        if (timestamp != null) {
            createdAt = timestamp.toLocalDateTime();
        }

        return new Meal(
                rs.getInt("meal_id"),
                rs.getInt("user_id"),
                rs.getString("meal_type"),
                mealDate,
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
