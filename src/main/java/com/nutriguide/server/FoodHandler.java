package com.nutriguide.server;

import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.Food;
import com.nutriguide.service.FoodService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;

/**
 * FoodHandler: Exposes available food items catalog.
 * Route: GET /api/foods
 */
public class FoodHandler extends BaseHandler {

    private final FoodService foodService;
    private final SessionManager sessionManager;

    public FoodHandler(FoodService foodService, SessionManager sessionManager) {
        this.foodService = foodService;
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Integer authUserId = requireAuth(exchange, sessionManager);
        if (authUserId == null) {
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed. Use GET.");
            return;
        }

        try {
            ArrayList<Food> foods = foodService.getAllFoods();
            sendSuccess(exchange, 200, "Foods retrieved", foods);
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while loading foods catalog.");
        }
    }
}
