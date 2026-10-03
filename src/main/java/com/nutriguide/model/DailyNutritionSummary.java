package com.nutriguide.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DailyNutritionSummary: Represents aggregated macronutrient and caloric totals for a user on a given date.
 * Populated from either the 'daily_nutrition_summary' view or the 'get_daily_summary' stored procedure.
 */
public class DailyNutritionSummary {

    private Integer userId;
    private LocalDate mealDate;
    private BigDecimal dailyCalories;
    private BigDecimal dailyProtein;
    private BigDecimal dailyCarbs;
    private BigDecimal dailyFat;

    public DailyNutritionSummary() {
        this.dailyCalories = BigDecimal.ZERO;
        this.dailyProtein = BigDecimal.ZERO;
        this.dailyCarbs = BigDecimal.ZERO;
        this.dailyFat = BigDecimal.ZERO;
    }

    public DailyNutritionSummary(Integer userId, LocalDate mealDate, BigDecimal dailyCalories,
                                 BigDecimal dailyProtein, BigDecimal dailyCarbs, BigDecimal dailyFat) {
        this.userId = userId;
        this.mealDate = mealDate;
        this.dailyCalories = dailyCalories != null ? dailyCalories : BigDecimal.ZERO;
        this.dailyProtein = dailyProtein != null ? dailyProtein : BigDecimal.ZERO;
        this.dailyCarbs = dailyCarbs != null ? dailyCarbs : BigDecimal.ZERO;
        this.dailyFat = dailyFat != null ? dailyFat : BigDecimal.ZERO;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public BigDecimal getDailyCalories() {
        return dailyCalories;
    }

    public void setDailyCalories(BigDecimal dailyCalories) {
        this.dailyCalories = dailyCalories;
    }

    public BigDecimal getDailyProtein() {
        return dailyProtein;
    }

    public void setDailyProtein(BigDecimal dailyProtein) {
        this.dailyProtein = dailyProtein;
    }

    public BigDecimal getDailyCarbs() {
        return dailyCarbs;
    }

    public void setDailyCarbs(BigDecimal dailyCarbs) {
        this.dailyCarbs = dailyCarbs;
    }

    public BigDecimal getDailyFat() {
        return dailyFat;
    }

    public void setDailyFat(BigDecimal dailyFat) {
        this.dailyFat = dailyFat;
    }

    @Override
    public String toString() {
        return "DailyNutritionSummary{" +
                "userId=" + userId +
                ", mealDate=" + mealDate +
                ", calories=" + dailyCalories +
                ", protein=" + dailyProtein + "g" +
                ", carbs=" + dailyCarbs + "g" +
                ", fat=" + dailyFat + "g" +
                '}';
    }
}
