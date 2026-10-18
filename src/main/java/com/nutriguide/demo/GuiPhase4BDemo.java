package com.nutriguide.demo;

import com.nutriguide.gui.DashboardFrame;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.Recommendation;
import com.nutriguide.model.User;
import com.nutriguide.service.NutritionService;
import com.nutriguide.service.RecommendationService;
import com.nutriguide.service.UserService;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * GuiPhase4BDemo: Programmatic verification of the Java Swing GUI (Phase 4B: Main Dashboard + Navigation).
 *
 * Verifies:
 * 1. DashboardFrame can be constructed with a User.
 * 2. Dashboard correctly preserves and presents User profile data.
 * 3. BMI section calculates BMI via NutritionService (stored function).
 * 4. Nutrition summary section retrieves today's intake via NutritionService.
 * 5. Active goal section displays target calories & macros via NutritionService.
 * 6. Recommendation preview section produces rule-based guidelines via RecommendationService.
 * 7. Navigation buttons exist and have ActionListeners attached.
 * 8. Logout button exists and has ActionListener attached.
 * 9. Architecture Rule: DashboardFrame contains NO direct JDBC / SQL dependencies.
 */
public class GuiPhase4BDemo {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   NutriGuide Phase 4B: Main Dashboard & Navigation Verification");
        System.out.println("================================================================================");

        DashboardFrame dashboard = null;

        try {
            UserService userService = new UserService();
            User user = userService.getUserById(1); // User 1: John Doe

            // 1. Test Dashboard Instantiation & User Binding
            dashboard = testDashboardInstantiation(user);

            // 2. Test BMI Section (via NutritionService)
            testBmiSection(dashboard);

            // 3. Test Today's Nutrition Intake Section
            testNutritionSection(dashboard);

            // 4. Test Active Goal Section
            testActiveGoalSection(dashboard);

            // 5. Test Recommendation Preview Section
            testRecommendationSection(dashboard);

            // 6. Test Navigation & Logout Buttons
            testNavigationButtons(dashboard);

            // 7. Verify Architectural Integrity (No SQL/JDBC in DashboardFrame)
            testArchitecturalIntegrity();

        } catch (Exception e) {
            System.err.println("Unexpected exception in GuiPhase4BDemo: " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            if (dashboard != null) {
                dashboard.dispose();
            }
        }

        // Final Summary
        System.out.println("\n================================================================================");
        System.out.println("   Phase 4B Dashboard Verification Summary");
        System.out.println("================================================================================");
        System.out.println("Total Tests Run: " + (passed + failed));
        System.out.println("Tests Passed:    " + passed);
        System.out.println("Tests Failed:    " + failed);
        System.out.println("Pass Rate:       " + (passed * 100 / (passed + failed)) + "%");
        System.out.println("================================================================================\n");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static DashboardFrame testDashboardInstantiation(User user) {
        System.out.println("\n--- [1] Testing DashboardFrame Instantiation & Header ---");

        DashboardFrame frame = new DashboardFrame(user, new NutritionService(), new RecommendationService());
        assertCondition("1.1: DashboardFrame instantiated successfully", frame != null);
        assertCondition("1.2: Window title contains 'NutriGuide Dashboard'",
                frame.getTitle() != null && frame.getTitle().contains("NutriGuide Dashboard"));
        assertCondition("1.3: User model stored in dashboard matches User 1",
                frame.getUser() != null && frame.getUser().getUserId() == 1);

        JLabel welcomeLabel = frame.getWelcomeLabel();
        assertCondition("1.4: Welcome label is present and contains user name",
                welcomeLabel != null && welcomeLabel.getText().contains("John Doe"));

        return frame;
    }

    private static void testBmiSection(DashboardFrame frame) {
        System.out.println("\n--- [2] Testing BMI Calculation Section ---");

        BigDecimal bmi = frame.getBmi();
        assertCondition("2.1: BMI was computed via NutritionService", bmi != null);
        assertCondition("2.2: Computed BMI matches expected value 22.86",
                bmi != null && new BigDecimal("22.86").compareTo(bmi) == 0);

        JLabel bmiLabel = frame.getBmiLabel();
        assertCondition("2.3: BMI label displays formatted BMI",
                bmiLabel != null && bmiLabel.getText().contains("22.86"));
    }

    private static void testNutritionSection(DashboardFrame frame) {
        System.out.println("\n--- [3] Testing Today's Nutrition Summary Section ---");

        DailyNutritionSummary summary = frame.getDailySummary();
        assertCondition("3.1: DailyNutritionSummary loaded via NutritionService", summary != null);

        JLabel calLabel = frame.getCaloriesLabel();
        assertCondition("3.2: Calories label is present and formatted",
                calLabel != null && calLabel.getText().contains("Calories:"));
    }

    private static void testActiveGoalSection(DashboardFrame frame) {
        System.out.println("\n--- [4] Testing Active Nutrition Goal Section ---");

        NutritionGoal goal = frame.getActiveGoal();
        assertCondition("4.1: Active goal loaded for User 1", goal != null);
        if (goal != null) {
            assertCondition("4.2: Target calories match user 1 goal (2200.00 kcal)",
                    new BigDecimal("2200.00").compareTo(goal.getTargetCalories()) == 0);
            JLabel targetCalLabel = frame.getTargetCaloriesLabel();
            assertCondition("4.3: Target calories label displays target",
                    targetCalLabel != null && targetCalLabel.getText().contains("2200.00"));
        }
    }

    private static void testRecommendationSection(DashboardFrame frame) {
        System.out.println("\n--- [5] Testing Recommendation Preview Section ---");

        ArrayList<Recommendation> recs = frame.getRecommendations();
        assertCondition("5.1: Recommendations list is initialized", recs != null);
        assertCondition("5.2: Recommendations produced by rule-based engine", recs != null && !recs.isEmpty());
        if (recs != null && !recs.isEmpty()) {
            System.out.println("    Sample Preview Recommendation: " + recs.get(0).getMessage());
        }
    }

    private static void testNavigationButtons(DashboardFrame frame) {
        System.out.println("\n--- [6] Testing Navigation & Logout Buttons ---");

        JButton mealLogBtn = frame.getMealLogButton();
        JButton goalBtn = frame.getGoalButton();
        JButton recBtn = frame.getRecButton();
        JButton reportsBtn = frame.getReportsButton();
        JButton profileBtn = frame.getProfileButton();
        JButton logoutBtn = frame.getLogoutButton();

        assertCondition("6.1: Meal Log button exists", mealLogBtn != null);
        assertCondition("6.2: Nutrition Goal button exists", goalBtn != null);
        assertCondition("6.3: Recommendations button exists", recBtn != null);
        assertCondition("6.4: Reports button exists", reportsBtn != null);
        assertCondition("6.5: Profile button exists", profileBtn != null);
        assertCondition("6.6: Logout button exists", logoutBtn != null);

        ActionListener[] mealListeners = mealLogBtn.getActionListeners();
        assertCondition("6.7: Meal Log button has ActionListener attached",
                mealListeners != null && mealListeners.length > 0);

        ActionListener[] goalListeners = goalBtn.getActionListeners();
        assertCondition("6.8: Goal button has ActionListener attached",
                goalListeners != null && goalListeners.length > 0);

        ActionListener[] recListeners = recBtn.getActionListeners();
        assertCondition("6.9: Recommendations button has ActionListener attached",
                recListeners != null && recListeners.length > 0);

        ActionListener[] reportListeners = reportsBtn.getActionListeners();
        assertCondition("6.10: Reports button has ActionListener attached",
                reportListeners != null && reportListeners.length > 0);

        ActionListener[] profileListeners = profileBtn.getActionListeners();
        assertCondition("6.11: Profile button has ActionListener attached",
                profileListeners != null && profileListeners.length > 0);

        ActionListener[] logoutListeners = logoutBtn.getActionListeners();
        assertCondition("6.12: Logout button has ActionListener attached",
                logoutListeners != null && logoutListeners.length > 0);
    }

    private static void testArchitecturalIntegrity() {
        System.out.println("\n--- [7] Verifying Architectural Integrity (No JDBC in GUI) ---");

        Class<?> dashboardClass = DashboardFrame.class;
        boolean hasDirectConnection = false;
        for (Field f : dashboardClass.getDeclaredFields()) {
            if (f.getType().getName().startsWith("java.sql.")) {
                hasDirectConnection = true;
                break;
            }
        }
        assertCondition("7.1: DashboardFrame has NO java.sql fields (Connection/PreparedStatement/ResultSet)",
                !hasDirectConnection);
    }

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
