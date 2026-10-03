package com.nutriguide.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Meal: Represents a logged meal event corresponding to the 'meal' relation.
 * Multiple meals of the same meal_type per date are intentionally supported.
 */
public class Meal {

    private Integer mealId;
    private Integer userId;
    private String mealType;
    private LocalDate mealDate;
    private LocalDateTime createdAt;

    public Meal() {
    }

    public Meal(Integer userId, String mealType, LocalDate mealDate) {
        this.userId = userId;
        this.mealType = mealType;
        this.mealDate = mealDate;
        this.createdAt = LocalDateTime.now();
    }

    public Meal(Integer mealId, Integer userId, String mealType, LocalDate mealDate, LocalDateTime createdAt) {
        this.mealId = mealId;
        this.userId = userId;
        this.mealType = mealType;
        this.mealDate = mealDate;
        this.createdAt = createdAt;
    }

    public Meal(Meal other) {
        if (other != null) {
            this.mealId = other.mealId;
            this.userId = other.userId;
            this.mealType = other.mealType;
            this.mealDate = other.mealDate;
            this.createdAt = other.createdAt;
        }
    }

    public Integer getMealId() {
        return mealId;
    }

    public void setMealId(Integer mealId) {
        this.mealId = mealId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meal meal = (Meal) o;
        return Objects.equals(mealId, meal.mealId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mealId);
    }

    @Override
    public String toString() {
        return "Meal{" +
                "mealId=" + mealId +
                ", userId=" + userId +
                ", mealType='" + mealType + '\'' +
                ", mealDate=" + mealDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
