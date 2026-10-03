-- ============================================================================
-- NutriGuide Sample Data: sample_data.sql
-- Covers DBMS Exp 2 (DML commands: INSERT)
-- Populates 10 tables with realistic nutrition and diet data
-- ============================================================================

USE nutriguide;

-- 1. Insert Food Categories
INSERT INTO food_category (category_name) VALUES
('Grains'),
('Proteins'),
('Dairy'),
('Fruits'),
('Vegetables')
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name);

-- 2. Insert Realistic Food Items (Values per 100g serving size)
-- Columns: category_id, food_name, serving_size, calories, protein, carbohydrates, fat
INSERT INTO food (category_id, food_name, serving_size, calories, protein, carbohydrates, fat) VALUES
((SELECT category_id FROM food_category WHERE category_name = 'Grains'), 'Cooked White Rice', 100.00, 130.00, 2.70, 28.20, 0.30),
((SELECT category_id FROM food_category WHERE category_name = 'Grains'), 'Whole Wheat Roti', 50.00, 120.00, 3.50, 22.00, 2.00),
((SELECT category_id FROM food_category WHERE category_name = 'Grains'), 'Rolled Oats', 100.00, 389.00, 16.90, 66.30, 6.90),
((SELECT category_id FROM food_category WHERE category_name = 'Proteins'), 'Grilled Chicken Breast', 100.00, 165.00, 31.00, 0.00, 3.60),
((SELECT category_id FROM food_category WHERE category_name = 'Proteins'), 'Yellow Dal (Cooked)', 100.00, 116.00, 7.30, 20.00, 1.20),
((SELECT category_id FROM food_category WHERE category_name = 'Proteins'), 'Fresh Paneer', 100.00, 265.00, 18.30, 3.40, 20.80),
((SELECT category_id FROM food_category WHERE category_name = 'Proteins'), 'Boiled Egg', 50.00, 78.00, 6.30, 0.60, 5.30),
((SELECT category_id FROM food_category WHERE category_name = 'Fruits'), 'Apple', 100.00, 52.00, 0.30, 13.80, 0.20),
((SELECT category_id FROM food_category WHERE category_name = 'Fruits'), 'Banana', 100.00, 89.00, 1.10, 22.80, 0.30),
((SELECT category_id FROM food_category WHERE category_name = 'Fruits'), 'Orange', 100.00, 47.00, 0.90, 11.80, 0.10),
((SELECT category_id FROM food_category WHERE category_name = 'Dairy'), 'Whole Milk', 100.00, 61.00, 3.20, 4.80, 3.30),
((SELECT category_id FROM food_category WHERE category_name = 'Dairy'), 'Plain Curd (Yogurt)', 100.00, 60.00, 3.50, 4.70, 3.10),
((SELECT category_id FROM food_category WHERE category_name = 'Dairy'), 'Greek Yogurt', 100.00, 97.00, 10.00, 3.60, 5.00),
((SELECT category_id FROM food_category WHERE category_name = 'Vegetables'), 'Steamed Spinach', 100.00, 23.00, 2.90, 3.60, 0.40),
((SELECT category_id FROM food_category WHERE category_name = 'Vegetables'), 'Steamed Broccoli', 100.00, 35.00, 2.40, 7.20, 0.40)
ON DUPLICATE KEY UPDATE
    calories = VALUES(calories),
    protein = VALUES(protein),
    carbohydrates = VALUES(carbohydrates),
    fat = VALUES(fat);

-- 3. Insert Mock Users with SHA-256 Hashed Passwords
-- Passwords: 'Password@123' -> SHA2('Password@123', 256)
INSERT INTO user_profile (name, email, password_hash, age, gender, height_cm, weight_kg, activity_level) VALUES
('John Doe', 'user@example.com', SHA2('Password@123', 256), 24, 'Male', 175.00, 70.00, 'Moderately Active'),
('Admin Nutri', 'admin@example.com', SHA2('Admin@123', 256), 28, 'Female', 165.00, 58.00, 'Lightly Active')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    height_cm = VALUES(height_cm),
    weight_kg = VALUES(weight_kg);

-- 4. Insert Nutrition Goals for Users
INSERT INTO nutrition_goal (user_id, target_calories, target_protein, target_carbs, target_fats, is_active) VALUES
(1, 2200.00, 120.00, 250.00, 65.00, TRUE),
(2, 1800.00, 85.00, 210.00, 50.00, TRUE);

-- 5. Insert Sample Meals for John Doe (user_id = 1) and Admin (user_id = 2)
-- Using CURDATE() and CURDATE() - INTERVAL 1 DAY
INSERT INTO meal (user_id, meal_type, meal_date) VALUES
(1, 'Breakfast', CURDATE()),
(1, 'Lunch', CURDATE()),
(1, 'Snack', CURDATE()),
(1, 'Dinner', CURDATE()),
(1, 'Breakfast', CURDATE() - INTERVAL 1 DAY),
(1, 'Lunch', CURDATE() - INTERVAL 1 DAY),
(2, 'Breakfast', CURDATE()),
(2, 'Lunch', CURDATE());

-- 6. Insert Meal Items (Quantities represent servings multiplier)
-- Meal 1 (Breakfast John): Oats (food_id 3, qty 1), Whole Milk (food_id 11, qty 2), Banana (food_id 9, qty 1)
INSERT INTO meal_item (meal_id, food_id, quantity) VALUES
(1, 3, 1.00),
(1, 11, 2.00),
(1, 9, 1.00);

-- Meal 2 (Lunch John): Cooked Rice (food_id 1, qty 2), Yellow Dal (food_id 5, qty 2), Steamed Broccoli (food_id 15, qty 1)
INSERT INTO meal_item (meal_id, food_id, quantity) VALUES
(2, 1, 2.00),
(2, 5, 2.00),
(2, 15, 1.00);

-- Meal 3 (Snack John): Apple (food_id 8, qty 1), Greek Yogurt (food_id 13, qty 1)
INSERT INTO meal_item (meal_id, food_id, quantity) VALUES
(3, 8, 1.00),
(3, 13, 1.00);

-- Meal 4 (Dinner John): Whole Wheat Roti (food_id 2, qty 3), Grilled Chicken Breast (food_id 4, qty 1.5)
INSERT INTO meal_item (meal_id, food_id, quantity) VALUES
(4, 2, 3.00),
(4, 4, 1.50);

-- Meal 5 (Yesterday Breakfast John): Boiled Egg (food_id 7, qty 2), Whole Wheat Roti (food_id 2, qty 2)
INSERT INTO meal_item (meal_id, food_id, quantity) VALUES
(5, 7, 2.00),
(5, 2, 2.00);

-- Meal 6 (Yesterday Lunch John): Cooked Rice (food_id 1, qty 2), Fresh Paneer (food_id 6, qty 1)
INSERT INTO meal_item (meal_id, food_id, quantity) VALUES
(6, 1, 2.00),
(6, 6, 1.00);

-- Meal 7 (Breakfast Admin): Apple (food_id 8, qty 1), Plain Curd (food_id 12, qty 1.5)
INSERT INTO meal_item (meal_id, food_id, quantity) VALUES
(7, 8, 1.00),
(7, 12, 1.50);

-- Meal 8 (Lunch Admin): Whole Wheat Roti (food_id 2, qty 2), Steamed Spinach (food_id 14, qty 1.5)
INSERT INTO meal_item (meal_id, food_id, quantity) VALUES
(8, 2, 2.00),
(8, 14, 1.50);

-- 7. Insert Sample Meal Plan for John Doe
INSERT INTO meal_plan (user_id, plan_name, start_date, end_date, is_active) VALUES
(1, 'High Protein Muscle Gain Plan', CURDATE(), CURDATE() + INTERVAL 7 DAY, TRUE);

-- 8. Insert Meal Plan Items
INSERT INTO meal_plan_item (meal_plan_id, food_id, planned_meal_type, quantity) VALUES
(1, 3, 'Breakfast', 1.00),
(1, 7, 'Breakfast', 2.00),
(1, 4, 'Lunch', 2.00),
(1, 1, 'Lunch', 2.00),
(1, 13, 'Snack', 1.00),
(1, 6, 'Dinner', 1.50),
(1, 2, 'Dinner', 3.00);

-- 9. Insert Sample Recommendations generated by Rule-Based Engine
INSERT INTO recommendation (user_id, message, rec_type) VALUES
(1, 'Great job! Your protein intake is on track to meet your 120g target today.', 'PROTEIN_STATUS'),
(1, 'Consider drinking at least 3 liters of water given your moderate physical activity level.', 'HYDRATION'),
(2, 'Your calorie intake is within 10% of your daily maintenance target of 1800 kcal.', 'CALORIE_STATUS');

-- 10. Insert Audit Activity Logs (Trigger also populates this on meal inserts)
INSERT INTO activity_log (user_id, action_taken) VALUES
(1, 'Initial user profile and baseline health metrics recorded.'),
(1, 'Assigned active nutrition target: 2200 kcal / 120g protein.'),
(2, 'Admin account initialized with dietitian role.');
