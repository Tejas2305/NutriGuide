package com.nutriguide.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * MealPlanItem: Junction entity resolving M:N relationship between 'meal_plan' and 'food',
 * corresponding to the 'meal_plan_item' relation.
 */
public class MealPlanItem {

    private Integer mealPlanItemId;
    private Integer mealPlanId;
    private Integer foodId;
    private String plannedMealType;
    private BigDecimal quantity;

    public MealPlanItem() {
    }

    public MealPlanItem(Integer mealPlanId, Integer foodId, String plannedMealType, BigDecimal quantity) {
        this.mealPlanId = mealPlanId;
        this.foodId = foodId;
        this.plannedMealType = plannedMealType;
        this.quantity = quantity;
    }

    public MealPlanItem(Integer mealPlanItemId, Integer mealPlanId, Integer foodId,
                        String plannedMealType, BigDecimal quantity) {
        this.mealPlanItemId = mealPlanItemId;
        this.mealPlanId = mealPlanId;
        this.foodId = foodId;
        this.plannedMealType = plannedMealType;
        this.quantity = quantity;
    }

    public MealPlanItem(MealPlanItem other) {
        if (other != null) {
            this.mealPlanItemId = other.mealPlanItemId;
            this.mealPlanId = other.mealPlanId;
            this.foodId = other.foodId;
            this.plannedMealType = other.plannedMealType;
            this.quantity = other.quantity;
        }
    }

    public Integer getMealPlanItemId() {
        return mealPlanItemId;
    }

    public void setMealPlanItemId(Integer mealPlanItemId) {
        this.mealPlanItemId = mealPlanItemId;
    }

    public Integer getMealPlanId() {
        return mealPlanId;
    }

    public void setMealPlanId(Integer mealPlanId) {
        this.mealPlanId = mealPlanId;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
    }

    public String getPlannedMealType() {
        return plannedMealType;
    }

    public void setPlannedMealType(String plannedMealType) {
        this.plannedMealType = plannedMealType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public double getQuantityDouble() {
        return quantity != null ? quantity.doubleValue() : 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealPlanItem that = (MealPlanItem) o;
        return Objects.equals(mealPlanItemId, that.mealPlanItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mealPlanItemId);
    }

    @Override
    public String toString() {
        return "MealPlanItem{" +
                "mealPlanItemId=" + mealPlanItemId +
                ", mealPlanId=" + mealPlanId +
                ", foodId=" + foodId +
                ", plannedMealType='" + plannedMealType + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
