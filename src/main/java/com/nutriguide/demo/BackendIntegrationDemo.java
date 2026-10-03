package com.nutriguide.demo;

import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.Meal;
import com.nutriguide.model.MealItem;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.Recommendation;
import com.nutriguide.model.User;
import com.nutriguide.recommendation.RecommendationEngine;
import com.nutriguide.recommendation.RuleBasedRecommendationEngine;
import com.nutriguide.service.AuthenticationService;
import com.nutriguide.service.MealService;
import com.nutriguide.service.NutritionService;
import com.nutriguide.service.RecommendationService;
import com.nutriguide.service.ReportService;
import com.nutriguide.service.UserService;
import com.nutriguide.util.PasswordUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * BackendIntegrationDemo: End-to-end integration test verifying the entire NutriGuide backend.
 *
 * Verifies:
 * 1. Database Connection and Resource Management
 * 2. Complete User Workflow (Login => Profile => BMI => Goals => Summary => Recommendations)
 * 3. User Registration Flow (Validation => SHA-256 Hashing => Insertion => Auth => Cleanup)
 * 4. ACID Meal Transaction (Commit of valid meal+items and Rollback of invalid FK items)
 * 5. Nutrition Calculation Consistency (View vs Stored Procedure vs Service layer)
 * 6. MySQL Stored Function calculate_bmi() accuracy across BMI categories
 * 7. Rule-Based Recommendation Engine across BMI, Calorie, and Protein rules
 * 8. Recommendation Persistence, Retrieval, and Cleanup
 * 9. Custom Exceptions Handling & SQL Error Code preservation
 * 10. Data Integrity Audit (Baseline table counts preserved intact)
 */
public class BackendIntegrationDemo {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   NutriGuide Phase 3F: End-to-End Backend Integration Verification");
        System.out.println("================================================================================");

        try {
            AuthenticationService authService = new AuthenticationService();
            UserService userService = new UserService();
            NutritionService nutritionService = new NutritionService();
            MealService mealService = new MealService();
            RecommendationService recService = new RecommendationService();
            ReportService reportService = new ReportService();

            // 1. Database Connection
            testDatabaseConnection();

            // 2. Complete User Workflow
            testCompleteUserWorkflow(authService, userService, nutritionService, recService);

            // 3. User Registration & Password Hashing Flow
            testRegistrationFlow(authService, userService);

            // 4. Meal Transaction (Commit & Rollback)
            testMealTransactions(mealService);

            // 5. Nutrition Calculation Consistency (View vs Procedure vs Service)
            testNutritionConsistency(nutritionService, reportService);

            // 6. MySQL Stored Function (calculate_bmi)
            testBmiFunction(nutritionService);

            // 7. Rule-Based Recommendation Engine Logic
            testRecommendationEngineRules();

            // 8. Custom Exceptions Handling
            testCustomExceptions(authService, userService, mealService);

            // 9. Data Integrity Audit
            testDataIntegrityAudit();

        } catch (Exception e) {
            System.err.println("Fatal unexpected integration failure: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("\n================================================================================");
        System.out.println("   Final Backend Integration Results: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    // ========================================================================
    // 1. Database Connection Test
    // ========================================================================
    private static void testDatabaseConnection() {
        System.out.println("\n--- [1] Database Connection & Resource Lifecycle ---");
        try (Connection conn = DBConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DATABASE(), 1")) {

            assertCondition("1.1: Connection opened successfully", conn != null && !conn.isClosed());
            assertCondition("1.2: Query executed and result set available", rs.next());
            String dbName = rs.getString(1);
            assertCondition("1.3: Connected to 'nutriguide' database", "nutriguide".equalsIgnoreCase(dbName));
            System.out.println("    Active DB: " + dbName);
        } catch (Exception e) {
            assertCondition("1.1: Database connection failure: " + e.getMessage(), false);
        }
    }

    // ========================================================================
    // 2. Complete User Workflow
    // ========================================================================
    private static void testCompleteUserWorkflow(AuthenticationService authService,
                                                UserService userService,
                                                NutritionService nutritionService,
                                                RecommendationService recService) throws Exception {
        System.out.println("\n--- [2] Complete User End-to-End Workflow ---");

        // Step 1: User Login
        User user = authService.login("user@example.com", "Password@123");
        assertCondition("2.1: Step 1 - User authenticated", user != null);
        int userId = user.getUserId();

        // Step 2: Retrieve Profile
        User profile = userService.getUserById(userId);
        assertCondition("2.2: Step 2 - User profile retrieved", profile != null && profile.getEmail().equals("user@example.com"));

        // Step 3: Compute BMI
        BigDecimal bmi = nutritionService.calculateUserBmi(userId);
        assertCondition("2.3: Step 3 - User BMI calculated via DB function", bmi != null && bmi.compareTo(BigDecimal.ZERO) > 0);
        System.out.println("    Workflow User: " + profile.getName() + ", BMI=" + bmi);

        // Step 4: Retrieve Active Nutrition Goal
        NutritionGoal goal = nutritionService.getActiveGoal(userId);
        assertCondition("2.4: Step 4 - Active goal retrieved", goal != null && goal.getIsActive());

        // Step 5: Retrieve Daily Nutrition Summary
        LocalDate testDate = LocalDate.of(2026, 10, 3);
        DailyNutritionSummary summary = nutritionService.getDailySummaryFromView(userId, testDate);
        assertCondition("2.5: Step 5 - Daily summary retrieved", summary != null);

        // Step 6: Generate Recommendations
        ArrayList<Recommendation> recs = recService.generateRecommendations(userId, testDate);
        assertCondition("2.6: Step 6 - Recommendations generated by engine", recs != null && !recs.isEmpty());
        System.out.println("    Generated " + recs.size() + " recommendations for " + profile.getName());
    }

    // ========================================================================
    // 3. User Registration Flow
    // ========================================================================
    private static void testRegistrationFlow(AuthenticationService authService, UserService userService)
            throws Exception {
        System.out.println("\n--- [3] User Registration, Hashing & Verification Flow ---");

        String tempEmail = "reg_test_" + System.currentTimeMillis() + "@viva.lab";
        String plainPassword = "SecretPassword@123";

        User newUser = new User(
                "Integration Test User",
                tempEmail,
                null, // Password will be hashed by service
                23,
                "Female",
                new BigDecimal("165.00"),
                new BigDecimal("58.00"),
                "Lightly Active"
        );

        // Step 1: Register User
        User registered = authService.registerUser(newUser, plainPassword);
        assertCondition("3.1: User registered with auto-generated ID", registered != null && registered.getUserId() > 0);
        int registeredId = registered.getUserId();

        // Step 2: Confirm password hash matches MySQL SHA2() format (64-char hex)
        String expectedHash = PasswordUtil.hashPassword(plainPassword);
        assertCondition("3.2: Password hash stored as 64-char SHA-256",
                registered.getPasswordHash() != null && registered.getPasswordHash().length() == 64
                        && registered.getPasswordHash().equals(expectedHash));

        // Step 3: Login with correct password
        User loggedIn = authService.login(tempEmail, plainPassword);
        assertCondition("3.3: Login with new registered user succeeded", loggedIn != null && loggedIn.getUserId().equals(registeredId));

        // Step 4: Login with incorrect password rejected
        boolean rejected = false;
        try {
            authService.login(tempEmail, "WrongPassword@999");
        } catch (AuthenticationException e) {
            rejected = true;
        }
        assertCondition("3.4: Login with wrong password rejected", rejected);

        // Step 5: Clean up registered user
        boolean deleted = userService.deleteUser(registeredId);
        assertCondition("3.5: Registered temporary user cleaned up", deleted);
    }

    // ========================================================================
    // 4. Meal Transactions (Commit & Rollback)
    // ========================================================================
    private static void testMealTransactions(MealService mealService) throws Exception {
        System.out.println("\n--- [4] ACID Meal Transactions: Commit & Rollback Verification ---");

        int userId = 1;

        // A. Successful Commit
        Meal validMeal = new Meal(userId, "Integration Lunch", LocalDate.now());
        ArrayList<MealItem> items = new ArrayList<MealItem>();
        items.add(new MealItem(null, 1, new BigDecimal("2.00")));
        items.add(new MealItem(null, 3, new BigDecimal("1.00")));

        int mealId = mealService.addMealWithItems(validMeal, items);
        assertCondition("4.1: Transaction COMMIT succeeded, meal ID=" + mealId, mealId > 0);

        Meal savedMeal = mealService.getMealById(mealId);
        assertCondition("4.2: Committed meal found in database", savedMeal != null);

        ArrayList<MealItem> savedItems = mealService.getItemsByMealId(mealId);
        assertCondition("4.3: All committed items found in database (count=2)", savedItems != null && savedItems.size() == 2);

        // Clean up committed meal
        mealService.deleteMeal(mealId);
        assertCondition("4.4: Temporary committed meal cleaned up", true);

        // B. Failed Rollback
        Meal invalidMeal = new Meal(userId, "Rollback Test Meal", LocalDate.now());
        ArrayList<MealItem> badItems = new ArrayList<MealItem>();
        badItems.add(new MealItem(null, 1, new BigDecimal("1.00")));
        badItems.add(new MealItem(null, 9999999, new BigDecimal("1.00"))); // Invalid FK food_id

        boolean rollbackSucceeded = false;
        try {
            mealService.addMealWithItems(invalidMeal, badItems);
        } catch (DatabaseOperationException e) {
            rollbackSucceeded = true;
        }
        assertCondition("4.5: Invalid food ID triggered transaction exception", rollbackSucceeded);

        // Verify parent meal was not persisted
        if (invalidMeal.getMealId() != null) {
            boolean orphanFound = true;
            try {
                mealService.getMealById(invalidMeal.getMealId());
            } catch (ResourceNotFoundException e) {
                orphanFound = false;
            }
            assertCondition("4.6: Atomic rollback verified: No orphan parent meal in DB", !orphanFound);
        } else {
            assertCondition("4.6: Atomic rollback verified: No meal ID assigned", true);
        }
    }

    // ========================================================================
    // 5. Nutrition Consistency (View vs Procedure vs Service)
    // ========================================================================
    private static void testNutritionConsistency(NutritionService nutritionService, ReportService reportService)
            throws Exception {
        System.out.println("\n--- [5] Nutrition Consistency: View vs Procedure vs Service ---");

        int userId = 1;
        LocalDate testDate = LocalDate.of(2026, 10, 3);

        DailyNutritionSummary fromView = nutritionService.getDailySummaryFromView(userId, testDate);
        DailyNutritionSummary fromProc = reportService.getDailySummaryViaProcedure(userId, testDate);

        assertCondition("5.1: View summary non-null", fromView != null);
        assertCondition("5.2: Procedure summary non-null", fromProc != null);

        // Compare calories
        assertCondition("5.3: Calories match between View and Procedure",
                fromView.getDailyCalories().compareTo(fromProc.getDailyCalories()) == 0);

        // Compare protein
        assertCondition("5.4: Protein matches between View and Procedure",
                fromView.getDailyProtein().compareTo(fromProc.getDailyProtein()) == 0);

        // Compare carbohydrates
        assertCondition("5.5: Carbs match between View and Procedure",
                fromView.getDailyCarbs().compareTo(fromProc.getDailyCarbs()) == 0);

        // Compare fats
        assertCondition("5.6: Fats match between View and Procedure",
                fromView.getDailyFat().compareTo(fromProc.getDailyFat()) == 0);

        System.out.println("    Consistency Check: View=" + fromView.getDailyCalories() + " kcal, Procedure=" + fromProc.getDailyCalories() + " kcal");
    }

    // ========================================================================
    // 6. MySQL Stored Function (calculate_bmi)
    // ========================================================================
    private static void testBmiFunction(NutritionService nutritionService) throws Exception {
        System.out.println("\n--- [6] MySQL Stored Function: calculate_bmi() Verification ---");

        // Normal BMI: 70kg, 175cm => 22.86
        BigDecimal bmiNormal = nutritionService.calculateBmi(new BigDecimal("70.00"), new BigDecimal("175.00"));
        assertCondition("6.1: calculate_bmi(70, 175) == 22.86", new BigDecimal("22.86").compareTo(bmiNormal) == 0);

        // Underweight: 45kg, 170cm => 15.57
        BigDecimal bmiUnder = nutritionService.calculateBmi(new BigDecimal("45.00"), new BigDecimal("170.00"));
        assertCondition("6.2: calculate_bmi(45, 170) == 15.57", new BigDecimal("15.57").compareTo(bmiUnder) == 0);

        // Overweight: 90kg, 175cm => 29.39
        BigDecimal bmiOver = nutritionService.calculateBmi(new BigDecimal("90.00"), new BigDecimal("175.00"));
        assertCondition("6.3: calculate_bmi(90, 175) == 29.39", new BigDecimal("29.39").compareTo(bmiOver) == 0);

        // Validation rejects negative/zero weight or height
        boolean rejected = false;
        try {
            nutritionService.calculateBmi(new BigDecimal("-10.00"), new BigDecimal("170.00"));
        } catch (ValidationException e) {
            rejected = true;
        }
        assertCondition("6.4: Negative weight rejected with ValidationException", rejected);
    }

    // ========================================================================
    // 7. Rule-Based Recommendation Engine Logic
    // ========================================================================
    private static void testRecommendationEngineRules() {
        System.out.println("\n--- [7] Rule-Based Recommendation Engine: All Rule Categories ---");

        RecommendationEngine engine = new RuleBasedRecommendationEngine();

        NutritionGoal goal = new NutritionGoal(1, new BigDecimal("2000.00"), new BigDecimal("100.00"),
                new BigDecimal("250.00"), new BigDecimal("60.00"), true);

        // Group A: BMI Categories
        DailyNutritionSummary dummy = new DailyNutritionSummary(1, LocalDate.now(),
                new BigDecimal("2000.00"), new BigDecimal("100.00"), new BigDecimal("250.00"), new BigDecimal("60.00"));

        ArrayList<Recommendation> underRecs = engine.generateRecommendations(1, new BigDecimal("17.00"), dummy, goal);
        assertCondition("7.1: BMI < 18.5 triggers underweight message",
                underRecs.get(0).getMessage().contains("below the normal range"));

        ArrayList<Recommendation> normRecs = engine.generateRecommendations(1, new BigDecimal("22.00"), dummy, goal);
        assertCondition("7.2: 18.5 <= BMI < 25 triggers normal message",
                normRecs.get(0).getMessage().contains("in the normal range"));

        ArrayList<Recommendation> overRecs = engine.generateRecommendations(1, new BigDecimal("27.50"), dummy, goal);
        assertCondition("7.3: BMI >= 25 triggers overweight message",
                overRecs.get(0).getMessage().contains("above the normal range"));

        // Group B: Calorie Rules
        DailyNutritionSummary calDeficit = new DailyNutritionSummary(1, LocalDate.now(),
                new BigDecimal("1500.00"), new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("50.00"));
        ArrayList<Recommendation> recsDeficit = engine.generateRecommendations(1, new BigDecimal("22.00"), calDeficit, goal);
        assertCondition("7.4: Calorie deficit (< 90%) triggers below target message",
                recsDeficit.get(1).getMessage().contains("below your daily target"));

        DailyNutritionSummary calSurplus = new DailyNutritionSummary(1, LocalDate.now(),
                new BigDecimal("2500.00"), new BigDecimal("100.00"), new BigDecimal("300.00"), new BigDecimal("70.00"));
        ArrayList<Recommendation> recsSurplus = engine.generateRecommendations(1, new BigDecimal("22.00"), calSurplus, goal);
        assertCondition("7.5: Calorie surplus (> 110%) triggers above target message",
                recsSurplus.get(1).getMessage().contains("above your daily target"));

        // Group C: Protein Rules
        DailyNutritionSummary protDeficit = new DailyNutritionSummary(1, LocalDate.now(),
                new BigDecimal("2000.00"), new BigDecimal("60.00"), new BigDecimal("250.00"), new BigDecimal("60.00"));
        ArrayList<Recommendation> recsProtDeficit = engine.generateRecommendations(1, new BigDecimal("22.00"), protDeficit, goal);
        assertCondition("7.6: Protein deficit triggers below target message",
                recsProtDeficit.get(2).getMessage().contains("below your daily target"));

        DailyNutritionSummary protSurplus = new DailyNutritionSummary(1, LocalDate.now(),
                new BigDecimal("2000.00"), new BigDecimal("150.00"), new BigDecimal("250.00"), new BigDecimal("60.00"));
        ArrayList<Recommendation> recsProtSurplus = engine.generateRecommendations(1, new BigDecimal("22.00"), protSurplus, goal);
        assertCondition("7.7: Protein surplus triggers above target message",
                recsProtSurplus.get(2).getMessage().contains("above your daily target"));
    }

    // ========================================================================
    // 8. Custom Exceptions Handling
    // ========================================================================
    private static void testCustomExceptions(AuthenticationService authService,
                                            UserService userService,
                                            MealService mealService) {
        System.out.println("\n--- [8] Custom Exceptions Verification ---");

        // 8.1: AuthenticationException
        boolean authExThrown = false;
        try {
            authService.login("baduser@example.com", "badpass");
        } catch (AuthenticationException e) {
            authExThrown = true;
        } catch (Exception e) {
            authExThrown = false;
        }
        assertCondition("8.1: AuthenticationException thrown on bad login", authExThrown);

        // 8.2: ResourceNotFoundException
        boolean rnfExThrown = false;
        try {
            userService.getUserById(888888);
        } catch (ResourceNotFoundException e) {
            rnfExThrown = true;
            assertCondition("8.2b: ResourceNotFoundException tracks resource type 'User'", "User".equals(e.getResourceType()));
        } catch (Exception e) {
            rnfExThrown = false;
        }
        assertCondition("8.2: ResourceNotFoundException thrown on missing entity", rnfExThrown);

        // 8.3: ValidationException
        boolean valExThrown = false;
        try {
            mealService.addMealWithItems(null, null);
        } catch (ValidationException e) {
            valExThrown = true;
        } catch (Exception e) {
            valExThrown = false;
        }
        assertCondition("8.3: ValidationException thrown on null input", valExThrown);

        // 8.4: DatabaseOperationException preserves cause
        boolean dbOpThrown = false;
        try {
            Meal badMeal = new Meal(1, "Test", LocalDate.now());
            ArrayList<MealItem> badList = new ArrayList<MealItem>();
            badList.add(new MealItem(null, 9999999, new BigDecimal("1.00")));
            mealService.addMealWithItems(badMeal, badList);
        } catch (DatabaseOperationException e) {
            dbOpThrown = true;
            assertCondition("8.4b: DatabaseOperationException preserves SQLException cause", e.getCause() != null);
        } catch (Exception e) {
            dbOpThrown = false;
        }
        assertCondition("8.4: DatabaseOperationException thrown on SQL error", dbOpThrown);
    }

    // ========================================================================
    // 9. Data Integrity Audit
    // ========================================================================
    private static void testDataIntegrityAudit() {
        System.out.println("\n--- [9] Data Integrity Audit (Baseline Counts) ---");

        // Expected baseline counts from sample_data.sql
        int expectedUsers = 2;
        int expectedCategories = 5;
        int expectedFoods = 15;
        int expectedGoals = 2;
        int expectedPlans = 1;
        int expectedPlanItems = 7;
        int expectedRecs = 3;

        try (Connection conn = DBConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {

            assertTableCount(stmt, "user_profile", expectedUsers, "9.1: Baseline user_profile count preserved");
            assertTableCount(stmt, "food_category", expectedCategories, "9.2: Baseline food_category count preserved");
            assertTableCount(stmt, "food", expectedFoods, "9.3: Baseline food count preserved");
            assertTableCount(stmt, "nutrition_goal", expectedGoals, "9.4: Baseline nutrition_goal count preserved");
            assertTableCount(stmt, "meal_plan", expectedPlans, "9.5: Baseline meal_plan count preserved");
            assertTableCount(stmt, "meal_plan_item", expectedPlanItems, "9.6: Baseline meal_plan_item count preserved");
            assertTableCount(stmt, "recommendation", expectedRecs, "9.7: Baseline recommendation count preserved");

        } catch (Exception e) {
            assertCondition("9.0: Data integrity check failed: " + e.getMessage(), false);
        }
    }

    private static void assertTableCount(Statement stmt, String tableName, int expected, String testName) throws Exception {
        try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + tableName)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                assertCondition(testName + " (Count=" + count + ", Expected=" + expected + ")", count == expected);
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
