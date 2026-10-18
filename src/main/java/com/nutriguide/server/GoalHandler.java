package com.nutriguide.server;

import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.service.NutritionService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;

/**
 * GoalHandler: Manages nutrition targets and historical goals.
 * Routes:
 * - GET /api/goals/active
 * - GET /api/goals/history
 * - POST /api/goals
 * - PUT /api/goals/{id}
 * - POST /api/goals/{id}/deactivate
 */
public class GoalHandler extends BaseHandler {

    private final NutritionService nutritionService;
    private final SessionManager sessionManager;

    public GoalHandler(NutritionService nutritionService, SessionManager sessionManager) {
        this.nutritionService = nutritionService;
        this.sessionManager = sessionManager;
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
                if (path.endsWith("/active")) {
                    handleGetActiveGoal(exchange, authUserId);
                } else if (path.endsWith("/history")) {
                    handleGetGoalHistory(exchange, authUserId);
                } else {
                    sendError(exchange, 404, "Unknown goals endpoint: " + path);
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                if (path.endsWith("/deactivate")) {
                    handleDeactivateGoal(exchange, authUserId, path);
                } else {
                    handleCreateActiveGoal(exchange, authUserId);
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                handleUpdateGoal(exchange, authUserId, path);
            } else {
                sendError(exchange, 405, "Method not allowed. Use GET, POST, or PUT.");
            }
        } catch (Exception ex) {
            sendError(exchange, 500, "An internal server error occurred while processing nutrition goals.");
        }
    }

    private void handleGetActiveGoal(HttpExchange exchange, int userId) throws IOException {
        try {
            NutritionGoal active = nutritionService.getActiveGoal(userId);
            sendSuccess(exchange, 200, "Active goal retrieved", active);
        } catch (ResourceNotFoundException ex) {
            // Normal case when user has not configured an active goal yet
            sendSuccess(exchange, 200, "No active goal configured", null);
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while retrieving active goal.");
        }
    }

    private void handleGetGoalHistory(HttpExchange exchange, int userId) throws IOException {
        try {
            ArrayList<NutritionGoal> goals = nutritionService.getGoalsByUserId(userId);
            sendSuccess(exchange, 200, "Goal history retrieved", goals);
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while retrieving goal history.");
        }
    }

    private void handleCreateActiveGoal(HttpExchange exchange, int userId) throws IOException {
        String body = readBody(exchange);
        NutritionGoal goal = JsonUtil.fromJson(body, NutritionGoal.class);
        if (goal == null) {
            sendError(exchange, 400, "Invalid goal payload.");
            return;
        }

        goal.setUserId(userId);

        try {
            NutritionGoal saved = nutritionService.saveNewActiveGoal(goal);
            sendSuccess(exchange, 201, "New active goal created successfully", saved);
        } catch (ValidationException ex) {
            sendError(exchange, 400, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while saving nutrition goal.");
        }
    }

    private void handleUpdateGoal(HttpExchange exchange, int userId, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length < 4) {
            sendError(exchange, 400, "Goal ID must be specified in URL path: /api/goals/{id}");
            return;
        }

        int goalId;
        try {
            goalId = Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException ex) {
            sendError(exchange, 400, "Invalid goal ID.");
            return;
        }

        String body = readBody(exchange);
        NutritionGoal goal = JsonUtil.fromJson(body, NutritionGoal.class);
        if (goal == null) {
            sendError(exchange, 400, "Invalid goal payload.");
            return;
        }

        goal.setGoalId(goalId);
        goal.setUserId(userId);

        try {
            boolean updated = nutritionService.updateGoalForUser(goal, userId);
            if (updated) {
                sendSuccess(exchange, 200, "Goal updated successfully", goal);
            } else {
                sendError(exchange, 400, "Failed to update goal.");
            }
        } catch (ValidationException ex) {
            sendError(exchange, 400, ex.getMessage());
        } catch (ResourceNotFoundException ex) {
            sendError(exchange, 404, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while updating nutrition goal.");
        }
    }

    private void handleDeactivateGoal(HttpExchange exchange, int userId, String path) throws IOException {
        // Path format: /api/goals/{id}/deactivate
        String[] parts = path.split("/");
        if (parts.length < 5) {
            sendError(exchange, 400, "Goal ID must be specified: /api/goals/{id}/deactivate");
            return;
        }

        int goalId;
        try {
            goalId = Integer.parseInt(parts[parts.length - 2]);
        } catch (NumberFormatException ex) {
            sendError(exchange, 400, "Invalid goal ID.");
            return;
        }

        try {
            boolean deactivated = nutritionService.deactivateGoalForUser(goalId, userId);
            if (deactivated) {
                sendSuccess(exchange, 200, "Goal #" + goalId + " deactivated successfully", null);
            } else {
                sendError(exchange, 400, "Failed to deactivate goal #" + goalId);
            }
        } catch (ValidationException ex) {
            sendError(exchange, 403, "Forbidden: " + ex.getMessage());
        } catch (ResourceNotFoundException ex) {
            sendError(exchange, 404, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while deactivating nutrition goal.");
        }
    }
}
