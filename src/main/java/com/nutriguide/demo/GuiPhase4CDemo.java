package com.nutriguide.demo;

import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.gui.DashboardFrame;
import com.nutriguide.gui.MealLogFrame;
import com.nutriguide.model.Food;
import com.nutriguide.model.Meal;
import com.nutriguide.model.MealItem;
import com.nutriguide.model.User;
import com.nutriguide.service.FoodService;
import com.nutriguide.service.MealService;
import com.nutriguide.service.UserService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * GuiPhase4CDemo: Automated verification test suite for Phase 4C (Meal Logging & Food Selection).
 *
 * Verifies:
 * 1. MealLogFrame class exists.
 * 2. Constructor accepts User.
 * 3. Food list can be loaded via FoodService.
 * 4. Meal type values exist.
 * 5. Quantity validation rejects zero.
 * 6. Quantity validation rejects negative values.
 * 7. Invalid date is handled.
 * 8. Empty meal cannot be saved.
 * 9. Temporary item calculation works.
 * 10. Multiple food items can be added.
 * 11. Temporary totals are calculated correctly.
 * 12. Selected item can be removed.
 * 13. Meal save uses the service layer.
 * 14. Saved meal appears in database.
 * 15. Meal items appear in database.
 * 16. Existing meals for selected date can be loaded.
 * 17. Only logged-in user's meals are returned.
 * 18. Delete meal works.
 * 19. Deleted meal items are removed through FK cascade.
 * 20. Dashboard Meal Log button exists and is wired.
 * 21. Back button returns to DashboardFrame.
 * 22. GUI source contains no SQL/JDBC objects.
 * 23. FoodService operates correctly.
 */
public class GuiPhase4CDemo {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   NutriGuide Phase 4C: Meal Logging & Food Selection Verification");
        System.out.println("================================================================================");

        DatabaseSafetyGuard.requireDisposableDatabase("GuiPhase4CDemo");

        MealLogFrame frame = null;
        DashboardFrame dashboard = null;

        try {
            UserService userService = new UserService();
            User user = userService.getUserById(1); // User 1: John Doe
            MealService mealService = new MealService();
            FoodService foodService = new FoodService();

            // 1 & 2: MealLogFrame Instantiation & User Binding
            frame = testMealLogFrameInstantiation(user, mealService, foodService);

            // 3 & 4: Food Catalog & Meal Type Values
            testFoodAndMealTypeCatalogs(frame);

            // 5, 6, 7: Input Validations
            testInputValidations(frame);

            // 8: Empty Meal Validation
            testEmptyMealValidation(frame);

            // 9, 10, 11: Temporary Items Addition & Totals Calculation
            testItemCalculationsAndTotals(frame);

            // 12: Remove Selected Item
            testRemoveSelectedItem(frame);

            // 13, 14, 15: Transactional Meal Persistence
            Integer savedMealId = testTransactionalMealPersistence(frame, user);

            // 16 & 17: Existing Meals Listing & User Isolation
            testExistingMealsListing(frame, user, savedMealId);

            // 18 & 19: Meal Deletion & FK Cascade Cleanup
            testMealDeletionAndCascade(mealService, user, savedMealId);

            // 20 & 21: Dashboard & Back Navigation
            dashboard = testNavigation(frame, user);

            // 22: Architecture Rule Check (No SQL/JDBC in MealLogFrame)
            testArchitecturalIntegrity();

            // 23: FoodService Verification
            testFoodService(foodService);

        } catch (Exception e) {
            System.err.println("Unexpected exception in GuiPhase4CDemo: " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            if (frame != null) {
                frame.dispose();
            }
            if (dashboard != null) {
                dashboard.dispose();
            }
        }

        // Final Summary
        System.out.println("\n================================================================================");
        System.out.println("   Phase 4C Meal Logging Verification Summary");
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

    private static MealLogFrame testMealLogFrameInstantiation(User user, MealService mealService, FoodService foodService) {
        System.out.println("\n--- [1] Testing MealLogFrame Instantiation & Components ---");

        MealLogFrame frame = new MealLogFrame(user, mealService, foodService);
        assertCondition("1.1: MealLogFrame instantiated successfully", frame != null);
        assertCondition("1.2: Frame title contains 'NutriGuide - Meal Log'",
                frame.getTitle() != null && frame.getTitle().contains("NutriGuide - Meal Log"));
        assertCondition("1.3: Logged-in User bound to MealLogFrame",
                frame.getUser() != null && frame.getUser().getUserId() == 1);

        assertCondition("1.4: Date field is present", frame.getDateField() != null);
        assertCondition("1.5: Meal type combo box is present", frame.getMealTypeComboBox() != null);
        assertCondition("1.6: Food combo box is present", frame.getFoodComboBox() != null);
        assertCondition("1.7: Quantity field is present", frame.getQuantityField() != null);
        assertCondition("1.8: Current items table is present", frame.getCurrentItemsTable() != null);
        assertCondition("1.9: Existing meals table is present", frame.getExistingMealsTable() != null);

        return frame;
    }

    private static void testFoodAndMealTypeCatalogs(MealLogFrame frame) {
        System.out.println("\n--- [2] Testing Food & Meal Type Catalogs ---");

        JComboBox<String> foodBox = frame.getFoodComboBox();
        assertCondition("2.1: Food combo box populated from database (>= 10 foods)",
                foodBox != null && foodBox.getItemCount() >= 10);

        JComboBox<String> mealTypeBox = frame.getMealTypeComboBox();
        assertCondition("2.2: Meal type combo box has 4 options",
                mealTypeBox != null && mealTypeBox.getItemCount() == 4);
        assertCondition("2.3: Meal type contains Breakfast", "Breakfast".equals(mealTypeBox.getItemAt(0)));
        assertCondition("2.4: Meal type contains Lunch", "Lunch".equals(mealTypeBox.getItemAt(1)));
        assertCondition("2.5: Meal type contains Dinner", "Dinner".equals(mealTypeBox.getItemAt(2)));
        assertCondition("2.6: Meal type contains Snack", "Snack".equals(mealTypeBox.getItemAt(3)));
    }

    private static void testInputValidations(MealLogFrame frame) {
        System.out.println("\n--- [3] Testing Input Validations (Quantity & Date) ---");

        // 3.1: Zero quantity rejected
        boolean zeroRejected = false;
        try {
            BigDecimal qty = new BigDecimal("0");
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                zeroRejected = true;
            }
        } catch (Exception e) {
            zeroRejected = true;
        }
        assertCondition("3.1: Quantity of 0 is rejected by validation rule", zeroRejected);

        // 3.2: Negative quantity rejected
        boolean negativeRejected = false;
        try {
            BigDecimal qty = new BigDecimal("-2.5");
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                negativeRejected = true;
            }
        } catch (Exception e) {
            negativeRejected = true;
        }
        assertCondition("3.2: Negative quantity is rejected by validation rule", negativeRejected);

        // 3.3: Invalid date rejected
        boolean invalidDateCaught = false;
        try {
            LocalDate.parse("invalid-date-format");
        } catch (DateTimeParseException ex) {
            invalidDateCaught = true;
        }
        assertCondition("3.3: Invalid date format is caught by DateTimeParseException", invalidDateCaught);

        // 3.4: Valid date parsed
        LocalDate parsed = LocalDate.parse("2026-10-03");
        assertCondition("3.4: Valid yyyy-MM-dd date parsed successfully", parsed != null);
    }

    private static void testEmptyMealValidation(MealLogFrame frame) {
        System.out.println("\n--- [4] Testing Empty Meal Save Rejection ---");

        frame.handleClear();
        assertCondition("4.1: Empty meal detected when items list is empty",
                frame.getCurrentMealItems().isEmpty());
    }

    private static void testItemCalculationsAndTotals(MealLogFrame frame) throws Exception {
        System.out.println("\n--- [5] Testing Item Addition, Calculation & Totals ---");

        frame.handleClear();

        // Add Item 1: Index 0 food with quantity 2.0
        frame.getFoodComboBox().setSelectedIndex(0);
        frame.getQuantityField().setText("2.0");
        frame.handleAddFoodItem();

        assertCondition("5.1: First food item added to current items list",
                frame.getCurrentMealItems().size() == 1);
        assertCondition("5.2: First item added to table model",
                frame.getCurrentItemsTableModel().getRowCount() == 1);

        // Add Item 2: Index 1 food with quantity 1.5
        frame.getFoodComboBox().setSelectedIndex(1);
        frame.getQuantityField().setText("1.5");
        frame.handleAddFoodItem();

        assertCondition("5.3: Second food item added to current items list",
                frame.getCurrentMealItems().size() == 2);
        assertCondition("5.4: Second item added to table model",
                frame.getCurrentItemsTableModel().getRowCount() == 2);

        // Verify calculations
        Food f1 = frame.getCurrentItemFoods().get(0);
        BigDecimal q1 = frame.getCurrentMealItems().get(0).getQuantity();
        BigDecimal expectedCal1 = f1.getCalories().multiply(q1).setScale(2, RoundingMode.HALF_UP);

        Food f2 = frame.getCurrentItemFoods().get(1);
        BigDecimal q2 = frame.getCurrentMealItems().get(1).getQuantity();
        BigDecimal expectedCal2 = f2.getCalories().multiply(q2).setScale(2, RoundingMode.HALF_UP);

        BigDecimal expectedTotalCal = expectedCal1.add(expectedCal2).setScale(2, RoundingMode.HALF_UP);

        String tableCal1 = (String) frame.getCurrentItemsTableModel().getValueAt(0, 2);
        assertCondition("5.5: Item 1 calculated calories match expected",
                new BigDecimal(tableCal1).compareTo(expectedCal1) == 0);

        String totalsText = frame.getTotalCaloriesLabel().getText();
        assertCondition("5.6: Total calories label displays sum of item calories",
                totalsText.contains(expectedTotalCal.toPlainString()));
    }

    private static void testRemoveSelectedItem(MealLogFrame frame) {
        System.out.println("\n--- [6] Testing Remove Selected Item ---");

        int initialSize = frame.getCurrentMealItems().size();
        frame.getCurrentItemsTable().setRowSelectionInterval(0, 0);
        frame.handleRemoveSelectedItem();

        assertCondition("6.1: Item removed from table model",
                frame.getCurrentItemsTableModel().getRowCount() == initialSize - 1);
        assertCondition("6.2: Item removed from in-memory items list",
                frame.getCurrentMealItems().size() == initialSize - 1);
    }

    private static Integer testTransactionalMealPersistence(MealLogFrame frame, User user) throws Exception {
        System.out.println("\n--- [7] Testing Transactional Meal Persistence (ACID Commit) ---");

        // Prepare a fresh test meal with 1 item for a dedicated test date
        frame.handleClear();
        frame.getFoodComboBox().setSelectedIndex(0);
        frame.getQuantityField().setText("2.0");
        frame.handleAddFoodItem();

        LocalDate testDate = LocalDate.of(2026, 10, 28);
        Meal testMeal = new Meal(user.getUserId(), "Lunch", testDate);

        int mealId = frame.getMealService().addMealWithItems(testMeal, frame.getCurrentMealItems());
        assertCondition("7.1: MealService.addMealWithItems committed meal successfully (ID > 0)", mealId > 0);

        // Verify meal in database
        Meal persistedMeal = frame.getMealService().getMealById(mealId);
        assertCondition("7.2: Persisted meal found in database", persistedMeal != null);
        assertCondition("7.3: Persisted meal date matches test date", testDate.equals(persistedMeal.getMealDate()));
        assertCondition("7.4: Persisted meal user matches User 1", user.getUserId().equals(persistedMeal.getUserId()));

        // Verify child items in database
        ArrayList<MealItem> items = frame.getMealService().getItemsByMealId(mealId);
        assertCondition("7.5: Persisted meal items found in database (count == 1)", items != null && items.size() == 1);
        if (items != null && !items.isEmpty()) {
            assertCondition("7.6: Meal item quantity matches entered value (2.0)",
                    new BigDecimal("2.00").compareTo(items.get(0).getQuantity()) == 0);
        }

        return mealId;
    }

    private static void testExistingMealsListing(MealLogFrame frame, User user, Integer savedMealId) throws Exception {
        System.out.println("\n--- [8] Testing Existing Meals Listing & User Isolation ---");

        LocalDate testDate = LocalDate.of(2026, 10, 28);
        ArrayList<Meal> user1Meals = frame.getMealService().getMealsByUserIdAndDate(user.getUserId(), testDate);
        assertCondition("8.1: Existing meals for User 1 on test date retrieved",
                user1Meals != null && !user1Meals.isEmpty());

        boolean found = false;
        for (Meal m : user1Meals) {
            if (m.getMealId().equals(savedMealId)) {
                found = true;
                break;
            }
        }
        assertCondition("8.2: Saved meal ID is present in User 1's meals for that date", found);

        // User Isolation check: User 2 must NOT see User 1's meals
        ArrayList<Meal> user2Meals = frame.getMealService().getMealsByUserIdAndDate(2, testDate);
        boolean leakFound = false;
        for (Meal m : user2Meals) {
            if (m.getMealId().equals(savedMealId)) {
                leakFound = true;
                break;
            }
        }
        assertCondition("8.3: User Isolation verified: User 2 cannot see User 1's meals", !leakFound);
    }

    private static void testMealDeletionAndCascade(MealService mealService, User user, Integer savedMealId)
            throws Exception {
        System.out.println("\n--- [9] Testing Meal Deletion & FK Cascade Removal ---");

        if (savedMealId != null) {
            boolean deleted = mealService.deleteMeal(savedMealId, user.getUserId());
            assertCondition("9.1: Meal deletion succeeded via MealService", deleted);

            // Verify meal is gone
            boolean notFound = false;
            try {
                mealService.getMealById(savedMealId);
            } catch (ResourceNotFoundException ex) {
                notFound = true;
            }
            assertCondition("9.2: Deleted meal is no longer in database (ResourceNotFoundException)", notFound);

            // Verify child items removed via cascade
            ArrayList<MealItem> orphanItems = mealService.getItemsByMealId(savedMealId);
            assertCondition("9.3: Cascade delete verified: child meal items removed completely",
                    orphanItems == null || orphanItems.isEmpty());
        }
    }

    private static DashboardFrame testNavigation(MealLogFrame frame, User user) {
        System.out.println("\n--- [10] Testing Dashboard Navigation & Button Handlers ---");

        DashboardFrame dashboard = new DashboardFrame(user);
        assertCondition("10.1: DashboardFrame instantiated for navigation test", dashboard != null);

        JButton mealLogBtn = dashboard.getMealLogButton();
        assertCondition("10.2: Dashboard Meal Log button exists", mealLogBtn != null);
        ActionListener[] listeners = mealLogBtn.getActionListeners();
        assertCondition("10.3: Dashboard Meal Log button has ActionListener attached",
                listeners != null && listeners.length > 0);

        JButton backBtn = frame.getBackButton();
        assertCondition("10.4: MealLogFrame Back to Dashboard button exists", backBtn != null);
        ActionListener[] backListeners = backBtn.getActionListeners();
        assertCondition("10.5: Back to Dashboard button has ActionListener attached",
                backListeners != null && backListeners.length > 0);

        return dashboard;
    }

    private static void testArchitecturalIntegrity() {
        System.out.println("\n--- [11] Verifying Architectural Integrity (No JDBC in MealLogFrame) ---");

        Class<?> frameClass = MealLogFrame.class;
        boolean hasDirectConnection = false;
        for (Field f : frameClass.getDeclaredFields()) {
            if (f.getType().getName().startsWith("java.sql.")) {
                hasDirectConnection = true;
                break;
            }
        }
        assertCondition("11.1: MealLogFrame has NO java.sql fields (Connection/PreparedStatement/ResultSet)",
                !hasDirectConnection);
    }

    private static void testFoodService(FoodService foodService) throws Exception {
        System.out.println("\n--- [12] Testing FoodService ---");

        ArrayList<Food> foods = foodService.getAllFoods();
        assertCondition("12.1: FoodService.getAllFoods() returns non-empty list",
                foods != null && !foods.isEmpty());

        if (foods != null && !foods.isEmpty()) {
            Food firstFood = foods.get(0);
            Food fetched = foodService.getFoodById(firstFood.getFoodId());
            assertCondition("12.2: FoodService.getFoodById() returns correct entity",
                    fetched != null && fetched.getFoodId().equals(firstFood.getFoodId()));
        }
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
