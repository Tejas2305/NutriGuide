package com.nutriguide.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * MealItem: Junction entity resolving the M:N relationship between 'meal' and 'food',
 * corresponding to the 'meal_item' relation.
 */
public class MealItem {

    private Integer mealItemId;
    private Integer mealId;
    private Integer foodId;
    private BigDecimal quantity;

    public MealItem() {
    }

    public MealItem(Integer mealId, Integer foodId, BigDecimal quantity) {
        this.mealId = mealId;
        this.foodId = foodId;
        this.quantity = quantity;
    }

    public MealItem(Integer mealItemId, Integer mealId, Integer foodId, BigDecimal quantity) {
        this.mealItemId = mealItemId;
        this.mealId = mealId;
        this.foodId = foodId;
        this.quantity = quantity;
    }

    public MealItem(MealItem other) {
        if (other != null) {
            this.mealItemId = other.mealItemId;
            this.mealId = other.mealId;
            this.foodId = other.foodId;
            this.quantity = other.quantity;
        }
    }

    public Integer getMealItemId() {
        return mealItemId;
    }

    public void setMealItemId(Integer mealItemId) {
        this.mealItemId = mealItemId;
    }

    public Integer getMealId() {
        return mealId;
    }

    public void setMealId(Integer mealId) {
        this.mealId = mealId;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
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
        MealItem mealItem = (MealItem) o;
        return Objects.equals(mealItemId, mealItem.mealItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mealItemId);
    }

    @Override
    public String toString() {
        return "MealItem{" +
                "mealItemId=" + mealItemId +
                ", mealId=" + mealId +
                ", foodId=" + foodId +
                ", quantity=" + quantity +
                '}';
    }
}
