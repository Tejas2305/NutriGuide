-- ============================================================================
-- NutriGuide DBMS Demonstration Queries: queries.sql
-- Covers DBMS Exp 3 (Functions), Exp 4 (GROUP BY, HAVING, ORDER BY, Views), Exp 5 (Joins & Subqueries)
-- ============================================================================

USE nutriguide;

-- ============================================================================
-- EXPERIMENT 3: SQL FUNCTIONS (String, Numeric, Date, Aggregate)
-- ============================================================================

-- 3.1 String Functions: UPPER, LOWER, CONCAT, LENGTH
SELECT 
    food_id,
    UPPER(food_name) AS upper_name,
    LOWER(food_name) AS lower_name,
    CONCAT(food_name, ' (Serving: ', serving_size, 'g)') AS food_description,
    LENGTH(food_name) AS name_char_count
FROM food
ORDER BY food_id
LIMIT 5;

-- 3.2 Numeric Functions: ROUND, CEIL, FLOOR, ABS
SELECT 
    food_name,
    calories,
    ROUND(calories / 4.184, 2) AS calories_in_kilojoules,
    CEIL(protein) AS ceil_protein,
    FLOOR(protein) AS floor_protein,
    ABS(calories - 100) AS diff_from_100_cal
FROM food
WHERE calories > 100
LIMIT 5;

-- 3.3 Date Functions: CURDATE(), NOW(), DATEDIFF(), DATE_FORMAT()
SELECT 
    user_id,
    name,
    created_at,
    DATE_FORMAT(created_at, '%d-%b-%Y %H:%i') AS formatted_signup_date,
    DATEDIFF(CURDATE(), DATE(created_at)) AS days_since_registration
FROM user_profile;

-- 3.4 Aggregate Functions: SUM, AVG, COUNT, MIN, MAX
SELECT 
    COUNT(*) AS total_food_items,
    ROUND(AVG(calories), 2) AS avg_calories,
    ROUND(MIN(calories), 2) AS min_calories,
    ROUND(MAX(calories), 2) AS max_calories,
    ROUND(SUM(protein), 2) AS total_protein_all_items
FROM food;


-- ============================================================================
-- EXPERIMENT 4: GROUP BY, HAVING, ORDER BY & VIEW DEMONSTRATION
-- ============================================================================

-- 4.1 GROUP BY & ORDER BY: Average calories and protein per food category
SELECT 
    fc.category_name,
    COUNT(f.food_id) AS items_in_category,
    ROUND(AVG(f.calories), 2) AS avg_category_calories,
    ROUND(AVG(f.protein), 2) AS avg_category_protein
FROM food_category fc
JOIN food f ON fc.category_id = f.category_id
GROUP BY fc.category_id, fc.category_name
ORDER BY avg_category_calories DESC;

-- 4.2 GROUP BY with HAVING: Categories with average protein strictly greater than 5.0 grams
SELECT 
    fc.category_name,
    COUNT(f.food_id) AS total_items,
    ROUND(AVG(f.protein), 2) AS avg_protein
FROM food_category fc
JOIN food f ON fc.category_id = f.category_id
GROUP BY fc.category_id, fc.category_name
HAVING AVG(f.protein) > 5.0
ORDER BY avg_protein DESC;

-- 4.3 Querying the daily_nutrition_summary VIEW with filtering and ordering
SELECT 
    user_id,
    meal_date,
    daily_calories,
    daily_protein,
    daily_carbs,
    daily_fat
FROM daily_nutrition_summary
WHERE daily_calories > 500
ORDER BY meal_date DESC, daily_calories DESC;

-- 4.4 Querying the food_category_details VIEW
SELECT 
    food_name,
    category_name,
    calories,
    protein,
    carbohydrates,
    fat
FROM food_category_details
WHERE category_name IN ('Proteins', 'Grains')
ORDER BY category_name, calories DESC;


-- ============================================================================
-- EXPERIMENT 5: SQL JOINS (INNER, LEFT, RIGHT, EXISTS, NOT EXISTS)
-- ============================================================================

-- 5.1 INNER JOIN: Display meal details with corresponding food and quantity consumed
SELECT 
    m.meal_id,
    u.name AS user_name,
    m.meal_type,
    m.meal_date,
    f.food_name,
    mi.quantity,
    ROUND(f.calories * mi.quantity, 2) AS item_total_calories
FROM meal m
INNER JOIN user_profile u ON m.user_id = u.user_id
INNER JOIN meal_item mi ON m.meal_id = mi.meal_id
INNER JOIN food f ON mi.food_id = f.food_id
WHERE m.user_id = 1
ORDER BY m.meal_date DESC, m.meal_id;

-- 5.2 LEFT OUTER JOIN: All meals and their items (includes meals that have no items logged yet)
SELECT 
    m.meal_id,
    m.meal_type,
    m.meal_date,
    COALESCE(f.food_name, 'No food logged') AS food_name,
    COALESCE(mi.quantity, 0) AS quantity
FROM meal m
LEFT JOIN meal_item mi ON m.meal_id = mi.meal_id
LEFT JOIN food f ON mi.food_id = f.food_id
ORDER BY m.meal_id;

-- 5.3 RIGHT OUTER JOIN: Food categories and any foods in them (ensuring empty categories appear)
SELECT 
    fc.category_name,
    COALESCE(f.food_name, 'No foods assigned') AS food_name,
    COALESCE(f.calories, 0) AS calories
FROM food f
RIGHT JOIN food_category fc ON f.category_id = fc.category_id
ORDER BY fc.category_name;

-- 5.4 Subquery with EXISTS: Find users who have actively logged at least one meal
SELECT 
    u.user_id,
    u.name,
    u.email
FROM user_profile u
WHERE EXISTS (
    SELECT 1 
    FROM meal m 
    WHERE m.user_id = u.user_id
);

-- 5.5 Subquery with NOT EXISTS: Find foods in the catalog that have never been logged in any meal
SELECT 
    f.food_id,
    f.food_name,
    f.calories
FROM food f
WHERE NOT EXISTS (
    SELECT 1 
    FROM meal_item mi 
    WHERE mi.food_id = f.food_id
);
