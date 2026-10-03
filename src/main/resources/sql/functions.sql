-- ============================================================================
-- NutriGuide Stored Functions: functions.sql
-- Covers DBMS Exp 6 (Stored Functions / PL/SQL-style subprograms)
-- ============================================================================

USE nutriguide;

DROP FUNCTION IF EXISTS calculate_bmi;
DROP FUNCTION IF EXISTS get_total_calories;

DELIMITER $$

-- Function 1: calculate_bmi
-- Calculates Body Mass Index: weight_kg / (height_m ^ 2)
CREATE FUNCTION calculate_bmi(weight_kg DECIMAL(5, 2), height_cm DECIMAL(5, 2))
RETURNS DECIMAL(5, 2)
DETERMINISTIC
NO SQL
BEGIN
    DECLARE height_m DECIMAL(5, 4);
    DECLARE bmi DECIMAL(5, 2);

    IF height_cm IS NULL OR height_cm <= 0 OR weight_kg IS NULL OR weight_kg <= 0 THEN
        RETURN 0.00;
    END IF;

    SET height_m = height_cm / 100.0;
    SET bmi = ROUND(weight_kg / (height_m * height_m), 2);

    RETURN bmi;
END$$

-- Function 2: get_total_calories
-- Computes total calories consumed by a user on a given date
CREATE FUNCTION get_total_calories(p_user_id INT, p_date DATE)
RETURNS DECIMAL(7, 2)
READS SQL DATA
BEGIN
    DECLARE total_cals DECIMAL(7, 2);

    SELECT COALESCE(SUM(f.calories * mi.quantity), 0.00)
    INTO total_cals
    FROM meal m
    JOIN meal_item mi ON m.meal_id = mi.meal_id
    JOIN food f ON mi.food_id = f.food_id
    WHERE m.user_id = p_user_id AND m.meal_date = p_date;

    RETURN total_cals;
END$$

DELIMITER ;
