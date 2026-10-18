# -*- coding: utf-8 -*-
"""
NutriGuide DBMS Project Report - Content Sections (Part 1)
Contains Front Matter, Index, Chapters 1-6
"""

def get_front_matter_md():
    return """# SYMBIOSIS INTERNATIONAL (DEEMED UNIVERSITY)
### Department of Computer Science, Symbiosis Institute of Technology, Pune

---

# A DBMS MINI PROJECT REPORT ON
## **NutriGuide — Your Personal Nutrition Companion**

![Cover Illustration](images/cover_illustration.jpg)

### **Submitted by:**
- **Tejas Jagdish Patil** (PRN: 26070122510)
- **Veer Parmar** (PRN: 26070122508)
- **Postiwala Vatsal Dipakbhai** (PRN: 26070122509)
- **Parth Bhayani** (PRN: 26070122512)

### **Under the Guidance of:**
**Dr. Balkrishna Patil**

**Department of Computer Science**  
**Symbiosis Institute of Technology (SIT), Pune**  
**Symbiosis International (Deemed University)**  
*Academic Year 2026–2027*

---

## INDEX

| Sr. No. | Title | Page No. |
|:---|:---|:---|
| 1. | **Introduction** | 2 |
| 2. | **Problem Statement (Step 1)** | 3 |
| 3. | **System Architecture and Modules** | 4 |
| 4. | **Functional and Non-Functional Requirements (Step 2)** | 5–6 |
| 5. | **Entities, Relationships and Attributes (Step 3)** | 7–8 |
| 6. | **ER and EER Diagrams (Step 4)** | 9–10 |
| 7. | **Relational Schema (Step 5)** | 11 |
| 8. | **Anomalies (Step 6)** | 12 |
| 9. | **Functional Dependencies (Step 7)** | 13–14 |
| 10. | **Normalization (Step 8: 1NF to BCNF)** | 15–17 |
| 11. | **Implementation of Tables in MySQL (Step 9)** | 18–22 |
| 12. | **Functions, Procedures, Triggers and Queries (Step 10)** | 23–30 |
| 13. | **Application of Codd's 12 Rules** | 31–32 |
| 14. | **Individual Contributions (Step 11)** | 33 |
| 15. | **Conclusion** | 34 |
| 16. | **References** | 35 |
| **App. A** | **Complete SQL Script (`NutriGuide.sql`)** | 35–36 |
| **App. B** | **Stored Functions, Procedures, Views & Triggers** | 37–38 |

---

## 1. Introduction

In modern preventive healthcare and personal wellness management, dietary intake and nutrient balance play a pivotal role. The widespread availability of processed foods, irregular eating routines, and diverging nutritional needs across individuals have created an urgent imperative for structured dietary tracking systems. Many individuals attempt to monitor their caloric and macronutrient intake (proteins, carbohydrates, fats) using manual logbooks, generic smartphone note apps, or disconnected spreadsheets. However, manual recording systems quickly deteriorate: mathematical aggregation is labor-intensive, multi-item meals are recorded with varying serving definitions, and longitudinal history becomes fragmented or lost.

**NutriGuide — Your Personal Nutrition Companion** is a comprehensive, production-grade relational database management system designed to systematically capture, organize, analyze, and optimize personal nutrition data. Engineered specifically for academic evaluation and clinical-demonstration rigor under the Database Management Systems (DBMS) curriculum, NutriGuide provides an enterprise-quality relational backend running on the **MySQL 8.x** relational database engine with InnoDB ACID transactional semantics.

The database models ten tightly integrated entities spanning:
1. **User Demographics & Anthropometrics**: Secure user credentials, demographic parameters (age, gender), physical measurements (height in centimeters, weight in kilograms), and physical activity levels.
2. **Macronutrient & Caloric Target Goals**: User-defined nutritional targets (calories, protein, carbohydrates, fat) with historical versioning and single-active-goal business rule enforcement.
3. **Reference Food Catalog**: Categorized food items (Grains, Proteins, Dairy, Fruits, Vegetables) with precise nutritional densities per standard serving size.
4. **Multi-Item Meal Transactions**: Real-time logging of breakfast, lunch, dinner, and snack meal events, resolved atomically through an associative junction entity (`meal_item`).
5. **Prospective Meal Planning**: Weekly and monthly diet regimens scheduled across date intervals, linked to designated target meal slots.
6. **Rule-Based Recommendation History**: Persisted dietary advisory guidelines evaluating user caloric deficits, surpluses, and protein compliance against standard WHO nutritional benchmarks.
7. **Automated Audit Logging**: Real-time audit trails maintained via database triggers capturing user operations without application-side overhead.

The core relational database is implemented in MySQL 8.x, enforcing schema stability through primary keys, foreign keys with explicit referential integrity actions (`ON UPDATE CASCADE`, `ON DELETE CASCADE`, `ON DELETE RESTRICT`), candidate key uniqueness, and declarative CHECK constraints. Above the table storage layer, NutriGuide incorporates high-level database programmability including stored functions for dynamic Body Mass Index (BMI) computation, stored procedures with output parameters for aggregate daily nutrition summaries, analytical views, and audit/validation triggers.

---

## 2. Problem Statement (Step 1)

Individuals and dietary consultants seeking to manage nutritional health through manual record-keeping face several systematic data management challenges:

1. **Scattered and Disorganized Records**: Dietary intake, body weight fluctuations, target goals, and food nutrient values are frequently noted in disparate physical notebooks, phone memos, or isolated spreadsheets. As a result, compiling a consolidated, longitudinal picture of an individual's nutritional health is virtually impossible.
2. **Nutrient Calculation Errors & Latency**: Computing cumulative daily calories and macro splits (grams of protein, carbohydrates, and fats) requires repetitive arithmetic scaling based on portion size. Manual calculations are prone to severe calculation errors, misleading users regarding their actual intake.
3. **Data Redundancy & Inconsistency (Flat File Flaws)**: When nutritional data is stored in unnormalized flat files, food descriptions and nutrient densities are repeatedly duplicated across every logged meal. If the nutritional profile of an item (e.g., Cooked White Rice) is updated, records in earlier rows remain unchanged, creating contradictory records.
4. **Lack of Validation & Boundary Integrity**: Manual systems fail to prevent erroneous inputs such as negative serving sizes, negative weights or heights, or impossible calorie amounts.
5. **Absence of Transactional Atomicity**: A meal typically consists of multiple foods consumed together. In traditional spreadsheets, a partial crash during entry leaves orphaned records where a meal is recorded without its accompanying food components.
6. **No Automated Audit History**: Manual systems provide no reliable mechanism to detect when a meal was logged, modified, or deleted, impeding auditability and compliance tracking.
7. **Disconnected Goal Analysis**: Without automated relational joins between logged intake and active goals, users cannot deterministically evaluate whether they are in a caloric surplus or deficit.

### Proposed Solution
NutriGuide resolves each of these challenges through a centralized, strictly normalized relational database. The schema eliminates data redundancy, guarantees referential and transactional integrity, automates dynamic metric calculations through stored subprograms, enforces data validation at the storage layer via CHECK constraints and triggers, and enables instantaneous reporting through pre-aggregated SQL views.

---

## 3. System Architecture and Modules

### 3.1 Three-Tier Relational Topology
The NutriGuide architecture is structured into three clean, decoupled tiers:

1. **Presentation Layer (Java Swing Client)**: An ergonomic Windows desktop user interface built using Java Swing. It provides dedicated user interface windows (`LoginFrame`, `RegisterFrame`, `DashboardFrame`, `MealLogFrame`, `NutritionGoalFrame`, `RecommendationFrame`, `ReportFrame`, `ProfileFrame`) running on the Swing Event Dispatch Thread (EDT). All long-running API operations are dispatched asynchronously using `SwingWorker` threads to prevent user interface freezing.
2. **Application & Logic Layer (Java HttpServer)**: A lightweight, high-performance HTTP REST server running on `http://127.0.0.1:8081`. It encapsulates the application services (`AuthenticationService`, `NutritionService`, `MealService`, `FoodService`, `RecommendationService`, `ReportService`, `UserService`), token-based `SessionManager`, and salted `PBKDF2-HMAC-SHA256` password security. It issues parameterized JDBC queries to MySQL.
3. **Relational Database Layer (MySQL 8.x Engine)**: The relational database tier manages table storage using the InnoDB storage engine. It provides ACID transactional guarantees, enforces relational integrity, executes stored functions and procedures, maintains aggregation views, and automatically invokes audit and validation triggers.

![Figure 3.1: System Architecture](images/system_architecture.png)
*Figure 3.1: NutriGuide System Architecture & Multi-Tier Topology*

### 3.2 System Modules

1. **User Profile & Authentication Module (`user_profile`)**:
   - Manages user account intake, authentication, and secure demographic tracking.
   - Stores user height, weight, age, gender, and physical activity tier.
   - Protects user credentials using salted PBKDF2-HMAC-SHA256 password digests with an adaptive 600,000-iteration work factor.
2. **Nutrition Goal Management Module (`nutrition_goal`)**:
   - Allows users to configure custom daily caloric and macronutrient targets.
   - Implements historical versioning through an `is_active` status flag.
   - Enforces the business constraint that exactly one goal may remain active per user at any given timestamp.
3. **Food Catalog & Categorization Module (`food_category`, `food`)**:
   - Maintains a standardized, immutable reference catalog of foods classified into primary dietary groups (Grains, Proteins, Dairy, Fruits, Vegetables).
   - Tracks standard serving sizes in grams and associated energy/macronutrient values (calories, protein, carbs, fat).
4. **Meal Logging & Transaction Module (`meal`, `meal_item`)**:
   - Captures daily meal events (Breakfast, Lunch, Dinner, Snack).
   - Utilizes an associative junction entity (`meal_item`) to resolve the many-to-many relationship between meals and food items.
   - Enforces ACID transactions with atomic commit and rollback capabilities.
5. **Meal Planning Module (`meal_plan`, `meal_plan_item`)**:
   - Enables prospective diet regimen scheduling across structured date windows (e.g., 7-day weight cut plan, 30-day muscle gain plan).
   - Resolves planned meal schedules against food catalog entries.
6. **Rule-Based Recommendation Engine (`recommendation`)**:
   - Evaluates active nutritional goals against actual aggregate daily intake.
   - Deterministically generates clinically grounded educational guidance regarding caloric deficits, surpluses, and protein adequacy.
7. **Automated Audit & Logging Module (`activity_log`)**:
   - Maintains a tamper-evident audit history of user lifecycle actions and trigger-logged meal transactions.
8. **Reporting & Analytics Module (`views`, `procedures`)**:
   - Leverages SQL views (`daily_nutrition_summary`, `food_category_details`) and stored procedures (`get_daily_summary`) to generate instantaneous analytical reports for date ranges.

---

## 4. Functional and Non-Functional Requirements (Step 2)

### 4.1 Functional Requirements

| Requirement ID | Functional Description | Implemented Database Object |
|:---|:---|:---|
| **FR1** | Store and retrieve user profiles including height, weight, age, gender, and activity level. | Table `user_profile` |
| **FR2** | Securely hash user passwords using salted PBKDF2 with 600,000 iterations; prevent plaintext leakage. | Table `user_profile.password_hash`, `PasswordUtil.java` |
| **FR3** | Enforce global uniqueness of user email addresses to prevent duplicate registrations. | Constraint `UNIQUE KEY uq_user_email` on `user_profile` |
| **FR4** | Maintain food categories to systematically classify nutritional catalog items. | Table `food_category` |
| **FR5** | Store verified food items with per-serving caloric, protein, carbohydrate, and fat densities. | Table `food` (FK `category_id`) |
| **FR6** | Enforce uniqueness of food names to avoid duplicate food item creation. | Constraint `UNIQUE KEY uq_food_name` on `food` |
| **FR7** | Prevent physical deletion of food items referenced in past meal records to preserve historical integrity. | Referential Action `ON DELETE RESTRICT` on `meal_item.food_id` |
| **FR8** | Allow users to establish macronutrient and caloric target goals with historical archiving. | Table `nutrition_goal` |
| **FR9** | Enforce that only one nutrition goal is active (`is_active = TRUE`) per user account. | Service Layer Transaction & Table `nutrition_goal` |
| **FR10** | Record meal events linked to an authenticated user with meal type and date. | Table `meal` (FK `user_id`) |
| **FR11** | Atomically record multi-item meal lines linked to a parent meal and food catalog item. | Table `meal_item` (Composite/Surrogate PK, FKs) |
| **FR12** | Automatically log meal insertion events into the audit log without application intervention. | Trigger `trg_after_meal_insert` on `meal` |
| **FR13** | Reject invalid or non-positive food serving quantities at the database engine level. | Trigger `trg_validate_meal_quantity` (SIGNAL '45000') |
| **FR14** | Automatically compute user Body Mass Index (BMI) dynamically via a stored database function. | Stored Function `calculate_bmi(weight_kg, height_cm)` |
| **FR15** | Provide instantaneous multi-table aggregated daily nutritional intake totals. | Relational View `daily_nutrition_summary` |
| **FR16** | Return consolidated daily macro totals via stored procedure with output parameters. | Stored Procedure `get_daily_summary(...)` |
| **FR17** | Store prospective multi-day diet plans and their component planned meal items. | Tables `meal_plan`, `meal_plan_item` |
| **FR18** | Automatically clean up all dependent records (goals, meals, plans, logs) upon user profile deletion. | Referential Action `ON DELETE CASCADE` across foreign keys |

### 4.2 Non-Functional Requirements

- **Data Integrity & Consistency**: Enforce strict schema constraints including declarative PRIMARY KEYs, FOREIGN KEYs with explicit referential integrity rules, and CHECK constraints guarding against negative physical metrics.
- **ACID Transaction Compliance**: Multi-item meal logging and goal transitions must execute within explicit atomic transaction boundaries with rollback on exception.
- **Precision of Nutritional Data**: All caloric and macronutrient quantities must use high-precision fixed-point numeric types (`DECIMAL(7,2)`, `DECIMAL(6,2)`, `DECIMAL(5,2)`) instead of floating-point representations (`FLOAT`, `DOUBLE`) to prevent IEEE 754 rounding drift.
- **Architectural Security**: The database binds strictly to local loops; passwords are never persisted or returned in plaintext; all SQL queries utilize parameterized statements to prevent SQL injection.
- **Modularity & High Cohesion**: Schema separation strictly adheres to Third Normal Form (3NF), ensuring that tables correspond to discrete real-world business entities.

---

## 5. Entities, Relationships and Attributes (Step 3)

### 5.1 Entities and Attributes

| Entity / Table Name | Primary Key | Foreign Keys | Key Attributes | Business Purpose |
|:---|:---|:---|:---|:---|
| **`user_profile`** | `user_id` | *None* | `name`, `email`, `password_hash`, `age`, `gender`, `height_cm`, `weight_kg`, `activity_level`, `created_at` | Stores registered user demographic, anthropometric, and credential records. |
| **`nutrition_goal`** | `goal_id` | `user_id` | `target_calories`, `target_protein`, `target_carbs`, `target_fats`, `is_active`, `created_at` | Stores caloric and macronutrient daily targets configured by users. |
| **`food_category`** | `category_id` | *None* | `category_name` | Classifies food items into distinct dietary classifications. |
| **`food`** | `food_id` | `category_id` | `food_name`, `serving_size`, `calories`, `protein`, `carbohydrates`, `fat` | Reference catalog of consumable foods and their nutrient densities. |
| **`meal`** | `meal_id` | `user_id` | `meal_type`, `meal_date`, `created_at` | Represents a discrete meal event consumed by a user on a specific date. |
| **`meal_item`** | `meal_item_id` | `meal_id`, `food_id` | `quantity` | Associative junction resolving the M:N relationship between meals and foods. |
| **`meal_plan`** | `meal_plan_id` | `user_id` | `plan_name`, `start_date`, `end_date`, `is_active`, `created_at` | Prospective diet regimen schedules established over date intervals. |
| **`meal_plan_item`** | `meal_plan_item_id` | `meal_plan_id`, `food_id` | `planned_meal_type`, `quantity` | Associative junction resolving the M:N relationship between diet plans and foods. |
| **`recommendation`** | `rec_id` | `user_id` | `rec_type`, `message`, `generated_at` | Persisted rule-based educational guidelines and advisories. |
| **`activity_log`** | `log_id` | `user_id` | `action_taken`, `log_time` | Audit trail capturing user system interactions and trigger-logged meal transactions. |

### 5.2 Relationships and Cardinalities

| Relationship | Entity 1 | Entity 2 | Cardinality | Participation | Description |
|:---|:---|:---|:---|:---|:---|
| **User_Sets_Goals** | `user_profile` | `nutrition_goal` | **1 : N** | User: Partial<br>Goal: Total | One user can configure multiple historical goals; each goal belongs to exactly one user. |
| **User_Logs_Meals** | `user_profile` | `meal` | **1 : N** | User: Partial<br>Meal: Total | A user can log zero, one, or many meals; each meal is logged by exactly one user. |
| **Meal_Contains_Items** | `meal` | `meal_item` | **1 : N** | Meal: Total<br>Item: Total | A meal contains one or more meal items; each line item belongs to one meal header. |
| **Food_Appears_In_Items**| `food` | `meal_item` | **1 : N** | Food: Partial<br>Item: Total | A food can appear in multiple meal items; each line item references one specific food. |
| **Category_Classifies_Food**| `food_category` | `food` | **1 : N** | Category: Partial<br>Food: Total | A category groups multiple foods; each food belongs to exactly one category. |
| **User_Schedules_Plans** | `user_profile` | `meal_plan` | **1 : N** | User: Partial<br>Plan: Total | A user can create zero, one, or many prospective diet plans. |
| **Plan_Includes_Items** | `meal_plan` | `meal_plan_item`| **1 : N** | Plan: Total<br>Item: Total | A meal plan incorporates one or more scheduled food entries. |
| **Food_In_Meal_Plans** | `food` | `meal_plan_item`| **1 : N** | Food: Partial<br>Item: Total | A food can be scheduled across multiple diet plans. |
| **User_Receives_Recs** | `user_profile` | `recommendation`| **1 : N** | User: Partial<br>Rec: Total | A user can receive multiple tailored recommendations over time. |
| **User_Generates_Audit**| `user_profile` | `activity_log` | **1 : N** | User: Partial<br>Log: Total | User events and trigger executions generate audit records in the activity log. |

#### Resolution of Many-to-Many Relationships
- **Meal and Food (M:N)**: A meal can consist of multiple foods (e.g., Rice, Dal, Salad), and a single food can be consumed across thousands of different meals. This M:N cardinality is decomposed into two 1:N relationships via the **`meal_item`** associative entity with `quantity` as an intersection attribute.
- **Meal_Plan and Food (M:N)**: A prospective diet plan contains multiple planned foods, and a food can be scheduled across numerous diet plans. This M:N relationship is resolved into two 1:N relationships via the **`meal_plan_item`** associative entity.

---

## 6. ER and EER Diagrams (Step 4)

### 6.1 Conceptual ER Diagram (Chen Notation)
The Entity-Relationship (ER) diagram below illustrates the conceptual schema of NutriGuide following standard Chen notation:
- **Rectangles**: Represent the ten fundamental entity sets.
- **Diamonds**: Represent relationship sets linking entities.
- **Ellipses**: Represent entity attributes, with primary keys underlined and unique constraints indicated.
- **Numeric Cardinalities**: Expressed along the relationship lines (`1` and `N`).

![Figure 6.1: ER Diagram](images/er_diagram.png)
*Figure 6.1: NutriGuide Conceptual ER Diagram (Chen Notation) with M:N Associative Resolution*

### 6.2 Enhanced Entity-Relationship (EER) Diagram
The Enhanced ER (EER) diagram provides full physical implementation specifics:
- Detailed schema table cards with explicit data types, primary keys, and foreign key linkages.
- Exact referential integrity constraints (`ON DELETE CASCADE`, `ON DELETE RESTRICT`, `ON UPDATE CASCADE`).
- Declarative CHECK constraints governing demographic and nutritional boundaries.
- The trigger-maintained audit relationship: a dashed pipeline illustrating `trg_after_meal_insert` automatically populating `activity_log`.

![Figure 6.2: EER Diagram](images/eer_diagram.png)
*Figure 6.2: NutriGuide Enhanced Entity-Relationship (EER) Relational Schema with Constraints and Foreign Keys*
"""
