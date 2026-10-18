# -*- coding: utf-8 -*-
"""
NutriGuide DBMS Project Report - Content Sections (Part 2)
Contains Chapters 7 to 10: Relational Schema, Anomalies, Functional Dependencies, Normalization (1NF to BCNF)
Authentic student engineering tone for Symbiosis Institute of Technology (SIT), Pune.
"""

def get_part2_md():
    return """
---

## 7. Relational Schema (Step 5)

### 7.1 Relational Notation
*(Underlined: Primary Key | Italic: Foreign Key)*

1. **user_profile** (<u>user_id</u>, name, email, password_hash, age, gender, height_cm, weight_kg, activity_level, created_at)
2. **nutrition_goal** (<u>goal_id</u>, *user_id*, target_calories, target_protein, target_carbs, target_fats, is_active, created_at)
3. **food_category** (<u>category_id</u>, category_name)
4. **food** (<u>food_id</u>, *category_id*, food_name, serving_size, calories, protein, carbohydrates, fat)
5. **meal** (<u>meal_id</u>, *user_id*, meal_type, meal_date, created_at)
6. **meal_item** (<u>meal_item_id</u>, *meal_id*, *food_id*, quantity)
7. **meal_plan** (<u>meal_plan_id</u>, *user_id*, plan_name, start_date, end_date, is_active, created_at)
8. **meal_plan_item** (<u>meal_plan_item_id</u>, *meal_plan_id*, *food_id*, planned_meal_type, quantity)
9. **recommendation** (<u>rec_id</u>, *user_id*, rec_type, message, generated_at)
10. **activity_log** (<u>log_id</u>, *user_id*, action_taken, log_time)

### 7.2 Foreign Keys and Referential Integrity Rules

When setting up foreign key constraints in MySQL, our team thought carefully about how user actions affect connected data. We realized there is a fundamental difference between user-owned records (which should be removed if the user deletes their account) and shared catalog data (which must never be deleted if past meal history relies on it).

| Foreign Key | Child Table | Parent Table & Referenced Column | ON UPDATE | ON DELETE | Practical Engineering Rationale |
|:---|:---|:---|:---|:---|:---|
| fk_nutrition_goal_user | nutrition_goal | user_profile(user_id) | CASCADE | CASCADE | If a user deletes their profile, their personal nutrition targets should be removed too. |
| fk_food_category | food | food_category(category_id) | CASCADE | RESTRICT | A food category cannot be deleted if there are still food items filed under it. |
| fk_meal_user | meal | user_profile(user_id) | CASCADE | CASCADE | When a user deletes their account, all their logged meal events are removed. |
| fk_meal_item_meal | meal_item | meal(meal_id) | CASCADE | CASCADE | Removing a meal header automatically deletes all individual food items in that meal. |
| fk_meal_item_food | meal_item | food(food_id) | CASCADE | RESTRICT | A food item cannot be dropped from the catalog if someone has logged it before. |
| fk_meal_plan_user | meal_plan | user_profile(user_id) | CASCADE | CASCADE | Deleting a user account cleans up all upcoming meal plans created by that user. |
| fk_meal_plan_item_plan | meal_plan_item | meal_plan(meal_plan_id) | CASCADE | CASCADE | Deleting a meal plan automatically removes all scheduled food line items in it. |
| fk_meal_plan_item_food | meal_plan_item | food(food_id) | CASCADE | RESTRICT | Stops an admin from deleting a catalog food if it is scheduled in an active diet plan. |
| fk_recommendation_user | recommendation | user_profile(user_id) | CASCADE | CASCADE | Removes past automated dietary advice when the user profile is deleted. |
| fk_activity_log_user | activity_log | user_profile(user_id) | CASCADE | CASCADE | Account termination cascades to clean up the user's historical audit log entries. |

---

## 8. Anomalies (Step 6)

In relational database theory, an anomaly is a data integrity flaw that happens when unrelated real-world entities are combined into a single flat table. To understand why NutriGuide must be split into normalized relations, our team created a sample flat relation called NUTRITION_FLAT. This table represents how a beginner would typically store food and meal logs in a single Excel sheet:

#### Unnormalized Relation: NUTRITION_FLAT
*(Composite Candidate Key: {email, meal_date, meal_type, food_name})*

| user_id | name | email | height_cm | weight_kg | meal_date | meal_type | food_name | serving_size | calories | protein | category_name | quantity |
|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|
| 1 | John Doe | user@example.com | 175.00 | 70.00 | 2026-10-18 | Lunch | Grilled Chicken | 100.00 | 165.00 | 31.00 | Proteins | 1.00 |
| 1 | John Doe | user@example.com | 175.00 | 70.00 | 2026-10-18 | Lunch | Cooked White Rice | 100.00 | 130.00 | 2.70 | Grains | 1.50 |
| 1 | John Doe | user@example.com | 175.00 | 70.00 | 2026-10-18 | Dinner | Yellow Dal | 100.00 | 116.00 | 7.30 | Proteins | 1.00 |
| 2 | Jane Smith | jane@example.com | 162.00 | 58.00 | 2026-10-18 | Breakfast| Rolled Oats | 100.00 | 389.00 | 16.90 | Grains | 1.00 |

Looking at this flat table, we quickly identified three major types of database anomalies:

### 8.1 Insertion Anomalies
1. Adding New Foods to the Catalog: Suppose an administrator wants to add a new food item like *Broccoli* to the database catalog. In NUTRITION_FLAT, this is impossible because the candidate key requires email, meal_date, and meal_type. Since primary key columns cannot contain NULL values, the food item cannot be entered until some user actually eats and logs it.
2. Registering a New User: When a student signs up on NutriGuide, their account cannot be saved in NUTRITION_FLAT until they log their very first meal. Their row would need food details, which do not exist yet.
3. Creating Empty Food Categories: An administrator cannot set up a new category like *Beverages* without simultaneously entering a user, a meal, and a food in that category.

### 8.2 Update Anomalies
1. Redundant Profile Updates: If John Doe loses weight and updates his body weight from 70.00 kg to 68.50 kg, we have to search and update every single meal row he ever logged in the past. If he logged 200 meals over the last six months and a query only updates recent entries, John will end up with two different body weights in the same table.
2. Correcting Nutritional Facts: If we realize that *Cooked White Rice* was mistakenly entered with 150 calories instead of 130 calories, we have to update every historical meal row where anyone ate white rice. A partial update leaves contradictory calorie counts in the database.
3. Renaming Food Categories: If we want to rename the *Proteins* category to *Protein Sources*, we must scan thousands of meal rows. Any small typo leaves some rows in the old category and some in the new one.

### 8.3 Deletion Anomalies
1. Losing User Profiles on Meal Deletion: If John Doe has only logged one meal so far and decides to delete that meal record, deleting that single row wipes out his user account, password hash, height, and weight as well.
2. Losing Catalog Foods on History Cleanup: If Jane Smith is the only user who logged *Rolled Oats* for breakfast, and she later deletes that meal entry, the entire nutritional profile of Rolled Oats and its category are permanently lost from the system.

---

## 9. Functional Dependencies (Step 7)

A functional dependency X → Y means that if two rows agree on attribute set X, they must also agree on attribute set Y. 

Our schema is governed by the following formal functional dependencies and candidate keys:

### 1. user_profile
- **Candidate Keys**: {user_id}, {email}
- **Primary Key**: user_id
- **Functional Dependencies**:
  - user_id → name, email, password_hash, age, gender, height_cm, weight_kg, activity_level, created_at
  - email → user_id, name, password_hash, age, gender, height_cm, weight_kg, activity_level, created_at
  - *Explanation*: Each user is uniquely identified by auto-incremented user_id. Since email is marked UNIQUE, it also uniquely determines all remaining user attributes.

### 2. nutrition_goal
- **Candidate Key**: {goal_id}
- **Primary Key**: goal_id
- **Functional Dependencies**:
  - goal_id → user_id, target_calories, target_protein, target_carbs, target_fats, is_active, created_at
  - *Explanation*: Every nutrition goal is uniquely identified by its primary key goal_id.

### 3. food_category
- **Candidate Keys**: {category_id}, {category_name}
- **Primary Key**: category_id
- **Functional Dependencies**:
  - category_id → category_name
  - category_name → category_id
  - *Explanation*: Dietary categories are uniquely identified by both their numeric ID and their unique category name.

### 4. food
- **Candidate Keys**: {food_id}, {food_name}
- **Primary Key**: food_id
- **Functional Dependencies**:
  - food_id → category_id, food_name, serving_size, calories, protein, carbohydrates, fat
  - food_name → food_id, category_id, serving_size, calories, protein, carbohydrates, fat
  - *Explanation*: Nutritional facts are fixed physical properties of a food item; knowing either food_id or unique food_name determines all nutritional facts.

### 5. meal
- **Candidate Key**: {meal_id}
- **Primary Key**: meal_id
- **Functional Dependencies**:
  - meal_id → user_id, meal_type, meal_date, created_at
  - *Explanation*: A meal event is uniquely identified by meal_id. Users can log multiple meals of the same type on the same date (e.g., two snacks).

### 6. meal_item
- **Candidate Keys**: {meal_item_id}, {meal_id, food_id}
- **Primary Key**: meal_item_id
- **Functional Dependencies**:
  - meal_item_id → meal_id, food_id, quantity
  - (meal_id, food_id) → meal_item_id, quantity
  - *Explanation*: Each meal item line references a specific food and portion quantity consumed within a parent meal event.

### 7. meal_plan
- **Candidate Key**: {meal_plan_id}
- **Primary Key**: meal_plan_id
- **Functional Dependencies**:
  - meal_plan_id → user_id, plan_name, start_date, end_date, is_active, created_at

### 8. meal_plan_item
- **Candidate Key**: {meal_plan_item_id}
- **Primary Key**: meal_plan_item_id
- **Functional Dependencies**:
  - meal_plan_item_id → meal_plan_id, food_id, planned_meal_type, quantity

### 9. recommendation
- **Candidate Key**: {rec_id}
- **Primary Key**: rec_id
- **Functional Dependencies**:
  - rec_id → user_id, rec_type, message, generated_at

### 10. activity_log
- **Candidate Key**: {log_id}
- **Primary Key**: log_id
- **Functional Dependencies**:
  - log_id → user_id, action_taken, log_time

---

## 10. Normalization (Step 8: 1NF to BCNF)

Normalization is the process of breaking down a large, unorganized table into smaller, well-structured tables to remove duplicate data and prevent anomalies. While designing NutriGuide, our group worked step-by-step through the normalization stages from the unnormalized spreadsheet all the way to Boyce-Codd Normal Form (BCNF).

### 10.1 First Normal Form (1NF)
**Rule**: A relation is in 1NF if every column contains only atomic (indivisible) values, and there are no repeating groups or comma-separated lists inside a single cell.

- **Violation in Unnormalized Data**: In the raw flat spreadsheet, multiple foods and their quantities were lumped together as comma-separated text strings within single cells (for instance, `"Grilled Chicken, Cooked Rice"` and `"1.0, 1.5"`).

![Figure 10.1: Before 1NF](images/figure_10_1_before_1nf.png)
*Figure 10.1: Before 1NF — Multi-valued repeating groups packed into single cells*

- **1NF Decomposition**: We eliminated repeating groups by splitting the data so that every cell stores exactly one value. The parent meal event is separated into meal, while each food consumed is stored as an individual row in meal_item.

![Figure 10.2: After 1NF](images/figure_10_2_after_1nf.png)
*Figure 10.2: After 1NF — Decomposed into meal and meal_item with strictly atomic attributes*

---

### 10.2 Second Normal Form (2NF)
**Rule**: A relation is in 2NF if it is already in 1NF and every non-key column is **fully functionally dependent** on the entire primary key. In other words, there must be no partial dependencies where an attribute depends on only part of a composite primary key.

- **Violation in 1NF Composite Table**: If we look at an associative table using (meal_id, food_id) as the composite key alongside user attributes and food nutritional values:
  - (meal_id, food_id) → quantity (Full dependency on the whole composite key)
  - food_id → food_name, serving_size, calories, protein (Partial dependency on just food_id)
  - meal_id → user_id, meal_date (Partial dependency on just meal_id)

![Figure 10.3: Before 2NF](images/figure_10_3_before_2nf.png)
*Figure 10.3: Before 2NF — Partial dependencies on proper subsets of composite candidate key*

- **2NF Decomposition**: We extracted partially dependent attributes into separate tables:
  1. food stores attributes that depend solely on food_id.
  2. meal stores attributes that depend solely on meal_id.
  3. meal_item keeps only quantity, which genuinely depends on the pair (meal_id, food_id).

![Figure 10.4: After 2NF](images/figure_10_4_after_2nf.png)
*Figure 10.4: After 2NF — Decomposed into relations where all non-key attributes are fully dependent on the primary key*

---

### 10.3 Third Normal Form (3NF)
**Rule**: A relation is in 3NF if it is in 2NF and has **no transitive dependencies**. A transitive dependency occurs when non-key attribute A determines non-key attribute B (i.e., X → Y and Y → Z, where Y is not a candidate key).

- **Violation in 2NF Food Table**: In the food table before 3NF, the category name was stored directly alongside the category ID:
  - food_id → category_id
  - category_id → category_name
  - This created a transitive dependency: food_id → category_name via category_id.

![Figure 10.5: Before 3NF](images/figure_10_5_before_3nf.png)
*Figure 10.5: Before 3NF — Transitive dependency creates update and deletion anomalies*

- **3NF Decomposition**: We removed this transitive dependency by separating food_category into its own table (category_id, category_name). The food table now keeps only category_id as a foreign key.

![Figure 10.6: After 3NF](images/figure_10_6_after_3nf.png)
*Figure 10.6: After 3NF — Transitive dependency eliminated by isolating food_category lookup entity*

---

### 10.4 Boyce-Codd Normal Form (BCNF)
**Rule**: A relation is in BCNF if for every non-trivial functional dependency X → Y, the determinant X is a **superkey** (candidate key).

#### BCNF Verification for NutriGuide Relations:
- In user_profile: Determinants are user_id and email. Both are candidate keys.
- In food: Determinants are food_id and food_name. Both are candidate keys.
- In food_category: Determinants are category_id and category_name. Both are candidate keys.
- In nutrition_goal, meal, meal_item, meal_plan, meal_plan_item, recommendation, and activity_log: Every determinant is a primary key or candidate key.

**Result**: All ten tables in our schema strictly satisfy **Boyce-Codd Normal Form (BCNF)**.

### 10.5 Decomposition Summary

| Original Flat Concept | Normalized 3NF/BCNF Relations | Primary Keys | Foreign Keys |
|:---|:---|:---|:---|
| User Profile & Demographics | user_profile | user_id | *None* |
| Daily Target Goals | nutrition_goal | goal_id | user_id |
| Dietary Categories | food_category | category_id | *None* |
| Food Nutrient Catalog | food | food_id | category_id |
| Meal Events Header | meal | meal_id | user_id |
| Multi-Item Meal Lines | meal_item | meal_item_id | meal_id, food_id |
| Scheduled Diet Plans | meal_plan | meal_plan_id | user_id |
| Diet Plan Meal Lines | meal_plan_item | meal_plan_item_id | meal_plan_id, food_id |
| Nutritional Feedback | recommendation | rec_id | user_id |
| Activity Audit Log | activity_log | log_id | user_id |

### 10.6 Lossless Join Verification
To verify that our normalization process was strictly **lossless** (meaning we can join the normalized tables back together without creating fake tuples or losing real data), we run the following multi-table SQL query:

```sql
SELECT 
    u.name AS user_name,
    m.meal_date,
    m.meal_type,
    f.food_name,
    c.category_name,
    mi.quantity,
    ROUND(f.calories * mi.quantity, 2) AS total_calories,
    ROUND(f.protein * mi.quantity, 2) AS total_protein
FROM meal m
JOIN user_profile u ON m.user_id = u.user_id
JOIN meal_item mi ON m.meal_id = mi.meal_id
JOIN food f ON mi.food_id = f.food_id
JOIN food_category c ON f.category_id = c.category_id
WHERE u.user_id = 1
ORDER BY m.meal_date, m.meal_id, mi.meal_item_id;
```

#### Lossless Join Output:
| user_name | meal_date | meal_type | food_name | category_name | quantity | total_calories | total_protein |
|:---|:---|:---|:---|:---|:---|:---|:---|
| John Doe | 2026-10-18 | Lunch | Grilled Chicken Breast | Proteins | 1.00 | 165.00 | 31.00 |
| John Doe | 2026-10-18 | Lunch | Cooked White Rice | Grains | 1.50 | 195.00 | 4.05 |
| John Doe | 2026-10-18 | Dinner | Yellow Dal (Cooked) | Proteins | 1.00 | 116.00 | 7.30 |
| John Doe | 2026-10-18 | Dinner | Whole Wheat Roti | Grains | 2.00 | 240.00 | 7.00 |
"""
