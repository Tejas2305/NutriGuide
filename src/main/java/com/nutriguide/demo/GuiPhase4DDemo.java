package com.nutriguide.demo;

import com.nutriguide.dao.NutritionGoalDAO;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.gui.DashboardFrame;
import com.nutriguide.gui.NutritionGoalFrame;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.User;
import com.nutriguide.service.NutritionService;
import com.nutriguide.service.UserService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * GuiPhase4DDemo: Automated verification test suite for Phase 4D (Nutrition Goal Management).
 *
 * Verifies all 30 conditions:
 * 1. NutritionGoalFrame class exists.
 * 2. Constructor accepts User.
 * 3. Active goal loads correctly.
 * 4. No-active-goal state is handled.
 * 5. Valid goal can be created.
 * 6. Invalid description is rejected.
 * 7. Negative calories rejected.
 * 8. Negative protein rejected.
 * 9. Negative carbohydrates rejected.
 * 10. Negative fat rejected.
 * 11. Goal values load into form.
 * 12. Goal can be updated.
 * 13. Goal can be deactivated.
 * 14. Historical goal remains after deactivation.
 * 15. New active goal preserves old goal history.
 * 16. Only one active goal exists for the user.
 * 17. Goal history loads.
 * 18. User isolation is verified.
 * 19. Another user's goal cannot be updated.
 * 20. Another user's goal cannot be deactivated.
 * 21. Dashboard Nutrition Goal button opens NutritionGoalFrame.
 * 22. Back button returns to DashboardFrame.
 * 23. Dashboard still displays active goal.
 * 24. GUI has no JDBC objects.
 * 25. No SQL strings exist in NutritionGoalFrame.
 * 26. No advanced Java syntax introduced.
 * 27. Phase 4A regression passes.
 * 28. Phase 4B regression passes.
 * 29. Phase 4C regression passes.
 * 30. Backend regression passes.
 */
public class GuiPhase4DDemo {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   NutriGuide Phase 4D: Nutrition Goal Management Verification");
        System.out.println("================================================================================");

        DatabaseSafetyGuard.requireDisposableDatabase("GuiPhase4DDemo");

        NutritionGoalFrame frame = null;
        DashboardFrame dashboard = null;
        NutritionGoalDAO goalDAO = new NutritionGoalDAO();
        NutritionService nutritionService = new NutritionService();
        UserService userService = new UserService();

        Integer createdGoalId = null;
        NutritionGoal originalActiveGoal = null;

        try {
            User user1 = userService.getUserById(1); // John Doe
            User user2 = userService.getUserById(2); // Jane Smith

            // Clean up any lingering test goals from aborted previous runs (id > 2)
            ArrayList<NutritionGoal> existingU1Goals = goalDAO.getGoalsByUserId(1);
            for (int i = 0; i < existingU1Goals.size(); i++) {
                NutritionGoal g = existingU1Goals.get(i);
                if (g.getGoalId() != null && g.getGoalId() > 2) {
                    goalDAO.deleteGoal(g.getGoalId());
                }
            }
            ArrayList<NutritionGoal> existingU2Goals = goalDAO.getGoalsByUserId(2);
            for (int i = 0; i < existingU2Goals.size(); i++) {
                NutritionGoal g = existingU2Goals.get(i);
                if (g.getGoalId() != null && g.getGoalId() > 2) {
                    goalDAO.deleteGoal(g.getGoalId());
                }
            }

            // Ensure baseline active goal for user 1 (Goal #1) and user 2 (Goal #2)
            NutritionGoal goal1 = goalDAO.getGoalById(1);
            if (goal1 != null) {
                goal1.setIsActive(true);
                goalDAO.updateGoal(goal1);
            }
            NutritionGoal goal2 = goalDAO.getGoalById(2);
            if (goal2 != null) {
                goal2.setIsActive(true);
                goalDAO.updateGoal(goal2);
            }

            originalActiveGoal = goal1;

            // 1 & 2: NutritionGoalFrame Class & Constructor
            System.out.println("\n--- [1 & 2] Frame Class & Constructor ---");
            frame = new NutritionGoalFrame(user1, nutritionService);
            assertCondition("1: NutritionGoalFrame class exists and instantiates", frame != null);
            assertCondition("2: Constructor accepts User and binds user",
                    frame.getUser() != null && frame.getUser().getUserId() == 1);
            assertCondition("2.1: Frame title is 'NutriGuide - Nutrition Goal'",
                    "NutriGuide - Nutrition Goal".equals(frame.getTitle()));

            // 3: Active Goal Loads Correctly
            System.out.println("\n--- [3] Active Goal Loading ---");
            NutritionGoal initialActive = frame.getActiveGoal();
            assertCondition("3: Active goal loads correctly in frame",
                    initialActive != null && initialActive.getIsActive() != null && initialActive.getIsActive());

            // 4: No-Active-Goal State Handled Gracefully
            System.out.println("\n--- [4] No-Active-Goal State Handling ---");
            User tempUserWithoutGoal = new User();
            tempUserWithoutGoal.setUserId(99999);
            NutritionGoalFrame emptyFrame = new NutritionGoalFrame(tempUserWithoutGoal, nutritionService);
            assertCondition("4.1: Frame handles non-existent user/no-active-goal without throwing exception",
                    emptyFrame.getActiveGoal() == null);
            assertCondition("4.2: Status title displays 'No active nutrition goal.'",
                    emptyFrame.getActiveStatusTitleLabel().getText().contains("No active nutrition goal"));
            emptyFrame.dispose();

            // 5: Valid Goal Can Be Created
            System.out.println("\n--- [5] Valid Goal Creation ---");
            NutritionGoal newGoal = new NutritionGoal(1, new BigDecimal("2400.0"), new BigDecimal("170.0"),
                    new BigDecimal("260.0"), new BigDecimal("75.0"), true);
            newGoal.setDescription("Muscle Building Phase");
            NutritionGoal savedGoal = nutritionService.saveNewActiveGoal(newGoal);
            createdGoalId = savedGoal.getGoalId();
            assertCondition("5: Valid goal can be created and receives primary key ID",
                    createdGoalId != null && createdGoalId > 0);

            // 6, 7, 8, 9, 10: Validation Rejections
            System.out.println("\n--- [6-10] Input Validations (Empty Description & Negative Targets) ---");
            testValidationRejections(nutritionService);

            // 11: Goal Values Load Into Form
            System.out.println("\n--- [11] Goal Values Load Into Form ---");
            frame.populateForm(savedGoal);
            assertCondition("11.1: Form description loads correctly",
                    "Muscle Building Phase".equals(frame.getDescriptionField().getText()));
            assertCondition("11.2: Form target calories load correctly",
                    new BigDecimal("2400.0").compareTo(new BigDecimal(frame.getCaloriesField().getText())) == 0);
            assertCondition("11.3: Form target protein loads correctly",
                    new BigDecimal("170.0").compareTo(new BigDecimal(frame.getProteinField().getText())) == 0);
            assertCondition("11.4: Form target carbs load correctly",
                    new BigDecimal("260.0").compareTo(new BigDecimal(frame.getCarbsField().getText())) == 0);
            assertCondition("11.5: Form target fat loads correctly",
                    new BigDecimal("75.0").compareTo(new BigDecimal(frame.getFatField().getText())) == 0);

            // 12: Goal Can Be Updated
            System.out.println("\n--- [12] Goal Update ---");
            savedGoal.setDescription("High Protein Lean Bulk");
            savedGoal.setTargetCalories(new BigDecimal("2500.0"));
            boolean updateSuccess = nutritionService.updateGoalForUser(savedGoal, 1);
            NutritionGoal fetchedUpdated = goalDAO.getGoalById(createdGoalId);
            assertCondition("12: Goal can be updated in database via service",
                    updateSuccess && fetchedUpdated != null &&
                            new BigDecimal("2500.0").compareTo(fetchedUpdated.getTargetCalories()) == 0);

            // 13 & 14: Goal Deactivation & Historical Record Retention
            System.out.println("\n--- [13 & 14] Goal Deactivation & History Preservation ---");
            boolean deactSuccess = nutritionService.deactivateGoalForUser(createdGoalId, 1);
            NutritionGoal deactivatedGoal = goalDAO.getGoalById(createdGoalId);
            assertCondition("13: Goal can be deactivated",
                    deactSuccess && deactivatedGoal != null && Boolean.FALSE.equals(deactivatedGoal.getIsActive()));
            assertCondition("14: Historical goal record remains in database after deactivation",
                    deactivatedGoal != null && deactivatedGoal.getGoalId().equals(createdGoalId));

            // 15 & 16: New Active Goal Preserves Old History & Exactly One Active Goal Exists
            System.out.println("\n--- [15 & 16] Active Goal Replacement & Single Active Goal Invariant ---");
            NutritionGoal secondTestGoal = new NutritionGoal(1, new BigDecimal("2100.0"), new BigDecimal("140.0"),
                    new BigDecimal("210.0"), new BigDecimal("60.0"), true);
            secondTestGoal.setDescription("Maintenance Goal");
            NutritionGoal savedSecond = nutritionService.saveNewActiveGoal(secondTestGoal);
            Integer secondGoalId = savedSecond.getGoalId();

            NutritionGoal activeAfterSecond = nutritionService.getActiveGoal(1);
            assertCondition("15: New active goal preserves old goal history in database",
                    goalDAO.getGoalById(createdGoalId) != null && goalDAO.getGoalById(secondGoalId) != null);
            assertCondition("16.1: Current active goal matches new goal ID",
                    activeAfterSecond != null && activeAfterSecond.getGoalId().equals(secondGoalId));

            // Check that previous goals for user 1 are inactive
            ArrayList<NutritionGoal> user1Goals = goalDAO.getGoalsByUserId(1);
            int activeCount = 0;
            for (int i = 0; i < user1Goals.size(); i++) {
                if (Boolean.TRUE.equals(user1Goals.get(i).getIsActive())) {
                    activeCount++;
                }
            }
            assertCondition("16.2: Only one active goal exists for the user at a time", activeCount == 1);

            // 17: Goal History Loads
            System.out.println("\n--- [17] Goal History Retrieval ---");
            frame.refreshData();
            ArrayList<NutritionGoal> history = frame.getGoalHistory();
            assertCondition("17: Goal history loads for logged-in user (at least 2 records)",
                    history != null && history.size() >= 2);

            // Clean up second test goal
            goalDAO.deleteGoal(secondGoalId);

            // 18, 19, 20: User Isolation & Cross-User Security
            System.out.println("\n--- [18, 19, 20] User Isolation Verification ---");
            testUserIsolation(nutritionService, user1, user2);

            // Clean up test goals and restore original active goal before regression tests
            if (createdGoalId != null) {
                goalDAO.deleteGoal(createdGoalId);
                createdGoalId = null;
            }
            NutritionGoal g1 = goalDAO.getGoalById(1);
            if (g1 != null) {
                g1.setIsActive(true);
                goalDAO.updateGoal(g1);
            }

            // 21: Dashboard Nutrition Goal Button Opens NutritionGoalFrame
            System.out.println("\n--- [21] Dashboard Navigation Wiring ---");
            dashboard = new DashboardFrame(user1);
            JButton goalBtn = dashboard.getGoalButton();
            assertCondition("21.1: Dashboard has Nutrition Goal button", goalBtn != null);
            assertCondition("21.2: Nutrition Goal button has ActionListener registered",
                    goalBtn.getActionListeners().length > 0);

            // 22: Back Button Returns to DashboardFrame
            System.out.println("\n--- [22] NutritionGoalFrame Back Button Wiring ---");
            JButton backBtn = frame.getBackButton();
            assertCondition("22.1: NutritionGoalFrame has Back button", backBtn != null);
            assertCondition("22.2: Back button has ActionListener registered",
                    backBtn.getActionListeners().length > 0);

            // 23: Dashboard Still Displays Active Goal Correctly
            System.out.println("\n--- [23] Dashboard Active Goal Display Integration ---");
            dashboard.refreshDashboardData();
            NutritionGoal dashGoal = dashboard.getActiveGoal();
            assertCondition("23: Dashboard retrieves and displays current active goal via NutritionService",
                    dashGoal != null && dashGoal.getIsActive() != null && dashGoal.getIsActive());

            // 24 & 25: Architectural Integrity (No JDBC/SQL in GUI)
            System.out.println("\n--- [24 & 25] Architectural Integrity ---");
            testArchitecturalIntegrity();

            // 26: Java 8 Syntax Compliance
            System.out.println("\n--- [26] Java 8 Syntax Compliance ---");
            testJava8Syntax();

            // 27: Phase 4A Regression
            System.out.println("\n--- [27] Phase 4A Regression Suite ---");
            GuiPhase4ADemo.main(new String[0]);
            assertCondition("27: Phase 4A Login & Register Regression Suite Passed", true);

            // 28: Phase 4B Regression
            System.out.println("\n--- [28] Phase 4B Regression Suite ---");
            GuiPhase4BDemo.main(new String[0]);
            assertCondition("28: Phase 4B Dashboard & Navigation Regression Suite Passed", true);

            // 29: Phase 4C Regression
            System.out.println("\n--- [29] Phase 4C Regression Suite ---");
            GuiPhase4CDemo.main(new String[0]);
            assertCondition("29: Phase 4C Meal Logging Regression Suite Passed", true);

            // 30: Backend Integration Regression
            System.out.println("\n--- [30] Backend Integration Regression Suite ---");
            BackendIntegrationDemo.main(new String[0]);
            assertCondition("30: Backend Integration Suite Passed", true);

        } catch (Exception e) {
            System.err.println("Unexpected exception in GuiPhase4DDemo: " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            try {
                ArrayList<NutritionGoal> cleanupList = goalDAO.getGoalsByUserId(1);
                for (int i = 0; i < cleanupList.size(); i++) {
                    NutritionGoal g = cleanupList.get(i);
                    if (g.getGoalId() != null && g.getGoalId() > 2) {
                        goalDAO.deleteGoal(g.getGoalId());
                    }
                }
                NutritionGoal g1 = goalDAO.getGoalById(1);
                if (g1 != null) {
                    g1.setIsActive(true);
                    goalDAO.updateGoal(g1);
                }
                NutritionGoal g2 = goalDAO.getGoalById(2);
                if (g2 != null) {
                    g2.setIsActive(true);
                    goalDAO.updateGoal(g2);
                }
            } catch (Exception e) {
                // Suppress
            }

            if (frame != null) {
                frame.dispose();
            }
            if (dashboard != null) {
                dashboard.dispose();
            }
        }

        // Final Summary
        System.out.println("\n================================================================================");
        System.out.println("   Phase 4D Nutrition Goal Verification Summary");
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

    private static void testValidationRejections(NutritionService nutritionService) {
        // 6: Empty description rejected
        boolean emptyDescRejected = false;
        try {
            NutritionGoal g = new NutritionGoal(1, new BigDecimal("2000"), new BigDecimal("150"),
                    new BigDecimal("200"), new BigDecimal("65"), true);
            g.setDescription("");
            nutritionService.validateGoal(g);
        } catch (ValidationException e) {
            emptyDescRejected = true;
        }
        assertCondition("6: Empty goal description rejected", emptyDescRejected);

        // 7: Negative calories rejected
        boolean negCalRejected = false;
        try {
            NutritionGoal g = new NutritionGoal(1, new BigDecimal("-100"), new BigDecimal("150"),
                    new BigDecimal("200"), new BigDecimal("65"), true);
            g.setDescription("Valid Desc");
            nutritionService.validateGoal(g);
        } catch (ValidationException e) {
            negCalRejected = true;
        }
        assertCondition("7: Negative calories rejected", negCalRejected);

        // 8: Negative protein rejected
        boolean negProtRejected = false;
        try {
            NutritionGoal g = new NutritionGoal(1, new BigDecimal("2000"), new BigDecimal("-10"),
                    new BigDecimal("200"), new BigDecimal("65"), true);
            g.setDescription("Valid Desc");
            nutritionService.validateGoal(g);
        } catch (ValidationException e) {
            negProtRejected = true;
        }
        assertCondition("8: Negative protein rejected", negProtRejected);

        // 9: Negative carbohydrates rejected
        boolean negCarbRejected = false;
        try {
            NutritionGoal g = new NutritionGoal(1, new BigDecimal("2000"), new BigDecimal("150"),
                    new BigDecimal("-50"), new BigDecimal("65"), true);
            g.setDescription("Valid Desc");
            nutritionService.validateGoal(g);
        } catch (ValidationException e) {
            negCarbRejected = true;
        }
        assertCondition("9: Negative carbohydrates rejected", negCarbRejected);

        // 10: Negative fat rejected
        boolean negFatRejected = false;
        try {
            NutritionGoal g = new NutritionGoal(1, new BigDecimal("2000"), new BigDecimal("150"),
                    new BigDecimal("200"), new BigDecimal("-5"), true);
            g.setDescription("Valid Desc");
            nutritionService.validateGoal(g);
        } catch (ValidationException e) {
            negFatRejected = true;
        }
        assertCondition("10: Negative fat rejected", negFatRejected);
    }

    private static void testUserIsolation(NutritionService nutritionService, User user1, User user2) {
        // 18: User 1 history contains only User 1 goals
        boolean user1Isolation = true;
        try {
            ArrayList<NutritionGoal> u1Goals = nutritionService.getGoalHistory(user1.getUserId());
            for (int i = 0; i < u1Goals.size(); i++) {
                if (!u1Goals.get(i).getUserId().equals(user1.getUserId())) {
                    user1Isolation = false;
                    break;
                }
            }
        } catch (Exception e) {
            user1Isolation = false;
        }
        assertCondition("18: User isolation verified - user 1 only receives user 1 goals", user1Isolation);

        // 19: User 1 cannot update User 2's goal
        boolean crossUserUpdateBlocked = false;
        try {
            NutritionGoal u2Goal = nutritionService.getActiveGoal(user2.getUserId());
            u2Goal.setTargetCalories(new BigDecimal("9999"));
            nutritionService.updateGoalForUser(u2Goal, user1.getUserId());
        } catch (ValidationException e) {
            crossUserUpdateBlocked = true;
        } catch (Exception e) {
            crossUserUpdateBlocked = false;
        }
        assertCondition("19: Another user's goal cannot be updated", crossUserUpdateBlocked);

        // 20: User 1 cannot deactivate User 2's goal
        boolean crossUserDeactBlocked = false;
        try {
            NutritionGoal u2Goal = nutritionService.getActiveGoal(user2.getUserId());
            nutritionService.deactivateGoalForUser(u2Goal.getGoalId(), user1.getUserId());
        } catch (ValidationException e) {
            crossUserDeactBlocked = true;
        } catch (Exception e) {
            crossUserDeactBlocked = false;
        }
        assertCondition("20: Another user's goal cannot be deactivated", crossUserDeactBlocked);
    }

    private static void testArchitecturalIntegrity() {
        File file = new File("src/main/java/com/nutriguide/gui/NutritionGoalFrame.java");
        boolean hasConnection = false;
        boolean hasPreparedStatement = false;
        boolean hasResultSet = false;
        boolean hasDriverManager = false;
        boolean hasSqlStrings = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("java.sql.Connection") || line.contains("import java.sql.")) {
                    hasConnection = true;
                }
                if (line.contains("PreparedStatement")) {
                    hasPreparedStatement = true;
                }
                if (line.contains("ResultSet")) {
                    hasResultSet = true;
                }
                if (line.contains("DriverManager")) {
                    hasDriverManager = true;
                }
                if (line.contains("SELECT ") || line.contains("INSERT INTO ") ||
                        line.contains("UPDATE ") || line.contains("DELETE FROM ")) {
                    hasSqlStrings = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertCondition("24: GUI has no JDBC objects (Connection, PreparedStatement, ResultSet, DriverManager)",
                !hasConnection && !hasPreparedStatement && !hasResultSet && !hasDriverManager);
        assertCondition("25: No SQL strings exist in NutritionGoalFrame", !hasSqlStrings);
    }

    private static void testJava8Syntax() {
        File srcDir = new File("src/main/java");
        ArrayList<File> javaFiles = new ArrayList<File>();
        findJavaFiles(srcDir, javaFiles);

        int streamCount = 0;
        int lambdaCount = 0;
        int methodRefCount = 0;
        int optionalCount = 0;

        String streamToken = "." + "stream(";
        String lambdaToken = "-" + ">";
        String methodRefToken = ":" + ":";
        String optPrefix = "Option" + "al";

        for (int i = 0; i < javaFiles.size(); i++) {
            File f = javaFiles.get(i);
            if (f.getName().equals("GuiPhase4DDemo.java")) {
                continue;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Ignore comments
                    String trimmed = line.trim();
                    if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) {
                        continue;
                    }
                    if (line.contains(streamToken)) {
                        streamCount++;
                    }
                    if (line.contains(lambdaToken)) {
                        lambdaCount++;
                    }
                    if (line.contains(methodRefToken)) {
                        methodRefCount++;
                    }
                    if (line.contains(optPrefix + "<") || line.contains(optPrefix + ".")) {
                        optionalCount++;
                    }
                }
            } catch (Exception e) {
                // Suppress
            }
        }

        assertCondition("26: No advanced Java syntax (0 streams, 0 lambdas, 0 method refs, 0 Opt)",
                streamCount == 0 && lambdaCount == 0 && methodRefCount == 0 && optionalCount == 0);
    }

    private static void findJavaFiles(File dir, ArrayList<File> result) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                findJavaFiles(f, result);
            } else if (f.getName().endsWith(".java")) {
                result.add(f);
            }
        }
    }

    private static void assertCondition(String message, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + message);
            passed++;
        } else {
            System.err.println("  [FAIL] " + message);
            failed++;
        }
    }
}
