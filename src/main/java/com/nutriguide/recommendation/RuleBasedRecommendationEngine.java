package com.nutriguide.recommendation;

import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.Recommendation;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * RuleBasedRecommendationEngine: Concrete implementation of RecommendationEngine.
 *
 * <p>Uses deterministic, rule-based logic (if / else if / else) to analyze user BMI,
 * calorie intake, and macronutrient targets.</p>
 *
 * Demonstrates Java Lab Manual concepts:
 * - Exp 7: Implementing an interface and achieving runtime polymorphism
 * - Exp 11: Interface method implementation with @Override
 * - Basic control flow: if / else if / else conditional branches
 * - Standard ArrayList collection usage
 */
public class RuleBasedRecommendationEngine implements RecommendationEngine {

    // Standard BMI Thresholds
    private static final BigDecimal BMI_UNDERWEIGHT = new BigDecimal("18.5");
    private static final BigDecimal BMI_NORMAL_MAX = new BigDecimal("25.0");

    // Calorie & Protein tolerance factors (within 10% is considered close to target)
    private static final BigDecimal RATIO_LOWER = new BigDecimal("0.90");
    private static final BigDecimal RATIO_UPPER = new BigDecimal("1.10");

    /**
     * Evaluates nutritional status and generates personalized recommendations.
     *
     * @param userId The ID of the user
     * @param bmi Calculated Body Mass Index (BMI)
     * @param actualIntake Aggregated actual daily intake
     * @param targetGoal Target nutrition goal
     * @return List of generated Recommendation objects
     */
    @Override
    public ArrayList<Recommendation> generateRecommendations(int userId,
                                                             BigDecimal bmi,
                                                             DailyNutritionSummary actualIntake,
                                                             NutritionGoal targetGoal) {

        ArrayList<Recommendation> recommendations = new ArrayList<Recommendation>();

        // 1. Evaluate BMI rule if BMI is valid
        if (bmi != null && bmi.compareTo(BigDecimal.ZERO) > 0) {
            recommendations.add(evaluateBmiRule(userId, bmi));
        }

        // 2. Evaluate Calorie and Protein rules against daily target goal
        if (actualIntake != null && targetGoal != null) {
            BigDecimal targetCalories = targetGoal.getTargetCalories();
            if (targetCalories != null && targetCalories.compareTo(BigDecimal.ZERO) > 0) {
                recommendations.add(evaluateCalorieRule(userId, actualIntake.getDailyCalories(), targetCalories));
            }

            BigDecimal targetProtein = targetGoal.getTargetProtein();
            if (targetProtein != null && targetProtein.compareTo(BigDecimal.ZERO) > 0) {
                recommendations.add(evaluateProteinRule(userId, actualIntake.getDailyProtein(), targetProtein));
            }
        }

        return recommendations;
    }

    /**
     * Evaluates BMI category using standard thresholds.
     */
    private Recommendation evaluateBmiRule(int userId, BigDecimal bmi) {
        String message;

        if (bmi.compareTo(BMI_UNDERWEIGHT) < 0) {
            message = "Your BMI is below the normal range. Consider maintaining adequate calorie and nutrient intake.";
        } else if (bmi.compareTo(BMI_NORMAL_MAX) < 0) {
            message = "Your BMI is in the normal range. Continue following a balanced diet and regular physical activity.";
        } else {
            message = "Your BMI is above the normal range. Consider monitoring calorie intake and maintaining regular physical activity.";
        }

        return new Recommendation(userId, message, "BMI");
    }

    /**
     * Evaluates daily calorie intake against user's target calorie goal.
     */
    private Recommendation evaluateCalorieRule(int userId, BigDecimal actualCalories, BigDecimal targetCalories) {
        if (actualCalories == null) {
            actualCalories = BigDecimal.ZERO;
        }

        BigDecimal lowerThreshold = targetCalories.multiply(RATIO_LOWER);
        BigDecimal upperThreshold = targetCalories.multiply(RATIO_UPPER);

        String message;
        if (actualCalories.compareTo(lowerThreshold) < 0) {
            message = "Your calorie intake is below your daily target. Consider maintaining adequate energy intake.";
        } else if (actualCalories.compareTo(upperThreshold) > 0) {
            message = "Your calorie intake is above your daily target. Consider monitoring portion sizes and overall calorie intake.";
        } else {
            message = "Your calorie intake is close to your daily target.";
        }

        return new Recommendation(userId, message, "CALORIE");
    }

    /**
     * Evaluates daily protein intake against user's target protein goal.
     */
    private Recommendation evaluateProteinRule(int userId, BigDecimal actualProtein, BigDecimal targetProtein) {
        if (actualProtein == null) {
            actualProtein = BigDecimal.ZERO;
        }

        BigDecimal lowerThreshold = targetProtein.multiply(RATIO_LOWER);
        BigDecimal upperThreshold = targetProtein.multiply(RATIO_UPPER);

        String message;
        if (actualProtein.compareTo(lowerThreshold) < 0) {
            message = "Your protein intake is below your daily target.";
        } else if (actualProtein.compareTo(upperThreshold) > 0) {
            message = "Your protein intake is above your daily target.";
        } else {
            message = "Your protein intake is close to your daily target.";
        }

        return new Recommendation(userId, message, "PROTEIN");
    }
}
