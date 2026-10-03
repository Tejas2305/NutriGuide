package com.nutriguide.service;

import com.nutriguide.dao.MealDAO;
import com.nutriguide.dao.NutritionGoalDAO;
import com.nutriguide.dao.UserDAO;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.Meal;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.User;
import com.nutriguide.util.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * ReportService: Generates nutrition summaries, audit histories, and user reports.
 *
 * Demonstrates:
 * - Exp 5: StringBuffer for efficient report string accumulation
 * - Exp 7 (DBMS): Calling stored procedure 'get_daily_summary' via MealDAO
 * - Exp 4 (DBMS): Calling database view 'daily_nutrition_summary' via MealDAO
 * - Exp 6 (DBMS): Calling stored function 'calculate_bmi' via UserDAO
 */
public class ReportService {

    private final UserDAO userDAO;
    private final MealDAO mealDAO;
    private final NutritionGoalDAO goalDAO;

    public ReportService() {
        this.userDAO = new UserDAO();
        this.mealDAO = new MealDAO();
        this.goalDAO = new NutritionGoalDAO();
    }

    public ReportService(UserDAO userDAO, MealDAO mealDAO, NutritionGoalDAO goalDAO) {
        this.userDAO = userDAO;
        this.mealDAO = mealDAO;
        this.goalDAO = goalDAO;
    }

    /**
     * Retrieves daily nutrition totals using the MySQL stored procedure 'get_daily_summary' (DBMS Exp 7).
     */
    public DailyNutritionSummary getDailySummaryViaProcedure(int userId, LocalDate date)
            throws DatabaseOperationException {
        ValidationUtil.requireNonNullDate(date, "date");
        return mealDAO.getDailySummaryProcedure(userId, date);
    }

    /**
     * Retrieves daily nutrition totals using the MySQL view 'daily_nutrition_summary' (DBMS Exp 4).
     */
    public DailyNutritionSummary getDailySummaryViaView(int userId, LocalDate date)
            throws DatabaseOperationException {
        ValidationUtil.requireNonNullDate(date, "date");
        return mealDAO.getDailySummaryFromView(userId, date);
    }

    /**
     * Retrieves nutrition history for a user over a date range from the view (DBMS Exp 4).
     */
    public ArrayList<DailyNutritionSummary> getNutritionHistory(int userId, LocalDate fromDate, LocalDate toDate)
            throws DatabaseOperationException {
        ValidationUtil.requireNonNullDate(fromDate, "fromDate");
        ValidationUtil.requireNonNullDate(toDate, "toDate");
        ValidationUtil.requireValidDateRange(fromDate, toDate);
        return mealDAO.getNutritionHistoryFromView(userId, fromDate, toDate);
    }

    /**
     * Retrieves all nutrition history for a user from the view (DBMS Exp 4).
     */
    public ArrayList<DailyNutritionSummary> getAllNutritionHistory(int userId)
            throws DatabaseOperationException {
        return mealDAO.getAllNutritionHistoryFromView(userId);
    }

    /**
     * Retrieves the meal history for a user.
     */
    public ArrayList<Meal> getMealHistory(int userId) throws DatabaseOperationException {
        return mealDAO.getMealsByUserId(userId);
    }

    /**
     * Retrieves active goal for comparison against intake.
     */
    public NutritionGoal getGoalSummary(int userId) throws DatabaseOperationException {
        return goalDAO.getActiveGoalByUserId(userId);
    }

    /**
     * Generates a comprehensive plain-text nutrition report for a user on a given date.
     * Demonstrates Java Lab Exp 5 by using StringBuffer for string concatenation.
     *
     * @param userId User ID
     * @param date Date of the report
     * @return Formatted report as String
     * @throws ResourceNotFoundException if user does not exist
     * @throws DatabaseOperationException if database access fails
     */
    public String generateDailyUserReport(int userId, LocalDate date)
            throws ResourceNotFoundException, DatabaseOperationException {

        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User", userId);
        }

        BigDecimal bmi = userDAO.calculateBmi(user.getWeightKg(), user.getHeightCm());
        DailyNutritionSummary summary = mealDAO.getDailySummaryProcedure(userId, date);
        NutritionGoal activeGoal = goalDAO.getActiveGoalByUserId(userId);
        ArrayList<Meal> meals = mealDAO.getMealsByUserIdAndDate(userId, date);

        // Accumulate report using StringBuffer (Java Lab Exp 5)
        StringBuffer buffer = new StringBuffer();
        buffer.append("================================================================================\n");
        buffer.append("                       NUTRIGUIDE DAILY NUTRITION REPORT                       \n");
        buffer.append("================================================================================\n");
        buffer.append("User Name:       ").append(user.getName()).append("\n");
        buffer.append("Email:           ").append(user.getEmail()).append("\n");
        buffer.append("Age / Gender:    ").append(user.getAge()).append(" yrs / ").append(user.getGender()).append("\n");
        buffer.append("Height / Weight: ").append(user.getHeightCm()).append(" cm / ").append(user.getWeightKg()).append(" kg\n");
        buffer.append("Calculated BMI:  ").append(bmi).append(" (via MySQL calculate_bmi function)\n");
        buffer.append("Activity Level:  ").append(user.getActivityLevel()).append("\n");
        buffer.append("Report Date:     ").append(date).append("\n");
        buffer.append("--------------------------------------------------------------------------------\n");
        buffer.append("DAILY INTAKE (via get_daily_summary stored procedure):\n");
        buffer.append("  Calories:      ").append(summary.getDailyCalories()).append(" kcal\n");
        buffer.append("  Protein:       ").append(summary.getDailyProtein()).append(" g\n");
        buffer.append("  Carbohydrates: ").append(summary.getDailyCarbs()).append(" g\n");
        buffer.append("  Fats:          ").append(summary.getDailyFat()).append(" g\n");

        buffer.append("--------------------------------------------------------------------------------\n");
        if (activeGoal != null) {
            buffer.append("TARGET GOAL COMPARISON:\n");
            buffer.append("  Target Calories: ").append(activeGoal.getTargetCalories()).append(" kcal\n");
            buffer.append("  Target Protein:  ").append(activeGoal.getTargetProtein()).append(" g\n");
            buffer.append("  Target Carbs:    ").append(activeGoal.getTargetCarbs()).append(" g\n");
            buffer.append("  Target Fats:     ").append(activeGoal.getTargetFats()).append(" g\n");
        } else {
            buffer.append("TARGET GOAL: No active goal configured.\n");
        }

        buffer.append("--------------------------------------------------------------------------------\n");
        buffer.append("MEALS LOGGED TODAY: ").append(meals != null ? meals.size() : 0).append("\n");
        if (meals != null) {
            for (Meal m : meals) {
                buffer.append("  - [Meal #").append(m.getMealId()).append("] ").append(m.getMealType()).append("\n");
            }
        }
        buffer.append("================================================================================\n");

        return buffer.toString();
    }
}
