package com.nutriguide.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * NutritionGoal: Represents target macronutrient and caloric goals for a user
 * corresponding to the 'nutrition_goal' relation.
 */
public class NutritionGoal {

    private Integer goalId;
    private Integer userId;
    private String description;
    private BigDecimal targetCalories;
    private BigDecimal targetProtein;
    private BigDecimal targetCarbs;
    private BigDecimal targetFats;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public NutritionGoal() {
        this.isActive = true;
    }

    public NutritionGoal(Integer userId, BigDecimal targetCalories, BigDecimal targetProtein,
                         BigDecimal targetCarbs, BigDecimal targetFats, Boolean isActive) {
        this.userId = userId;
        this.targetCalories = targetCalories;
        this.targetProtein = targetProtein;
        this.targetCarbs = targetCarbs;
        this.targetFats = targetFats;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = LocalDateTime.now();
    }

    public NutritionGoal(Integer goalId, Integer userId, BigDecimal targetCalories,
                         BigDecimal targetProtein, BigDecimal targetCarbs, BigDecimal targetFats,
                         Boolean isActive, LocalDateTime createdAt) {
        this.goalId = goalId;
        this.userId = userId;
        this.targetCalories = targetCalories;
        this.targetProtein = targetProtein;
        this.targetCarbs = targetCarbs;
        this.targetFats = targetFats;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public NutritionGoal(NutritionGoal other) {
        if (other != null) {
            this.goalId = other.goalId;
            this.userId = other.userId;
            this.description = other.description;
            this.targetCalories = other.targetCalories;
            this.targetProtein = other.targetProtein;
            this.targetCarbs = other.targetCarbs;
            this.targetFats = other.targetFats;
            this.isActive = other.isActive;
            this.createdAt = other.createdAt;
        }
    }

    public String getDescription() {
        if (description != null) {
            return description;
        }
        return "Daily Target (" + (targetCalories != null ? targetCalories.toPlainString() : "0.00") + " kcal)";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getGoalId() {
        return goalId;
    }

    public void setGoalId(Integer goalId) {
        this.goalId = goalId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(BigDecimal targetCalories) {
        this.targetCalories = targetCalories;
    }

    public BigDecimal getTargetProtein() {
        return targetProtein;
    }

    public void setTargetProtein(BigDecimal targetProtein) {
        this.targetProtein = targetProtein;
    }

    public BigDecimal getTargetCarbs() {
        return targetCarbs;
    }

    public void setTargetCarbs(BigDecimal targetCarbs) {
        this.targetCarbs = targetCarbs;
    }

    public BigDecimal getTargetFats() {
        return targetFats;
    }

    public void setTargetFats(BigDecimal targetFats) {
        this.targetFats = targetFats;
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

    public double getTargetCaloriesDouble() {
        return targetCalories != null ? targetCalories.doubleValue() : 0.0;
    }

    public double getTargetProteinDouble() {
        return targetProtein != null ? targetProtein.doubleValue() : 0.0;
    }

    public double getTargetCarbsDouble() {
        return targetCarbs != null ? targetCarbs.doubleValue() : 0.0;
    }

    public double getTargetFatsDouble() {
        return targetFats != null ? targetFats.doubleValue() : 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutritionGoal that = (NutritionGoal) o;
        return Objects.equals(goalId, that.goalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(goalId);
    }

    @Override
    public String toString() {
        return "NutritionGoal{" +
                "goalId=" + goalId +
                ", userId=" + userId +
                ", targetCalories=" + targetCalories +
                ", targetProtein=" + targetProtein +
                ", targetCarbs=" + targetCarbs +
                ", targetFats=" + targetFats +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
