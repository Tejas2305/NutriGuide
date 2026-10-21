package com.nutriguide.dao;

import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.Recommendation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * RecommendationDAO - handles all database operations for the 'recommendation' table.
 * This class only stores and retrieves recommendations - it does not generate them.
 */
public class RecommendationDAO {

    /**
     * Adds a new recommendation record to the database.
     * Returns the auto-generated rec_id.
     */
    public int addRecommendation(Recommendation recommendation) throws DatabaseOperationException {
        String sql = "INSERT INTO recommendation (user_id, message, rec_type) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet keys = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, recommendation.getUserId());
            stmt.setString(2, recommendation.getMessage());
            stmt.setString(3, recommendation.getRecType() != null ? recommendation.getRecType() : "GENERAL");

            stmt.executeUpdate();

            keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                recommendation.setRecId(id);
                return id;
            } else {
                throw new DatabaseOperationException("Recommendation added but could not get generated ID.");
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add recommendation for user: " + recommendation.getUserId(), e);
        } finally {
            closeResultSet(keys);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets a recommendation by its rec_id.
     * Returns null if not found.
     */
    public Recommendation getRecommendationById(int recId) throws DatabaseOperationException {
        String sql = "SELECT rec_id, user_id, message, rec_type, generated_at FROM recommendation WHERE rec_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, recId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get recommendation by ID: " + recId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all recommendations for a specific user, newest first.
     */
    public ArrayList<Recommendation> getRecommendationsByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT rec_id, user_id, message, rec_type, generated_at " +
                     "FROM recommendation WHERE user_id = ? ORDER BY generated_at DESC";

        ArrayList<Recommendation> list = new ArrayList<Recommendation>();
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
            throw new DatabaseOperationException("Failed to get recommendations for user: " + userId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Deletes a recommendation by its rec_id.
     * Returns true if deleted successfully.
     */
    public boolean deleteRecommendation(int recId) throws DatabaseOperationException {
        String sql = "DELETE FROM recommendation WHERE rec_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, recId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete recommendation ID: " + recId, e);
        } finally {
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Maps a database row to a Recommendation object.
     */
    private Recommendation mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("generated_at");
        LocalDateTime generatedAt = null;
        if (timestamp != null) {
            generatedAt = timestamp.toLocalDateTime();
        }

        return new Recommendation(
                rs.getInt("rec_id"),
                rs.getInt("user_id"),
                rs.getString("message"),
                rs.getString("rec_type"),
                generatedAt
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
