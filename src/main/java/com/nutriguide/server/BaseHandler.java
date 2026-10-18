package com.nutriguide.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * BaseHandler: Common utilities for HTTP exchange management, request parsing,
 * security headers, size limits, and uniform response serialization.
 */
public abstract class BaseHandler implements HttpHandler {

    private static final int MAX_REQUEST_BODY_BYTES = 1024 * 1024; // 1 MB limit

    protected String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int bytesRead;
        int total = 0;

        while ((bytesRead = is.read(buffer)) != -1) {
            total += bytesRead;
            if (total > MAX_REQUEST_BODY_BYTES) {
                throw new IOException("Request body exceeds maximum allowed size (1 MB).");
            }
            baos.write(buffer, 0, bytesRead);
        }

        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    protected void sendJson(HttpExchange exchange, int statusCode, ApiResponse response) throws IOException {
        byte[] bytes = JsonUtil.toJson(response).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store, no-cache, must-revalidate");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        OutputStream os = exchange.getResponseBody();
        try {
            os.write(bytes);
            os.flush();
        } finally {
            os.close();
            exchange.close();
        }
    }

    protected void sendError(HttpExchange exchange, int statusCode, String errorMessage) throws IOException {
        sendJson(exchange, statusCode, ApiResponse.err(errorMessage));
    }

    protected void sendSuccess(HttpExchange exchange, int statusCode, String message, Object data) throws IOException {
        sendJson(exchange, statusCode, ApiResponse.ok(message, data));
    }

    protected void sendSuccess(HttpExchange exchange, Object data) throws IOException {
        sendJson(exchange, 200, ApiResponse.ok(data));
    }

    protected String extractToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    protected Integer requireAuth(HttpExchange exchange, SessionManager sessionManager) throws IOException {
        String token = extractToken(exchange);
        if (token == null) {
            sendError(exchange, 401, "Authentication required. Missing Bearer token.");
            return null;
        }

        Integer userId = sessionManager.getUserId(token);
        if (userId == null) {
            sendError(exchange, 401, "Invalid or expired session token. Please log in again.");
            return null;
        }

        return userId;
    }

    protected Map<String, String> parseQueryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<String, String>();
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0 && idx < pair.length() - 1) {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    params.put(key, value);
                } else if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    params.put(key, "");
                }
            } catch (Exception ignored) {
            }
        }
        return params;
    }
}
