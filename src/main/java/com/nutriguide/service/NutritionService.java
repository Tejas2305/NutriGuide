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
 * NutritionService - handles BMI calculations, nutrition goals, and daily summaries.
 *
 * Uses:
 * - MySQL stored function 'calculate_bmi' for BMI calculation
 * - MySQL stored procedure 'get_daily_summary' for nutrition totals
 * - MySQL view 'daily_nutrition_summary' for querying nutrition data
 * - Transactions when saving a new active goal (deactivate old + insert new)
 */
public class NutritionService {

    private NutritionGoalDAO goalDAO;
    private UserDAO userDAO;
    private MealDAO mealDAO;

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

    // ===================================================================
    // BMI CALCULATIONS
    // ===================================================================

    /**
     * Calculates BMI using the MySQL stored function 'calculate_bmi'.
     * Formula (done by database): weight(kg) / (height(m))^2
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
     * Calculates BMI for a specific user using their stored height and weight.
     */
    public BigDecimal calculateUserBmi(int userId)
            throws ResourceNotFoundException, DatabaseOperationException {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User", userId);
        }
        return calculateBmi(user.getWeightKg(), user.getHeightCm());
    }

    // ===================================================================
    // NUTRITION GOAL MANAGEMENT
    // ===================================================================

    /**
     * Creates a new nutrition goal for a user.
     */
    public NutritionGoal createGoal(NutritionGoal goal)
            throws ValidationException, DatabaseOperationException {
        validateNutrientValues(goal);

        int id = goalDAO.addGoal(goal);
        goal.setGoalId(id);
        return goal;
    }

    /**
     * Gets the currently active nutrition goal for a user.
     * Throws exception if no active goal is found.
     */
    public NutritionGoal getActiveGoal(int userId)
            throws ResourceNotFoundException, DatabaseOperationException {
        NutritionGoal goal = goalDAO.getActiveGoalByUserId(userId);
        if (goal == null) {
            throw new ResourceNotFoundException("No active nutrition goal found for user: " + userId);
        }
        return goal;
    }

    /**
     * Gets all nutrition goals for a user (active + past history).
     */
    public ArrayList<NutritionGoal> getGoalsByUserId(int userId) throws DatabaseOperationException {
        return goalDAO.getGoalsByUserId(userId);
    }

    /**
     * Gets goal history (same as getGoalsByUserId, just an alias).
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
     * Validates a nutrition goal's values (used before saving).
     */
    public void validateGoal(NutritionGoal goal) throws ValidationException {
        if (goal == null || goal.getUserId() == null) {
            throw new ValidationException("goal", "Goal details and userId must not be null.");
        }
        // description is optional - getDescription() returns a default if null
        validateNutrientValues(goal);
    }

    /**
     * Helper: validates that all nutrient target values are non-negative.
     */
    private void validateNutrientValues(NutritionGoal goal) throws ValidationException {
        if (goal == null || goal.getUserId() == null) {
            throw new ValidationException("goal", "Goal details and userId must not be null.");
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
     * Saves a new nutrition goal as the active goal using a transaction.
     *
     * Transaction steps:
     * 1. Deactivate any existing active goals for this user
     * 2. Insert the new goal with is_active = TRUE
     * 3. Commit - if both steps worked
     * 4. Rollback - if either step failed
     */
    public NutritionGoal saveNewActiveGoal(NutritionGoal newGoal)
            throws ValidationException, DatabaseOperationException {
        validateGoal(newGoal);

        Connection conn = null;
        try {
            conn = DBConnectionFactory.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Deactivate current active goals
            goalDAO.deactivateActiveGoals(conn, newGoal.getUserId());

            // Step 2: Insert new goal as active
            newGoal.setIsActive(true);
            int id = goalDAO.addGoal(conn, newGoal);
            newGoal.setGoalId(id);

            // Step 3: Commit - everything succeeded
            conn.commit();
            return newGoal;

        } catch (SQLException e) {
            // Step 4: Rollback - something failed
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw new DatabaseOperationException("Failed to save new active goal - transaction rolled back.", e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Updates a nutrition goal, verifying it belongs to the given user.
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
            throw new ValidationException("userId", "Goal #" + goal.getGoalId() + " does not belong to user " + userId);
        }

        goal.setUserId(userId);
        return goalDAO.updateGoal(goal);
    }

    /**
     * Deactivates a specific goal for a user.
     */
    public boolean deactivateGoalForUser(int goalId, int userId)
            throws ResourceNotFoundException, ValidationException, DatabaseOperationException {
        NutritionGoal existing = goalDAO.getGoalById(goalId);
        if (existing == null) {
            throw new ResourceNotFoundException("NutritionGoal", goalId);
        }
        if (existing.getUserId() == null || existing.getUserId() != userId) {
            throw new ValidationException("userId", "Goal #" + goalId + " does not belong to user " + userId);
        }

        existing.setIsActive(false);
        return goalDAO.updateGoal(existing);
    }

    /**
     * Deactivates the currently active goal for a user.
     */
    public boolean deactivateActiveGoal(int userId)
            throws ResourceNotFoundException, DatabaseOperationException {
        NutritionGoal active = getActiveGoal(userId);
        active.setIsActive(false);
        return goalDAO.updateGoal(active);
    }

    // ===================================================================
    // DAILY NUTRITION SUMMARY
    // ===================================================================

    /**
     * Gets the daily nutrition summary from the database VIEW.
     * A VIEW is a pre-saved SELECT query in MySQL.
     */
    public DailyNutritionSummary getDailySummaryFromView(int userId, LocalDate date)
            throws DatabaseOperationException {
        ValidationUtil.requireNonNullDate(date, "date");
        return mealDAO.getDailySummaryFromView(userId, date);
    }

    /**
     * Gets the daily nutrition summary using the stored PROCEDURE.
     * A stored procedure is a set of SQL statements saved in the database.
     */
    public DailyNutritionSummary getDailySummaryFromProcedure(int userId, LocalDate date)
            throws DatabaseOperationException {
        ValidationUtil.requireNonNullDate(date, "date");
        return mealDAO.getDailySummaryProcedure(userId, date);
    }
}
