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
 * MealDAO: Handles JDBC database operations for the 'meal' relation.
 *
 * Demonstrates:
 * - Date and Timestamp handling with java.sql.Date / java.sql.Timestamp
 * - PreparedStatement query execution
 * - ResultSet navigation
 * - CallableStatement stored procedure calls (DBMS Exp 7)
 * - JDBC Transaction support via shared Connection parameter
 */
public class MealDAO {

    /**
     * Inserts a new meal using an existing database connection (for transactions).
     *
     * @param conn Active Connection (may participate in transaction)
     * @param meal Meal entity to insert
     * @return Generated meal_id
     * @throws SQLException If database operation fails
     */
    public int addMeal(Connection conn, Meal meal) throws SQLException {
        String sql = "INSERT INTO meal (user_id, meal_type, meal_date) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, meal.getUserId());
            stmt.setString(2, meal.getMealType());
            stmt.setDate(3, Date.valueOf(meal.getMealDate()));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    meal.setMealId(id);
                    return id;
                } else {
                    throw new SQLException("Meal insertion succeeded but failed to obtain ID.");
                }
            }
        }
    }

    /**
     * Inserts a new meal record and returns the generated meal_id.
     *
     * @param meal Meal entity to insert
     * @return Generated meal_id
     * @throws DatabaseOperationException If database operation fails
     */
    public int addMeal(Meal meal) throws DatabaseOperationException {
        try (Connection conn = DBConnectionFactory.getConnection()) {
            return addMeal(conn, meal);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add meal for user ID: " + meal.getUserId(), e);
        }
    }

    /**
     * Retrieves a meal by its primary key meal_id.
     *
     * @param mealId Primary key ID
     * @return Meal object or null if not found
     * @throws DatabaseOperationException If database operation fails
     */
    public Meal getMealById(int mealId) throws DatabaseOperationException {
        String sql = "SELECT meal_id, user_id, meal_type, meal_date, created_at FROM meal WHERE meal_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mealId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get meal by ID: " + mealId, e);
        }
    }

    /**
     * Retrieves all meals logged by a user.
     *
     * @param userId User ID
     * @return ArrayList of Meal objects
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<Meal> getMealsByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT meal_id, user_id, meal_type, meal_date, created_at " +
                     "FROM meal WHERE user_id = ? ORDER BY meal_date DESC, created_at DESC";

        ArrayList<Meal> list = new ArrayList<Meal>();

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
            throw new DatabaseOperationException("Failed to get meals for user ID: " + userId, e);
        }
    }

    /**
     * Retrieves all meals logged by a user for a specific date.
     *
     * @param userId User ID
     * @param date Date of the meals
     * @return ArrayList of Meal objects
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<Meal> getMealsByUserIdAndDate(int userId, LocalDate date) throws DatabaseOperationException {
        String sql = "SELECT meal_id, user_id, meal_type, meal_date, created_at " +
                     "FROM meal WHERE user_id = ? AND meal_date = ? ORDER BY created_at ASC";

        ArrayList<Meal> list = new ArrayList<Meal>();

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get meals for user ID: " + userId + " on date: " + date, e);
        }
    }

    /**
     * Updates an existing meal record.
     *
     * @param meal Meal entity with updated fields
     * @return true if updated, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean updateMeal(Meal meal) throws DatabaseOperationException {
        String sql = "UPDATE meal SET meal_type = ?, meal_date = ? WHERE meal_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, meal.getMealType());
            stmt.setDate(2, Date.valueOf(meal.getMealDate()));
            stmt.setInt(3, meal.getMealId());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update meal ID: " + meal.getMealId(), e);
        }
    }

    /**
     * Deletes a meal by ID (cascades to meal items in database).
     *
     * @param mealId Primary key ID
     * @return true if deleted, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean deleteMeal(int mealId) throws DatabaseOperationException {
        String sql = "DELETE FROM meal WHERE meal_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mealId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete meal ID: " + mealId, e);
        }
    }

    private Meal mapRow(ResultSet rs) throws SQLException {
        Date date = rs.getDate("meal_date");
        LocalDate mealDate = (date != null) ? date.toLocalDate() : null;

        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = (timestamp != null) ? timestamp.toLocalDateTime() : null;

        return new Meal(
                rs.getInt("meal_id"),
                rs.getInt("user_id"),
                rs.getString("meal_type"),
                mealDate,
                createdAt
        );
    }

    /**
     * Calls the MySQL stored procedure 'get_daily_summary' using CallableStatement (DBMS Exp 7).
     * Uses IN parameters for user_id and date, and OUT parameters for macro totals.
     *
     * @param userId User ID
     * @param date Date to summarize
     * @return DailyNutritionSummary containing aggregated totals
     * @throws DatabaseOperationException If stored procedure call fails
     */
    public DailyNutritionSummary getDailySummaryProcedure(int userId, LocalDate date)
            throws DatabaseOperationException {
        String sql = "{CALL get_daily_summary(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBConnectionFactory.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {

            // Set IN parameters
            cstmt.setInt(1, userId);
            cstmt.setDate(2, Date.valueOf(date));

            // Register OUT parameters
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
            throw new DatabaseOperationException("Failed to call get_daily_summary procedure for user ID: " + userId, e);
        }
    }

    /**
     * Queries the MySQL database view 'daily_nutrition_summary' (DBMS Exp 4).
     *
     * @param userId User ID
     * @param date Date to query
     * @return DailyNutritionSummary from view, or an empty summary if no meals logged
     * @throws DatabaseOperationException If view query fails
     */
    public DailyNutritionSummary getDailySummaryFromView(int userId, LocalDate date)
            throws DatabaseOperationException {
        String sql = "SELECT user_id, meal_date, daily_calories, daily_protein, daily_carbs, daily_fat " +
                     "FROM daily_nutrition_summary WHERE user_id = ? AND meal_date = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
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
                // Return empty summary with 0 values
                return new DailyNutritionSummary(userId, date, null, null, null, null);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query daily_nutrition_summary view for user ID: " + userId, e);
        }
    }

    /**
     * Queries the MySQL database view 'daily_nutrition_summary' for a user across a date range (DBMS Exp 4).
     *
     * @param userId User ID
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @return ArrayList of DailyNutritionSummary
     * @throws DatabaseOperationException If view query fails
     */
    public ArrayList<DailyNutritionSummary> getNutritionHistoryFromView(int userId, LocalDate fromDate, LocalDate toDate)
            throws DatabaseOperationException {
        String sql = "SELECT user_id, meal_date, daily_calories, daily_protein, daily_carbs, daily_fat " +
                     "FROM daily_nutrition_summary WHERE user_id = ? AND meal_date >= ? AND meal_date <= ? " +
                     "ORDER BY meal_date DESC";

        ArrayList<DailyNutritionSummary> list = new ArrayList<DailyNutritionSummary>();

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(fromDate));
            stmt.setDate(3, Date.valueOf(toDate));

            try (ResultSet rs = stmt.executeQuery()) {
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
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query nutrition history for user ID: " + userId, e);
        }
    }

    /**
     * Queries the MySQL database view 'daily_nutrition_summary' for all records of a user (DBMS Exp 4).
     *
     * @param userId User ID
     * @return ArrayList of DailyNutritionSummary
     * @throws DatabaseOperationException If view query fails
     */
    public ArrayList<DailyNutritionSummary> getAllNutritionHistoryFromView(int userId)
            throws DatabaseOperationException {
        String sql = "SELECT user_id, meal_date, daily_calories, daily_protein, daily_carbs, daily_fat " +
                     "FROM daily_nutrition_summary WHERE user_id = ? " +
                     "ORDER BY meal_date DESC";

        ArrayList<DailyNutritionSummary> list = new ArrayList<DailyNutritionSummary>();

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
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
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to query all nutrition history for user ID: " + userId, e);
        }
    }
}
