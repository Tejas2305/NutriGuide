package com.nutriguide.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Food: Represents a nutritional catalog item corresponding to the 'food' relation.
 * Demonstrates Java Lab Manual Exp 1-2:
 * - Default constructor
 * - Parameterized constructors (with/without foodId)
 * - Copy constructor (defensive copies)
 * - Encapsulation (private fields, getters, setters)
 * - Object method overrides (toString, equals, hashCode)
 */
public class Food {

    private Integer foodId;
    private Integer categoryId;
    private String foodName;
    private BigDecimal servingSize;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbohydrates;
    private BigDecimal fat;

    /**
     * Default constructor
     */
    public Food() {
    }

    /**
     * Parameterized constructor for creating a new Food item (before database assigns foodId)
     */
    public Food(Integer categoryId, String foodName, BigDecimal servingSize,
                BigDecimal calories, BigDecimal protein, BigDecimal carbohydrates, BigDecimal fat) {
        this.categoryId = categoryId;
        this.foodName = foodName;
        this.servingSize = servingSize;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
    }

    /**
     * Full parameterized constructor for hydrating food from database
     */
    public Food(Integer foodId, Integer categoryId, String foodName, BigDecimal servingSize,
                BigDecimal calories, BigDecimal protein, BigDecimal carbohydrates, BigDecimal fat) {
        this.foodId = foodId;
        this.categoryId = categoryId;
        this.foodName = foodName;
        this.servingSize = servingSize;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
    }

    /**
     * Copy constructor (Java Lab Exp 1-2: defensive copy)
     *
     * @param other Existing Food instance to duplicate
     */
    public Food(Food other) {
        if (other != null) {
            this.foodId = other.foodId;
            this.categoryId = other.categoryId;
            this.foodName = other.foodName;
            this.servingSize = other.servingSize;
            this.calories = other.calories;
            this.protein = other.protein;
            this.carbohydrates = other.carbohydrates;
            this.fat = other.fat;
        }
    }

    // Getters and Setters
    public Integer getFoodId() {
        return foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public BigDecimal getServingSize() {
        return servingSize;
    }

    public void setServingSize(BigDecimal servingSize) {
        this.servingSize = servingSize;
    }

    public BigDecimal getCalories() {
        return calories;
    }

    public void setCalories(BigDecimal calories) {
        this.calories = calories;
    }

    public BigDecimal getProtein() {
        return protein;
    }

    public void setProtein(BigDecimal protein) {
        this.protein = protein;
    }

    public BigDecimal getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(BigDecimal carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public BigDecimal getFat() {
        return fat;
    }

    public void setFat(BigDecimal fat) {
        this.fat = fat;
    }

    // Convenience double-conversion getters for calculations
    public double getServingSizeDouble() {
        return servingSize != null ? servingSize.doubleValue() : 0.0;
    }

    public double getCaloriesDouble() {
        return calories != null ? calories.doubleValue() : 0.0;
    }

    public double getProteinDouble() {
        return protein != null ? protein.doubleValue() : 0.0;
    }

    public double getCarbohydratesDouble() {
        return carbohydrates != null ? carbohydrates.doubleValue() : 0.0;
    }

    public double getFatDouble() {
        return fat != null ? fat.doubleValue() : 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return Objects.equals(foodId, food.foodId) &&
               Objects.equals(foodName, food.foodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foodId, foodName);
    }

    @Override
    public String toString() {
        return "Food{" +
                "foodId=" + foodId +
                ", categoryId=" + categoryId +
                ", foodName='" + foodName + '\'' +
                ", servingSize=" + servingSize +
                ", calories=" + calories +
                ", protein=" + protein +
                ", carbohydrates=" + carbohydrates +
                ", fat=" + fat +
                '}';
    }
}
