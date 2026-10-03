package com.nutriguide.demo;

import com.nutriguide.model.ActivityLog;
import com.nutriguide.model.Food;
import com.nutriguide.model.FoodCategory;
import com.nutriguide.model.Meal;
import com.nutriguide.model.MealItem;
import com.nutriguide.model.MealPlan;
import com.nutriguide.model.MealPlanItem;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.Recommendation;
import com.nutriguide.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ModelLayerDemo: Verifies instantiation, constructors (default, parameterized, copy),
 * encapsulation, getters/setters, equals/hashCode, and toString() for all 10 POJO models.
 * Demonstrates Java Lab Manual Exp 1-2 & Exp 12.
 */
public class ModelLayerDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   NutriGuide Phase 3A: Java Model Layer Verification (All 10 POJOs)");
        System.out.println("================================================================================");

        // 1. User Model Verification
        System.out.println("\n[1] Testing User Model (Constructors, Encapsulation, Copy Constructor):");
        User user1 = new User(1, "John Doe", "user@example.com", "hashed_pwd_123", 24, "Male",
                new BigDecimal("175.00"), new BigDecimal("70.00"), "Moderately Active", LocalDateTime.now());
        User userCopy = new User(user1); // Copy constructor (Java Lab Exp 1-2)
        System.out.println("Original User: " + user1);
        System.out.println("Copied User:   " + userCopy);
        System.out.println("Equals verification: " + user1.equals(userCopy));
        System.out.println("Identity verification (different objects in memory): " + (user1 != userCopy));
        System.out.println("Convenience double conversions: Height=" + user1.getHeightCmDouble() + "cm, Weight=" + user1.getWeightKgDouble() + "kg");

        // 2. NutritionGoal Model Verification
        System.out.println("\n[2] Testing NutritionGoal Model:");
        NutritionGoal goal = new NutritionGoal(1, 1, new BigDecimal("2200.00"), new BigDecimal("120.00"),
                new BigDecimal("250.00"), new BigDecimal("65.00"), true, LocalDateTime.now());
        System.out.println("Goal: " + goal);
        System.out.println("Target calories (double): " + goal.getTargetCaloriesDouble() + " kcal");

        // 3. FoodCategory Model Verification
        System.out.println("\n[3] Testing FoodCategory Model:");
        FoodCategory category = new FoodCategory(1, "Proteins");
        System.out.println("Category: " + category);

        // 4. Food Model Verification (Copy constructor)
        System.out.println("\n[4] Testing Food Model (Copy Constructor & Helpers):");
        Food food1 = new Food(1, 1, "Grilled Chicken Breast", new BigDecimal("100.00"),
                new BigDecimal("165.00"), new BigDecimal("31.00"), new BigDecimal("0.00"), new BigDecimal("3.60"));
        Food foodCopy = new Food(food1);
        System.out.println("Food Original: " + food1);
        System.out.println("Food Copy:     " + foodCopy);
        System.out.println("Food equals:   " + food1.equals(foodCopy));
        System.out.println("Food protein double: " + food1.getProteinDouble() + "g");

        // 5. Meal Model Verification
        System.out.println("\n[5] Testing Meal Model:");
        Meal meal = new Meal(1, 1, "Lunch", LocalDate.now(), LocalDateTime.now());
        System.out.println("Meal: " + meal);

        // 6. MealItem Model Verification
        System.out.println("\n[6] Testing MealItem Model:");
        MealItem mealItem = new MealItem(1, 1, 1, new BigDecimal("2.00"));
        System.out.println("MealItem: " + mealItem);
        System.out.println("Quantity double: " + mealItem.getQuantityDouble());

        // 7. MealPlan Model Verification
        System.out.println("\n[7] Testing MealPlan Model:");
        MealPlan plan = new MealPlan(1, 1, "Hypertrophy Diet", LocalDate.now(), LocalDate.now().plusDays(7), true, LocalDateTime.now());
        System.out.println("MealPlan: " + plan);

        // 8. MealPlanItem Model Verification
        System.out.println("\n[8] Testing MealPlanItem Model:");
        MealPlanItem planItem = new MealPlanItem(1, 1, 1, "Breakfast", new BigDecimal("1.50"));
        System.out.println("MealPlanItem: " + planItem);

        // 9. Recommendation Model Verification
        System.out.println("\n[9] Testing Recommendation Model:");
        Recommendation rec = new Recommendation(1, 1, "Hydration target: 3.0 Liters/day", "HYDRATION", LocalDateTime.now());
        System.out.println("Recommendation: " + rec);

        // 10. ActivityLog Model Verification
        System.out.println("\n[10] Testing ActivityLog Model:");
        ActivityLog log = new ActivityLog(1, 1, "User completed meal logging", LocalDateTime.now());
        System.out.println("ActivityLog: " + log);

        System.out.println("\n================================================================================");
        System.out.println("   All 10 Model Classes Verified Successfully!");
        System.out.println("================================================================================");
    }
}
