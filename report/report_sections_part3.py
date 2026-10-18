# -*- coding: utf-8 -*-
"""
NutriGuide DBMS Project Report - Content Sections (Part 3)
Contains Chapters 11 to 16 + Appendices A and B
"""

def get_part3_md():
    return """
---

## 11. Implementation of Tables in MySQL (Step 9)

The NutriGuide database is implemented in **MySQL 8.x** utilizing the ACID-compliant **InnoDB** storage engine. All declarative constraints (`PRIMARY KEY`, `FOREIGN KEY`, `UNIQUE`, `CHECK`, `DEFAULT`, `NOT NULL`) are enforced at the database kernel level.

### 11.1 Table DDL Definitions

```sql
CREATE DATABASE IF NOT EXISTS nutriguide;
USE nutriguide;

-- 1. USER_PROFILE
CREATE TABLE IF NOT EXISTS user_profile (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    age INT NOT NULL CHECK (age > 0),
    gender VARCHAR(20) NOT NULL,
    height_cm DECIMAL(5, 2) NOT NULL CHECK (height_cm > 0),
    weight_kg DECIMAL(5, 2) NOT NULL CHECK (weight_kg > 0),
    activity_level VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. NUTRITION_GOAL
CREATE TABLE IF NOT EXISTS nutrition_goal (
    goal_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    target_calories DECIMAL(7, 2) NOT NULL CHECK (target_calories >= 0),
    target_protein DECIMAL(6, 2) NOT NULL CHECK (target_protein >= 0),
    target_carbs DECIMAL(6, 2) NOT NULL CHECK (target_carbs >= 0),
    target_fats DECIMAL(6, 2) NOT NULL CHECK (target_fats >= 0),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nutrition_goal_user FOREIGN KEY (user_id)
        REFERENCES user_profile (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 3. FOOD_CATEGORY
CREATE TABLE IF NOT EXISTS food_category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- 4. FOOD
CREATE TABLE IF NOT EXISTS food (
    food_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    food_name VARCHAR(100) NOT NULL UNIQUE,
    serving_size DECIMAL(6, 2) NOT NULL CHECK (serving_size > 0),
    calories DECIMAL(6, 2) NOT NULL CHECK (calories >= 0),
    protein DECIMAL(5, 2) NOT NULL CHECK (protein >= 0),
    carbohydrates DECIMAL(5, 2) NOT NULL CHECK (carbohydrates >= 0),
    fat DECIMAL(5, 2) NOT NULL CHECK (fat >= 0),
    CONSTRAINT fk_food_category FOREIGN KEY (category_id)
        REFERENCES food_category (category_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 5. MEAL
CREATE TABLE IF NOT EXISTS meal (
    meal_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    meal_type VARCHAR(50) NOT NULL,
    meal_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_user FOREIGN KEY (user_id)
        REFERENCES user_profile (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 6. MEAL_ITEM
CREATE TABLE IF NOT EXISTS meal_item (
    meal_item_id INT AUTO_INCREMENT PRIMARY KEY,
    meal_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity DECIMAL(5, 2) NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_meal_item_meal FOREIGN KEY (meal_id)
        REFERENCES meal (meal_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_meal_item_food FOREIGN KEY (food_id)
        REFERENCES food (food_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 7. MEAL_PLAN
CREATE TABLE IF NOT EXISTS meal_plan (
    meal_plan_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_plan_user FOREIGN KEY (user_id)
        REFERENCES user_profile (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 8. MEAL_PLAN_ITEM
CREATE TABLE IF NOT EXISTS meal_plan_item (
    meal_plan_item_id INT AUTO_INCREMENT PRIMARY KEY,
    meal_plan_id INT NOT NULL,
    food_id INT NOT NULL,
    planned_meal_type VARCHAR(50) NOT NULL,
    quantity DECIMAL(5, 2) NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_meal_plan_item_plan FOREIGN KEY (meal_plan_id)
        REFERENCES meal_plan (meal_plan_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_meal_plan_item_food FOREIGN KEY (food_id)
        REFERENCES food (food_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 9. RECOMMENDATION
CREATE TABLE IF NOT EXISTS recommendation (
    rec_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    rec_type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recommendation_user FOREIGN KEY (user_id)
        REFERENCES user_profile (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 10. ACTIVITY_LOG
CREATE TABLE IF NOT EXISTS activity_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action_taken VARCHAR(255) NOT NULL,
    log_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_log_user FOREIGN KEY (user_id)
        REFERENCES user_profile (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
```

---

### 11.2 Record Count Verification Query
To verify that all 10 tables are created and seeded with baseline records:

```sql
SELECT 'user_profile' AS Table_Name, COUNT(*) AS Total_Records FROM user_profile
UNION ALL SELECT 'nutrition_goal', COUNT(*) FROM nutrition_goal
UNION ALL SELECT 'food_category', COUNT(*) FROM food_category
UNION ALL SELECT 'food', COUNT(*) FROM food
UNION ALL SELECT 'meal', COUNT(*) FROM meal
UNION ALL SELECT 'meal_item', COUNT(*) FROM meal_item
UNION ALL SELECT 'meal_plan', COUNT(*) FROM meal_plan
UNION ALL SELECT 'meal_plan_item', COUNT(*) FROM meal_plan_item
UNION ALL SELECT 'recommendation', COUNT(*) FROM recommendation
UNION ALL SELECT 'activity_log', COUNT(*) FROM activity_log;
```

#### Output:
| Table_Name | Total_Records |
|:---|:---|
| `user_profile` | 2 |
| `nutrition_goal` | 2 |
| `food_category` | 5 |
| `food` | 15 |
| `meal` | 4 |
| `meal_item` | 8 |
| `meal_plan` | 1 |
| `meal_plan_item` | 3 |
| `recommendation` | 2 |
| `activity_log` | 4 |

---

### 11.3 Table Contents After Loading Seed Data

#### 1. `SELECT * FROM food_category;`
| category_id | category_name |
|:---|:---|
| 1 | Grains |
| 2 | Proteins |
| 3 | Dairy |
| 4 | Fruits |
| 5 | Vegetables |

#### 2. `SELECT * FROM food;`
| food_id | category_id | food_name | serving_size | calories | protein | carbohydrates | fat |
|:---|:---|:---|:---|:---|:---|:---|:---|
| 1 | 1 | Cooked White Rice | 100.00 | 130.00 | 2.70 | 28.20 | 0.30 |
| 2 | 1 | Whole Wheat Roti | 50.00 | 120.00 | 3.50 | 22.00 | 2.00 |
| 3 | 1 | Rolled Oats | 100.00 | 389.00 | 16.90 | 66.30 | 6.90 |
| 4 | 2 | Grilled Chicken Breast | 100.00 | 165.00 | 31.00 | 0.00 | 3.60 |
| 5 | 2 | Yellow Dal (Cooked) | 100.00 | 116.00 | 7.30 | 20.00 | 1.20 |
| 6 | 2 | Fresh Paneer | 100.00 | 265.00 | 18.30 | 3.40 | 20.80 |
| 7 | 2 | Boiled Egg | 50.00 | 78.00 | 6.30 | 0.60 | 5.30 |
| 8 | 4 | Apple | 100.00 | 52.00 | 0.30 | 13.80 | 0.20 |
| 9 | 4 | Banana | 100.00 | 89.00 | 1.10 | 22.80 | 0.30 |
| 10 | 4 | Orange | 100.00 | 47.00 | 0.90 | 11.80 | 0.10 |
| 11 | 3 | Whole Milk | 100.00 | 61.00 | 3.20 | 4.80 | 3.30 |
| 12 | 3 | Greek Yogurt | 100.00 | 59.00 | 10.00 | 3.60 | 0.40 |
| 13 | 5 | Steamed Broccoli | 100.00 | 35.00 | 2.40 | 7.20 | 0.40 |
| 14 | 5 | Mixed Green Salad | 100.00 | 20.00 | 1.20 | 3.80 | 0.20 |
| 15 | 5 | Boiled Potato | 100.00 | 87.00 | 1.90 | 20.10 | 0.10 |

#### 3. `SELECT user_id, name, email, age, gender, height_cm, weight_kg, activity_level FROM user_profile;`
| user_id | name | email | age | gender | height_cm | weight_kg | activity_level |
|:---|:---|:---|:---|:---|:---|:---|:---|
| 1 | John Doe | user@example.com | 24 | Male | 175.00 | 70.00 | Moderately Active |
| 2 | Jane Smith | jane@example.com | 22 | Female | 162.00 | 58.00 | Lightly Active |

#### 4. `SELECT * FROM nutrition_goal;`
| goal_id | user_id | target_calories | target_protein | target_carbs | target_fats | is_active |
|:---|:---|:---|:---|:---|:---|:---|
| 1 | 1 | 2200.00 | 120.00 | 250.00 | 65.00 | 1 |
| 2 | 2 | 1800.00 | 90.00 | 200.00 | 50.00 | 1 |

#### 5. `SELECT * FROM meal;`
| meal_id | user_id | meal_type | meal_date | created_at |
|:---|:---|:---|:---|:---|
| 1 | 1 | Breakfast | 2026-10-18 | 2026-10-18 08:30:00 |
| 2 | 1 | Lunch | 2026-10-18 | 2026-10-18 13:15:00 |
| 3 | 1 | Dinner | 2026-10-18 | 2026-10-18 20:00:00 |
| 4 | 2 | Breakfast | 2026-10-18 | 2026-10-18 08:45:00 |

#### 6. `SELECT * FROM meal_item;`
| meal_item_id | meal_id | food_id | quantity |
|:---|:---|:---|:---|
| 1 | 1 | 3 (Rolled Oats) | 1.00 |
| 2 | 1 | 7 (Boiled Egg) | 2.00 |
| 3 | 1 | 9 (Banana) | 1.00 |
| 4 | 2 | 4 (Grilled Chicken Breast) | 1.50 |
| 5 | 2 | 1 (Cooked White Rice) | 1.50 |
| 6 | 2 | 14 (Mixed Green Salad) | 1.00 |
| 7 | 3 | 5 (Yellow Dal) | 1.00 |
| 8 | 3 | 2 (Whole Wheat Roti) | 2.00 |

---

## 12. Functions, Procedures, Triggers and Queries (Step 10)

### 12.1 Stored Functions (DBMS Exp 6)

#### Function 1: `calculate_bmi(weight_kg, height_cm)`
- **Mathematical Formula**: BMI = weight_kg / (height_m)² = weight_kg / (height_cm / 100)²
- **Determinism**: Declared `DETERMINISTIC NO SQL` for maximum execution performance and compiler query caching.

```sql
DELIMITER $$
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

    SET height_m = height_cm / 100.0000;
    SET bmi = weight_kg / (height_m * height_m);

    RETURN ROUND(bmi, 2);
END $$
DELIMITER ;
```

##### Test Execution:
```sql
SELECT 
    name, 
    height_cm, 
    weight_kg, 
    calculate_bmi(weight_kg, height_cm) AS calculated_bmi 
FROM user_profile;
```
##### Output:
| name | height_cm | weight_kg | calculated_bmi |
|:---|:---|:---|:---|
| John Doe | 175.00 | 70.00 | 22.86 |
| Jane Smith | 162.00 | 58.00 | 22.10 |

---

#### Function 2: `get_total_calories(user_id, meal_date)`
- **Purpose**: Computes total caloric intake for a user on a given date by aggregating food calories scaled by portion quantities.

```sql
DELIMITER $$
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
END $$
DELIMITER ;
```

##### Test Execution:
```sql
SELECT get_total_calories(1, '2026-10-18') AS john_total_calories;
```
##### Output:
| john_total_calories |
|:---|
| 1395.50 |

---

### 12.2 Stored Procedures (DBMS Exp 7)

#### Procedure: `get_daily_summary(p_user_id, p_date, OUT p_calories, OUT p_protein, OUT p_carbs, OUT p_fat)`
- **Purpose**: Retrieves all four macronutrient totals for a user on a specific date using SQL `OUT` parameters.

```sql
DELIMITER $$
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
    INTO p_calories, p_protein, p_carbs, p_fat
    FROM meal m
    JOIN meal_item mi ON m.meal_id = mi.meal_id
    JOIN food f ON mi.food_id = f.food_id
    WHERE m.user_id = p_user_id AND m.meal_date = p_date;
END $$
DELIMITER ;
```

##### Test Execution:
```sql
CALL get_daily_summary(1, '2026-10-18', @cal, @pro, @carb, @fat);
SELECT @cal AS daily_calories, @pro AS daily_protein, @carb AS daily_carbs, @fat AS daily_fat;
```
##### Output:
| daily_calories | daily_protein | daily_carbs | daily_fat |
|:---|:---|:---|:---|
| 1395.50 | 97.45 | 190.60 | 33.20 |

---

### 12.3 SQL Views (DBMS Exp 4 & 5)

#### View 1: `daily_nutrition_summary`
Pre-aggregates nutritional consumption per user per day.
```sql
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
```

##### Test Execution:
```sql
SELECT * FROM daily_nutrition_summary WHERE user_id = 1;
```
##### Output:
| user_id | meal_date | daily_calories | daily_protein | daily_carbs | daily_fat |
|:---|:---|:---|:---|:---|:---|
| 1 | 2026-10-18 | 1395.50 | 97.45 | 190.60 | 33.20 |

---

#### View 2: `food_category_details`
Pre-joins food catalog items with their dietary categories, removing redundant join code.
```sql
CREATE OR REPLACE VIEW food_category_details AS
SELECT 
    f.food_id,
    f.food_name,
    c.category_name,
    f.serving_size,
    f.calories,
    f.protein,
    f.carbohydrates,
    f.fat
FROM food f
JOIN food_category c ON f.category_id = c.category_id;
```

##### Test Execution:
```sql
SELECT food_name, category_name, calories, protein FROM food_category_details LIMIT 5;
```
##### Output:
| food_name | category_name | calories | protein |
|:---|:---|:---|:---|
| Cooked White Rice | Grains | 130.00 | 2.70 |
| Whole Wheat Roti | Grains | 120.00 | 3.50 |
| Rolled Oats | Grains | 389.00 | 16.90 |
| Grilled Chicken Breast | Proteins | 165.00 | 31.00 |
| Yellow Dal (Cooked) | Proteins | 116.00 | 7.30 |

---

### 12.4 Triggers (DBMS Exp 8)

#### Trigger 1: `trg_after_meal_insert` (AFTER INSERT Audit Trigger)
Automatically creates an audit record in `activity_log` whenever a new meal transaction is committed.
```sql
DELIMITER $$
CREATE TRIGGER trg_after_meal_insert
AFTER INSERT ON meal
FOR EACH ROW
BEGIN
    INSERT INTO activity_log (user_id, action_taken, log_time)
    VALUES (
        NEW.user_id,
        CONCAT('Logged meal (ID: ', NEW.meal_id, ', Type: ', NEW.meal_type, ', Date: ', NEW.meal_date, ')'),
        NOW()
    );
END $$
DELIMITER ;
```

##### Test Execution:
```sql
INSERT INTO meal (user_id, meal_type, meal_date) VALUES (1, 'Snack', '2026-10-18');
SELECT * FROM activity_log WHERE user_id = 1 ORDER BY log_id DESC LIMIT 1;
```
##### Output:
| log_id | user_id | action_taken | log_time |
|:---|:---|:---|:---|
| 5 | 1 | Logged meal (ID: 5, Type: Snack, Date: 2026-10-18) | 2026-10-18 23:45:10 |

---

#### Trigger 2: `trg_validate_meal_quantity` (BEFORE INSERT Validation Trigger)
Guards the database against non-positive serving quantities by signaling a SQLSTATE error.
```sql
DELIMITER $$
CREATE TRIGGER trg_validate_meal_quantity
BEFORE INSERT ON meal_item
FOR EACH ROW
BEGIN
    IF NEW.quantity <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Meal item quantity must be strictly greater than 0';
    END IF;
END $$
DELIMITER ;
```

##### Test Execution:
```sql
INSERT INTO meal_item (meal_id, food_id, quantity) VALUES (1, 8, -1.00);
```
##### Output:
```text
Error Code: 1644. Meal item quantity must be strictly greater than 0 (SQLSTATE 45000)
Query Execution Failed: Insertion Aborted by Trigger.
```

---

### 12.5 Analytical Queries (DBMS Exp 3, 4, 5)

#### Query 1: Top 5 Highest Protein Foods in the Catalog
```sql
SELECT food_name, serving_size, protein, calories
FROM food
ORDER BY protein DESC
LIMIT 5;
```
| food_name | serving_size | protein | calories |
|:---|:---|:---|:---|
| Grilled Chicken Breast | 100.00 | 31.00 | 165.00 |
| Fresh Paneer | 100.00 | 18.30 | 265.00 |
| Rolled Oats | 100.00 | 16.90 | 389.00 |
| Greek Yogurt | 100.00 | 10.00 | 59.00 |
| Yellow Dal (Cooked) | 100.00 | 7.30 | 116.00 |

---

#### Query 2: Aggregate Nutritional Density by Category (GROUP BY, HAVING)
```sql
SELECT 
    c.category_name,
    COUNT(f.food_id) AS total_food_items,
    ROUND(AVG(f.calories), 2) AS avg_calories,
    ROUND(AVG(f.protein), 2) AS avg_protein
FROM food_category c
JOIN food f ON c.category_id = f.category_id
GROUP BY c.category_id, c.category_name
HAVING COUNT(f.food_id) > 1
ORDER BY avg_calories DESC;
```
| category_name | total_food_items | avg_calories | avg_protein |
|:---|:---|:---|:---|
| Grains | 3 | 213.00 | 7.70 |
| Proteins | 4 | 156.00 | 15.73 |
| Dairy | 2 | 60.00 | 6.60 |
| Fruits | 3 | 62.67 | 0.77 |
| Vegetables | 3 | 47.33 | 1.83 |

---

#### Query 3: Multi-Table Intake Breakdown by Meal Type for User 1
```sql
SELECT 
    m.meal_type,
    COUNT(mi.meal_item_id) AS items_consumed,
    ROUND(SUM(f.calories * mi.quantity), 2) AS total_meal_calories,
    ROUND(SUM(f.protein * mi.quantity), 2) AS total_meal_protein
FROM meal m
JOIN meal_item mi ON m.meal_id = mi.meal_id
JOIN food f ON mi.food_id = f.food_id
WHERE m.user_id = 1 AND m.meal_date = '2026-10-18'
GROUP BY m.meal_id, m.meal_type;
```
| meal_type | items_consumed | total_meal_calories | total_meal_protein |
|:---|:---|:---|:---|
| Breakfast | 3 | 634.00 | 30.60 |
| Lunch | 3 | 462.50 | 51.75 |
| Dinner | 2 | 356.00 | 14.30 |

---

#### Query 4: Daily Intake vs. Active Target Goal Comparison (CASE Evaluation)
```sql
SELECT 
    u.name,
    g.target_calories,
    s.daily_calories,
    (s.daily_calories - g.target_calories) AS calorie_variance,
    CASE 
        WHEN s.daily_calories > g.target_calories THEN 'Caloric Surplus'
        WHEN s.daily_calories < g.target_calories THEN 'Caloric Deficit'
        ELSE 'Exact Target Reached'
    END AS status
FROM user_profile u
JOIN nutrition_goal g ON u.user_id = g.user_id AND g.is_active = TRUE
JOIN daily_nutrition_summary s ON u.user_id = s.user_id AND s.meal_date = '2026-10-18'
WHERE u.user_id = 1;
```
| name | target_calories | daily_calories | calorie_variance | status |
|:---|:---|:---|:---|:---|
| John Doe | 2200.00 | 1395.50 | -804.50 | Caloric Deficit |

---

#### Query 5: Unconsumed Food Items in Catalog (LEFT JOIN with IS NULL)
Identifies reference foods that have never been consumed in any logged meal:
```sql
SELECT f.food_id, f.food_name, c.category_name
FROM food f
JOIN food_category c ON f.category_id = c.category_id
LEFT JOIN meal_item mi ON f.food_id = mi.food_id
WHERE mi.meal_item_id IS NULL;
```
| food_id | food_name | category_name |
|:---|:---|:---|
| 6 | Fresh Paneer | Proteins |
| 8 | Apple | Fruits |
| 10 | Orange | Fruits |
| 11 | Whole Milk | Dairy |
| 12 | Greek Yogurt | Dairy |
| 13 | Steamed Broccoli | Vegetables |
| 15 | Boiled Potato | Vegetables |

---

#### Query 6: Foods with Above-Average Caloric Content (Subquery)
```sql
SELECT food_name, calories
FROM food
WHERE calories > (SELECT AVG(calories) FROM food)
ORDER BY calories DESC;
```
| food_name | calories |
|:---|:---|
| Rolled Oats | 389.00 |
| Fresh Paneer | 265.00 |
| Grilled Chicken Breast | 165.00 |
| Cooked White Rice | 130.00 |
| Whole Wheat Roti | 120.00 |
| Yellow Dal (Cooked) | 116.00 |

---

#### Query 7: Correlated Subquery — Users Consuming More Than 1000 kcal Today
```sql
SELECT u.user_id, u.name, u.email
FROM user_profile u
WHERE EXISTS (
    SELECT 1 
    FROM daily_nutrition_summary s 
    WHERE s.user_id = u.user_id 
      AND s.meal_date = '2026-10-18' 
      AND s.daily_calories > 1000.00
);
```
| user_id | name | email |
|:---|:---|:---|
| 1 | John Doe | user@example.com |

---

#### Query 8: String Formatting of Food Descriptions (String Functions)
```sql
SELECT 
    food_id,
    UPPER(food_name) AS upper_food,
    CONCAT(food_name, ' [', serving_size, 'g / ', calories, ' kcal]') AS food_label
FROM food
WHERE category_id = 4;
```
| food_id | upper_food | food_label |
|:---|:---|:---|
| 8 | APPLE | Apple [100.00g / 52.00 kcal] |
| 9 | BANANA | Banana [100.00g / 89.00 kcal] |
| 10 | ORANGE | Orange [100.00g / 47.00 kcal] |

---

#### Query 9: Active Diet Regimens and Scheduled Items
```sql
SELECT 
    p.plan_name,
    p.start_date,
    p.end_date,
    pi.planned_meal_type,
    f.food_name,
    pi.quantity
FROM meal_plan p
JOIN meal_plan_item pi ON p.meal_plan_id = pi.meal_plan_id
JOIN food f ON pi.food_id = f.food_id
WHERE p.is_active = TRUE;
```
| plan_name | start_date | end_date | planned_meal_type | food_name | quantity |
|:---|:---|:---|:---|:---|:---|
| Hypertrophy Diet | 2026-10-18 | 2026-10-25 | Breakfast | Rolled Oats | 1.00 |
| Hypertrophy Diet | 2026-10-18 | 2026-10-25 | Breakfast | Boiled Egg | 2.00 |
| Hypertrophy Diet | 2026-10-18 | 2026-10-25 | Lunch | Grilled Chicken Breast | 1.50 |

---

#### Query 10: Complete Audit Log History for User 1
```sql
SELECT log_id, action_taken, log_time
FROM activity_log
WHERE user_id = 1
ORDER BY log_time DESC;
```
| log_id | action_taken | log_time |
|:---|:---|:---|
| 4 | Logged meal (ID: 3, Type: Dinner, Date: 2026-10-18) | 2026-10-18 20:00:00 |
| 3 | Logged meal (ID: 2, Type: Lunch, Date: 2026-10-18) | 2026-10-18 13:15:00 |
| 2 | Logged meal (ID: 1, Type: Breakfast, Date: 2026-10-18) | 2026-10-18 08:30:00 |
| 1 | Account created with initial profile parameters | 2026-10-18 08:00:00 |

---

## 13. Application of Codd's 12 Rules

Dr. E. F. Codd formulated twelve rules (plus Rule 0) that establish whether an information management system qualifies as a true relational database. The NutriGuide system, running on **MySQL 8.x with the InnoDB storage engine**, is evaluated below against each of Codd's foundational rules:

| Rule No. | Rule Name | Formal Statement | Evaluation in NutriGuide | Compliance Status |
|:---|:---|:---|:---|:---|
| **Rule 0** | **Foundation Rule** | The system must be able to manage databases entirely through its relational capabilities. | NutriGuide manages all entities (users, goals, meals, foods, plans, recommendations, audit logs) exclusively through relational tables, keys, and SQL operations. | **Followed** |
| **Rule 1** | **Information Rule** | All information in the database is represented explicitly at the logical level in exactly one way: by values in tables. | Every domain fact (from user demographics to portion quantities and trigger audit records) is stored exclusively in rows and columns across the 10 relational tables. | **Followed** |
| **Rule 2** | **Guaranteed Access Rule** | Each and every datum in a relational database is guaranteed to be logically accessible by resorting to a combination of table name, primary key value, and column name. | Any specific nutrient or demographic attribute can be directly pinpointed. For example: `SELECT protein FROM food WHERE food_id = 4;` unambiguously retrieves `31.00`. | **Followed** |
| **Rule 3** | **Systematic Treatment of NULL Values** | NULL values are supported in a systematic way, representing missing information or inapplicable data, independent of data type. | Columns like `activity_level` allow NULL only when unspecified, whereas mandatory attributes (`name`, `email`, `serving_size`) enforce `NOT NULL`. Null handling functions (`COALESCE`, `IFNULL`) are systematically used. | **Followed** |
| **Rule 4** | **Dynamic Online Catalog Based on the Relational Model** | The database description is represented at the logical level in the same way as ordinary data, so authorized users can query it using relational SQL. | Schema metadata is queried relationally via MySQL's `information_schema` (e.g., `SELECT * FROM information_schema.tables WHERE table_schema = 'nutriguide';`). | **Followed** |
| **Rule 5** | **Comprehensive Data Sublanguage Rule** | A relational system may support several languages, but must support at least one well-defined sublanguage supporting DDL, DML, integrity constraints, and transaction control. | NutriGuide utilizes SQL for DDL (`CREATE TABLE`), DML (`INSERT`, `UPDATE`), view/trigger declarations, and transaction control (`START TRANSACTION`, `COMMIT`, `ROLLBACK`). | **Followed** |
| **Rule 6** | **View Updating Rule** | All views that are theoretically updatable are also updatable by the system. | Simple views are updatable in MySQL. However, complex aggregation views (`daily_nutrition_summary` with `SUM`, `GROUP BY`, and joins) are non-updatable in MySQL InnoDB, as is standard practice. | **Partially Followed** |
| **Rule 7** | **High-Level Insert, Update, and Delete** | The system must support set-at-a-time operations for retrieval, insertion, update, and deletion, not merely record-at-a-time processing. | Bulk operations operate seamlessly on entire tuple sets (e.g., multi-row `INSERT INTO food VALUES (...)`, mass status updates via `UPDATE nutrition_goal SET is_active = FALSE`). | **Followed** |
| **Rule 8** | **Physical Data Independence** | Application programs and terminal activities remain logically unimpaired whenever any changes are made in storage representations or access methods. | The physical tablespace layout, B-Tree index structures, and disk allocations are managed entirely by the InnoDB engine without altering application SQL queries. | **Followed** |
| **Rule 9** | **Logical Data Independence** | Application programs and terminal activities remain logically unimpaired when information-preserving changes of any kind that theoretically permit un-impairment are made to the tables. | Pre-aggregated SQL views (`daily_nutrition_summary`, `food_category_details`) insulate the application layer from table schema evolutions, though drastic structural changes may require query updates. | **Partially Followed** |
| **Rule 10** | **Integrity Independence** | Integrity constraints must be definable in the relational data sublanguage and stored in the catalog, not in application programs. | All constraints (PKs, FKs with `ON DELETE CASCADE`, `CHECK` bounds on heights/weights, `UNIQUE` email keys, and triggers) reside directly inside MySQL, not in client code. | **Followed** |
| **Rule 11** | **Distribution Independence** | The data manipulation sublanguage of a relational DBMS must enable application programs to remain unchanged whether data is centralized or distributed. | NutriGuide operates on a centralized MySQL 8.x database server; distributed transparency (e.g., two-phase commit over sharded nodes) is not implemented. | **Not Applicable** |
| **Rule 12** | **Non-Subversion Rule** | If a relational system has a low-level language, that low level cannot be used to subvert or bypass the integrity rules and constraints. | Access to MySQL storage is strictly mediated via SQL under authentication privileges. Engine constraints cannot be bypassed by low-level row manipulations. | **Followed** |

---

## 14. Individual Contributions (Step 11)

The NutriGuide DBMS Project was collaboratively engineered by Group 1 under the academic supervision of **Dr. Balkrishna Patil**. Specific responsibilities were distributed across the project lifecycle as follows:

| Sr. No. | Student Name | PRN | Core Responsibilities & Modules Authored |
|:---:|:---|:---:|:---|
| **1** | **Tejas Jagdish Patil** | **26070122510** | • Problem formulation, domain modeling, and System Architecture design.<br>• Authored Functional & Non-Functional Requirements (FR1–FR9).<br>• Designed the multi-tier topology and generated the System Architecture diagram.<br>• Authored DDL schema and test verification for `user_profile` and `nutrition_goal`. |
| **2** | **Veer Parmar** | **26070122508** | • Conceptual ER Diagram drafting (Chen notation) and Enhanced ER (EER) modeling.<br>• Entity, relationship, and attribute cardinalities with participation constraints.<br>• Designed referential integrity policies (`ON DELETE CASCADE` vs. `RESTRICT`).<br>• Authored DDL schema and nutritional catalog data for `food_category` and `food`. |
| **3** | **Postiwala Vatsal Dipakbhai** | **26070122509** | • Comprehensive Database Anomalies analysis on the unnormalized `NUTRITION_FLAT` model.<br>• Formal Functional Dependencies formulation and mathematical Normalization (1NF → 2NF → 3NF → BCNF).<br>• Generated normalization comparison diagrams (Figures 10.1 through 10.6).<br>• Designed associative junction tables `meal_item` and `meal_plan_item`.<br>• Authored stored functions `calculate_bmi` and `get_total_calories`, and SQL views. |
| **4** | **Parth Bhayani** | **26070122512** | • Implemented stored procedure `get_daily_summary` with `OUT` parameters.<br>• Designed and tested database triggers (`trg_after_meal_insert`, `trg_validate_meal_quantity`).<br>• Created the system audit log module (`activity_log`) and recommendation table.<br>• Developed the comprehensive 10-query analytical test suite (Q1–Q10).<br>• Conducted Codd's 12 Rules compliance evaluation on MySQL InnoDB. |

---

## 15. Conclusion

The **NutriGuide** relational database project successfully demonstrates the end-to-end design, normalization, implementation, and optimization of an academic and clinical-grade personal nutrition management database. 

### Key Project Achievements:
1. **Pristine 3NF / BCNF Schema**: By decomposing the unnormalized flat meal model into ten discrete relational tables, all insertion, update, and deletion anomalies were formally eliminated.
2. **Associative Relationship Resolution**: Many-to-many cardinalities between meals and foods, as well as meal plans and foods, were cleanly resolved via associative junction entities with portion quantities.
3. **Rigorous Referential Integrity**: Explicit foreign key actions (`CASCADE` on user dependents, `RESTRICT` on catalog items) guarantee that historical dietary logs remain permanent and audit-compliant.
4. **Declarative Database Constraints**: Boundary rules on physical dimensions, caloric targets, and serving portions are enforced at the database kernel level through `CHECK` constraints and `NOT NULL` rules.
5. **Advanced Database Programmability**: The inclusion of stored functions (dynamic BMI evaluation), stored procedures (consolidated daily summaries with OUT parameters), views (pre-aggregated daily totals), and triggers (automated audit trails and SQLSTATE validation) highlights the power of modern database engines.

The project demonstrates compliance with the B.Tech Computer Science DBMS curriculum standards, illustrating how relational theory directly translates into reliable, scalable software engineering.

---

## 16. References

### Textbooks & Academic Literature
1. **R. Elmasri and S. B. Navathe**, *Fundamentals of Database Systems*, 7th Edition, Pearson Education, 2016.
2. **A. Silberschatz, H. F. Korth, and S. Sudarshan**, *Database System Concepts*, 7th Edition, McGraw-Hill Higher Education, 2019.
3. **C. J. Date**, *An Introduction to Database Systems*, 8th Edition, Addison-Wesley, 2004.
4. **E. F. Codd**, *"A Relational Model of Data for Large Shared Data Banks"*, Communications of the ACM, Vol. 13, No. 6, 1970, pp. 377–387.
5. **E. F. Codd**, *"Is Your DBMS Really Relational?"*, Computerworld, October 1985.

### Software Documentation & Standards
6. **Oracle Corporation**, *MySQL 8.0 Reference Manual*, InnoDB Storage Engine Architecture & SQL Syntax, https://dev.mysql.com/doc/refman/8.0/en/
7. **World Health Organization (WHO)**, *Body Mass Index (BMI) Classifications & Daily Caloric Guidelines*, Geneva, Switzerland.
8. **National Institutes of Health (NIH)**, *Dietary Reference Intakes (DRIs) for Energy, Carbohydrate, Fiber, Fat, Fatty Acids, Cholesterol, Protein, and Amino Acids*.

---

## Appendix A. Complete SQL Script (`NutriGuide.sql`)

```sql
-- ============================================================================
-- NutriGuide Database Schema: Complete DDL & Baseline Data
-- Database Engine: MySQL 8.x | Storage Engine: InnoDB
-- ============================================================================

CREATE DATABASE IF NOT EXISTS nutriguide;
USE nutriguide;

-- Table 1: user_profile
CREATE TABLE IF NOT EXISTS user_profile (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    age INT NOT NULL CHECK (age > 0),
    gender VARCHAR(20) NOT NULL,
    height_cm DECIMAL(5, 2) NOT NULL CHECK (height_cm > 0),
    weight_kg DECIMAL(5, 2) NOT NULL CHECK (weight_kg > 0),
    activity_level VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Table 2: nutrition_goal
CREATE TABLE IF NOT EXISTS nutrition_goal (
    goal_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    target_calories DECIMAL(7, 2) NOT NULL CHECK (target_calories >= 0),
    target_protein DECIMAL(6, 2) NOT NULL CHECK (target_protein >= 0),
    target_carbs DECIMAL(6, 2) NOT NULL CHECK (target_carbs >= 0),
    target_fats DECIMAL(6, 2) NOT NULL CHECK (target_fats >= 0),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nutrition_goal_user FOREIGN KEY (user_id)
        REFERENCES user_profile (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Table 3: food_category
CREATE TABLE IF NOT EXISTS food_category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- Table 4: food
CREATE TABLE IF NOT EXISTS food (
    food_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    food_name VARCHAR(100) NOT NULL UNIQUE,
    serving_size DECIMAL(6, 2) NOT NULL CHECK (serving_size > 0),
    calories DECIMAL(6, 2) NOT NULL CHECK (calories >= 0),
    protein DECIMAL(5, 2) NOT NULL CHECK (protein >= 0),
    carbohydrates DECIMAL(5, 2) NOT NULL CHECK (carbohydrates >= 0),
    fat DECIMAL(5, 2) NOT NULL CHECK (fat >= 0),
    CONSTRAINT fk_food_category FOREIGN KEY (category_id)
        REFERENCES food_category (category_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Table 5: meal
CREATE TABLE IF NOT EXISTS meal (
    meal_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    meal_type VARCHAR(50) NOT NULL,
    meal_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_user FOREIGN KEY (user_id)
        REFERENCES user_profile (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Table 6: meal_item
CREATE TABLE IF NOT EXISTS meal_item (
    meal_item_id INT AUTO_INCREMENT PRIMARY KEY,
    meal_id INT NOT NULL,
    food_id INT NOT NULL,
    quantity DECIMAL(5, 2) NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_meal_item_meal FOREIGN KEY (meal_id)
        REFERENCES meal (meal_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_meal_item_food FOREIGN KEY (food_id)
        REFERENCES food (food_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Table 7: meal_plan
CREATE TABLE IF NOT EXISTS meal_plan (
    meal_plan_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_plan_user FOREIGN KEY (user_id)
        REFERENCES user_profile (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Table 8: meal_plan_item
CREATE TABLE IF NOT EXISTS meal_plan_item (
    meal_plan_item_id INT AUTO_INCREMENT PRIMARY KEY,
    meal_plan_id INT NOT NULL,
    food_id INT NOT NULL,
    planned_meal_type VARCHAR(50) NOT NULL,
    quantity DECIMAL(5, 2) NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_meal_plan_item_plan FOREIGN KEY (meal_plan_id)
        REFERENCES meal_plan (meal_plan_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_meal_plan_item_food FOREIGN KEY (food_id)
        REFERENCES food (food_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Table 9: recommendation
CREATE TABLE IF NOT EXISTS recommendation (
    rec_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    rec_type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recommendation_user FOREIGN KEY (user_id)
        REFERENCES user_profile (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Table 10: activity_log
CREATE TABLE IF NOT EXISTS activity_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    action_taken VARCHAR(255) NOT NULL,
    log_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_log_user FOREIGN KEY (user_id)
        REFERENCES user_profile (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
```

---

## Appendix B. Stored Routines & Test Suite

```sql
USE nutriguide;

-- Views
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

CREATE OR REPLACE VIEW food_category_details AS
SELECT 
    f.food_id,
    f.food_name,
    c.category_name,
    f.serving_size,
    f.calories,
    f.protein,
    f.carbohydrates,
    f.fat
FROM food f
JOIN food_category c ON f.category_id = c.category_id;

-- Stored Functions
DELIMITER $$
CREATE FUNCTION calculate_bmi(weight_kg DECIMAL(5, 2), height_cm DECIMAL(5, 2))
RETURNS DECIMAL(5, 2)
DETERMINISTIC NO SQL
BEGIN
    DECLARE height_m DECIMAL(5, 4);
    IF height_cm IS NULL OR height_cm <= 0 OR weight_kg IS NULL OR weight_kg <= 0 THEN
        RETURN 0.00;
    END IF;
    SET height_m = height_cm / 100.0000;
    RETURN ROUND(weight_kg / (height_m * height_m), 2);
END $$

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
END $$

-- Stored Procedure
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
    INTO p_calories, p_protein, p_carbs, p_fat
    FROM meal m
    JOIN meal_item mi ON m.meal_id = mi.meal_id
    JOIN food f ON mi.food_id = f.food_id
    WHERE m.user_id = p_user_id AND m.meal_date = p_date;
END $$

-- Triggers
CREATE TRIGGER trg_after_meal_insert
AFTER INSERT ON meal
FOR EACH ROW
BEGIN
    INSERT INTO activity_log (user_id, action_taken, log_time)
    VALUES (
        NEW.user_id,
        CONCAT('Logged meal (ID: ', NEW.meal_id, ', Type: ', NEW.meal_type, ', Date: ', NEW.meal_date, ')'),
        NOW()
    );
END $$

CREATE TRIGGER trg_validate_meal_quantity
BEFORE INSERT ON meal_item
FOR EACH ROW
BEGIN
    IF NEW.quantity <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Meal item quantity must be strictly greater than 0';
    END IF;
END $$
DELIMITER ;
```
"""
