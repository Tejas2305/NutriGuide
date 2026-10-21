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
 * UserDAO - handles all database operations for the 'user_profile' table.
 * DAO stands for Data Access Object - it separates database code from the rest of the app.
 */
public class UserDAO {

    /**
     * Adds a new user to the database.
     * Returns the auto-generated user_id created by MySQL.
     */
    public int addUser(User user) throws DatabaseOperationException {
        String sql = "INSERT INTO user_profile (name, email, password_hash, age, gender, " +
                     "height_cm, weight_kg, activity_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet keys = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setInt(4, user.getAge());
            stmt.setString(5, user.getGender());
            stmt.setBigDecimal(6, user.getHeightCm());
            stmt.setBigDecimal(7, user.getWeightKg());
            stmt.setString(8, user.getActivityLevel());

            stmt.executeUpdate();

            keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int generatedId = keys.getInt(1);
                user.setUserId(generatedId);
                return generatedId;
            } else {
                throw new DatabaseOperationException("User added but could not get generated ID.");
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add user: " + user.getEmail(), e);
        } finally {
            // Always close resources in finally block
            closeResultSet(keys);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Finds a user by their user_id (primary key).
     * Returns null if no user is found with that ID.
     */
    public User getUserById(int userId) throws DatabaseOperationException {
        String sql = "SELECT user_id, name, email, password_hash, age, gender, " +
                     "height_cm, weight_kg, activity_level, created_at FROM user_profile WHERE user_id = ?";

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
            return null; // No user found

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get user by ID: " + userId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Finds a user by their email address.
     * Returns null if no user is found with that email.
     */
    public User getUserByEmail(String email) throws DatabaseOperationException {
        String sql = "SELECT user_id, name, email, password_hash, age, gender, " +
                     "height_cm, weight_kg, activity_level, created_at FROM user_profile WHERE email = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null; // No user found

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get user by email: " + email, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Updates an existing user's profile details.
     * Returns true if the update was successful.
     */
    public boolean updateUser(User user) throws DatabaseOperationException {
        String sql = "UPDATE user_profile SET name = ?, email = ?, password_hash = ?, age = ?, " +
                     "gender = ?, height_cm = ?, weight_kg = ?, activity_level = ? WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);

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
            throw new DatabaseOperationException("Failed to update user ID: " + user.getUserId(), e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Deletes a user from the database by their user_id.
     * Returns true if a row was deleted.
     */
    public boolean deleteUser(int userId) throws DatabaseOperationException {
        String sql = "DELETE FROM user_profile WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete user ID: " + userId, e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all users from the database.
     * Returns an ArrayList (list) of User objects.
     */
    public ArrayList<User> getAllUsers() throws DatabaseOperationException {
        String sql = "SELECT user_id, name, email, password_hash, age, gender, " +
                     "height_cm, weight_kg, activity_level, created_at FROM user_profile ORDER BY user_id";

        ArrayList<User> userList = new ArrayList<User>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                userList.add(mapRow(rs));
            }
            return userList;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get all users", e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Calls the MySQL stored function 'calculate_bmi' to compute BMI.
     * The function is defined in the database and does the calculation.
     */
    public BigDecimal calculateBmi(BigDecimal weightKg, BigDecimal heightCm) throws DatabaseOperationException {
        String sql = "SELECT calculate_bmi(?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setBigDecimal(1, weightKg);
            stmt.setBigDecimal(2, heightCm);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to calculate BMI", e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Maps a row from the ResultSet to a User object.
     * This is called each time we read a user from the database.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = null;
        if (timestamp != null) {
            createdAt = timestamp.toLocalDateTime();
        }

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
