package com.nutriguide.server;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionManager: Manages in-memory authentication tokens for localhost sessions.
 * Provides thread-safe token generation, expiration tracking, and user mapping.
 */
public class SessionManager {

    private static final long SESSION_TIMEOUT_MS = 2 * 60 * 60 * 1000L; // 2 hours

    public static class SessionInfo {
        private final int userId;
        private final String email;
        private final long createdAt;
        private volatile long lastAccessedAt;

        public SessionInfo(int userId, String email) {
            this.userId = userId;
            this.email = email;
            this.createdAt = System.currentTimeMillis();
            this.lastAccessedAt = this.createdAt;
        }

        public int getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public void touch() {
            this.lastAccessedAt = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return (System.currentTimeMillis() - lastAccessedAt) > SESSION_TIMEOUT_MS;
        }
    }

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<String, SessionInfo>();

    /**
     * Creates a new session token for the authenticated user.
     *
     * @param userId Authenticated user ID
     * @param email User's email
     * @return Generated session token
     */
    public String createSession(int userId, String email) {
        cleanExpiredSessions();
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new SessionInfo(userId, email));
        return token;
    }

    /**
     * Resolves the user ID for a valid, non-expired session token.
     *
     * @param token Authentication token
     * @return User ID if valid, null if token is missing, invalid, or expired
     */
    public Integer getUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        SessionInfo info = sessions.get(token.trim());
        if (info == null) {
            return null;
        }
        if (info.isExpired()) {
            sessions.remove(token.trim());
            return null;
        }
        info.touch();
        return info.getUserId();
    }

    /**
     * Invalidates a session token (e.g. on user logout).
     */
    public void removeSession(String token) {
        if (token != null) {
            sessions.remove(token.trim());
        }
    }

    private void cleanExpiredSessions() {
        Iterator<Map.Entry<String, SessionInfo>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, SessionInfo> entry = it.next();
            if (entry.getValue().isExpired()) {
                it.remove();
            }
        }
    }
}
