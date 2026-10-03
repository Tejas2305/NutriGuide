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
 * RecommendationDAO: Handles JDBC database operations for the 'recommendation' relation.
 * Does not generate recommendations — only stores and queries them.
 */
public class RecommendationDAO {

    /**
     * Inserts a new recommendation record and returns the generated rec_id.
     *
     * @param recommendation Recommendation entity
     * @return Generated rec_id
     * @throws DatabaseOperationException If database operation fails
     */
    public int addRecommendation(Recommendation recommendation) throws DatabaseOperationException {
        String sql = "INSERT INTO recommendation (user_id, message, rec_type) VALUES (?, ?, ?)";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, recommendation.getUserId());
            stmt.setString(2, recommendation.getMessage());
            stmt.setString(3, recommendation.getRecType() != null ? recommendation.getRecType() : "GENERAL");

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    recommendation.setRecId(id);
                    return id;
                } else {
                    throw new DatabaseOperationException("Recommendation insertion succeeded but failed to obtain ID.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add recommendation for user ID: " + recommendation.getUserId(), e);
        }
    }

    /**
     * Retrieves a recommendation by primary key ID.
     *
     * @param recId Primary key ID
     * @return Recommendation object or null if not found
     * @throws DatabaseOperationException If database operation fails
     */
    public Recommendation getRecommendationById(int recId) throws DatabaseOperationException {
        String sql = "SELECT rec_id, user_id, message, rec_type, generated_at FROM recommendation WHERE rec_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, recId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get recommendation by ID: " + recId, e);
        }
    }

    /**
     * Retrieves all recommendations generated for a user.
     *
     * @param userId User ID
     * @return ArrayList of Recommendation objects
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<Recommendation> getRecommendationsByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT rec_id, user_id, message, rec_type, generated_at " +
                     "FROM recommendation WHERE user_id = ? ORDER BY generated_at DESC";

        ArrayList<Recommendation> list = new ArrayList<Recommendation>();

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
            throw new DatabaseOperationException("Failed to get recommendations for user ID: " + userId, e);
        }
    }

    /**
     * Deletes a recommendation by ID.
     *
     * @param recId Primary key ID
     * @return true if deleted, false otherwise
     * @throws DatabaseOperationException If database operation fails
     */
    public boolean deleteRecommendation(int recId) throws DatabaseOperationException {
        String sql = "DELETE FROM recommendation WHERE rec_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, recId);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete recommendation ID: " + recId, e);
        }
    }

    private Recommendation mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("generated_at");
        LocalDateTime generatedAt = (timestamp != null) ? timestamp.toLocalDateTime() : null;

        return new Recommendation(
                rs.getInt("rec_id"),
                rs.getInt("user_id"),
                rs.getString("message"),
                rs.getString("rec_type"),
                generatedAt
        );
    }
}
