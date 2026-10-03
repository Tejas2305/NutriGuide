package com.nutriguide.model;

import java.util.Objects;

/**
 * FoodCategory: Represents high-level nutritional categories (e.g. Grains, Proteins, Fruits)
 * corresponding to the 'food_category' relation.
 */
public class FoodCategory {

    private Integer categoryId;
    private String categoryName;

    public FoodCategory() {
    }

    public FoodCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public FoodCategory(Integer categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public FoodCategory(FoodCategory other) {
        if (other != null) {
            this.categoryId = other.categoryId;
            this.categoryName = other.categoryName;
        }
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodCategory that = (FoodCategory) o;
        return Objects.equals(categoryId, that.categoryId) &&
               Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, categoryName);
    }

    @Override
    public String toString() {
        return "FoodCategory{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
