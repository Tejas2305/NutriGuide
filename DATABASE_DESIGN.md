# NutriGuide – Relational Database Design & Normalization Document

This document provides the foundational engineering specification for the **NutriGuide** relational database (`nutriguide`), addressing the academic requirements of **DBMS Lab Manual Experiments 1 & 9**.

---

## 1. Entity-Relationship (ER) Architecture (DBMS Exp 1)

### 1.1 Identified Entities & Cardinalities

The NutriGuide domain models 10 distinct entities structured around the user, food catalog, meal logging, and planning domains:

1. **`user_profile`**: Central entity capturing demographic and anthropometric parameters.
2. **`nutrition_goal`**: Target macronutrient goals (1:N with `user_profile`, historical versioning via `is_active`).
3. **`food_category`**: High-level food groupings (1:N with `food`).
4. **`food`**: Nutritional catalog with caloric and macronutrient values per serving.
5. **`meal`**: Historical meal events logged by users (1:N with `user_profile`). Multiple meals per type/day allowed (e.g., multiple snacks).
6. **`meal_item`**: Associative junction table resolving the **M:N relationship between `meal` and `food`**.
7. **`meal_plan`**: Prospective diet plans scheduled over date intervals (1:N with `user_profile`).
8. **`meal_plan_item`**: Associative junction table resolving the **M:N relationship between `meal_plan` and `food`**.
9. **`recommendation`**: Rule-based engine outputs advising users on nutritional gaps (1:N with `user_profile`).
10. **`activity_log`**: System audit trail capturing user lifecycle events and trigger-logged meal records (1:N with `user_profile`).

### 1.2 ER Cardinality Diagram (Textual Representation)

```text
[user_profile] 1 ──── N [nutrition_goal]
[user_profile] 1 ──── N [meal] 1 ──── N [meal_item] N ──── 1 [food]
[user_profile] 1 ──── N [meal_plan] 1 ──── N [meal_plan_item] N ──── 1 [food]
[user_profile] 1 ──── N [recommendation]
[user_profile] 1 ──── N [activity_log]
[food_category] 1 ──── N [food]
```

### 1.3 Referencing & Integrity Policies
- **`meal` -> `meal_item`**: `ON DELETE CASCADE` (deleting a meal removes its item lines).
- **`food` -> `meal_item`**: `ON DELETE RESTRICT` (prevents deleting active food items referenced in user history).
- **`user_profile` -> child tables**: `ON DELETE CASCADE` (user account deletion cleans up user data).
- **`food_category` -> `food`**: `ON DELETE RESTRICT` (categories with active foods cannot be dropped).

---

## 2. Functional Dependencies & Normalization (DBMS Exp 9)

### 2.1 Formal Functional Dependencies for All 10 Relations

#### 1. `user_profile`
- **Candidate Key**: `{user_id}`, `{email}`
- **Primary Key**: `user_id`
- **Functional Dependencies**:
  - `user_id -> name, email, password_hash, age, gender, height_cm, weight_kg, activity_level, created_at`
  - `email -> user_id, name, password_hash, age, gender, height_cm, weight_kg, activity_level, created_at`

#### 2. `nutrition_goal`
- **Candidate Key**: `{goal_id}`
- **Primary Key**: `goal_id`
- **Functional Dependencies**:
  - `goal_id -> user_id, target_calories, target_protein, target_carbs, target_fats, is_active, created_at`

#### 3. `food_category`
- **Candidate Key**: `{category_id}`, `{category_name}`
- **Primary Key**: `category_id`
- **Functional Dependencies**:
  - `category_id -> category_name`
  - `category_name -> category_id`

#### 4. `food`
- **Candidate Key**: `{food_id}`, `{food_name}`
- **Primary Key**: `food_id`
- **Functional Dependencies**:
  - `food_id -> category_id, food_name, serving_size, calories, protein, carbohydrates, fat`
  - `food_name -> food_id, category_id, serving_size, calories, protein, carbohydrates, fat`

#### 5. `meal`
- **Candidate Key**: `{meal_id}`
- **Primary Key**: `meal_id`
- **Functional Dependencies**:
  - `meal_id -> user_id, meal_type, meal_date, created_at`

#### 6. `meal_item`
- **Candidate Key**: `{meal_item_id}`
- **Primary Key**: `meal_item_id`
- **Functional Dependencies**:
  - `meal_item_id -> meal_id, food_id, quantity`

#### 7. `meal_plan`
- **Candidate Key**: `{meal_plan_id}`
- **Primary Key**: `meal_plan_id`
- **Functional Dependencies**:
  - `meal_plan_id -> user_id, plan_name, start_date, end_date, is_active, created_at`

#### 8. `meal_plan_item`
- **Candidate Key**: `{meal_plan_item_id}`
- **Primary Key**: `meal_plan_item_id`
- **Functional Dependencies**:
  - `meal_plan_item_id -> meal_plan_id, food_id, planned_meal_type, quantity`

#### 9. `recommendation`
- **Candidate Key**: `{rec_id}`
- **Primary Key**: `rec_id`
- **Functional Dependencies**:
  - `rec_id -> user_id, message, rec_type, generated_at`

#### 10. `activity_log`
- **Candidate Key**: `{log_id}`
- **Primary Key**: `log_id`
- **Functional Dependencies**:
  - `log_id -> user_id, action_taken, log_time`

---

## 3. Formal Proof of Third Normal Form (3NF)

### 3.1 First Normal Form (1NF) Compliance
- Every attribute contains only atomic (indivisible) scalar values (e.g. `VARCHAR`, `DECIMAL`, `INT`, `DATE`).
- No multi-valued attributes (e.g., foods consumed are normalized into `meal_item` records rather than stored as a comma-separated array or JSON list inside `meal`).
- No repeating column groups (e.g., `food_1`, `food_2`, `food_3`).

### 3.2 Second Normal Form (2NF) Compliance
- The schema is in 1NF.
- In every relation with a composite candidate key or surrogate single-attribute primary key, every non-prime attribute is fully functionally dependent on the whole candidate key.
- Because all 10 relations use single-attribute surrogate primary keys (`user_id`, `meal_id`, `meal_item_id`, etc.), **no partial functional dependencies can exist**.

### 3.3 Third Normal Form (3NF) Compliance
- The schema is in 2NF.
- For every non-trivial functional dependency $X \rightarrow Y$:
  1. $X$ is a superkey, or
  2. $Y$ is a prime attribute.
- **Absence of Transitive Dependencies**:
  - No non-prime attribute depends on another non-prime attribute ($A \rightarrow B \rightarrow C$).
  - **Critical Design Case: Total Calories in `meal`**:
    - If `meal` had stored `total_meal_calories`, the functional dependency chain would be:
      $$\text{meal\_id} \rightarrow \text{meal\_item\_id} \rightarrow \text{food\_id} \rightarrow \text{calories} \rightarrow \text{total\_meal\_calories}$$
      This is a textbook **transitive dependency** violating 3NF, creating update/delete anomalies if a food's nutritional value changed or an item quantity was updated.
    - **NutriGuide Solution**: `meal` intentionally does NOT store derived totals. Totals are computed on-demand via the `daily_nutrition_summary` SQL VIEW or stored procedures, preserving mathematical 3NF purity.
