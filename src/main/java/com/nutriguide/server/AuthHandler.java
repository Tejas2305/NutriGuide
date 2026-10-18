package com.nutriguide.server;

import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.User;
import com.nutriguide.service.AuthenticationService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * AuthHandler: Handles user authentication, registration, and logout.
 * Routes:
 * - POST /api/auth/login
 * - POST /api/auth/register
 * - POST /api/auth/logout
 */
public class AuthHandler extends BaseHandler {

    private final AuthenticationService authService;
    private final SessionManager sessionManager;

    public AuthHandler(AuthenticationService authService, SessionManager sessionManager) {
        this.authService = authService;
        this.sessionManager = sessionManager;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class RegisterRequest {
        public String name;
        public String email;
        public String password;
        public Integer age;
        public String gender;
        public BigDecimal heightCm;
        public BigDecimal weightKg;
        public String activityLevel;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (!"POST".equalsIgnoreCase(method)) {
            sendError(exchange, 405, "Method not allowed. Use POST.");
            return;
        }

        try {
            if (path.endsWith("/login")) {
                handleLogin(exchange);
            } else if (path.endsWith("/register")) {
                handleRegister(exchange);
            } else if (path.endsWith("/logout")) {
                handleLogout(exchange);
            } else {
                sendError(exchange, 404, "Unknown authentication endpoint: " + path);
            }
        } catch (Exception ex) {
            sendError(exchange, 500, "An internal server error occurred during authentication.");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        LoginRequest req = JsonUtil.fromJson(body, LoginRequest.class);

        if (req == null || req.email == null || req.password == null) {
            sendError(exchange, 400, "Email and password are required.");
            return;
        }

        try {
            User user = authService.login(req.email.trim(), req.password);
            String token = sessionManager.createSession(user.getUserId(), user.getEmail());

            user.setPasswordHash(null); // Never return password hash to client

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("token", token);
            data.put("user", user);

            sendSuccess(exchange, 200, "Login successful", data);

        } catch (AuthenticationException ex) {
            sendError(exchange, 401, ex.getMessage());
        } catch (ValidationException ex) {
            sendError(exchange, 400, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred during authentication.");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        RegisterRequest req = JsonUtil.fromJson(body, RegisterRequest.class);

        if (req == null) {
            sendError(exchange, 400, "Invalid registration payload.");
            return;
        }

        User newUser = new User(
                req.name,
                req.email,
                null,
                req.age,
                req.gender,
                req.heightCm,
                req.weightKg,
                req.activityLevel
        );

        try {
            User registered = authService.registerUser(newUser, req.password);
            String token = sessionManager.createSession(registered.getUserId(), registered.getEmail());

            registered.setPasswordHash(null);

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("token", token);
            data.put("user", registered);

            sendSuccess(exchange, 201, "Registration successful", data);

        } catch (ValidationException ex) {
            sendError(exchange, 400, ex.getMessage());
        } catch (AuthenticationException ex) {
            sendError(exchange, 401, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred during registration.");
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = extractToken(exchange);
        if (token != null) {
            sessionManager.removeSession(token);
        }
        sendSuccess(exchange, 200, "Logged out successfully", null);
    }
}
