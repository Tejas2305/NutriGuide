package com.nutriguide.server;

import com.nutriguide.db.DBConnectionFactory;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * HealthHandler: Endpoint for health checks, connection verification, and client pinging.
 * Route: GET /api/health
 */
public class HealthHandler extends BaseHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed. Use GET.");
            return;
        }

        Map<String, Object> healthData = new HashMap<String, Object>();
        healthData.put("status", "UP");
        healthData.put("timestamp", LocalDateTime.now().toString());

        boolean dbOk = false;
        try (Connection conn = DBConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            if (rs.next()) {
                dbOk = true;
            }
        } catch (Exception ex) {
            dbOk = false;
        }

        healthData.put("database", dbOk ? "CONNECTED" : "UNAVAILABLE");

        if (dbOk) {
            sendSuccess(exchange, 200, "NutriGuide localhost backend is healthy", healthData);
        } else {
            sendJson(exchange, 503, new ApiResponse(false, "Backend running, but database connection is unavailable", healthData, "DATABASE_ERROR"));
        }
    }
}
