package com.nutriguide.server;

import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.User;
import com.nutriguide.service.NutritionService;
import com.nutriguide.service.UserService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * ProfileHandler: Manages authenticated user profile, updates, password change, and BMI.
 * Routes:
 * - GET /api/profile
 * - PUT /api/profile
 * - PUT /api/profile/password
 * - GET /api/profile/bmi
 */
public class ProfileHandler extends BaseHandler {

    private final UserService userService;
    private final NutritionService nutritionService;
    private final SessionManager sessionManager;

    public ProfileHandler(UserService userService, NutritionService nutritionService, SessionManager sessionManager) {
        this.userService = userService;
        this.nutritionService = nutritionService;
        this.sessionManager = sessionManager;
    }

    public static class PasswordChangeRequest {
        public String currentPassword;
        public String newPassword;
        public String confirmPassword;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Integer authUserId = requireAuth(exchange, sessionManager);
        if (authUserId == null) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                if (path.endsWith("/bmi")) {
                    handleGetBmi(exchange, authUserId);
                } else {
                    handleGetProfile(exchange, authUserId);
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                if (path.endsWith("/password")) {
                    handleChangePassword(exchange, authUserId);
                } else {
                    handleUpdateProfile(exchange, authUserId);
                }
            } else {
                sendError(exchange, 405, "Method not allowed. Use GET or PUT.");
            }
        } catch (Exception ex) {
            sendError(exchange, 500, "An internal server error occurred while processing profile request.");
        }
    }

    private void handleGetProfile(HttpExchange exchange, int userId) throws IOException {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                sendError(exchange, 404, "User profile not found.");
                return;
            }
            user.setPasswordHash(null);
            sendSuccess(exchange, 200, "Profile loaded", user);
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while loading profile.");
        }
    }

    private void handleUpdateProfile(HttpExchange exchange, int userId) throws IOException {
        String body = readBody(exchange);
        User updated = JsonUtil.fromJson(body, User.class);
        if (updated == null) {
            sendError(exchange, 400, "Invalid profile update payload.");
            return;
        }

        // Enforce authenticated ownership
        updated.setUserId(userId);

        try {
            boolean success = userService.updateProfile(updated);
            if (success) {
                User fresh = userService.getUserById(userId);
                if (fresh != null) {
                    fresh.setPasswordHash(null);
                }
                sendSuccess(exchange, 200, "Profile updated successfully", fresh);
            } else {
                sendError(exchange, 400, "Failed to update profile.");
            }
        } catch (ValidationException ex) {
            sendError(exchange, 400, ex.getMessage());
        } catch (ResourceNotFoundException ex) {
            sendError(exchange, 404, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while updating profile.");
        }
    }

    private void handleChangePassword(HttpExchange exchange, int userId) throws IOException {
        String body = readBody(exchange);
        PasswordChangeRequest req = JsonUtil.fromJson(body, PasswordChangeRequest.class);
        if (req == null) {
            sendError(exchange, 400, "Invalid password change payload.");
            return;
        }

        try {
            boolean changed = userService.changePassword(userId, req.currentPassword, req.newPassword, req.confirmPassword);
            if (changed) {
                sendSuccess(exchange, 200, "Password changed successfully", null);
            } else {
                sendError(exchange, 400, "Password change failed.");
            }
        } catch (AuthenticationException ex) {
            sendError(exchange, 401, ex.getMessage());
        } catch (ValidationException ex) {
            sendError(exchange, 400, ex.getMessage());
        } catch (ResourceNotFoundException ex) {
            sendError(exchange, 404, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while changing password.");
        }
    }

    private void handleGetBmi(HttpExchange exchange, int userId) throws IOException {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                sendError(exchange, 404, "User profile not found.");
                return;
            }

            BigDecimal bmi = BigDecimal.ZERO;
            if (user.getWeightKg() != null && user.getHeightCm() != null) {
                bmi = nutritionService.calculateBmi(user.getWeightKg(), user.getHeightCm());
            }

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("bmi", bmi);
            sendSuccess(exchange, 200, "BMI calculated", data);

        } catch (ValidationException ex) {
            sendError(exchange, 400, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while calculating BMI.");
        }
    }
}
