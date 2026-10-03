-- ============================================================================
-- NutriGuide Database Views: views.sql
-- Covers DBMS Exp 4 (Views) and Exp 5 (Joins)
-- Note: SELECT * is NOT used; explicit projection ensures schema stability.
-- ============================================================================

USE nutriguide;

-- View 1: Daily Nutrition Summary (Aggregates daily consumption per user)
CREATE OR REPLACE VIEW daily_nutrition_summary AS
SELECT 
    m.user_id,
    m.meal_date,
    COALESCE(SUM(f.calories * mi.quantity), 0) AS daily_calories,
    COALESCE(SUM(f.protein * mi.quantity), 0) AS daily_protein,
    COALESCE(SUM(f.carbohydrates * mi.quantity), 0) AS daily_carbs,
    COALESCE(SUM(f.fat * mi.quantity), 0) AS daily_fat
FROM meal m
LEFT JOIN meal_item mi ON m.meal_id = mi.meal_id
LEFT JOIN food f ON mi.food_id = f.food_id
GROUP BY m.user_id, m.meal_date;

-- View 2: Food Category Details (Pre-joined food and category information)
CREATE OR REPLACE VIEW food_category_details AS
SELECT 
    f.food_id,
    f.food_name,
    fc.category_id,
    fc.category_name,
    f.serving_size,
    f.calories,
    f.protein,
    f.carbohydrates,
    f.fat
FROM food f
INNER JOIN food_category fc ON f.category_id = fc.category_id;
