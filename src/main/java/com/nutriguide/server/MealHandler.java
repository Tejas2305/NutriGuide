package com.nutriguide.server;

import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.Meal;
import com.nutriguide.model.MealItem;
import com.nutriguide.service.MealService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * MealHandler: Endpoints for meal creation, querying, and deletion.
 * Routes:
 * - GET /api/meals?date=YYYY-MM-DD
 * - POST /api/meals
 * - DELETE /api/meals/{id}
 */
public class MealHandler extends BaseHandler {

    private final MealService mealService;
    private final SessionManager sessionManager;

    public MealHandler(MealService mealService, SessionManager sessionManager) {
        this.mealService = mealService;
        this.sessionManager = sessionManager;
    }

    public static class CreateMealRequest {
        public LocalDate mealDate;
        public String mealType;
        public ArrayList<MealItem> items;
    }

    public static class MealResponseDto {
        public Integer mealId;
        public Integer userId;
        public String mealType;
        public LocalDate mealDate;
        public int itemCount;
        public ArrayList<MealItem> items;

        public MealResponseDto(Meal meal, ArrayList<MealItem> items) {
            this.mealId = meal.getMealId();
            this.userId = meal.getUserId();
            this.mealType = meal.getMealType();
            this.mealDate = meal.getMealDate();
            this.items = items != null ? items : new ArrayList<MealItem>();
            this.itemCount = this.items.size();
        }
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
                handleGetMeals(exchange, authUserId);
            } else if ("POST".equalsIgnoreCase(method)) {
                handleCreateMeal(exchange, authUserId);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDeleteMeal(exchange, authUserId, path);
            } else {
                sendError(exchange, 405, "Method not allowed. Use GET, POST, or DELETE.");
            }
        } catch (Exception ex) {
            sendError(exchange, 500, "An internal server error occurred while processing meals.");
        }
    }

    private void handleGetMeals(HttpExchange exchange, int userId) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange);
        String dateStr = queryParams.get("date");

        LocalDate targetDate;
        if (dateStr == null || dateStr.trim().isEmpty()) {
            targetDate = LocalDate.now();
        } else {
            try {
                targetDate = LocalDate.parse(dateStr.trim());
            } catch (DateTimeParseException ex) {
                sendError(exchange, 400, "Invalid date format: " + dateStr + ". Expected yyyy-MM-dd.");
                return;
            }
        }

        try {
            ArrayList<Meal> meals = mealService.getMealsByUserIdAndDate(userId, targetDate);
            ArrayList<MealResponseDto> dtos = new ArrayList<MealResponseDto>();

            for (Meal meal : meals) {
                ArrayList<MealItem> items = mealService.getItemsByMealId(meal.getMealId());
                dtos.add(new MealResponseDto(meal, items));
            }

            sendSuccess(exchange, 200, "Meals retrieved", dtos);

        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while retrieving meals.");
        }
    }

    private void handleCreateMeal(HttpExchange exchange, int userId) throws IOException {
        String body = readBody(exchange);
        CreateMealRequest req = JsonUtil.fromJson(body, CreateMealRequest.class);

        if (req == null || req.mealType == null || req.items == null || req.items.isEmpty()) {
            sendError(exchange, 400, "Meal type, valid date, and at least one food item are required.");
            return;
        }

        LocalDate mealDate = req.mealDate != null ? req.mealDate : LocalDate.now();
        Meal meal = new Meal(userId, req.mealType, mealDate);

        try {
            int generatedMealId = mealService.addMealWithItems(meal, req.items);

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("mealId", generatedMealId);
            data.put("mealType", req.mealType);
            data.put("mealDate", mealDate.toString());
            data.put("itemCount", req.items.size());

            sendSuccess(exchange, 201, "Meal and items created successfully", data);

        } catch (ValidationException ex) {
            sendError(exchange, 400, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while creating meal.");
        }
    }

    private void handleDeleteMeal(HttpExchange exchange, int userId, String path) throws IOException {
        // Expected path: /api/meals/{id}
        String[] parts = path.split("/");
        if (parts.length < 4) {
            sendError(exchange, 400, "Meal ID must be specified in URL path: /api/meals/{id}");
            return;
        }

        int mealId;
        try {
            mealId = Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException ex) {
            sendError(exchange, 400, "Invalid meal ID format.");
            return;
        }

        try {
            boolean deleted = mealService.deleteMeal(mealId, userId);
            if (deleted) {
                sendSuccess(exchange, 200, "Meal #" + mealId + " deleted successfully", null);
            } else {
                sendError(exchange, 404, "Meal #" + mealId + " not found or already deleted.");
            }
        } catch (ResourceNotFoundException ex) {
            sendError(exchange, 404, ex.getMessage());
        } catch (ValidationException ex) {
            sendError(exchange, 403, "Forbidden: " + ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while deleting meal.");
        }
    }
}
