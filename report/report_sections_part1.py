# -*- coding: utf-8 -*-
"""
NutriGuide DBMS Project Report - Content Sections (Part 1)
Contains Front Matter, Index, Chapters 1-6
Authentic student engineering tone for Symbiosis Institute of Technology (SIT), Pune.
"""

def get_front_matter_md():
    return """# SYMBIOSIS INTERNATIONAL (DEEMED UNIVERSITY)
### Department of Computer Science, Symbiosis Institute of Technology, Pune

---

# A DBMS MINI PROJECT REPORT ON
## **NutriGuide — Your Personal Nutrition Companion**

![Cover Illustration](images/cover_illustration.jpg)

### **Batch: B3 | Group: GRP-1**

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
| **App. A** | **Complete SQL Script (NutriGuide.sql)** | 35–36 |
| **App. B** | **Stored Functions, Procedures, Views & Triggers** | 37–38 |

---

## 1. Introduction

Tracking daily nutrition is surprisingly difficult in everyday student and working life. Most of us eat meals on erratic schedules, grab quick snacks between classes or meetings, and rarely have an accurate idea of our actual calorie and macronutrient intake. When students or gym-goers attempt to fix their diet, they typically write food names in spiral notebooks, smartphone notes apps, or basic Excel sheets. In practice, this manual record-keeping quickly falls apart. Scaling protein and calorie numbers by hand every time portion sizes change is tedious. Food names get entered with spelling inconsistencies, and data ends up scattered across months without any clear structure or summary.

To solve this practical problem, our group designed and built NutriGuide, a desktop database application powered by a Java Swing user interface, an embedded Java HTTP REST backend running on port 8081, and a relational MySQL 8.0 database engine using InnoDB. Developed as our Database Management Systems (DBMS) mini-project, our main goal was to take database concepts taught in the classroom and apply them directly to a working software system. We focused on solid relational schema design: eliminating insertion, update, and deletion anomalies through normalization up to Boyce-Codd Normal Form (BCNF), preserving data integrity with explicit foreign key rules (CASCADE and RESTRICT), and using stored database programs to run heavy aggregations directly inside the database engine.

Our database schema consists of ten connected tables that manage the complete nutrition workflow:
1. User Profiles and Health Metrics: Storing basic demographic information (age, gender), physical measurements (height in cm, weight in kg), and physical activity levels along with securely hashed user credentials.
2. Nutrition Goals: Storing daily targets for calories, protein, carbohydrates, and fats. We implemented active-flag versioning so users can update targets over time without erasing their previous goal history.
3. Food Item Catalog: A verified catalog of food items organized into primary dietary categories (Grains, Proteins, Dairy, Fruits, Vegetables), complete with reference calories and macronutrient densities per standard 100g serving.
4. Multi-Item Meal Logs: Real-time logging of breakfast, lunch, dinner, and snack events, broken down cleanly into individual food line items using the associative junction table meal_item.
5. Diet Planning: Allowing users to set up weekly or monthly meal schedules in advance with defined start and end dates.
6. Dietary Recommendations: Automated guidelines that compare user goals against real consumed intake to identify caloric surpluses, deficits, and protein shortfalls.
7. Database Audit Logging: An automated audit trail written by database triggers on meal insertion, ensuring full accountability without requiring extra lines of application code.

The backend implementation relies on MySQL 8.0 features including primary keys, candidate key uniqueness (UNIQUE), foreign keys with explicit cascade and restrict policies, and declarative CHECK constraints to ensure invalid data never enters the tables. Above the basic tables, we implemented stored functions (dynamic BMI calculation), stored procedures with output parameters for daily summaries, database views for instant aggregations, and triggers for automated validation and auditing.

---

## 2. Problem Statement (Step 1)

When people try to record and track their nutrition by hand or through casual spreadsheets, they repeatedly run into practical data management hurdles:

1. Scattered and Disorganized Records: People tend to note daily meals in one place, body weight measurements in another, and their fitness goals on random sheets of paper. Because there is no central database linking these pieces together, analyzing changes in body weight against actual caloric intake over time becomes nearly impossible.
2. Repetitive Calculation Errors: Calculating daily totals of calories, proteins, carbs, and fats requires multiplying portion sizes by nutrient percentages for every single item on a plate. Performing these calculations manually every day leads to frequent arithmetic errors, giving users a false impression of what they actually consumed.
3. Data Redundancy in Flat Files: When food intake is saved in simple spreadsheets, common food items (like Boiled Eggs or Cooked White Rice) get their full nutrient facts re-typed for every logged meal. If someone discovers an error in the nutrient values and updates one row, all historical rows still hold the old value, creating conflicting and untrustworthy records.
4. No Input Validation: Spreadsheets and text notes do not stop users from typing invalid or nonsensical numbers, such as negative weights, zero heights, or negative portion sizes.
5. Lack of Transaction Atomicity: In reality, a meal consists of several food items eaten together. If an application crashes halfway through saving a multi-item meal, a manual or poorly designed system leaves orphaned records where a meal is recorded without its food items.
6. Missing Audit History: Traditional spreadsheets do not track when a record was logged, updated, or removed, making it impossible to audit past changes or identify user activity history.
7. Disconnected Goal Comparison: Without relational queries linking logged meals to active nutritional targets, users cannot easily know whether they hit their daily caloric surplus or deficit.

### Proposed Solution
NutriGuide directly resolves these flaws by implementing a centralized, fully normalized relational database in MySQL. The relational design eliminates redundant data, enforces strict primary and foreign key rules, validates inputs at the storage engine level with CHECK constraints, automatically computes daily metrics using stored procedures and views, and safeguards meal logging through atomic database transactions.

---

## 3. System Architecture and Modules

### 3.1 Three-Tier Relational Topology
To keep the application modular, maintainable, and easy to debug, we separated NutriGuide into three distinct architectural tiers:

1. Presentation Layer (Java Swing Client): A desktop interface built using standard Java Swing components. It contains dedicated windows for user workflows (LoginFrame, RegisterFrame, DashboardFrame, MealLogFrame, NutritionGoalFrame, RecommendationFrame, ReportFrame, and ProfileFrame). To make sure the GUI never freezes while waiting for network or database responses, all HTTP requests run asynchronously in the background using SwingWorker threads, keeping the Event Dispatch Thread (EDT) responsive.
2. Application & Logic Layer (Java HttpServer): A lightweight REST server running locally on http://127.0.0.1:8081. It handles route dispatching, input validation, and business logic across service classes (AuthenticationService, NutritionService, MealService, FoodService, RecommendationService, ReportService, and UserService). It manages user sessions through token authentication and protects user passwords using salted PBKDF2-HMAC-SHA256 with 600,000 hash iterations before communicating with MySQL over JDBC.
3. Relational Database Layer (MySQL 8.x Engine): The core storage tier runs on MySQL 8.x using the InnoDB engine. It provides ACID transactional safety, enforces relational integrity (CASCADE / RESTRICT), executes stored functions and procedures, maintains aggregation views, and automatically invokes audit triggers.

![Figure 3.1: System Architecture](images/system_architecture.png)
*Figure 3.1: NutriGuide System Architecture & Multi-Tier Topology*

### 3.2 System Modules

1. User Profile & Authentication Module (user_profile):
   - Handles account registration, login verification, and demographic tracking.
   - Stores user height, weight, age, gender, and physical activity level.
   - Keeps passwords secure using salted PBKDF2-HMAC-SHA256 hashing so plaintext passwords are never saved.
2. Nutrition Goal Management Module (nutrition_goal):
   - Lets users configure target daily calories, protein, carbohydrate, and fat amounts.
   - Supports historical tracking by marking older goals inactive (is_active = FALSE) whenever a new goal is set.
   - Enforces the business rule that each user has exactly one active goal at any given time.
3. Food Catalog & Categorization Module (food_category, food):
   - Stores a standardized catalog of foods organized into primary groups (Grains, Proteins, Dairy, Fruits, Vegetables).
   - Keeps track of serving sizes in grams along with calories and macronutrient densities.
4. Meal Logging & Transaction Module (meal, meal_item):
   - Records breakfast, lunch, dinner, and snack entries for authenticated users.
   - Uses the associative junction table meal_item to link meals with multiple foods and their consumed portion multipliers.
   - Executes meal insertion inside atomic database transactions (START TRANSACTION / COMMIT).
5. Meal Planning Module (meal_plan, meal_plan_item):
   - Enables users to prepare upcoming diet schedules across date intervals (e.g., a 7-day cutting plan).
   - Links planned meal times with food catalog items.
6. Rule-Based Recommendation Engine (recommendation):
   - Compares active user goals with actual aggregate food intake.
   - Generates educational guidance alerting users to caloric surpluses, deficits, or low protein intake.
7. Audit & Activity Log Module (activity_log):
   - Maintains an append-only log of user actions and trigger-captured meal events.
8. Reporting & Analytics Module (views, procedures):
   - Utilizes views (daily_nutrition_summary) and stored procedures (get_daily_summary) to deliver aggregated intake reports without writing repetitive SQL queries in the frontend.

---

## 4. Functional and Non-Functional Requirements (Step 2)

### 4.1 Functional Requirements

| Requirement ID | Functional Description | Implemented Database Object |
|:---|:---|:---|
| **FR1** | Store and retrieve user profiles including height, weight, age, gender, and activity level. | Table user_profile |
| **FR2** | Securely hash user passwords using salted PBKDF2 with 600,000 iterations; prevent plaintext leakage. | Table user_profile.password_hash, PasswordUtil.java |
| **FR3** | Enforce global uniqueness of user email addresses to prevent duplicate registrations. | Constraint UNIQUE KEY uq_user_email on user_profile |
| **FR4** | Maintain food categories to systematically classify nutritional catalog items. | Table food_category |
| **FR5** | Store verified food items with per-serving caloric, protein, carbohydrate, and fat densities. | Table food (FK category_id) |
| **FR6** | Enforce uniqueness of food names to avoid duplicate food item creation. | Constraint UNIQUE KEY uq_food_name on food |
| **FR7** | Prevent physical deletion of food items referenced in past meal records to preserve historical integrity. | Referential Action ON DELETE RESTRICT on meal_item.food_id |
| **FR8** | Allow users to establish macronutrient and caloric target goals with historical archiving. | Table nutrition_goal |
| **FR9** | Enforce that only one nutrition goal is active (is_active = TRUE) per user account. | Service Layer Transaction & Table nutrition_goal |
| **FR10** | Record meal events linked to an authenticated user with meal type and date. | Table meal (FK user_id) |
| **FR11** | Atomically record multi-item meal lines linked to a parent meal and food catalog item. | Table meal_item (Composite/Surrogate PK, FKs) |
| **FR12** | Automatically log meal insertion events into the audit log without application intervention. | Trigger trg_after_meal_insert on meal |
| **FR13** | Reject invalid or non-positive food serving quantities at the database engine level. | Trigger trg_validate_meal_quantity (SIGNAL '45000') |
| **FR14** | Automatically compute user Body Mass Index (BMI) dynamically via a stored database function. | Stored Function calculate_bmi(weight_kg, height_cm) |
| **FR15** | Provide instantaneous multi-table aggregated daily nutritional intake totals. | Relational View daily_nutrition_summary |
| **FR16** | Return consolidated daily macro totals via stored procedure with output parameters. | Stored Procedure get_daily_summary(...) |
| **FR17** | Store prospective multi-day diet plans and their component planned meal items. | Tables meal_plan, meal_plan_item |
| **FR18** | Automatically clean up all dependent records (goals, meals, plans, logs) upon user profile deletion. | Referential Action ON DELETE CASCADE across foreign keys |

### 4.2 Non-Functional Requirements

- Data Integrity & Consistency: All relations enforce primary keys, explicit foreign key cascade and restrict policies, and declarative CHECK constraints to reject negative heights, weights, or serving sizes.
- ACID Transaction Safety: Meal logging and goal updates must execute atomically using database transactions; if any line item fails to save, all changes roll back to prevent half-saved meals.
- Numeric Precision: All nutritional numbers and portion multipliers use exact fixed-point DECIMAL types rather than FLOAT or DOUBLE to avoid cumulative IEEE 754 rounding discrepancies.
- Database Security: The database listens only on local loopback (127.0.0.1); all backend SQL queries use parameterized PreparedStatement calls to prevent SQL injection.
- Cohesion & Normalization: The database tables are normalized up to 3NF and BCNF, ensuring every table represents exactly one clear real-world concept.

---

## 5. Entities, Relationships and Attributes (Step 3)

### 5.1 Entities and Attributes

| Entity / Table Name | Primary Key | Foreign Keys | Key Attributes | Business Purpose |
|:---|:---|:---|:---|:---|
| **user_profile** | user_id | *None* | name, email, password_hash, age, gender, height_cm, weight_kg, activity_level, created_at | Stores user account information, demographic facts, and physical measurements. |
| **nutrition_goal** | goal_id | user_id | target_calories, target_protein, target_carbs, target_fats, is_active, created_at | Tracks daily calorie and macronutrient targets configured by users. |
| **food_category** | category_id | *None* | category_name | Organizes food catalog items into standard dietary groups. |
| **food** | food_id | category_id | food_name, serving_size, calories, protein, carbohydrates, fat | Contains nutritional facts per standard serving for catalog food items. |
| **meal** | meal_id | user_id | meal_type, meal_date, created_at | Represents a meal event consumed by a user on a given day. |
| **meal_item** | meal_item_id | meal_id, food_id | quantity | Associative table linking meals with multiple catalog foods and portion sizes. |
| **meal_plan** | meal_plan_id | user_id | plan_name, start_date, end_date, is_active, created_at | Prospective diet schedules created for specific date ranges. |
| **meal_plan_item** | meal_plan_item_id | meal_plan_id, food_id | planned_meal_type, quantity | Associative table linking planned diets with scheduled food catalog items. |
| **recommendation** | rec_id | user_id | rec_type, message, generated_at | Stores automated dietary feedback comparing intake with target goals. |
| **activity_log** | log_id | user_id | action_taken, log_time | Audit trail recording user account actions and meal insertions. |

### 5.2 Relationships and Cardinalities

| Relationship | Entity 1 | Entity 2 | Cardinality | Participation | Description |
|:---|:---|:---|:---|:---|:---|
| **User_Sets_Goals** | user_profile | nutrition_goal | **1 : N** | User: Partial; Goal: Total | One user can have multiple historical goals, but each goal belongs to one user. |
| **User_Logs_Meals** | user_profile | meal | **1 : N** | User: Partial; Meal: Total | A user logs multiple meals over time; each meal belongs to one specific user. |
| **Meal_Contains_Items** | meal | meal_item | **1 : N** | Meal: Total; Item: Total | A meal contains one or more food items; each item links to one parent meal. |
| **Food_Appears_In_Items**| food | meal_item | **1 : N** | Food: Partial; Item: Total | A food can be chosen in many meal items; each line item references one food. |
| **Category_Classifies_Food**| food_category | food | **1 : N** | Category: Partial; Food: Total | A category groups multiple foods; each food belongs to exactly one category. |
| **User_Schedules_Plans** | user_profile | meal_plan | **1 : N** | User: Partial; Plan: Total | A user can create several planned diet schedules over time. |
| **Plan_Includes_Items** | meal_plan | meal_plan_item | **1 : N** | Plan: Total; Item: Total | A meal plan includes multiple scheduled food entries. |
| **Food_In_Meal_Plans** | food | meal_plan_item | **1 : N** | Food: Partial; Item: Total | A food item can appear across multiple diet plans. |
| **User_Receives_Recs** | user_profile | recommendation | **1 : N** | User: Partial; Rec: Total | A user receives multiple dietary recommendations as they log meals. |
| **User_Generates_Audit**| user_profile | activity_log | **1 : N** | User: Partial; Log: Total | User operations and meal triggers produce timestamped audit entries. |

#### Resolving Many-to-Many Relationships
- Meal and Food (M:N): A meal contains multiple foods, and the same food can appear in hundreds of different meals across different days and users. We resolved this M:N relationship by creating the associative table meal_item, which stores meal_id, food_id, and the portion multiplier quantity.
- Meal_Plan and Food (M:N): A planned diet regimen involves multiple foods, and a food can be scheduled in different plans. We resolved this M:N relationship with the associative table meal_plan_item.

---

## 6. ER and EER Diagrams (Step 4)

### 6.1 Conceptual ER Diagram (Chen Notation)
The Entity-Relationship (ER) diagram below presents the conceptual design of NutriGuide drawn according to standard Chen notation:
- Rectangles: Represent the ten entity sets in our database.
- Diamonds: Represent relationships connecting the entities.
- Ellipses: Represent attributes, with primary keys clearly underlined.
- Numbers (1 and N): Indicate relationship cardinalities along connecting lines.

![Figure 6.1: ER Diagram](images/er_diagram.png)
*Figure 6.1: NutriGuide Conceptual ER Diagram (Chen Notation) with M:N Associative Resolution*

### 6.2 Enhanced Entity-Relationship (EER) Diagram
The Enhanced ER (EER) diagram shows the physical implementation schema in MySQL:
- Table cards showing exact column data types, primary keys ([PK]), and foreign keys ([FK]).
- Explicit foreign key referential actions (ON DELETE CASCADE and ON DELETE RESTRICT).
- Declarative CHECK constraints guarding physical boundaries (height > 0, weight > 0, quantity > 0).
- The automated trigger flow connecting meal to activity_log via trg_after_meal_insert.

![Figure 6.2: EER Diagram](images/eer_diagram.png)
*Figure 6.2: NutriGuide Enhanced Entity-Relationship (EER) Relational Schema with Constraints and Foreign Keys*
"""
