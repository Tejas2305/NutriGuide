package com.nutriguide.recommendation;

import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.Recommendation;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * RecommendationEngine: Interface defining the contract for dietary insight generation.
 *
 * Demonstrates Java Lab Manual concepts:
 * - Exp 7: Interfaces and Polymorphism (RecommendationEngine interface implemented by RuleBasedRecommendationEngine)
 * - Exp 11: Interface implementation
 */
public interface RecommendationEngine {

    /**
     * Evaluates nutritional status and generates a list of personalized recommendations
     * based on BMI, daily actual intake, and configured nutrition goals.
     *
     * @param userId The ID of the user
     * @param bmi Calculated Body Mass Index (BMI)
     * @param actualIntake Aggregated actual daily intake
     * @param targetGoal Target nutrition goal
     * @return ArrayList of generated Recommendation objects
     */
    ArrayList<Recommendation> generateRecommendations(int userId,
                                                      BigDecimal bmi,
                                                      DailyNutritionSummary actualIntake,
                                                      NutritionGoal targetGoal);
}
