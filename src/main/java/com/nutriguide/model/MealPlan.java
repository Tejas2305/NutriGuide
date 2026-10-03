package com.nutriguide.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MealPlan: Represents a scheduled nutritional regimen corresponding to the 'meal_plan' relation.
 */
public class MealPlan {

    private Integer mealPlanId;
    private Integer userId;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public MealPlan() {
        this.isActive = true;
    }

    public MealPlan(Integer userId, String planName, LocalDate startDate, LocalDate endDate, Boolean isActive) {
        this.userId = userId;
        this.planName = planName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = LocalDateTime.now();
    }

    public MealPlan(Integer mealPlanId, Integer userId, String planName, LocalDate startDate,
                    LocalDate endDate, Boolean isActive, LocalDateTime createdAt) {
        this.mealPlanId = mealPlanId;
        this.userId = userId;
        this.planName = planName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public MealPlan(MealPlan other) {
        if (other != null) {
            this.mealPlanId = other.mealPlanId;
            this.userId = other.userId;
            this.planName = other.planName;
            this.startDate = other.startDate;
            this.endDate = other.endDate;
            this.isActive = other.isActive;
            this.createdAt = other.createdAt;
        }
    }

    public Integer getMealPlanId() {
        return mealPlanId;
    }

    public void setMealPlanId(Integer mealPlanId) {
        this.mealPlanId = mealPlanId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
        MealPlan mealPlan = (MealPlan) o;
        return Objects.equals(mealPlanId, mealPlan.mealPlanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mealPlanId);
    }

    @Override
    public String toString() {
        return "MealPlan{" +
                "mealPlanId=" + mealPlanId +
                ", userId=" + userId +
                ", planName='" + planName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
