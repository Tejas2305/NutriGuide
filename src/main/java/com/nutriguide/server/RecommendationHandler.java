package com.nutriguide.server;

import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.model.Recommendation;
import com.nutriguide.service.RecommendationService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * RecommendationHandler: Rule-based recommendation generation, persistence, and history.
 * Routes:
 * - GET /api/recommendations?date=YYYY-MM-DD
 * - POST /api/recommendations/generate?date=YYYY-MM-DD
 * - GET /api/recommendations/history
 */
public class RecommendationHandler extends BaseHandler {

    public static final String EDUCATIONAL_DISCLAIMER =
            "NutriGuide recommendations provide general dietary guidance for educational purposes only and are not a substitute for professional medical or clinical advice.";

    private final RecommendationService recommendationService;
    private final SessionManager sessionManager;

    public RecommendationHandler(RecommendationService recommendationService, SessionManager sessionManager) {
        this.recommendationService = recommendationService;
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
                if (path.endsWith("/history")) {
                    handleGetHistory(exchange, authUserId);
                } else {
                    handleGetRecommendations(exchange, authUserId);
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                if (path.endsWith("/generate")) {
                    handleGenerateAndSave(exchange, authUserId);
                } else {
                    sendError(exchange, 404, "Unknown recommendation endpoint: " + path);
                }
            } else {
                sendError(exchange, 405, "Method not allowed. Use GET or POST.");
            }
        } catch (Exception ex) {
            sendError(exchange, 500, "An internal server error occurred while processing recommendations.");
        }
    }

    private void handleGetRecommendations(HttpExchange exchange, int userId) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange);
        String dateStr = queryParams.get("date");
        LocalDate date;
        if (dateStr == null || dateStr.trim().isEmpty()) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(dateStr.trim());
            } catch (DateTimeParseException ex) {
                sendError(exchange, 400, "Invalid date format: " + dateStr + ". Expected yyyy-MM-dd.");
                return;
            }
        }

        try {
            ArrayList<Recommendation> recs = recommendationService.generateRecommendations(userId, date);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("recommendations", recs);
            data.put("disclaimer", EDUCATIONAL_DISCLAIMER);
            data.put("date", date.toString());

            sendSuccess(exchange, 200, "Recommendations generated", data);

        } catch (ResourceNotFoundException ex) {
            sendError(exchange, 404, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while generating recommendations.");
        }
    }

    private void handleGenerateAndSave(HttpExchange exchange, int userId) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange);
        String dateStr = queryParams.get("date");
        LocalDate date;
        if (dateStr == null || dateStr.trim().isEmpty()) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(dateStr.trim());
            } catch (DateTimeParseException ex) {
                sendError(exchange, 400, "Invalid date format: " + dateStr + ". Expected yyyy-MM-dd.");
                return;
            }
        }

        try {
            ArrayList<Recommendation> saved = recommendationService.generateAndSaveRecommendations(userId, date);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("savedRecommendations", saved);
            data.put("count", saved != null ? saved.size() : 0);
            data.put("disclaimer", EDUCATIONAL_DISCLAIMER);
            data.put("date", date.toString());

            sendSuccess(exchange, 201, "Recommendations generated and saved to history", data);

        } catch (ResourceNotFoundException ex) {
            sendError(exchange, 404, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while saving recommendations.");
        }
    }

    private void handleGetHistory(HttpExchange exchange, int userId) throws IOException {
        try {
            ArrayList<Recommendation> history = recommendationService.getRecommendationHistory(userId);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("history", history);
            data.put("disclaimer", EDUCATIONAL_DISCLAIMER);

            sendSuccess(exchange, 200, "Recommendation history retrieved", data);

        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while retrieving recommendation history.");
        }
    }
}
