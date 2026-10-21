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
 * ActivityLogDAO - handles all database operations for the 'activity_log' table.
 * The activity log keeps track of user actions in the system.
 */
public class ActivityLogDAO {

    /**
     * Adds a new activity log entry to the database.
     * Returns the auto-generated log_id.
     */
    public int addLog(ActivityLog log) throws DatabaseOperationException {
        String sql = "INSERT INTO activity_log (user_id, action_taken) VALUES (?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet keys = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, log.getUserId());
            stmt.setString(2, log.getActionTaken());

            stmt.executeUpdate();

            keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                log.setLogId(id);
                return id;
            } else {
                throw new DatabaseOperationException("Log added but could not get generated ID.");
            }

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to add activity log for user: " + log.getUserId(), e);
        } finally {
            closeResultSet(keys);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets a single activity log entry by its log_id.
     * Returns null if not found.
     */
    public ActivityLog getLogById(int logId) throws DatabaseOperationException {
        String sql = "SELECT log_id, user_id, action_taken, log_time FROM activity_log WHERE log_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, logId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to get activity log by ID: " + logId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Gets all activity log entries for a specific user, newest first.
     */
    public ArrayList<ActivityLog> getLogsByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT log_id, user_id, action_taken, log_time " +
                     "FROM activity_log WHERE user_id = ? ORDER BY log_time DESC";

        ArrayList<ActivityLog> list = new ArrayList<ActivityLog>();
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
            throw new DatabaseOperationException("Failed to get activity logs for user: " + userId, e);
        } finally {
            closeResultSet(rs);
            closeStatement(stmt);
            closeConnection(conn);
        }
    }

    /**
     * Maps a database row to an ActivityLog object.
     */
    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("log_time");
        LocalDateTime logTime = null;
        if (timestamp != null) {
            logTime = timestamp.toLocalDateTime();
        }

        return new ActivityLog(
                rs.getInt("log_id"),
                rs.getInt("user_id"),
                rs.getString("action_taken"),
                logTime
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
