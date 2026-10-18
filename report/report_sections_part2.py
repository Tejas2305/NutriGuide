# -*- coding: utf-8 -*-
"""
NutriGuide DBMS Project Report - Content Sections (Part 2)
Contains Chapters 7 to 10: Relational Schema, Anomalies, Functional Dependencies, Normalization (1NF to BCNF)
"""

def get_part2_md():
    return """
---

## 7. Relational Schema (Step 5)

### 7.1 Relational Notation
*(Underlined: Primary Key | Italic: Foreign Key)*

1. **`user_profile`** (<u>user_id</u>, name, email, password_hash, age, gender, height_cm, weight_kg, activity_level, created_at)
2. **`nutrition_goal`** (<u>goal_id</u>, *user_id*, target_calories, target_protein, target_carbs, target_fats, is_active, created_at)
3. **`food_category`** (<u>category_id</u>, category_name)
4. **`food`** (<u>food_id</u>, *category_id*, food_name, serving_size, calories, protein, carbohydrates, fat)
5. **`meal`** (<u>meal_id</u>, *user_id*, meal_type, meal_date, created_at)
6. **`meal_item`** (<u>meal_item_id</u>, *meal_id*, *food_id*, quantity)
7. **`meal_plan`** (<u>meal_plan_id</u>, *user_id*, plan_name, start_date, end_date, is_active, created_at)
8. **`meal_plan_item`** (<u>meal_plan_item_id</u>, *meal_plan_id*, *food_id*, planned_meal_type, quantity)
9. **`recommendation`** (<u>rec_id</u>, *user_id*, rec_type, message, generated_at)
10. **`activity_log`** (<u>log_id</u>, *user_id*, action_taken, log_time)

### 7.2 Foreign Keys and Referential Actions

| Foreign Key | Child Table | Parent Table & Referenced Column | ON UPDATE | ON DELETE | Rationale |
|:---|:---|:---|:---|:---|:---|
| `fk_nutrition_goal_user` | `nutrition_goal` | `user_profile(user_id)` | `CASCADE` | `CASCADE` | Deleting a user profile removes their historical goal records. |
| `fk_food_category` | `food` | `food_category(category_id)` | `CASCADE` | `RESTRICT` | Prevents accidental deletion of categories that currently contain active foods. |
| `fk_meal_user` | `meal` | `user_profile(user_id)` | `CASCADE` | `CASCADE` | Deleting a user profile cleans up all associated meal headers. |
| `fk_meal_item_meal` | `meal_item` | `meal(meal_id)` | `CASCADE` | `CASCADE` | Deleting a meal event automatically cascades to remove its constituent line items. |
| `fk_meal_item_food` | `meal_item` | `food(food_id)` | `CASCADE` | `RESTRICT` | Prevents deleting a catalog food item if it has been logged in user history. |
| `fk_meal_plan_user` | `meal_plan` | `user_profile(user_id)` | `CASCADE` | `CASCADE` | Removing a user account removes all scheduled diet plans. |
| `fk_meal_plan_item_plan` | `meal_plan_item` | `meal_plan(meal_plan_id)` | `CASCADE` | `CASCADE` | Removing a diet plan removes all associated planned item entries. |
| `fk_meal_plan_item_food` | `meal_plan_item` | `food(food_id)` | `CASCADE` | `RESTRICT` | Prevents deleting foods that are actively scheduled in future diet regimens. |
| `fk_recommendation_user` | `recommendation` | `user_profile(user_id)` | `CASCADE` | `CASCADE` | Purging a user cleans up all historical recommendations. |
| `fk_activity_log_user` | `activity_log` | `user_profile(user_id)` | `CASCADE` | `CASCADE` | Account termination cascades to clean up the user's audit log entries. |

---

## 8. Anomalies (Step 6)

A database anomaly is a data integrity flaw that manifests when unrelated business facts are merged into a single unnormalized relation. To illustrate why NutriGuide must be decomposed into multiple normalized relations, consider an unnormalized "flat" design (`NUTRITION_FLAT`) where user profiles, meal events, food catalog attributes, and food categories are merged into a single table:

#### Unnormalized Relation: `NUTRITION_FLAT`
*(Composite Candidate Key: `{email, meal_date, meal_type, food_name}`)*

| user_id | name | email | height_cm | weight_kg | meal_date | meal_type | food_name | serving_size | calories | protein | category_name | quantity |
|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|
| 1 | John Doe | user@example.com | 175.00 | 70.00 | 2026-10-18 | Lunch | Grilled Chicken | 100.00 | 165.00 | 31.00 | Proteins | 1.00 |
| 1 | John Doe | user@example.com | 175.00 | 70.00 | 2026-10-18 | Lunch | Cooked White Rice | 100.00 | 130.00 | 2.70 | Grains | 1.50 |
| 1 | John Doe | user@example.com | 175.00 | 70.00 | 2026-10-18 | Dinner | Yellow Dal | 100.00 | 116.00 | 7.30 | Proteins | 1.00 |
| 2 | Jane Smith | jane@example.com | 162.00 | 58.00 | 2026-10-18 | Breakfast| Rolled Oats | 100.00 | 389.00 | 16.90 | Grains | 1.00 |

### 8.1 Insertion Anomalies
1. **Inability to Add Foods Without Meals**: If a dietitian adds a newly certified food item (e.g., *Broccoli*) to the system, it cannot be inserted into `NUTRITION_FLAT` because `user_id`, `email`, `meal_date`, and `meal_type` form part of the candidate key and cannot be `NULL`.
2. **Inability to Register New Users**: A newly registered user cannot be stored until they log their first meal event, as food and meal attributes cannot be empty.
3. **Inability to Create Food Categories**: A new food category (e.g., *Beverages*) cannot be recorded unless an accompanying user, meal, and food item are simultaneously entered.

### 8.2 Update Anomalies
1. **Redundant User Attribute Updates**: If John Doe updates his body weight from 70.00 kg to 68.50 kg, the update must be applied across every historical meal row he has ever logged. If an update statement updates only recent rows, John Doe will have contradictory weights in the database.
2. **Food Nutritional Profile Changes**: If the laboratory nutritional evaluation of *Cooked White Rice* updates its carbohydrate content, every past row referencing white rice must be modified. Incomplete updates lead to conflicting nutrient data.
3. **Category Renaming Inconsistency**: Renaming the category *Proteins* to *Protein Sources* requires searching and updating millions of meal item rows.

### 8.3 Deletion Anomalies
1. **Unintended Loss of User Data**: If a user clears their meal history for a specific day, and that happens to be their only logged meal, deleting those rows completely purges their user profile, height, weight, and credentials from the system.
2. **Unintended Loss of Food Catalog Items**: If the only logged occurrence of *Rolled Oats* is deleted by Jane Smith, the entire nutritional profile of *Rolled Oats* and its category definition are permanently erased from the database.

---

## 9. Functional Dependencies (Step 7)

A functional dependency X → Y holds over a relation R if and only if whenever two tuples have identical values for X, they must also have identical values for Y.

The formal functional dependencies governing the NutriGuide schema and their candidate keys are defined as follows:

### 1. `user_profile`
- **Candidate Keys**: `{user_id}`, `{email}`
- **Primary Key**: `user_id`
- **Functional Dependencies**:
  - user_id → name, email, password_hash, age, gender, height_cm, weight_kg, activity_level, created_at
  - email → user_id, name, password_hash, age, gender, height_cm, weight_kg, activity_level, created_at
  - *Rationale*: `user_id` uniquely identifies each user record. Because `email` is defined with a `UNIQUE` constraint, it also deterministically determines all attributes.

### 2. `nutrition_goal`
- **Candidate Key**: `{goal_id}`
- **Primary Key**: `goal_id`
- **Functional Dependencies**:
  - goal_id → user_id, target_calories, target_protein, target_carbs, target_fats, is_active, created_at
  - *Rationale*: Each configured goal is identified by its surrogate key `goal_id`.

### 3. `food_category`
- **Candidate Keys**: `{category_id}`, `{category_name}`
- **Primary Key**: `category_id`
- **Functional Dependencies**:
  - category_id → category_name
  - category_name → category_id
  - *Rationale*: Standard categories are uniquely identified by both numeric ID and unique textual name.

### 4. `food`
- **Candidate Keys**: `{food_id}`, `{food_name}`
- **Primary Key**: `food_id`
- **Functional Dependencies**:
  - food_id → category_id, food_name, serving_size, calories, protein, carbohydrates, fat
  - food_name → food_id, category_id, serving_size, calories, protein, carbohydrates, fat
  - *Rationale*: Nutritional facts are fixed per food item; either `food_id` or unique `food_name` determines all nutritional values.

### 5. `meal`
- **Candidate Key**: `{meal_id}`
- **Primary Key**: `meal_id`
- **Functional Dependencies**:
  - meal_id → user_id, meal_type, meal_date, created_at
  - *Rationale*: A meal event header is uniquely identified by `meal_id`. Multiple meals of the same type on the same date are supported (e.g., multiple snacks).

### 6. `meal_item`
- **Candidate Keys**: `{meal_item_id}`, `{meal_id, food_id}`
- **Primary Key**: `meal_item_id`
- **Functional Dependencies**:
  - meal_item_id → meal_id, food_id, quantity
  - (meal_id, food_id) → meal_item_id, quantity
  - *Rationale*: An individual meal line item references a specific portion quantity of a food within a meal event.

### 7. `meal_plan`
- **Candidate Key**: `{meal_plan_id}`
- **Primary Key**: `meal_plan_id`
- **Functional Dependencies**:
  - meal_plan_id → user_id, plan_name, start_date, end_date, is_active, created_at

### 8. `meal_plan_item`
- **Candidate Key**: `{meal_plan_item_id}`
- **Primary Key**: `meal_plan_item_id`
- **Functional Dependencies**:
  - meal_plan_item_id → meal_plan_id, food_id, planned_meal_type, quantity

### 9. `recommendation`
- **Candidate Key**: `{rec_id}`
- **Primary Key**: `rec_id`
- **Functional Dependencies**:
  - rec_id → user_id, rec_type, message, generated_at

### 10. `activity_log`
- **Candidate Key**: `{log_id}`
- **Primary Key**: `log_id`
- **Functional Dependencies**:
  - log_id → user_id, action_taken, log_time

---

## 10. Normalization (Step 8: 1NF to BCNF)

Normalization is the formal mathematical decomposition of relational schemas to eliminate redundancy and prevent the anomalies identified in Section 8. NutriGuide was engineered by decomposing the initial flat design progressively from 1NF through Boyce-Codd Normal Form (BCNF).

### 10.1 First Normal Form (1NF)
**Rule**: A relation R is in 1NF if and only if all underlying domains contain only atomic (indivisible) values, and there are no repeating groups or multi-valued attributes.

- **Violation in Unnormalized Flat Design**: In the unnormalized flat meal record, multiple food items and their respective portion quantities are grouped into comma-separated strings inside a single table cell (e.g., `"Grilled Chicken, Cooked Rice"` and `"1.0, 1.5"`).

![Figure 10.1: Before 1NF](images/figure_10_1_before_1nf.png)
*Figure 10.1: Before 1NF — Multi-valued repeating groups packed into single cells*

- **1NF Decomposition**: Repeating groups are eliminated by decomposing the data into separate records where each cell holds exactly one atomic value. The parent meal header is isolated in `meal`, while individual food lines are stored in `meal_item`.

![Figure 10.2: After 1NF](images/figure_10_2_after_1nf.png)
*Figure 10.2: After 1NF — Decomposed into meal and meal_item with strictly atomic attributes*

---

### 10.2 Second Normal Form (2NF)
**Rule**: A relation R is in 2NF if and only if it is in 1NF and every non-prime attribute is **fully functionally dependent** on the entire primary key (no partial functional dependencies on proper subsets of candidate keys).

- **Violation in 1NF Composite Table**: If we consider an associative relation combining `(meal_id, food_id)` as the composite key along with user attributes and food nutritional values:
  - (meal_id, food_id) → quantity (Full dependency on composite key)
  - food_id → food_name, serving_size, calories, protein (Partial dependency on proper subset food_id)
  - meal_id → user_id, meal_date (Partial dependency on proper subset meal_id)

![Figure 10.3: Before 2NF](images/figure_10_3_before_2nf.png)
*Figure 10.3: Before 2NF — Partial dependencies on proper subsets of composite candidate key*

- **2NF Decomposition**: The partially dependent attributes are extracted into independent relations:
  1. `food` retains attributes determined solely by food_id.
  2. `meal` retains attributes determined solely by meal_id.
  3. `meal_item` retains only quantity, which requires the complete composite key (meal_id, food_id).

![Figure 10.4: After 2NF](images/figure_10_4_after_2nf.png)
*Figure 10.4: After 2NF — Decomposed into relations where all non-key attributes are fully dependent on the primary key*

---

### 10.3 Third Normal Form (3NF)
**Rule**: A relation R is in 3NF if and only if it is in 2NF and **no non-prime attribute is transitively dependent** on any candidate key (X → Y and Y → Z where Y is not a candidate key).

- **Violation in 2NF Food Table**: In the food relation before 3NF, the category name is included directly alongside the category ID:
  - food_id → category_id
  - category_id → category_name
  - Therefore: food_id → category_name is a **transitive dependency** through non-candidate key category_id.

![Figure 10.5: Before 3NF](images/figure_10_5_before_3nf.png)
*Figure 10.5: Before 3NF — Transitive dependency creates update and deletion anomalies*

- **3NF Decomposition**: The transitive dependency is eliminated by extracting `food_category` into an independent reference table (`category_id`, `category_name`), leaving `food` with only the foreign key `category_id`.

![Figure 10.6: After 3NF](images/figure_10_6_after_3nf.png)
*Figure 10.6: After 3NF — Transitive dependency eliminated by isolating food_category lookup entity*

---

### 10.4 Boyce-Codd Normal Form (BCNF)
**Rule**: A relation R is in BCNF if and only if for every non-trivial functional dependency X → Y, X is a **superkey** (candidate key).

#### BCNF Verification for NutriGuide Relations:
- In `user_profile`: Non-trivial dependencies have determinants user_id or email. Both are candidate keys. ✓
- In `food`: Determinants are food_id and food_name. Both are unique candidate keys. ✓
- In `food_category`: Determinants are category_id and category_name. Both are unique candidate keys. ✓
- In `nutrition_goal`, `meal`, `meal_item`, `meal_plan`, `meal_plan_item`, `recommendation`, `activity_log`: Every determinant is a primary or candidate key. ✓

**Conclusion**: All ten relations in the NutriGuide schema strictly satisfy **Boyce-Codd Normal Form (BCNF)**.

### 10.5 Decomposition Summary

| Unnormalized Flat Concept | Normalized 3NF/BCNF Relations | Primary Keys | Foreign Keys |
|:---|:---|:---|:---|
| User & Demographics | `user_profile` | `user_id` | *None* |
| Calorie & Macro Goals | `nutrition_goal` | `goal_id` | `user_id` |
| Food Categories | `food_category` | `category_id` | *None* |
| Reference Food Catalog | `food` | `food_id` | `category_id` |
| Meal Events Header | `meal` | `meal_id` | `user_id` |
| Meal Items (Multi-Item Lines) | `meal_item` | `meal_item_id` | `meal_id`, `food_id` |
| Prospective Diet Plans | `meal_plan` | `meal_plan_id` | `user_id` |
| Diet Plan Items | `meal_plan_item` | `meal_plan_item_id` | `meal_plan_id`, `food_id` |
| Recommendation History | `recommendation` | `rec_id` | `user_id` |
| System Audit Trail | `activity_log` | `log_id` | `user_id` |

### 10.6 Lossless Join Verification
To prove that the decomposition is mathematically **lossless** (no information is created or lost), the original flat meal record is reconstructed by joining the normalized relations:

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

#### Lossless Reconstruction Output:
| user_name | meal_date | meal_type | food_name | category_name | quantity | total_calories | total_protein |
|:---|:---|:---|:---|:---|:---|:---|:---|
| John Doe | 2026-10-18 | Lunch | Grilled Chicken Breast | Proteins | 1.00 | 165.00 | 31.00 |
| John Doe | 2026-10-18 | Lunch | Cooked White Rice | Grains | 1.50 | 195.00 | 4.05 |
| John Doe | 2026-10-18 | Dinner | Yellow Dal (Cooked) | Proteins | 1.00 | 116.00 | 7.30 |
| John Doe | 2026-10-18 | Dinner | Whole Wheat Roti | Grains | 2.00 | 240.00 | 7.00 |
"""
