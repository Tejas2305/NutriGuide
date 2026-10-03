package com.nutriguide.service;

import com.nutriguide.dao.MealPlanDAO;
import com.nutriguide.dao.MealPlanItemDAO;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.MealPlan;
import com.nutriguide.model.MealPlanItem;
import com.nutriguide.util.ValidationUtil;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * MealPlanService: Handles planned dietary regimens and scheduled meal items.
 */
public class MealPlanService {

    private final MealPlanDAO mealPlanDAO;
    private final MealPlanItemDAO mealPlanItemDAO;

    public MealPlanService() {
        this.mealPlanDAO = new MealPlanDAO();
        this.mealPlanItemDAO = new MealPlanItemDAO();
    }

    public MealPlanService(MealPlanDAO mealPlanDAO, MealPlanItemDAO mealPlanItemDAO) {
        this.mealPlanDAO = mealPlanDAO;
        this.mealPlanItemDAO = mealPlanItemDAO;
    }

    /**
     * Creates a new meal plan after validating date ranges and plan details.
     */
    public MealPlan createMealPlan(MealPlan plan)
            throws ValidationException, DatabaseOperationException {

        if (plan == null || plan.getUserId() == null) {
            throw new ValidationException("plan", "Meal plan and userId cannot be null.");
        }
        ValidationUtil.requireNonBlank(plan.getPlanName(), "planName");
        ValidationUtil.requireValidDateRange(plan.getStartDate(), plan.getEndDate());

        int id = mealPlanDAO.addMealPlan(plan);
        plan.setMealPlanId(id);
        return plan;
    }

    /**
     * Retrieves a meal plan by ID.
     */
    public MealPlan getMealPlanById(int mealPlanId)
            throws ResourceNotFoundException, DatabaseOperationException {
        MealPlan plan = mealPlanDAO.getMealPlanById(mealPlanId);
        if (plan == null) {
            throw new ResourceNotFoundException("MealPlan", mealPlanId);
        }
        return plan;
    }

    /**
     * Retrieves all meal plans for a user.
     */
    public ArrayList<MealPlan> getMealPlansByUserId(int userId) throws DatabaseOperationException {
        return mealPlanDAO.getMealPlansByUserId(userId);
    }

    /**
     * Retrieves the currently active meal plan for a user.
     */
    public MealPlan getActiveMealPlan(int userId)
            throws ResourceNotFoundException, DatabaseOperationException {
        MealPlan plan = mealPlanDAO.getActiveMealPlanByUserId(userId);
        if (plan == null) {
            throw new ResourceNotFoundException("MealPlan", "Active plan for user " + userId);
        }
        return plan;
    }

    /**
     * Updates an existing meal plan.
     */
    public boolean updateMealPlan(MealPlan plan)
            throws ValidationException, DatabaseOperationException {
        if (plan == null || plan.getMealPlanId() == null) {
            throw new ValidationException("plan", "Meal plan and mealPlanId cannot be null.");
        }
        ValidationUtil.requireNonBlank(plan.getPlanName(), "planName");
        ValidationUtil.requireValidDateRange(plan.getStartDate(), plan.getEndDate());

        return mealPlanDAO.updateMealPlan(plan);
    }

    /**
     * Deletes a meal plan by ID.
     */
    public boolean deleteMealPlan(int mealPlanId) throws DatabaseOperationException {
        return mealPlanDAO.deleteMealPlan(mealPlanId);
    }

    /**
     * Adds an item to a meal plan.
     */
    public int addPlanItem(MealPlanItem item) throws ValidationException, DatabaseOperationException {
        if (item == null || item.getMealPlanId() == null || item.getFoodId() == null) {
            throw new ValidationException("item", "Plan item, mealPlanId, and foodId cannot be null.");
        }
        ValidationUtil.requireNonBlank(item.getPlannedMealType(), "plannedMealType");
        if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("quantity", "Item quantity must be strictly positive.");
        }

        return mealPlanItemDAO.addMealPlanItem(item);
    }

    /**
     * Retrieves scheduled items for a meal plan.
     */
    public ArrayList<MealPlanItem> getPlanItems(int mealPlanId) throws DatabaseOperationException {
        return mealPlanItemDAO.getItemsByMealPlanId(mealPlanId);
    }
}
