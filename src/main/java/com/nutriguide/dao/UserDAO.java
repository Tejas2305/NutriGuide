package com.nutriguide.dao;

import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * UserDAO: Handles JDBC database operations for the 'user_profile' relation.
 *
 * Demonstrates Java Lab Manual concepts:
 * - Exp 3: Class methods and encapsulation
 * - Exp 5: ArrayList collection
 * - Exp 14-15: Exception handling with try-catch and throws
 * - Exp 19: JDBC SELECT queries
 * - Exp 20: JDBC INSERT, UPDATE, DELETE statements
 * - Exp 21-22: DriverManager and PreparedStatement with parameterized queries
 * - Exp 23: ResultSet navigation
 */
public class UserDAO {

    /**
     * Inserts a new user into the database and returns the generated user_id.
     *
     * @param user The user entity to persist
     * @return Generated auto-increment user_id
     * @throws DatabaseOperationException If a database access error occurs
     */
    public int addUser(User user) throws DatabaseOperationException {
        String sql = "INSERT INTO user_profile (name, email, password_hash, age, gender, " +
                     "height_cm, weight_kg, activity_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setInt(4, user.getAge());
            stmt.setString(5, user.getGender());
            stmt.setBigDecimal(6, user.getHeightCm());
            stmt.setBigDecimal(7, user.getWeightKg());
            stmt.setString(8, user.getActivityLevel());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    user.setUserId(generatedId);
                    return generatedId;
                } else {
                    throw new DatabaseOperationException("User insertion succeeded but failed to obtain generated ID.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add user with email: " + user.getEmail(), e);
        }
    }

    /**
     * Retrieves a user by their unique primary key ID.
     *
     * @param userId The primary key ID to find
     * @return User object if found, or null if not found
     * @throws DatabaseOperationException If a database access error occurs
     */
    public User getUserById(int userId) throws DatabaseOperationException {
        String sql = "SELECT user_id, name, email, password_hash, age, gender, " +
                     "height_cm, weight_kg, activity_level, created_at FROM user_profile WHERE user_id = ?";

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
            throw new DatabaseOperationException("Failed to fetch user by ID: " + userId, e);
        }
    }

    /**
     * Retrieves a user by their unique email address.
     *
     * @param email The email address to look up
     * @return User object if found, or null if not found
     * @throws DatabaseOperationException If a database access error occurs
     */
    public User getUserByEmail(String email) throws DatabaseOperationException {
        String sql = "SELECT user_id, name, email, password_hash, age, gender, " +
                     "height_cm, weight_kg, activity_level, created_at FROM user_profile WHERE email = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch user by email: " + email, e);
        }
    }

    /**
     * Updates an existing user's profile details.
     *
     * @param user The user with updated attributes
     * @return true if a row was updated, false otherwise
     * @throws DatabaseOperationException If a database access error occurs
     */
    public boolean updateUser(User user) throws DatabaseOperationException {
        String sql = "UPDATE user_profile SET name = ?, email = ?, password_hash = ?, age = ?, " +
                     "gender = ?, height_cm = ?, weight_kg = ?, activity_level = ? WHERE user_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setInt(4, user.getAge());
            stmt.setString(5, user.getGender());
            stmt.setBigDecimal(6, user.getHeightCm());
            stmt.setBigDecimal(7, user.getWeightKg());
            stmt.setString(8, user.getActivityLevel());
            stmt.setInt(9, user.getUserId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update user with ID: " + user.getUserId(), e);
        }
    }

    /**
     * Deletes a user by their primary key ID.
     *
     * @param userId The primary key ID of the user to delete
     * @return true if a row was deleted, false otherwise
     * @throws DatabaseOperationException If a database access error occurs
     */
    public boolean deleteUser(int userId) throws DatabaseOperationException {
        String sql = "DELETE FROM user_profile WHERE user_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete user with ID: " + userId, e);
        }
    }

    /**
     * Retrieves all users currently registered in the system.
     *
     * @return ArrayList of User objects
     * @throws DatabaseOperationException If a database access error occurs
     */
    public ArrayList<User> getAllUsers() throws DatabaseOperationException {
        String sql = "SELECT user_id, name, email, password_hash, age, gender, " +
                     "height_cm, weight_kg, activity_level, created_at FROM user_profile ORDER BY user_id";

        ArrayList<User> userList = new ArrayList<User>();

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                userList.add(mapRow(rs));
            }
            return userList;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve all users", e);
        }
    }

    /**
     * Helper method to map the current row of a ResultSet to a User entity.
     *
     * @param rs Active ResultSet positioned at a valid row
     * @return Fully populated User object
     * @throws SQLException If column reading fails
     */
    private User mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = (timestamp != null) ? timestamp.toLocalDateTime() : null;

        return new User(
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getBigDecimal("height_cm"),
                rs.getBigDecimal("weight_kg"),
                rs.getString("activity_level"),
                createdAt
        );
    }

    /**
     * Calls the MySQL stored function 'calculate_bmi' (DBMS Exp 6).
     *
     * @param weightKg Weight in kg
     * @param heightCm Height in cm
     * @return Computed BMI from database function
     * @throws DatabaseOperationException If database operation fails
     */
    public BigDecimal calculateBmi(BigDecimal weightKg, BigDecimal heightCm) throws DatabaseOperationException {
        String sql = "SELECT calculate_bmi(?, ?)";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, weightKg);
            stmt.setBigDecimal(2, heightCm);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
                return BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to calculate BMI via stored function", e);
        }
    }
}
