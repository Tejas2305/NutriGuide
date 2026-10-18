package com.nutriguide.service;

import com.nutriguide.dao.RecommendationDAO;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.Recommendation;
import com.nutriguide.model.User;
import com.nutriguide.recommendation.RecommendationEngine;
import com.nutriguide.recommendation.RuleBasedRecommendationEngine;
import com.nutriguide.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * RecommendationService: Coordinates recommendation generation and persistence.
 *
 * Demonstrates:
 * - Coordination between Services, RuleBasedRecommendationEngine, and RecommendationDAO
 * - Polymorphism via RecommendationEngine interface
 */
public class RecommendationService {

    private final RecommendationDAO recommendationDAO;
    private final RecommendationEngine recommendationEngine;
    private final NutritionService nutritionService;
    private final UserService userService;

    public RecommendationService() {
        this.recommendationDAO = new RecommendationDAO();
        this.recommendationEngine = new RuleBasedRecommendationEngine();
        this.nutritionService = new NutritionService();
        this.userService = new UserService();
    }

    public RecommendationService(RecommendationDAO recommendationDAO,
                                 RecommendationEngine recommendationEngine,
                                 NutritionService nutritionService,
                                 UserService userService) {
        this.recommendationDAO = recommendationDAO;
        this.recommendationEngine = recommendationEngine;
        this.nutritionService = nutritionService;
        this.userService = userService;
    }

    /**
     * Saves a recommendation record.
     */
    public Recommendation saveRecommendation(Recommendation recommendation)
            throws ValidationException, DatabaseOperationException {

        if (recommendation == null || recommendation.getUserId() == null) {
            throw new ValidationException("recommendation", "Recommendation and userId cannot be null.");
        }
        ValidationUtil.requireNonBlank(recommendation.getMessage(), "message");

        int id = recommendationDAO.addRecommendation(recommendation);
        recommendation.setRecId(id);
        return recommendation;
    }

    /**
     * Retrieves a recommendation by ID.
     */
    public Recommendation getRecommendationById(int recId)
            throws ResourceNotFoundException, DatabaseOperationException {
        Recommendation rec = recommendationDAO.getRecommendationById(recId);
        if (rec == null) {
            throw new ResourceNotFoundException("Recommendation", recId);
        }
        return rec;
    }

    /**
     * Retrieves all recommendations for a user.
     */
    public ArrayList<Recommendation> getRecommendationsByUserId(int userId)
            throws DatabaseOperationException {
        return recommendationDAO.getRecommendationsByUserId(userId);
    }

    /**
     * Retrieves recommendation history for a user.
     * <p>Note for Viva: Serves as a backward-compatible alias for getRecommendationsByUserId(),
     * used by GUI frames (e.g. RecommendationFrame) without duplicating database logic.</p>
     */
    public ArrayList<Recommendation> getRecommendationHistory(int userId)
            throws DatabaseOperationException {
        return getRecommendationsByUserId(userId);
    }

    /**
     * Deletes a recommendation by ID.
     */
    public boolean deleteRecommendation(int recId) throws DatabaseOperationException {
        return recommendationDAO.deleteRecommendation(recId);
    }

    /**
     * Evaluates rules without persisting them to the database.
     * Useful for live GUI display or testing.
     *
     * @param userId User ID
     * @param date Date to evaluate intake against
     * @return List of generated Recommendation objects
     * @throws ResourceNotFoundException if user is not found
     * @throws DatabaseOperationException if database access fails
     */
    public ArrayList<Recommendation> generateRecommendations(int userId, LocalDate date)
            throws ResourceNotFoundException, DatabaseOperationException {

        ValidationUtil.requireNonNullDate(date, "date");

        // 1. Verify user exists
        User user = userService.getUserById(userId);

        // 2. Compute BMI via MySQL stored function
        BigDecimal bmi = nutritionService.calculateBmi(user.getWeightKg(), user.getHeightCm());

        // 3. Retrieve active nutrition goal (if configured)
        NutritionGoal goal = null;
        try {
            goal = nutritionService.getActiveGoal(userId);
        } catch (ResourceNotFoundException e) {
            // No active goal configured; goal remains null
        }

        // 4. Retrieve daily actual intake from database view
        DailyNutritionSummary summary = nutritionService.getDailySummaryFromView(userId, date);

        // 5. Delegate to rule-based engine
        return recommendationEngine.generateRecommendations(userId, bmi, summary, goal);
    }

    /**
     * Evaluates rules and persists each generated recommendation to the database.
     *
     * @param userId User ID
     * @param date Date to evaluate intake against
     * @return List of saved Recommendation objects (with generated IDs)
     * @throws ResourceNotFoundException if user is not found
     * @throws DatabaseOperationException if database access fails
     */
    public ArrayList<Recommendation> generateAndSaveRecommendations(int userId, LocalDate date)
            throws ResourceNotFoundException, DatabaseOperationException {

        ArrayList<Recommendation> generated = generateRecommendations(userId, date);
        ArrayList<Recommendation> saved = new ArrayList<Recommendation>();

        for (Recommendation rec : generated) {
            saved.add(saveRecommendation(rec));
        }

        return saved;
    }
}
