package com.nutriguide.demo;

import com.nutriguide.dao.ActivityLogDAO;
import com.nutriguide.dao.FoodCategoryDAO;
import com.nutriguide.dao.FoodDAO;
import com.nutriguide.dao.MealDAO;
import com.nutriguide.dao.MealItemDAO;
import com.nutriguide.dao.NutritionGoalDAO;
import com.nutriguide.dao.RecommendationDAO;
import com.nutriguide.dao.UserDAO;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.model.ActivityLog;
import com.nutriguide.model.Food;
import com.nutriguide.model.FoodCategory;
import com.nutriguide.model.Meal;
import com.nutriguide.model.MealItem;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.Recommendation;
import com.nutriguide.model.User;
import com.nutriguide.util.PasswordUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * DaoLayerDemo: Demonstrates and verifies the JDBC DAO layer against the local MySQL database.
 *
 * Demonstrates Java Lab Manual concepts:
 * - Exp 3: Class methods and encapsulation
 * - Exp 5: ArrayList collection for query results
 * - Exp 14-15: try-catch blocks and checked DatabaseOperationException
 * - Exp 19: JDBC SELECT queries mapped to Java Model objects
 * - Exp 20: JDBC INSERT, UPDATE, DELETE execution
 * - Exp 21-22: DriverManager and PreparedStatement parameterized queries
 * - Exp 23: ResultSet navigation (while rs.next())
 *
 * All demo insertions use temporary test records that are cleaned up safely
 * so the initial sample data is preserved intact.
 */
public class DaoLayerDemo {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   NutriGuide Phase 3C: JDBC DAO Layer Verification");
        System.out.println("================================================================================");

        try {
            UserDAO userDAO = new UserDAO();
            FoodDAO foodDAO = new FoodDAO();
            FoodCategoryDAO categoryDAO = new FoodCategoryDAO();
            NutritionGoalDAO goalDAO = new NutritionGoalDAO();
            MealDAO mealDAO = new MealDAO();
            MealItemDAO mealItemDAO = new MealItemDAO();
            RecommendationDAO recDAO = new RecommendationDAO();
            ActivityLogDAO logDAO = new ActivityLogDAO();

            // Section A: Get user by email
            testGetUserByEmail(userDAO);

            // Section B: Get user by ID
            testGetUserById(userDAO);

            // Section C: Get food categories and food list
            testGetAllFoods(foodDAO, categoryDAO);

            // Section D: Search food by name
            testSearchFoodByName(foodDAO);

            // Section E: Get nutrition goals for a user
            testGetNutritionGoals(goalDAO);

            // Section F: Get meals for a user & date
            testGetMeals(mealDAO);

            // Section G: Controlled INSERT and UPDATE test (with cleanup)
            testInsertAndUpdate(userDAO, goalDAO, logDAO);

            // Section H: Controlled DELETE test
            testControlledDelete(userDAO);

            // Section I: Controlled Error Handling (ResourceNotFound / Unique Constraint)
            testErrorHandling(userDAO);

        } catch (Exception e) {
            System.err.println("Unexpected demo error: " + e.getMessage());
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
    // Section A: Get User by Email
    // ========================================================================
    private static void testGetUserByEmail(UserDAO userDAO) throws DatabaseOperationException {
        System.out.println("\n--- [A] UserDAO: getUserByEmail() ---");
        String testEmail = "user@example.com";
        User user = userDAO.getUserByEmail(testEmail);

        assertCondition("A1: User found by email 'user@example.com'", user != null);
        if (user != null) {
            assertCondition("A2: User email matches query", testEmail.equalsIgnoreCase(user.getEmail()));
            assertCondition("A3: User ID is positive", user.getUserId() != null && user.getUserId() > 0);
            System.out.println("    Found User: ID=" + user.getUserId() + ", Name=" + user.getName() + ", Age=" + user.getAge());
        }
    }

    // ========================================================================
    // Section B: Get User by ID
    // ========================================================================
    private static void testGetUserById(UserDAO userDAO) throws DatabaseOperationException {
        System.out.println("\n--- [B] UserDAO: getUserById() ---");
        User user = userDAO.getUserById(1);

        assertCondition("B1: User with ID=1 retrieved", user != null);
        if (user != null) {
            assertCondition("B2: User ID equals 1", user.getUserId() == 1);
            assertCondition("B3: User name is present", user.getName() != null && !user.getName().isEmpty());
            System.out.println("    User 1: Name=" + user.getName() + ", Activity=" + user.getActivityLevel());
        }
    }

    // ========================================================================
    // Section C: Get Food List and Categories
    // ========================================================================
    private static void testGetAllFoods(FoodDAO foodDAO, FoodCategoryDAO categoryDAO) throws DatabaseOperationException {
        System.out.println("\n--- [C] FoodDAO: getAllFoods() and FoodCategoryDAO ---");

        ArrayList<FoodCategory> categories = categoryDAO.getAllCategories();
        assertCondition("C1: Food categories retrieved", categories != null && !categories.isEmpty());
        System.out.println("    Found " + (categories != null ? categories.size() : 0) + " food categories in DB.");

        ArrayList<Food> foods = foodDAO.getAllFoods();
        assertCondition("C2: All foods retrieved (sample data contains >= 10 foods)", foods != null && foods.size() >= 10);
        if (foods != null && !foods.isEmpty()) {
            Food firstFood = foods.get(0);
            System.out.println("    Sample Food: ID=" + firstFood.getFoodId() + ", Name=" + firstFood.getFoodName() +
                    ", Calories=" + firstFood.getCalories() + " kcal");
        }
    }

    // ========================================================================
    // Section D: Search Food by Name
    // ========================================================================
    private static void testSearchFoodByName(FoodDAO foodDAO) throws DatabaseOperationException {
        System.out.println("\n--- [D] FoodDAO: searchFoodByName() ---");
        String keyword = "Chicken";
        ArrayList<Food> matches = foodDAO.searchFoodByName(keyword);

        assertCondition("D1: Search keyword 'Chicken' returns results", matches != null && !matches.isEmpty());
        if (matches != null) {
            for (Food food : matches) {
                System.out.println("    Matched: " + food.getFoodName() + " (" + food.getCalories() + " kcal)");
                assertCondition("D2: Result contains search term", food.getFoodName().toLowerCase().contains(keyword.toLowerCase()));
            }
        }
    }

    // ========================================================================
    // Section E: Get Nutrition Goals
    // ========================================================================
    private static void testGetNutritionGoals(NutritionGoalDAO goalDAO) throws DatabaseOperationException {
        System.out.println("\n--- [E] NutritionGoalDAO: getGoalsByUserId() & getActiveGoalByUserId() ---");
        int userId = 1;
        ArrayList<NutritionGoal> goals = goalDAO.getGoalsByUserId(userId);

        assertCondition("E1: Goals retrieved for user 1", goals != null && !goals.isEmpty());

        NutritionGoal activeGoal = goalDAO.getActiveGoalByUserId(userId);
        assertCondition("E2: Active goal retrieved for user 1", activeGoal != null);
        if (activeGoal != null) {
            assertCondition("E3: Target calories > 0", activeGoal.getTargetCalories().compareTo(BigDecimal.ZERO) > 0);
            System.out.println("    Active Goal for User 1: Calories=" + activeGoal.getTargetCalories() +
                    ", Protein=" + activeGoal.getTargetProtein() + "g, Carbs=" + activeGoal.getTargetCarbs() + "g");
        }
    }

    // ========================================================================
    // Section F: Get Meals for User and Date
    // ========================================================================
    private static void testGetMeals(MealDAO mealDAO) throws DatabaseOperationException {
        System.out.println("\n--- [F] MealDAO: getMealsByUserId() ---");
        int userId = 1;
        ArrayList<Meal> meals = mealDAO.getMealsByUserId(userId);

        assertCondition("F1: Meals retrieved for user 1", meals != null && !meals.isEmpty());
        if (meals != null && !meals.isEmpty()) {
            Meal meal = meals.get(0);
            System.out.println("    Found Meal: ID=" + meal.getMealId() + ", Type=" + meal.getMealType() + ", Date=" + meal.getMealDate());

            // Test getMealsByUserIdAndDate
            ArrayList<Meal> mealsOnDate = mealDAO.getMealsByUserIdAndDate(userId, meal.getMealDate());
            assertCondition("F2: Meals filtered by user and date returned results", mealsOnDate != null && !mealsOnDate.isEmpty());
        }
    }

    // ========================================================================
    // Section G: Controlled INSERT and UPDATE Test (With Cleanup)
    // ========================================================================
    private static void testInsertAndUpdate(UserDAO userDAO, NutritionGoalDAO goalDAO, ActivityLogDAO logDAO)
            throws DatabaseOperationException {
        System.out.println("\n--- [G] Controlled INSERT and UPDATE Test ---");

        if (!DatabaseSafetyGuard.isDisposableDatabaseConfigured()) {
            System.out.println("  [SKIPPED] Section G performs database writes. It requires an explicitly named disposable test database.");
            System.out.println("            Run with -Dnutriguide.test.database.name=<exact_test_db_name> matching database.properties to enable write tests.");
            return;
        }

        String tempEmail = "test_dao_user_" + System.currentTimeMillis() + "@viva.lab";
        String passwordHash = PasswordUtil.hashPassword("TempPass@123");

        User tempUser = new User(
                "Viva Test User",
                tempEmail,
                passwordHash,
                22,
                "Other",
                new BigDecimal("170.00"),
                new BigDecimal("65.00"),
                "Sedentary"
        );

        // 1. Insert temporary user
        int newUserId = userDAO.addUser(tempUser);
        assertCondition("G1: Temporary user inserted with generated ID=" + newUserId, newUserId > 0);
        System.out.println("    Inserted temporary user ID: " + newUserId);

        // 2. Update user name
        tempUser.setName("Viva Updated Name");
        boolean updated = userDAO.updateUser(tempUser);
        assertCondition("G2: User updated successfully", updated);

        // 3. Verify updated name from database
        User fetchedUpdated = userDAO.getUserById(newUserId);
        assertCondition("G3: Fetched user contains updated name",
                fetchedUpdated != null && "Viva Updated Name".equals(fetchedUpdated.getName()));

        // 4. Insert an activity log for this user
        ActivityLog log = new ActivityLog(newUserId, "User ran DAO verification test");
        int logId = logDAO.addLog(log);
        assertCondition("G4: Activity log inserted with generated ID=" + logId, logId > 0);

        // 5. Clean up temporary user (CASCADE deletes child log in MySQL)
        boolean deleted = userDAO.deleteUser(newUserId);
        assertCondition("G5: Temporary user deleted (cleanup successful)", deleted);

        User shouldBeNull = userDAO.getUserById(newUserId);
        assertCondition("G6: Verified temporary user is no longer in DB", shouldBeNull == null);
    }

    // ========================================================================
    // Section H: Controlled DELETE Test
    // ========================================================================
    private static void testControlledDelete(UserDAO userDAO) throws DatabaseOperationException {
        System.out.println("\n--- [H] Controlled DELETE Test ---");

        // Attempting to delete a non-existent ID should return false (0 rows affected), not crash
        boolean deletedNonExistent = userDAO.deleteUser(999999);
        assertCondition("H1: Deleting non-existent ID safely returns false without crashing", !deletedNonExistent);
    }

    // ========================================================================
    // Section I: Error Handling and Constraint Verification
    // ========================================================================
    private static void testErrorHandling(UserDAO userDAO) {
        System.out.println("\n--- [I] Error Handling: DatabaseOperationException ---");

        // Attempt duplicate insertion with existing email (violating UNIQUE constraint)
        boolean caughtConstraintViolation = false;
        try {
            User duplicateUser = new User(
                    "Duplicate User",
                    "user@example.com", // existing email in sample_data.sql
                    "dummyhash",
                    25,
                    "Male",
                    new BigDecimal("175.00"),
                    new BigDecimal("70.00"),
                    "Active"
            );
            userDAO.addUser(duplicateUser);
        } catch (DatabaseOperationException e) {
            caughtConstraintViolation = true;
            assertCondition("I1: Caught DatabaseOperationException on duplicate email", true);
            assertCondition("I2: Exception wraps original SQLException cause", e.getCause() != null);
            assertCondition("I3: MySQL error code preserved (1062 for duplicate key)", e.getSqlErrorCode() == 1062);
            System.out.println("    Caught expected DatabaseOperationException: " + e.getMessage());
            System.out.println("    SQL Error Code: " + e.getSqlErrorCode() + ", SQL State: " + e.getSqlState());
        }

        if (!caughtConstraintViolation) {
            assertCondition("I1: Duplicate email failed to trigger exception", false);
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
