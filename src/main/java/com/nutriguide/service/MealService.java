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
 * MealService: Handles meal logging and meal items management.
 *
 * Demonstrates:
 * - JDBC Transaction Management (DBMS Exp 10):
 *   setAutoCommit(false), commit(), rollback(), setAutoCommit(true)
 * - Atomic execution: meal and its items are logged together or rolled back completely
 * - Delegation to MealDAO and MealItemDAO
 */
public class MealService {

    private final MealDAO mealDAO;
    private final MealItemDAO mealItemDAO;

    public MealService() {
        this.mealDAO = new MealDAO();
        this.mealItemDAO = new MealItemDAO();
    }

    public MealService(MealDAO mealDAO, MealItemDAO mealItemDAO) {
        this.mealDAO = mealDAO;
        this.mealItemDAO = mealItemDAO;
    }

    /**
     * Adds a meal and all its associated meal items inside a single ACID transaction (DBMS Exp 10).
     *
     * <p>If all items are inserted successfully, the transaction is committed.
     * If any error occurs (e.g., invalid foodId, database constraint failure),
     * the transaction is rolled back completely so no partial meal records remain.</p>
     *
     * @param meal Meal entity to insert
     * @param items List of MealItem entities to associate with this meal
     * @return Generated mealId of the committed meal
     * @throws ValidationException if meal or items fail validation
     * @throws DatabaseOperationException if transaction fails and is rolled back
     */
    public int addMealWithItems(Meal meal, ArrayList<MealItem> items)
            throws ValidationException, DatabaseOperationException {

        // 1. Validation
        if (meal == null || meal.getUserId() == null) {
            throw new ValidationException("meal", "Meal and userId cannot be null.");
        }
        ValidationUtil.requireNonBlank(meal.getMealType(), "mealType");
        ValidationUtil.requireNonNullDate(meal.getMealDate(), "mealDate");

        if (items == null || items.isEmpty()) {
            throw new ValidationException("items", "A meal must contain at least one food item.");
        }

        for (MealItem item : items) {
            if (item.getFoodId() == null || item.getFoodId() <= 0) {
                throw new ValidationException("foodId", "Valid food ID is required for each item.");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("quantity", "Item quantity must be strictly greater than 0.");
            }
        }

        // 2. Transaction execution
        Connection conn = null;
        try {
            conn = DBConnectionFactory.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Step A: Insert parent meal
            int mealId = mealDAO.addMeal(conn, meal);

            // Step B: Insert child meal items
            for (MealItem item : items) {
                item.setMealId(mealId);
                mealItemDAO.addMealItem(conn, item);
            }

            // Step C: All operations succeeded => Commit transaction
            conn.commit();
            return mealId;

        } catch (SQLException e) {
            // Step D: Failure occurred => Rollback transaction
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    // Suppress or log rollback exception
                }
            }
            throw new DatabaseOperationException("Transaction failed: rolled back meal insertion", e);

        } finally {
            // Step E: Reset auto-commit and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    // Close failure ignored
                }
            }
        }
    }

    /**
     * Retrieves a meal by primary key ID.
     */
    public Meal getMealById(int mealId) throws ResourceNotFoundException, DatabaseOperationException {
        Meal meal = mealDAO.getMealById(mealId);
        if (meal == null) {
            throw new ResourceNotFoundException("Meal", mealId);
        }
        return meal;
    }

    /**
     * Retrieves all meals for a given user.
     */
    public ArrayList<Meal> getMealsByUserId(int userId) throws DatabaseOperationException {
        return mealDAO.getMealsByUserId(userId);
    }

    /**
     * Retrieves meals logged by a user for a specific date.
     */
    public ArrayList<Meal> getMealsByUserIdAndDate(int userId, LocalDate date) throws DatabaseOperationException {
        ValidationUtil.requireNonNullDate(date, "mealDate");
        return mealDAO.getMealsByUserIdAndDate(userId, date);
    }

    /**
     * Retrieves all items associated with a meal.
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
     * Deletes a meal (child items deleted via DB cascade constraint).
     */
    public boolean deleteMeal(int mealId) throws DatabaseOperationException {
        return mealDAO.deleteMeal(mealId);
    }

    /**
     * Deletes a meal verifying that it belongs to the specified user.
     */
    public boolean deleteMeal(int mealId, int userId)
            throws ResourceNotFoundException, ValidationException, DatabaseOperationException {
        Meal meal = getMealById(mealId);
        if (meal.getUserId() == null || meal.getUserId() != userId) {
            throw new ValidationException("userId", "Unauthorized: meal does not belong to user " + userId);
        }
        return deleteMeal(mealId);
    }
}
