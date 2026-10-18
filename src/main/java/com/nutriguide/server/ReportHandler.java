package com.nutriguide.server;

import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.service.ReportService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * ReportHandler: Serves nutrition reports, intake summaries, and date-range views.
 * Routes:
 * - GET /api/reports/daily?date=YYYY-MM-DD
 * - GET /api/reports/range?from=YYYY-MM-DD&to=YYYY-MM-DD
 * - GET /api/reports/text?date=YYYY-MM-DD
 */
public class ReportHandler extends BaseHandler {

    private final ReportService reportService;
    private final SessionManager sessionManager;

    public ReportHandler(ReportService reportService, SessionManager sessionManager) {
        this.reportService = reportService;
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

        String path = exchange.getRequestURI().getPath();

        try {
            if (path.endsWith("/daily")) {
                handleDailyReport(exchange, authUserId);
            } else if (path.endsWith("/range")) {
                handleRangeReport(exchange, authUserId);
            } else if (path.endsWith("/text")) {
                handleTextReport(exchange, authUserId);
            } else {
                sendError(exchange, 404, "Unknown report endpoint: " + path);
            }
        } catch (Exception ex) {
            sendError(exchange, 500, "An internal server error occurred while processing the report request.");
        }
    }

    private void handleDailyReport(HttpExchange exchange, int userId) throws IOException {
        Map<String, String> params = parseQueryParams(exchange);
        String dateStr = params.get("date");
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
            DailyNutritionSummary summary = reportService.getDailySummaryViaView(userId, date);
            NutritionGoal goal = reportService.getGoalSummary(userId);

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("date", date.toString());
            data.put("summary", summary);
            data.put("activeGoal", goal);

            sendSuccess(exchange, 200, "Daily summary retrieved", data);

        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while retrieving the daily summary.");
        }
    }

    private void handleRangeReport(HttpExchange exchange, int userId) throws IOException {
        Map<String, String> params = parseQueryParams(exchange);
        String toStr = params.get("to");
        String fromStr = params.get("from");

        LocalDate to;
        if (toStr == null || toStr.trim().isEmpty()) {
            to = LocalDate.now();
        } else {
            try {
                to = LocalDate.parse(toStr.trim());
            } catch (DateTimeParseException ex) {
                sendError(exchange, 400, "Invalid 'to' date format: " + toStr + ". Expected yyyy-MM-dd.");
                return;
            }
        }

        LocalDate from;
        if (fromStr == null || fromStr.trim().isEmpty()) {
            from = to.minusDays(6);
        } else {
            try {
                from = LocalDate.parse(fromStr.trim());
            } catch (DateTimeParseException ex) {
                sendError(exchange, 400, "Invalid 'from' date format: " + fromStr + ". Expected yyyy-MM-dd.");
                return;
            }
        }

        if (from.isAfter(to)) {
            sendError(exchange, 400, "Invalid date range: start date ('from'=" + from + ") cannot be after end date ('to'=" + to + ").");
            return;
        }

        try {
            ArrayList<DailyNutritionSummary> history = reportService.getNutritionHistory(userId, from, to);
            NutritionGoal goal = reportService.getGoalSummary(userId);

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("from", from.toString());
            data.put("to", to.toString());
            data.put("history", history);
            data.put("activeGoal", goal);

            sendSuccess(exchange, 200, "Range summary retrieved", data);

        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while retrieving the nutrition history range.");
        }
    }

    private void handleTextReport(HttpExchange exchange, int userId) throws IOException {
        Map<String, String> params = parseQueryParams(exchange);
        String dateStr = params.get("date");
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
            String reportText = reportService.generateDailyUserReport(userId, date);
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("date", date.toString());
            data.put("reportText", reportText);

            sendSuccess(exchange, 200, "Report generated", data);

        } catch (ResourceNotFoundException ex) {
            sendError(exchange, 404, ex.getMessage());
        } catch (DatabaseOperationException ex) {
            sendError(exchange, 500, "A database error occurred while generating the report text.");
        }
    }
}
