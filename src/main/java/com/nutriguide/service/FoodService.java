package com.nutriguide.service;

import com.nutriguide.dao.FoodDAO;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.model.Food;

import java.util.ArrayList;

/**
 * FoodService: Provides food catalog retrieval operations.
 *
 * Demonstrates:
 * - Service Layer abstraction between GUI and FoodDAO
 * - Textbook Java methods returning ArrayList<Food> and Food objects
 */
public class FoodService {

    private final FoodDAO foodDAO;

    public FoodService() {
        this.foodDAO = new FoodDAO();
    }

    public FoodService(FoodDAO foodDAO) {
        this.foodDAO = foodDAO;
    }

    /**
     * Retrieves all available food items from the catalog.
     *
     * @return ArrayList of Food items
     * @throws DatabaseOperationException if database query fails
     */
    public ArrayList<Food> getAllFoods() throws DatabaseOperationException {
        return foodDAO.getAllFoods();
    }

    /**
     * Retrieves a specific food item by its ID.
     *
     * @param foodId Food ID
     * @return Food item
     * @throws ResourceNotFoundException if food item is not found
     * @throws DatabaseOperationException if database query fails
     */
    public Food getFoodById(int foodId) throws ResourceNotFoundException, DatabaseOperationException {
        Food food = foodDAO.getFoodById(foodId);
        if (food == null) {
            throw new ResourceNotFoundException("Food", foodId);
        }
        return food;
    }
}
