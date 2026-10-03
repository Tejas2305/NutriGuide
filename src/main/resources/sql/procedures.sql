-- ============================================================================
-- NutriGuide Stored Procedures: procedures.sql
-- Covers DBMS Exp 7 (Stored Procedures with IN and OUT parameters)
-- ============================================================================

USE nutriguide;

DROP PROCEDURE IF EXISTS get_daily_summary;

DELIMITER $$

-- Procedure: get_daily_summary
-- Retrieves aggregated macronutrient totals for a user on a given date using OUT parameters
CREATE PROCEDURE get_daily_summary(
    IN p_user_id INT,
    IN p_date DATE,
    OUT p_calories DECIMAL(7, 2),
    OUT p_protein DECIMAL(6, 2),
    OUT p_carbs DECIMAL(6, 2),
    OUT p_fat DECIMAL(6, 2)
)
BEGIN
    SELECT 
        COALESCE(SUM(f.calories * mi.quantity), 0.00),
        COALESCE(SUM(f.protein * mi.quantity), 0.00),
        COALESCE(SUM(f.carbohydrates * mi.quantity), 0.00),
        COALESCE(SUM(f.fat * mi.quantity), 0.00)
    INTO 
        p_calories,
        p_protein,
        p_carbs,
        p_fat
    FROM meal m
    JOIN meal_item mi ON m.meal_id = mi.meal_id
    JOIN food f ON mi.food_id = f.food_id
    WHERE m.user_id = p_user_id AND m.meal_date = p_date;

    -- Ensure null-safety if no records matched
    SET p_calories = COALESCE(p_calories, 0.00);
    SET p_protein = COALESCE(p_protein, 0.00);
    SET p_carbs = COALESCE(p_carbs, 0.00);
    SET p_fat = COALESCE(p_fat, 0.00);
END$$

DELIMITER ;
