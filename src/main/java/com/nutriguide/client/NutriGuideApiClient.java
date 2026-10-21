package com.nutriguide.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.Food;
import com.nutriguide.model.MealItem;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.Recommendation;
import com.nutriguide.model.User;
import com.nutriguide.server.ApiResponse;
import com.nutriguide.server.AuthHandler;
import com.nutriguide.server.JsonUtil;
import com.nutriguide.server.MealHandler;
import com.nutriguide.server.ProfileHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * NutriGuideApiClient: Standard HTTP client for communicating with the local NutriGuide backend.
 *
 * Requirements satisfied:
 * - Decouples Swing GUI from direct MySQL/JDBC access.
 * - Manages in-memory authentication token across user sessions.
 * - Uses standard Java HttpURLConnection without heavy frameworks.
 * - Standardizes error handling and timeout detection.
 */
public class NutriGuideApiClient {

    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:8081/api";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 8000;

    private static NutriGuideApiClient instance;

    private final String baseUrl;
    private String sessionToken;
    private User currentUser;

    public NutriGuideApiClient() {
        this(System.getProperty("nutriguide.api.url", DEFAULT_BASE_URL));
    }

    public NutriGuideApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public static synchronized NutriGuideApiClient getInstance() {
        if (instance == null) {
            instance = new NutriGuideApiClient();
        }
        return instance;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isAuthenticated() {
        return sessionToken != null && !sessionToken.trim().isEmpty() && currentUser != null;
    }

    // ========================================================================
    // Health Check & Availability
    // ========================================================================

    /**
     * Pings the localhost backend server to verify it is running and healthy.
     */
    public boolean checkBackendHealth() {
        try {
            ApiResponse resp = executeRequest("GET", "/health", null, false);
            return resp.isSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    // ========================================================================
    // Authentication Operations
    // ========================================================================

    public User login(String email, String plainPassword) throws ApiClientException {
        AuthHandler.LoginRequest req = new AuthHandler.LoginRequest();
        req.email = email;
        req.password = plainPassword;

        ApiResponse resp = executeRequest("POST", "/auth/login", req, false);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Login failed.");
        }

        String dataJson = JsonUtil.toJson(resp.getData());
        JsonObject obj = JsonParser.parseString(dataJson).getAsJsonObject();

        this.sessionToken = obj.get("token").getAsString();
        this.currentUser = JsonUtil.fromJson(obj.get("user"), User.class);

        return this.currentUser;
    }

    public User register(User user, String plainPassword) throws ApiClientException {
        AuthHandler.RegisterRequest req = new AuthHandler.RegisterRequest();
        req.name = user.getName();
        req.email = user.getEmail();
        req.password = plainPassword;
        req.age = user.getAge();
        req.gender = user.getGender();
        req.heightCm = user.getHeightCm();
        req.weightKg = user.getWeightKg();
        req.activityLevel = user.getActivityLevel();

        ApiResponse resp = executeRequest("POST", "/auth/register", req, false);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Registration failed.");
        }

        String dataJson = JsonUtil.toJson(resp.getData());
        JsonObject obj = JsonParser.parseString(dataJson).getAsJsonObject();

        this.sessionToken = obj.get("token").getAsString();
        this.currentUser = JsonUtil.fromJson(obj.get("user"), User.class);

        return this.currentUser;
    }

    public void logout() {
        try {
            if (this.sessionToken != null) {
                executeRequest("POST", "/auth/logout", null, true);
            }
        } catch (Exception ignored) {
        } finally {
            this.sessionToken = null;
            this.currentUser = null;
        }
    }

    // ========================================================================
    // Profile Operations
    // ========================================================================

    public User getProfile() throws ApiClientException {
        ApiResponse resp = executeRequest("GET", "/profile", null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to load profile.");
        }
        User user = JsonUtil.fromJson(JsonUtil.toJson(resp.getData()), User.class);
        this.currentUser = user;
        return user;
    }

    public User updateProfile(User user) throws ApiClientException {
        ApiResponse resp = executeRequest("PUT", "/profile", user, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to update profile.");
        }
        User updated = JsonUtil.fromJson(JsonUtil.toJson(resp.getData()), User.class);
        this.currentUser = updated;
        return updated;
    }

    public boolean changePassword(String currentPassword, String newPassword, String confirmPassword) throws ApiClientException {
        ProfileHandler.PasswordChangeRequest req = new ProfileHandler.PasswordChangeRequest();
        req.currentPassword = currentPassword;
        req.newPassword = newPassword;
        req.confirmPassword = confirmPassword;

        ApiResponse resp = executeRequest("PUT", "/profile/password", req, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to change password.");
        }
        return true;
    }

    public BigDecimal calculateBmi() throws ApiClientException {
        ApiResponse resp = executeRequest("GET", "/profile/bmi", null, true);
        if (!resp.isSuccess()) {
            return BigDecimal.ZERO;
        }
        JsonObject obj = JsonParser.parseString(JsonUtil.toJson(resp.getData())).getAsJsonObject();
        if (obj.has("bmi")) {
            return obj.get("bmi").getAsBigDecimal();
        }
        return BigDecimal.ZERO;
    }

    // ========================================================================
    // Food Catalog Operations
    // ========================================================================

    public ArrayList<Food> getAllFoods() throws ApiClientException {
        ApiResponse resp = executeRequest("GET", "/foods", null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to load foods.");
        }
        Type type = new TypeToken<ArrayList<Food>>() {}.getType();
        return JsonUtil.fromJson(JsonUtil.toJson(resp.getData()), type);
    }

    // ========================================================================
    // Meal Operations
    // ========================================================================

    public ArrayList<MealHandler.MealResponseDto> getMealsByDate(LocalDate date) throws ApiClientException {
        String endpoint = "/meals" + (date != null ? "?date=" + date : "");
        ApiResponse resp = executeRequest("GET", endpoint, null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to load meals.");
        }
        Type type = new TypeToken<ArrayList<MealHandler.MealResponseDto>>() {}.getType();
        return JsonUtil.fromJson(JsonUtil.toJson(resp.getData()), type);
    }

    public int createMeal(LocalDate date, String mealType, ArrayList<MealItem> items) throws ApiClientException {
        MealHandler.CreateMealRequest req = new MealHandler.CreateMealRequest();
        req.mealDate = date;
        req.mealType = mealType;
        req.items = items;

        ApiResponse resp = executeRequest("POST", "/meals", req, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to create meal.");
        }
        JsonObject obj = JsonParser.parseString(JsonUtil.toJson(resp.getData())).getAsJsonObject();
        return obj.get("mealId").getAsInt();
    }

    public boolean deleteMeal(int mealId) throws ApiClientException {
        ApiResponse resp = executeRequest("DELETE", "/meals/" + mealId, null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to delete meal.");
        }
        return true;
    }

    // ========================================================================
    // Nutrition Goal Operations
    // ========================================================================

    public NutritionGoal getActiveGoal() throws ApiClientException {
        ApiResponse resp = executeRequest("GET", "/goals/active", null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to load active goal.");
        }
        if (resp.getData() == null) {
            return null;
        }
        return JsonUtil.fromJson(JsonUtil.toJson(resp.getData()), NutritionGoal.class);
    }

    public ArrayList<NutritionGoal> getGoalHistory() throws ApiClientException {
        ApiResponse resp = executeRequest("GET", "/goals/history", null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to load goal history.");
        }
        Type type = new TypeToken<ArrayList<NutritionGoal>>() {}.getType();
        return JsonUtil.fromJson(JsonUtil.toJson(resp.getData()), type);
    }

    public NutritionGoal saveNewActiveGoal(NutritionGoal goal) throws ApiClientException {
        ApiResponse resp = executeRequest("POST", "/goals", goal, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to save goal.");
        }
        return JsonUtil.fromJson(JsonUtil.toJson(resp.getData()), NutritionGoal.class);
    }

    public boolean updateGoal(NutritionGoal goal) throws ApiClientException {
        if (goal == null || goal.getGoalId() == null) {
            throw new ApiClientException("Goal and goalId cannot be null.");
        }
        ApiResponse resp = executeRequest("PUT", "/goals/" + goal.getGoalId(), goal, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to update goal.");
        }
        return true;
    }

    public boolean deactivateGoal(int goalId) throws ApiClientException {
        ApiResponse resp = executeRequest("POST", "/goals/" + goalId + "/deactivate", null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to deactivate goal.");
        }
        return true;
    }

    // ========================================================================
    // Recommendation Operations
    // ========================================================================

    public ArrayList<Recommendation> getRecommendations(LocalDate date) throws ApiClientException {
        String endpoint = "/recommendations" + (date != null ? "?date=" + date : "");
        ApiResponse resp = executeRequest("GET", endpoint, null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to load recommendations.");
        }
        JsonObject obj = JsonParser.parseString(JsonUtil.toJson(resp.getData())).getAsJsonObject();
        Type type = new TypeToken<ArrayList<Recommendation>>() {}.getType();
        return JsonUtil.fromJson(obj.get("recommendations"), type);
    }

    public ArrayList<Recommendation> generateAndSaveRecommendations(LocalDate date) throws ApiClientException {
        String endpoint = "/recommendations/generate" + (date != null ? "?date=" + date : "");
        ApiResponse resp = executeRequest("POST", endpoint, null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to generate recommendations.");
        }
        JsonObject obj = JsonParser.parseString(JsonUtil.toJson(resp.getData())).getAsJsonObject();
        Type type = new TypeToken<ArrayList<Recommendation>>() {}.getType();
        return JsonUtil.fromJson(obj.get("savedRecommendations"), type);
    }

    public ArrayList<Recommendation> getRecommendationHistory() throws ApiClientException {
        ApiResponse resp = executeRequest("GET", "/recommendations/history", null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to load history.");
        }
        JsonObject obj = JsonParser.parseString(JsonUtil.toJson(resp.getData())).getAsJsonObject();
        Type type = new TypeToken<ArrayList<Recommendation>>() {}.getType();
        return JsonUtil.fromJson(obj.get("history"), type);
    }

    // ========================================================================
    // Report Operations
    // ========================================================================

    public Map<String, Object> getDailyReport(LocalDate date) throws ApiClientException {
        String endpoint = "/reports/daily" + (date != null ? "?date=" + date : "");
        ApiResponse resp = executeRequest("GET", endpoint, null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to load daily report.");
        }
        JsonObject obj = JsonParser.parseString(JsonUtil.toJson(resp.getData())).getAsJsonObject();

        Map<String, Object> result = new HashMap<String, Object>();
        if (obj.has("summary") && !obj.get("summary").isJsonNull()) {
            result.put("summary", JsonUtil.fromJson(obj.get("summary"), DailyNutritionSummary.class));
        }
        if (obj.has("activeGoal") && !obj.get("activeGoal").isJsonNull()) {
            result.put("activeGoal", JsonUtil.fromJson(obj.get("activeGoal"), NutritionGoal.class));
        }
        return result;
    }

    public Map<String, Object> getRangeReport(LocalDate from, LocalDate to) throws ApiClientException {
        String endpoint = "/reports/range?from=" + from + "&to=" + to;
        ApiResponse resp = executeRequest("GET", endpoint, null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to load range report.");
        }
        JsonObject obj = JsonParser.parseString(JsonUtil.toJson(resp.getData())).getAsJsonObject();

        Map<String, Object> result = new HashMap<String, Object>();
        Type listType = new TypeToken<ArrayList<DailyNutritionSummary>>() {}.getType();
        if (obj.has("history")) {
            result.put("history", JsonUtil.fromJson(obj.get("history"), listType));
        }
        if (obj.has("activeGoal") && !obj.get("activeGoal").isJsonNull()) {
            result.put("activeGoal", JsonUtil.fromJson(obj.get("activeGoal"), NutritionGoal.class));
        }
        return result;
    }

    public String getTextReport(LocalDate date) throws ApiClientException {
        String endpoint = "/reports/text" + (date != null ? "?date=" + date : "");
        ApiResponse resp = executeRequest("GET", endpoint, null, true);
        if (!resp.isSuccess()) {
            throw new ApiClientException(resp.getError() != null ? resp.getError() : "Failed to load text report.");
        }
        JsonObject obj = JsonParser.parseString(JsonUtil.toJson(resp.getData())).getAsJsonObject();
        return obj.has("reportText") ? obj.get("reportText").getAsString() : "";
    }

    // ========================================================================
    // Internal HTTP Request Execution
    // ========================================================================

    private ApiResponse executeRequest(String method, String endpoint, Object requestBody, boolean requireAuth)
            throws ApiClientException {

        if (requireAuth && (sessionToken == null || sessionToken.trim().isEmpty())) {
            throw new ApiClientException(401, "No active session. Please log in first.");
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");

            if (sessionToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + sessionToken);
            }

            if (requestBody != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                byte[] bodyBytes = JsonUtil.toJson(requestBody).getBytes(StandardCharsets.UTF_8);
                OutputStream os = null;
                try {
                    os = conn.getOutputStream();
                    os.write(bodyBytes);
                    os.flush();
                } finally {
                    if (os != null) {
                        try { os.close(); } catch (IOException ignored) {}
                    }
                }
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (is == null) {
                throw new ApiClientException(responseCode, "Server returned status " + responseCode + " with empty body.");
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                if (reader != null) {
                    try { reader.close(); } catch (IOException ignored) {}
                }
            }

            String responseBody = sb.toString();
            ApiResponse apiResp = JsonUtil.fromJson(responseBody, ApiResponse.class);

            if (apiResp == null) {
                throw new ApiClientException(responseCode, "Malformed response from server: " + responseBody);
            }

            if (responseCode >= 400) {
                String err = apiResp.getError() != null ? apiResp.getError() : "HTTP " + responseCode;
                throw new ApiClientException(responseCode, err);
            }

            return apiResp;

        } catch (ConnectException ex) {
            throw new ApiClientException(
                    "Cannot connect to NutriGuide backend at " + baseUrl + ".\n" +
                    "Please verify that the backend process is running.\n" +
                    "(Start backend with: mvn compile exec:java -Dexec.mainClass=\"com.nutriguide.server.NutriGuideServer\")",
                    ex
            );
        } catch (IOException ex) {
            throw new ApiClientException("Network communication failure: " + ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
