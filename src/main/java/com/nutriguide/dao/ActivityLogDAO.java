package com.nutriguide.dao;

import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * ActivityLogDAO: Handles JDBC database operations for the 'activity_log' audit relation.
 */
public class ActivityLogDAO {

    /**
     * Inserts an activity log record and returns the generated log_id.
     *
     * @param log ActivityLog entity
     * @return Generated log_id
     * @throws DatabaseOperationException If database operation fails
     */
    public int addLog(ActivityLog log) throws DatabaseOperationException {
        String sql = "INSERT INTO activity_log (user_id, action_taken) VALUES (?, ?)";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, log.getUserId());
            stmt.setString(2, log.getActionTaken());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    log.setLogId(id);
                    return id;
                } else {
                    throw new DatabaseOperationException("ActivityLog insertion succeeded but failed to obtain ID.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add activity log for user ID: " + log.getUserId(), e);
        }
    }

    /**
     * Retrieves an activity log entry by primary key ID.
     *
     * @param logId Primary key ID
     * @return ActivityLog object or null if not found
     * @throws DatabaseOperationException If database operation fails
     */
    public ActivityLog getLogById(int logId) throws DatabaseOperationException {
        String sql = "SELECT log_id, user_id, action_taken, log_time FROM activity_log WHERE log_id = ?";

        try (Connection conn = DBConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, logId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get activity log by ID: " + logId, e);
        }
    }

    /**
     * Retrieves all activity log entries for a user, ordered by most recent first.
     *
     * @param userId User ID
     * @return ArrayList of ActivityLog objects
     * @throws DatabaseOperationException If database operation fails
     */
    public ArrayList<ActivityLog> getLogsByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT log_id, user_id, action_taken, log_time " +
                     "FROM activity_log WHERE user_id = ? ORDER BY log_time DESC";

        ArrayList<ActivityLog> list = new ArrayList<ActivityLog>();

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
            throw new DatabaseOperationException("Failed to get activity logs for user ID: " + userId, e);
        }
    }

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("log_time");
        LocalDateTime logTime = (timestamp != null) ? timestamp.toLocalDateTime() : null;

        return new ActivityLog(
                rs.getInt("log_id"),
                rs.getInt("user_id"),
                rs.getString("action_taken"),
                logTime
        );
    }
}
