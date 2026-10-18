package com.nutriguide.demo;

import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.Meal;
import com.nutriguide.model.MealItem;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.User;
import com.nutriguide.service.AuthenticationService;
import com.nutriguide.service.MealService;
import com.nutriguide.service.NutritionService;
import com.nutriguide.service.ReportService;
import com.nutriguide.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * ServiceLayerDemo: Verifies the business logic and transaction integrity of the Service Layer.
 *
 * Demonstrates:
 * - Successful & failed authentication
 * - Input validation & custom exception propagation
 * - Stored function execution (calculate_bmi)
 * - Stored procedure execution (get_daily_summary with OUT parameters)
 * - View querying (daily_nutrition_summary)
 * - JDBC Transaction COMMIT test (Meal + MealItems)
 * - JDBC Transaction ROLLBACK test (Atomic rollback verifying no orphan meal)
 * - Comprehensive reporting using StringBuffer
 */
public class ServiceLayerDemo {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   NutriGuide Phase 3D: Service Layer & Transaction Verification");
        System.out.println("================================================================================");

        DatabaseSafetyGuard.requireDisposableDatabase("ServiceLayerDemo");

        try {
            AuthenticationService authService = new AuthenticationService();
            UserService userService = new UserService();
            NutritionService nutritionService = new NutritionService();
            MealService mealService = new MealService();
            ReportService reportService = new ReportService();

            // A & B: Authentication (Success & Failure)
            testAuthentication(authService);

            // C: User profile retrieval & Not Found handling
            testUserService(userService);

            // D: BMI calculation (via MySQL stored function)
            testBmiCalculation(nutritionService);

            // E & F: Nutrition Goal management
            testNutritionGoals(nutritionService);

            // G: Daily nutrition summary (Stored Procedure & View)
            testDailySummaries(nutritionService, reportService);

            // H: Meal history
            testMealHistory(mealService);

            // I: Controlled meal transaction (COMMIT test)
            testTransactionCommit(mealService);

            // J: Controlled meal transaction (ROLLBACK test)
            testTransactionRollback(mealService);

            // K: Nutrition Report Generation (using StringBuffer)
            testReportGeneration(reportService);

        } catch (Exception e) {
            System.err.println("Unexpected ServiceLayerDemo failure: " + e.getMessage());
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
    // Section A & B: Authentication Tests
    // ========================================================================
    private static void testAuthentication(AuthenticationService authService) throws DatabaseOperationException {
        System.out.println("\n--- [A & B] AuthenticationService: Login & Failure Scenarios ---");

        // A1: Successful login with correct credentials
        User loggedInUser = authService.login("user@example.com", "Password@123");
        assertCondition("A1: Successful login with correct credentials", loggedInUser != null);
        if (loggedInUser != null) {
            assertCondition("A2: Authenticated user matches email", "user@example.com".equals(loggedInUser.getEmail()));
            System.out.println("    Logged in: " + loggedInUser.getName() + " (" + loggedInUser.getEmail() + ")");
        }

        // B1: Failed login with incorrect password
        boolean wrongPassCaught = false;
        try {
            authService.login("user@example.com", "WrongPassword!");
        } catch (AuthenticationException e) {
            wrongPassCaught = true;
            assertCondition("B1: Incorrect password throws AuthenticationException", true);
            System.out.println("    Caught expected: " + e.getMessage());
        }
        if (!wrongPassCaught) {
            assertCondition("B1: Incorrect password did NOT throw AuthenticationException", false);
        }

        // B2: Failed login with non-existent email
        boolean wrongEmailCaught = false;
        try {
            authService.login("nonexistent@example.com", "Password@123");
        } catch (AuthenticationException e) {
            wrongEmailCaught = true;
            assertCondition("B2: Non-existent email throws AuthenticationException", true);
        }
        if (!wrongEmailCaught) {
            assertCondition("B2: Non-existent email did NOT throw AuthenticationException", false);
        }

        // B3: Blank email throws ValidationException
        boolean blankEmailCaught = false;
        try {
            authService.login("", "Password@123");
        } catch (ValidationException e) {
            blankEmailCaught = true;
            assertCondition("B3: Blank email throws ValidationException", true);
        }
        if (!blankEmailCaught) {
            assertCondition("B3: Blank email did NOT throw ValidationException", false);
        }
    }

    // ========================================================================
    // Section C: UserService Tests
    // ========================================================================
    private static void testUserService(UserService userService) throws DatabaseOperationException {
        System.out.println("\n--- [C] UserService: User Retrieval & ResourceNotFoundException ---");

        // C1: Retrieve valid user
        User user = userService.getUserById(1);
        assertCondition("C1: User with ID=1 retrieved successfully", user != null && user.getUserId() == 1);

        // C2: Request non-existent user throws ResourceNotFoundException
        boolean notFoundCaught = false;
        try {
            userService.getUserById(99999);
        } catch (ResourceNotFoundException e) {
            notFoundCaught = true;
            assertCondition("C2: Non-existent user throws ResourceNotFoundException", true);
            System.out.println("    Caught expected: " + e.getMessage());
        }
        if (!notFoundCaught) {
            assertCondition("C2: Non-existent user did NOT throw ResourceNotFoundException", false);
        }
    }

    // ========================================================================
    // Section D: BMI Calculation via MySQL Function
    // ========================================================================
    private static void testBmiCalculation(NutritionService nutritionService) throws DatabaseOperationException {
        System.out.println("\n--- [D] NutritionService: calculateBmi via MySQL stored function ---");

        // D1: Explicit weight and height (70 kg, 175 cm => 70 / (1.75^2) = 22.86)
        BigDecimal bmi = nutritionService.calculateBmi(new BigDecimal("70.00"), new BigDecimal("175.00"));
        assertCondition("D1: BMI calculated via DB stored function", bmi != null);
        if (bmi != null) {
            assertCondition("D2: BMI matches expected value 22.86", new BigDecimal("22.86").compareTo(bmi) == 0);
            System.out.println("    Calculated BMI for 70kg, 175cm: " + bmi);
        }

        // D3: User BMI lookup
        BigDecimal userBmi = nutritionService.calculateUserBmi(1);
        assertCondition("D3: User 1 BMI lookup succeeded", userBmi != null && userBmi.compareTo(BigDecimal.ZERO) > 0);
        System.out.println("    User 1 Profile BMI: " + userBmi);
    }

    // ========================================================================
    // Section E & F: Nutrition Goals
    // ========================================================================
    private static void testNutritionGoals(NutritionService nutritionService) throws DatabaseOperationException {
        System.out.println("\n--- [E & F] NutritionService: Goals & Validation ---");

        // E1: Get active goal for user 1
        NutritionGoal activeGoal = nutritionService.getActiveGoal(1);
        assertCondition("E1: Active goal for user 1 retrieved", activeGoal != null);
        if (activeGoal != null) {
            assertCondition("E2: Active goal target calories > 0",
                    activeGoal.getTargetCalories().compareTo(BigDecimal.ZERO) > 0);
            System.out.println("    User 1 Active Goal: " + activeGoal.getTargetCalories() + " kcal, Protein: " +
                    activeGoal.getTargetProtein() + "g");
        }

        // F1: Validation rejects negative target calories
        boolean negativeRejected = false;
        try {
            NutritionGoal invalidGoal = new NutritionGoal(
                    1,
                    new BigDecimal("-500.00"),
                    new BigDecimal("100.00"),
                    new BigDecimal("200.00"),
                    new BigDecimal("50.00"),
                    true
            );
            nutritionService.createGoal(invalidGoal);
        } catch (ValidationException e) {
            negativeRejected = true;
            assertCondition("F1: Negative calories rejected with ValidationException", true);
            System.out.println("    Caught expected: " + e.getMessage());
        }
        if (!negativeRejected) {
            assertCondition("F1: Negative calories were NOT rejected", false);
        }
    }

    // ========================================================================
    // Section G: Daily Summaries (Procedure & View)
    // ========================================================================
    private static void testDailySummaries(NutritionService nutritionService, ReportService reportService)
            throws DatabaseOperationException {
        System.out.println("\n--- [G] NutritionService & ReportService: Stored Procedure vs View ---");

        int userId = 1;
        LocalDate date = LocalDate.of(2026, 10, 3);

        // G1: Summary via MySQL stored procedure get_daily_summary (CallableStatement)
        DailyNutritionSummary procSummary = reportService.getDailySummaryViaProcedure(userId, date);
        assertCondition("G1: Procedure get_daily_summary executed via CallableStatement", procSummary != null);
        if (procSummary != null) {
            System.out.println("    Procedure Output: " + procSummary);
            assertCondition("G2: Procedure returned non-negative calories",
                    procSummary.getDailyCalories().compareTo(BigDecimal.ZERO) >= 0);
        }

        // G3: Summary via MySQL view daily_nutrition_summary
        DailyNutritionSummary viewSummary = nutritionService.getDailySummaryFromView(userId, date);
        assertCondition("G3: View daily_nutrition_summary queried successfully", viewSummary != null);
        if (viewSummary != null) {
            System.out.println("    View Output:      " + viewSummary);
        }
    }

    // ========================================================================
    // Section H: Meal History
    // ========================================================================
    private static void testMealHistory(MealService mealService) throws DatabaseOperationException {
        System.out.println("\n--- [H] MealService: Meal History ---");

        ArrayList<Meal> meals = mealService.getMealsByUserId(1);
        assertCondition("H1: User meal history retrieved", meals != null && !meals.isEmpty());
        if (meals != null && !meals.isEmpty()) {
            System.out.println("    User 1 logged meals count: " + meals.size());
        }
    }

    // ========================================================================
    // Section I: Transaction COMMIT Test
    // ========================================================================
    private static void testTransactionCommit(MealService mealService) throws DatabaseOperationException {
        System.out.println("\n--- [I] MealService: JDBC Transaction COMMIT Test ---");

        int userId = 1;
        Meal testMeal = new Meal(userId, "Test Transaction Lunch", LocalDate.now());

        ArrayList<MealItem> items = new ArrayList<MealItem>();
        items.add(new MealItem(null, 1, new BigDecimal("1.50"))); // Food 1
        items.add(new MealItem(null, 2, new BigDecimal("2.00"))); // Food 2

        // Execute transaction
        int generatedMealId = mealService.addMealWithItems(testMeal, items);
        assertCondition("I1: Transaction committed successfully, meal ID=" + generatedMealId, generatedMealId > 0);

        // Verify parent meal and child items exist in database
        Meal committedMeal = mealService.getMealById(generatedMealId);
        assertCondition("I2: Committed meal found in database", committedMeal != null);

        ArrayList<MealItem> committedItems = mealService.getItemsByMealId(generatedMealId);
        assertCondition("I3: All child items found in database (count=2)",
                committedItems != null && committedItems.size() == 2);

        // Clean up test meal
        boolean cleaned = mealService.deleteMeal(generatedMealId);
        assertCondition("I4: Temporary transaction meal cleaned up successfully", cleaned);
    }

    // ========================================================================
    // Section J: Transaction ROLLBACK Test
    // ========================================================================
    private static void testTransactionRollback(MealService mealService) throws DatabaseOperationException {
        System.out.println("\n--- [J] MealService: JDBC Transaction ROLLBACK Test ---");

        int userId = 1;
        Meal testMeal = new Meal(userId, "Orphan Meal That Must Be Rolled Back", LocalDate.now());

        ArrayList<MealItem> itemsWithInvalidFood = new ArrayList<MealItem>();
        itemsWithInvalidFood.add(new MealItem(null, 1, new BigDecimal("1.00"))); // Valid food
        itemsWithInvalidFood.add(new MealItem(null, 999999, new BigDecimal("1.00"))); // INVALID FOOD ID (FK violation)

        boolean rollbackOccurred = false;
        try {
            mealService.addMealWithItems(testMeal, itemsWithInvalidFood);
        } catch (DatabaseOperationException e) {
            rollbackOccurred = true;
            assertCondition("J1: FK violation triggered exception during transaction", true);
            System.out.println("    Caught expected rollback cause: " + e.getMessage());
        }

        if (!rollbackOccurred) {
            assertCondition("J1: Transaction did NOT fail on invalid FK", false);
        }

        // Verify that the meal was NOT committed (no orphan parent meal)
        if (testMeal.getMealId() != null) {
            Meal orphanCheck = null;
            try {
                orphanCheck = mealService.getMealById(testMeal.getMealId());
            } catch (ResourceNotFoundException e) {
                orphanCheck = null;
            }
            assertCondition("J2: Atomic rollback verified: parent meal was not persisted in DB", orphanCheck == null);
        } else {
            assertCondition("J2: Atomic rollback verified: no meal ID was assigned", true);
        }
    }

    // ========================================================================
    // Section K: Report Generation with StringBuffer
    // ========================================================================
    private static void testReportGeneration(ReportService reportService)
            throws ResourceNotFoundException, DatabaseOperationException {
        System.out.println("\n--- [K] ReportService: Daily Nutrition Report (Java Lab Exp 5) ---");

        String report = reportService.generateDailyUserReport(1, LocalDate.of(2026, 10, 3));
        assertCondition("K1: Nutrition report generated successfully", report != null && !report.isEmpty());
        assertCondition("K2: Report contains user name 'John Doe'", report.contains("John Doe"));
        assertCondition("K3: Report contains BMI output", report.contains("Calculated BMI:"));
        assertCondition("K4: Report contains stored procedure intake section", report.contains("DAILY INTAKE"));

        System.out.println("\nGenerated Report Preview:\n" + report);
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
