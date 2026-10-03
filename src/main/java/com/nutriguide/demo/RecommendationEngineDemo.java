package com.nutriguide.demo;

import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.Recommendation;
import com.nutriguide.recommendation.RecommendationEngine;
import com.nutriguide.recommendation.RuleBasedRecommendationEngine;
import com.nutriguide.service.NutritionService;
import com.nutriguide.service.RecommendationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * RecommendationEngineDemo: Demonstrates and verifies the Rule-Based Recommendation Engine.
 *
 * Demonstrates:
 * - Exp 7: Interface implementation and Polymorphism:
 *   RecommendationEngine engine = new RuleBasedRecommendationEngine();
 * - Exp 11: Interface contract execution
 * - Rule evaluation for BMI, Calorie, and Protein conditions
 * - Persistence and retrieval of recommendations via RecommendationService
 * - Clean teardown of temporary records preserving initial sample data
 */
public class RecommendationEngineDemo {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   NutriGuide Phase 3E: Rule-Based Recommendation Engine Verification");
        System.out.println("================================================================================");

        try {
            RecommendationService recService = new RecommendationService();
            NutritionService nutritionService = new NutritionService();

            // Section 1: Polymorphism Demonstration & Direct Rule Evaluation
            testPolymorphismAndDirectRules();

            // Section 2: End-to-End Service Recommendation Generation
            testServiceRecommendationGeneration(recService, nutritionService);

            // Section 3: Database Persistence, Retrieval, and Cleanup
            testPersistenceAndCleanup(recService);

        } catch (Exception e) {
            System.err.println("Unexpected failure in RecommendationEngineDemo: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("\n================================================================================");
        System.out.println("   Results: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    // ========================================================================
    // Section 1: Polymorphism & Pure Rule Logic Evaluation
    // ========================================================================
    private static void testPolymorphismAndDirectRules() {
        System.out.println("\n--- [1] Polymorphism & Rule Logic Tests (Java Lab Exp 7 & 11) ---");

        // Polymorphic declaration: Interface reference storing concrete implementation
        RecommendationEngine engine = new RuleBasedRecommendationEngine();
        assertCondition("1.1: Polymorphism instantiated (engine is RuleBasedRecommendationEngine)",
                engine instanceof RuleBasedRecommendationEngine);

        // Test Scenario A: Normal BMI, Calorie & Protein Close to Target
        DailyNutritionSummary summaryBalanced = new DailyNutritionSummary(
                1, LocalDate.now(),
                new BigDecimal("2100.00"), // close to 2200
                new BigDecimal("122.00"),  // close to 120
                new BigDecimal("240.00"),
                new BigDecimal("60.00")
        );
        NutritionGoal goal = new NutritionGoal(
                1,
                new BigDecimal("2200.00"),
                new BigDecimal("120.00"),
                new BigDecimal("250.00"),
                new BigDecimal("65.00"),
                true
        );

        ArrayList<Recommendation> balancedRecs = engine.generateRecommendations(
                1, new BigDecimal("22.86"), summaryBalanced, goal);

        assertCondition("1.2: Generated 3 recommendations for balanced scenario", balancedRecs.size() == 3);
        assertCondition("1.3: Normal BMI rule triggered",
                balancedRecs.get(0).getMessage().contains("normal range"));
        assertCondition("1.4: Calorie close to target rule triggered",
                balancedRecs.get(1).getMessage().contains("close to your daily target"));
        assertCondition("1.5: Protein close to target rule triggered",
                balancedRecs.get(2).getMessage().contains("close to your daily target"));

        // Test Scenario B: Underweight BMI & Caloric / Protein Deficit
        DailyNutritionSummary summaryDeficit = new DailyNutritionSummary(
                1, LocalDate.now(),
                new BigDecimal("1400.00"), // < 2200 * 0.90 (1980)
                new BigDecimal("80.00"),   // < 120 * 0.90 (108)
                new BigDecimal("150.00"),
                new BigDecimal("40.00")
        );

        ArrayList<Recommendation> deficitRecs = engine.generateRecommendations(
                1, new BigDecimal("17.50"), summaryDeficit, goal);

        assertCondition("1.6: Underweight BMI rule triggered",
                deficitRecs.get(0).getMessage().contains("below the normal range"));
        assertCondition("1.7: Calorie deficit rule triggered",
                deficitRecs.get(1).getMessage().contains("below your daily target"));
        assertCondition("1.8: Protein deficit rule triggered",
                deficitRecs.get(2).getMessage().contains("below your daily target"));

        // Test Scenario C: Overweight BMI & Caloric / Protein Surplus
        DailyNutritionSummary summarySurplus = new DailyNutritionSummary(
                1, LocalDate.now(),
                new BigDecimal("2800.00"), // > 2200 * 1.10 (2420)
                new BigDecimal("160.00"),  // > 120 * 1.10 (132)
                new BigDecimal("350.00"),
                new BigDecimal("90.00")
        );

        ArrayList<Recommendation> surplusRecs = engine.generateRecommendations(
                1, new BigDecimal("28.40"), summarySurplus, goal);

        assertCondition("1.9: Overweight BMI rule triggered",
                surplusRecs.get(0).getMessage().contains("above the normal range"));
        assertCondition("1.10: Calorie surplus rule triggered",
                surplusRecs.get(1).getMessage().contains("above your daily target"));
        assertCondition("1.11: Protein surplus rule triggered",
                surplusRecs.get(2).getMessage().contains("above your daily target"));
    }

    // ========================================================================
    // Section 2: Service-Level Recommendation Generation
    // ========================================================================
    private static void testServiceRecommendationGeneration(RecommendationService recService,
                                                            NutritionService nutritionService)
            throws Exception {
        System.out.println("\n--- [2] Service-Level Recommendation Generation ---");

        int userId = 1;
        LocalDate date = LocalDate.of(2026, 10, 3);

        // Generate recommendations using live database profile & actual intake
        ArrayList<Recommendation> liveRecs = recService.generateRecommendations(userId, date);

        assertCondition("2.1: Live recommendations generated for user 1", liveRecs != null && !liveRecs.isEmpty());
        System.out.println("    Generated " + (liveRecs != null ? liveRecs.size() : 0) + " recommendations for User 1 on " + date + ":");
        if (liveRecs != null) {
            for (Recommendation r : liveRecs) {
                System.out.println("    - [" + r.getRecType() + "] " + r.getMessage());
            }
        }
    }

    // ========================================================================
    // Section 3: Database Persistence, Retrieval, and Cleanup
    // ========================================================================
    private static void testPersistenceAndCleanup(RecommendationService recService) throws Exception {
        System.out.println("\n--- [3] Recommendation Persistence, Retrieval & Cleanup ---");

        int userId = 1;
        LocalDate date = LocalDate.of(2026, 10, 3);

        // 1. Generate and save recommendations to MySQL
        ArrayList<Recommendation> saved = recService.generateAndSaveRecommendations(userId, date);
        assertCondition("3.1: Recommendations saved to database with generated IDs",
                saved != null && !saved.isEmpty() && saved.get(0).getRecId() != null);

        // 2. Query user recommendations to verify database persistence
        ArrayList<Recommendation> allUserRecs = recService.getRecommendationsByUserId(userId);
        assertCondition("3.2: User recommendations retrieved from database",
                allUserRecs != null && allUserRecs.size() >= saved.size());

        // 3. Clean up the saved demo recommendations
        if (saved != null) {
            for (Recommendation r : saved) {
                boolean deleted = recService.deleteRecommendation(r.getRecId());
                assertCondition("3.3: Temporary recommendation #" + r.getRecId() + " cleaned up", deleted);
            }
        }
    }

    // ========================================================================
    // Assertion Helper
    // ========================================================================
    private static void assertCondition(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.out.println("  [FAIL] " + testName);
            failed++;
        }
    }
}
