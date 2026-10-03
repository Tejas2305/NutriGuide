package com.nutriguide.service;

import com.nutriguide.dao.MealDAO;
import com.nutriguide.dao.NutritionGoalDAO;
import com.nutriguide.dao.UserDAO;
import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.User;
import com.nutriguide.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * NutritionService: Handles BMI calculations, nutrition goal management, and daily nutrient summaries.
 *
 * Demonstrates:
 * - Calling MySQL stored functions (calculate_bmi) via UserDAO
 * - Calling MySQL stored procedures (get_daily_summary) via MealDAO
 * - Querying MySQL views (daily_nutrition_summary) via MealDAO
 * - Input validation using ValidationUtil
 */
public class NutritionService {

    private final NutritionGoalDAO goalDAO;
    private final UserDAO userDAO;
    private final MealDAO mealDAO;

    public NutritionService() {
        this.goalDAO = new NutritionGoalDAO();
        this.userDAO = new UserDAO();
        this.mealDAO = new MealDAO();
    }

    public NutritionService(NutritionGoalDAO goalDAO, UserDAO userDAO, MealDAO mealDAO) {
        this.goalDAO = goalDAO;
        this.userDAO = userDAO;
        this.mealDAO = mealDAO;
    }

    // ========================================================================
    // BMI Calculations
    // ========================================================================

    /**
     * Calculates BMI for given weight and height using the MySQL stored function 'calculate_bmi' (DBMS Exp 6).
     *
     * @param weightKg Weight in kg
     * @param heightCm Height in cm
     * @return Computed BMI
     * @throws ValidationException if inputs are non-positive
     * @throws DatabaseOperationException if database function execution fails
     */
    public BigDecimal calculateBmi(BigDecimal weightKg, BigDecimal heightCm)
            throws ValidationException, DatabaseOperationException {

        if (weightKg == null || !ValidationUtil.isPositiveBigDecimal(weightKg)) {
            throw new ValidationException("weightKg", "Weight must be positive.");
        }
        if (heightCm == null || !ValidationUtil.isPositiveBigDecimal(heightCm)) {
            throw new ValidationException("heightCm", "Height must be positive.");
        }

        return userDAO.calculateBmi(weightKg, heightCm);
    }

    /**
     * Calculates BMI for a specific user using their profile data.
     *
     * @param userId User ID
     * @return User's BMI
     * @throws ResourceNotFoundException if user does not exist
     * @throws DatabaseOperationException if database execution fails
     */
    public BigDecimal calculateUserBmi(int userId)
            throws ResourceNotFoundException, DatabaseOperationException {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User", userId);
        }
        return calculateBmi(user.getWeightKg(), user.getHeightCm());
    }

    // ========================================================================
    // Nutrition Goal Management
    // ========================================================================

    /**
     * Creates a new nutrition goal after validating that target calories and macros are non-negative.
     *
     * @param goal NutritionGoal entity
     * @return Created NutritionGoal with generated goalId
     * @throws ValidationException if values are negative or user does not exist
     * @throws DatabaseOperationException if database insertion fails
     */
    public NutritionGoal createGoal(NutritionGoal goal)
            throws ValidationException, DatabaseOperationException {

        if (goal == null || goal.getUserId() == null) {
            throw new ValidationException("goal", "Goal details and userId must not be null.");
        }

        // Validate values
        if (!ValidationUtil.isNonNegativeBigDecimal(goal.getTargetCalories())) {
            throw new ValidationException("targetCalories", "Target calories cannot be negative.");
        }
        if (!ValidationUtil.isNonNegativeBigDecimal(goal.getTargetProtein())) {
            throw new ValidationException("targetProtein", "Target protein cannot be negative.");
        }
        if (!ValidationUtil.isNonNegativeBigDecimal(goal.getTargetCarbs())) {
            throw new ValidationException("targetCarbs", "Target carbohydrates cannot be negative.");
        }
        if (!ValidationUtil.isNonNegativeBigDecimal(goal.getTargetFats())) {
            throw new ValidationException("targetFats", "Target fats cannot be negative.");
        }

        int id = goalDAO.addGoal(goal);
        goal.setGoalId(id);
        return goal;
    }

    /**
     * Retrieves the currently active nutrition goal for a user.
     *
     * @param userId User ID
     * @return Active NutritionGoal
     * @throws ResourceNotFoundException if no active goal exists
     * @throws DatabaseOperationException if database retrieval fails
     */
    public NutritionGoal getActiveGoal(int userId)
            throws ResourceNotFoundException, DatabaseOperationException {
        NutritionGoal goal = goalDAO.getActiveGoalByUserId(userId);
        if (goal == null) {
            throw new ResourceNotFoundException("NutritionGoal", "User " + userId);
        }
        return goal;
    }

    /**
     * Retrieves all goals (active and historical) for a user.
     */
    public ArrayList<NutritionGoal> getGoalsByUserId(int userId) throws DatabaseOperationException {
        return goalDAO.getGoalsByUserId(userId);
    }

    /**
     * Retrieves goal history for a user (alias for getGoalsByUserId).
     */
    public ArrayList<NutritionGoal> getGoalHistory(int userId) throws DatabaseOperationException {
        return getGoalsByUserId(userId);
    }

    /**
     * Updates an existing nutrition goal.
     */
    public boolean updateGoal(NutritionGoal goal) throws ValidationException, DatabaseOperationException {
        if (goal == null || goal.getGoalId() == null) {
            throw new ValidationException("goal", "Goal and goalId cannot be null.");
        }
        return goalDAO.updateGoal(goal);
    }

    /**
     * Validates goal macronutrient and caloric values.
     */
    public void validateGoal(NutritionGoal goal) throws ValidationException {
        if (goal == null || goal.getUserId() == null) {
            throw new ValidationException("goal", "Goal details and userId must not be null.");
        }
        if (goal.getDescription() == null || goal.getDescription().trim().isEmpty()) {
            throw new ValidationException("description", "Goal description cannot be empty.");
        }
        if (goal.getTargetCalories() == null || !ValidationUtil.isNonNegativeBigDecimal(goal.getTargetCalories())) {
            throw new ValidationException("targetCalories", "Target calories cannot be negative.");
        }
        if (goal.getTargetProtein() == null || !ValidationUtil.isNonNegativeBigDecimal(goal.getTargetProtein())) {
            throw new ValidationException("targetProtein", "Target protein cannot be negative.");
        }
        if (goal.getTargetCarbs() == null || !ValidationUtil.isNonNegativeBigDecimal(goal.getTargetCarbs())) {
            throw new ValidationException("targetCarbs", "Target carbohydrates cannot be negative.");
        }
        if (goal.getTargetFats() == null || !ValidationUtil.isNonNegativeBigDecimal(goal.getTargetFats())) {
            throw new ValidationException("targetFats", "Target fats cannot be negative.");
        }
    }

    /**
     * Saves a new active nutrition goal transactionally (DBMS Exp 10):
     * 1. Deactivates any existing active goals for this user.
     * 2. Inserts the new goal with is_active = TRUE.
     * 3. Commits transaction (preserves historical goals).
     */
    public NutritionGoal saveNewActiveGoal(NutritionGoal newGoal)
            throws ValidationException, DatabaseOperationException {
        validateGoal(newGoal);

        Connection conn = null;
        try {
            conn = DBConnectionFactory.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Step 1: Deactivate existing active goals for this user
            goalDAO.deactivateActiveGoals(conn, newGoal.getUserId());

            // Step 2: Insert new goal as active
            newGoal.setIsActive(true);
            int id = goalDAO.addGoal(conn, newGoal);
            newGoal.setGoalId(id);

            conn.commit();
            return newGoal;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // Suppress
                }
            }
            throw new DatabaseOperationException("Failed to save new active goal with transaction rollback", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // Suppress
                }
            }
        }
    }

    /**
     * Updates an existing nutrition goal, verifying user ownership.
     */
    public boolean updateGoalForUser(NutritionGoal goal, int userId)
            throws ResourceNotFoundException, ValidationException, DatabaseOperationException {
        if (goal == null || goal.getGoalId() == null) {
            throw new ValidationException("goal", "Goal and goalId cannot be null.");
        }
        validateGoal(goal);

        NutritionGoal existing = goalDAO.getGoalById(goal.getGoalId());
        if (existing == null) {
            throw new ResourceNotFoundException("NutritionGoal", goal.getGoalId());
        }
        if (existing.getUserId() == null || existing.getUserId() != userId) {
            throw new ValidationException("userId", "Unauthorized: Goal #" + goal.getGoalId() + " does not belong to user " + userId);
        }

        goal.setUserId(userId);
        return goalDAO.updateGoal(goal);
    }

    /**
     * Deactivates a specific goal for a user, verifying user ownership.
     */
    public boolean deactivateGoalForUser(int goalId, int userId)
            throws ResourceNotFoundException, ValidationException, DatabaseOperationException {
        NutritionGoal existing = goalDAO.getGoalById(goalId);
        if (existing == null) {
            throw new ResourceNotFoundException("NutritionGoal", goalId);
        }
        if (existing.getUserId() == null || existing.getUserId() != userId) {
            throw new ValidationException("userId", "Unauthorized: Goal #" + goalId + " does not belong to user " + userId);
        }

        existing.setIsActive(false);
        return goalDAO.updateGoal(existing);
    }

    /**
     * Deactivates the currently active nutrition goal for a user.
     */
    public boolean deactivateActiveGoal(int userId)
            throws ResourceNotFoundException, DatabaseOperationException {
        NutritionGoal active = getActiveGoal(userId);
        active.setIsActive(false);
        return goalDAO.updateGoal(active);
    }

    // ========================================================================
    // Daily Nutrition Summary
    // ========================================================================

    /**
     * Retrieves the daily nutrition summary for a user on a given date using the database view (DBMS Exp 4).
     */
    public DailyNutritionSummary getDailySummaryFromView(int userId, LocalDate date)
            throws DatabaseOperationException {
        ValidationUtil.requireNonNullDate(date, "date");
        return mealDAO.getDailySummaryFromView(userId, date);
    }

    /**
     * Retrieves the daily nutrition summary for a user on a given date using the stored procedure (DBMS Exp 7).
     */
    public DailyNutritionSummary getDailySummaryFromProcedure(int userId, LocalDate date)
            throws DatabaseOperationException {
        ValidationUtil.requireNonNullDate(date, "date");
        return mealDAO.getDailySummaryProcedure(userId, date);
    }
}
