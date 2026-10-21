package com.nutriguide.service;

import com.nutriguide.dao.MealDAO;
import com.nutriguide.dao.MealItemDAO;
import com.nutriguide.db.DBConnectionFactory;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.Meal;
import com.nutriguide.model.MealItem;
import com.nutriguide.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * MealService - handles all meal logging business logic.
 *
 * Key concept: Transaction Management
 * When we add a meal with multiple food items, we use a TRANSACTION.
 * A transaction means: either ALL the database operations succeed together,
 * or if anything fails, we ROLLBACK (undo) everything.
 * This prevents partial data (e.g., meal exists but no food items).
 */
public class MealService {

    private MealDAO mealDAO;
    private MealItemDAO mealItemDAO;

    public MealService() {
        this.mealDAO = new MealDAO();
        this.mealItemDAO = new MealItemDAO();
    }

    public MealService(MealDAO mealDAO, MealItemDAO mealItemDAO) {
        this.mealDAO = mealDAO;
        this.mealItemDAO = mealItemDAO;
    }

    /**
     * Adds a meal along with all its food items using a database transaction.
     *
     * Steps:
     * 1. Validate input
     * 2. Get a connection and turn off auto-commit (start transaction)
     * 3. Insert the meal
     * 4. Insert all meal items
     * 5. Commit - if everything went well
     * 6. Rollback - if anything went wrong
     *
     * @param meal  The meal to insert
     * @param items The list of food items for this meal
     * @return The generated meal_id
     */
    public int addMealWithItems(Meal meal, ArrayList<MealItem> items)
            throws ValidationException, DatabaseOperationException {

        // Step 1: Validate inputs before touching the database
        if (meal == null || meal.getUserId() == null) {
            throw new ValidationException("meal", "Meal and userId cannot be null.");
        }
        ValidationUtil.requireNonBlank(meal.getMealType(), "mealType");
        ValidationUtil.requireNonNullDate(meal.getMealDate(), "mealDate");

        if (items == null || items.isEmpty()) {
            throw new ValidationException("items", "A meal must have at least one food item.");
        }

        for (MealItem item : items) {
            if (item.getFoodId() == null || item.getFoodId() <= 0) {
                throw new ValidationException("foodId", "Each item must have a valid food ID.");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("quantity", "Item quantity must be greater than 0.");
            }
        }

        // Step 2: Start the transaction
        Connection conn = null;
        try {
            conn = DBConnectionFactory.getConnection();
            conn.setAutoCommit(false); // This starts a transaction

            // Step 3: Insert the meal
            int mealId = mealDAO.addMeal(conn, meal);

            // Step 4: Insert each food item linked to this meal
            for (MealItem item : items) {
                item.setMealId(mealId);
                mealItemDAO.addMealItem(conn, item);
            }

            // Step 5: Everything succeeded, commit the transaction
            conn.commit();
            return mealId;

        } catch (SQLException e) {
            // Step 6: Something failed - rollback all changes
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // If rollback also fails, just print the error
                    rollbackEx.printStackTrace();
                }
            }
            throw new DatabaseOperationException("Transaction failed: meal insertion was rolled back.", e);

        } finally {
            // Always reset auto-commit and close the connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets a meal by its meal_id.
     * Throws an exception if the meal is not found.
     */
    public Meal getMealById(int mealId) throws ResourceNotFoundException, DatabaseOperationException {
        Meal meal = mealDAO.getMealById(mealId);
        if (meal == null) {
            throw new ResourceNotFoundException("Meal", mealId);
        }
        return meal;
    }

    /**
     * Gets all meals for a user.
     */
    public ArrayList<Meal> getMealsByUserId(int userId) throws DatabaseOperationException {
        return mealDAO.getMealsByUserId(userId);
    }

    /**
     * Gets meals for a user on a specific date.
     */
    public ArrayList<Meal> getMealsByUserIdAndDate(int userId, LocalDate date) throws DatabaseOperationException {
        ValidationUtil.requireNonNullDate(date, "mealDate");
        return mealDAO.getMealsByUserIdAndDate(userId, date);
    }

    /**
     * Gets all food items in a specific meal.
     */
    public ArrayList<MealItem> getItemsByMealId(int mealId) throws DatabaseOperationException {
        return mealItemDAO.getItemsByMealId(mealId);
    }

    /**
     * Updates an existing meal.
     */
    public boolean updateMeal(Meal meal) throws ValidationException, DatabaseOperationException {
        if (meal == null || meal.getMealId() == null) {
            throw new ValidationException("meal", "Meal and mealId cannot be null.");
        }
        ValidationUtil.requireNonBlank(meal.getMealType(), "mealType");
        ValidationUtil.requireNonNullDate(meal.getMealDate(), "mealDate");

        return mealDAO.updateMeal(meal);
    }

    /**
     * Deletes a meal (child meal items are deleted automatically by the database cascade).
     */
    public boolean deleteMeal(int mealId) throws DatabaseOperationException {
        return mealDAO.deleteMeal(mealId);
    }

    /**
     * Deletes a meal, but first checks it belongs to the specified user.
     */
    public boolean deleteMeal(int mealId, int userId)
            throws ResourceNotFoundException, ValidationException, DatabaseOperationException {
        Meal meal = getMealById(mealId);
        if (meal.getUserId() == null || meal.getUserId() != userId) {
            throw new ValidationException("userId", "This meal does not belong to user " + userId);
        }
        return deleteMeal(mealId);
    }
}
