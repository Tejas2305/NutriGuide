# NutriGuide – Personalized Nutrition and Diet Management System

A comprehensive, textbook-grade B.Tech Computer Science Java and DBMS practical project.

---

## 1. Project Title & Overview
**NutriGuide** is a personalized nutrition and diet management desktop application developed for academic evaluation. It combines a clean Java Swing graphical user interface, a modular layered backend (Services and DAOs), a rule-based recommendation engine, and a fully normalized MySQL relational database conforming to 3NF standards.

## 2. Project Objective
- To assist users in tracking daily caloric and macronutrient intake against personalized health goals.
- To demonstrate end-to-end multi-tier software architecture using pure Core Java (Java 8), JDBC, and MySQL.
- To serve as a practical viva demonstration fulfilling the university B.Tech curriculum for:
  - **Java Programming Lab Manual (Experiments 1–28)**
  - **Database Management Systems (DBMS) Lab Manual (Experiments 1–10)**

---

## 3. Key Features
1. **User Authentication & Session Management**:
   - Secure login and registration with SHA-256 hashed passwords.
   - Comprehensive input validation (name, email format, age range, height, weight, activity levels).
2. **Interactive Main Dashboard**:
   - Welcome banner and user profile overview.
   - Dynamic BMI calculation powered by MySQL stored function `calculate_bmi`.
   - Real-time display of today's actual caloric and macro intake (Protein, Carbs, Fat).
   - Active nutritional target goal comparison.
   - Live recommendation preview panel.
   - Direct navigation to all 6 submodules: Meal Log, Nutrition Goal, Recommendations, Reports, Profile, and Logout.
3. **Meal Logging & Nutrition Tracking**:
   - Catalog-driven food selection with instant serving and nutrient calculation.
   - Dynamic table for assembling meal items prior to submission.
   - ACID-compliant multi-item meal insertion with automatic rollback on failure.
   - Existing meal history browsing with cascade deletion.
4. **Nutrition Goal Management**:
   - Creation and activation of daily calorie and macro targets.
   - Automatic atomic deactivation of previous goals upon creating a new active goal.
   - Historical goal audit table.
5. **Rule-Based Recommendation Engine**:
   - Deterministic, extensible rule evaluation based on BMI category and calorie/protein goal adherence.
   - Generation and persistence of personalized dietary guidance.
6. **Reports & Nutrition History**:
   - Integration with MySQL stored procedure `get_daily_summary` and view `daily_nutrition_summary`.
   - Comprehensive daily intake breakdown and historical daily summaries.
   - Plaintext report generation.
7. **User Profile & Security Management**:
   - Viewing and editing profile attributes (Height, Weight, Activity Level).
   - Immediate recalculation of BMI across the application upon profile update.
   - Multi-field password change with current password verification and confirmation matching.
   - Strict multi-user data isolation (User 1 cannot view or modify User 2 data).

---

## 4. Technology Stack
- **Language**: Java 8 (Standard Edition, Zulu JDK 1.8)
- **GUI Framework**: Java Swing (`JFrame`, `JPanel`, `JLabel`, `JTextField`, `JPasswordField`, `JComboBox`, `JButton`, `JTable`, `DefaultTableModel`, `JOptionPane`)
- **Event Handling**: Standard AWT ActionListeners via Anonymous Inner Classes (0 lambdas, 0 streams, viva-friendly)
- **Database**: MySQL 8.x
- **Persistence**: Java Database Connectivity (JDBC) via `PreparedStatement`, `CallableStatement`, `ResultSet`, and explicit transaction management (`setAutoCommit(false)`, `commit()`, `rollback()`)
- **Build Tool**: Apache Maven 3.9.6

---

## 5. System Architecture
NutriGuide strictly enforces a 4-tier layered architecture with zero leakage of database logic into presentation layers:

```
+-----------------------------------------------------------+
|               Swing Presentation Layer (GUI)              |
|  LoginFrame, RegisterFrame, DashboardFrame, MealLogFrame, |
|  NutritionGoalFrame, RecommendationFrame, ReportFrame,    |
|  ProfileFrame, NutriGuideGUI                              |
+-----------------------------+-----------------------------+
                              | (Method calls, 0 JDBC, 0 SQL)
                              v
+-----------------------------------------------------------+
|                       Service Layer                       |
|  AuthenticationService, UserService, NutritionService,     |
|  MealService, RecommendationService, ReportService        |
+-----------------------------+-----------------------------+
                              | (Model entities & IDs)
                              v
+-----------------------------------------------------------+
|                   Data Access Layer (DAO)                 |
|  UserDAO, FoodDAO, MealDAO, NutritionGoalDAO,             |
|  RecommendationDAO, DBConnectionFactory                   |
+-----------------------------+-----------------------------+
                              | (PreparedStatement, SQL)
                              v
+-----------------------------------------------------------+
|                     MySQL 8.x Database                    |
|  10 Tables (3NF), 2 Views, 2 Functions, 1 Procedure,     |
|  2 Triggers, ACID Engine (InnoDB)                         |
+-----------------------------------------------------------+
```

---

## 6. Database Schema Overview
The database `nutriguide` is composed of 10 third-normal-form (3NF) relational tables:
1. `user_profile` — Primary user identity, demographics, physical statistics, activity level, and password hash.
2. `nutrition_goal` — Target calories and macronutrients; supports active flags.
3. `food_category` — High-level food classifications (Proteins, Carbs, Dairy, etc.).
4. `food` — Nutritional reference catalog (serving size, calories, protein, carbs, fat).
5. `meal` — Header records representing logged meals (Breakfast, Lunch, Dinner, Snack).
6. `meal_item` — M:N junction table mapping foods to meals with specific quantities.
7. `meal_plan` — Structured diet schedules over a date range.
8. `meal_plan_item` — Planned meal composition under a meal plan.
9. `recommendation` — Rule-generated advice history tied to user profiles.
10. `activity_log` — System event log automatically populated via database triggers.

---

## 7. Java Package Structure
```
src/main/java/com/nutriguide/
├── db/
│   └── DBConnectionFactory.java     # Centralized JDBC connection manager
├── exception/
│   ├── AuthenticationException.java # Unchecked auth errors
│   ├── DatabaseOperationException.java # Checked SQLException wrapper
│   ├── ResourceNotFoundException.java # Unchecked 404 entity errors
│   └── ValidationException.java     # Unchecked input validation errors
├── model/                           # 10 Encapsulated POJO Entities
│   ├── ActivityLog.java
│   ├── DailyNutritionSummary.java
│   ├── Food.java
│   ├── FoodCategory.java
│   ├── Meal.java
│   ├── MealItem.java
│   ├── MealPlan.java
│   ├── MealPlanItem.java
│   ├── NutritionGoal.java
│   ├── Recommendation.java
│   └── User.java
├── dao/                             # JDBC Data Access Objects (PreparedStatements)
│   ├── FoodCategoryDAO.java
│   ├── FoodDAO.java
│   ├── GenericDAO.java
│   ├── MealDAO.java
│   ├── MealPlanDAO.java
│   ├── NutritionGoalDAO.java
│   ├── RecommendationDAO.java
│   └── UserDAO.java
├── service/                         # Business Logic & Transactions
│   ├── AuthenticationService.java
│   ├── FoodService.java
│   ├── MealService.java
│   ├── NutritionService.java
│   ├── RecommendationEngine.java   # Interface
│   ├── RuleBasedRecommendationEngine.java
│   ├── RecommendationService.java
│   ├── ReportService.java
│   └── UserService.java
├── util/
│   ├── PasswordUtil.java            # SHA-256 MessageDigest hashing
│   └── ValidationUtil.java          # Static input validation rules
├── gui/                             # Pure Swing UI (0 SQL, 0 JDBC)
│   ├── DashboardFrame.java
│   ├── LoginFrame.java
│   ├── MealLogFrame.java
│   ├── NutritionGoalFrame.java
│   ├── NutriGuideGUI.java          # Main Application Launcher
│   ├── ProfileFrame.java
│   ├── RecommendationFrame.java
│   ├── RegisterFrame.java
│   └── ReportFrame.java
└── demo/                            # 14 Academic & Automated Verification Test Suites
    ├── BackendIntegrationDemo.java
    ├── DaoLayerDemo.java
    ├── FinalIntegrationDemo.java
    ├── GuiPhase4ADemo.java
    ├── GuiPhase4BDemo.java
    ├── GuiPhase4CDemo.java
    ├── GuiPhase4DDemo.java
    ├── GuiPhase4EDemo.java
    ├── GuiPhase4FDemo.java
    ├── GuiPhase4GDemo.java
    ├── ModelLayerDemo.java
    ├── RecommendationEngineDemo.java
    ├── ServiceLayerDemo.java
    ├── TransactionDemo.java
    └── UtilityLayerDemo.java
```

---

## 8. Database Setup Guide
To set up a fresh local MySQL instance:

### Step 1: Create Database
Open MySQL command-line client or MySQL Workbench:
```sql
CREATE DATABASE IF NOT EXISTS nutriguide;
USE nutriguide;
```

### Step 2: Execute SQL Scripts in Exact Order
Execute the scripts located in `src/main/resources/sql/`:
1. `tables.sql` — Creates 10 core tables with primary keys, foreign keys, CHECK constraints, and unique indexes.
2. `views.sql` — Creates views `daily_nutrition_summary` and `food_category_details`.
3. `functions.sql` — Creates stored functions `calculate_bmi` and `get_total_calories`.
4. `procedures.sql` — Creates stored procedure `get_daily_summary`.
5. `triggers.sql` — Creates audit trigger `trg_after_meal_insert` and validation trigger `trg_validate_meal_quantity`.
6. `sample_data.sql` — Seeds food categories, 15 reference foods, 2 baseline users (`user@example.com` and `jane@example.com`), initial goals, meals, items, and recommendations.

*(Optional demonstration scripts for DBMS experiments: `queries.sql`, `transactions_demo.sql`)*

---

## 9. Configuration (`database.properties`)
Verify or update `src/main/resources/database.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/nutriguide?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=DB_USERNAME
db.password=DB_PASSWORD
db.driver=com.mysql.cj.jdbc.Driver
```
*(Replace `DB_USERNAME` and `DB_PASSWORD` with your local credentials).*

---

## 10. How to Compile & Run

### Compile Project:
```powershell
.\apache-maven-3.9.6\bin\mvn.cmd clean compile
```

### Launch GUI Application:
```powershell
.\apache-maven-3.9.6\bin\mvn.cmd exec:java "-Dexec.mainClass=com.nutriguide.gui.NutriGuideGUI"
```

### Default Login Credentials:
- **Email**: `user@example.com`
- **Password**: `Password@123`

---

## 11. Verification Test Suites
Every phase has an automated, non-blocking test harness that can be run directly:

| Test Harness | Target Phase / Focus | Command |
|:---|:---|:---|
| `ModelLayerDemo` | Phase 3A: 10 Model POJOs | `java -cp "target\classes;..." com.nutriguide.demo.ModelLayerDemo` |
| `UtilityLayerDemo` | Phase 3B: Exceptions & Validation | `java -cp "target\classes;..." com.nutriguide.demo.UtilityLayerDemo` |
| `DaoLayerDemo` | Phase 3C: JDBC DAOs | `java -cp "target\classes;..." com.nutriguide.demo.DaoLayerDemo` |
| `ServiceLayerDemo` | Phase 3D: Service Layer & ACID | `java -cp "target\classes;..." com.nutriguide.demo.ServiceLayerDemo` |
| `RecommendationEngineDemo` | Phase 3E: Rule Engine | `java -cp "target\classes;..." com.nutriguide.demo.RecommendationEngineDemo` |
| `BackendIntegrationDemo` | Phase 3F: Backend Integration | `java -cp "target\classes;..." com.nutriguide.demo.BackendIntegrationDemo` |
| `GuiPhase4ADemo` | Phase 4A: Login & Register GUI | `java -cp "target\classes;..." com.nutriguide.demo.GuiPhase4ADemo` |
| `GuiPhase4BDemo` | Phase 4B: Dashboard GUI | `java -cp "target\classes;..." com.nutriguide.demo.GuiPhase4BDemo` |
| `GuiPhase4CDemo` | Phase 4C: Meal Log GUI | `java -cp "target\classes;..." com.nutriguide.demo.GuiPhase4CDemo` |
| `GuiPhase4DDemo` | Phase 4D: Nutrition Goal GUI | `java -cp "target\classes;..." com.nutriguide.demo.GuiPhase4DDemo` |
| `GuiPhase4EDemo` | Phase 4E: Recommendation GUI | `java -cp "target\classes;..." com.nutriguide.demo.GuiPhase4EDemo` |
| `GuiPhase4FDemo` | Phase 4F: Reports GUI | `java -cp "target\classes;..." com.nutriguide.demo.GuiPhase4FDemo` |
| `GuiPhase4GDemo` | Phase 4G: User Profile GUI | `java -cp "target\classes;..." com.nutriguide.demo.GuiPhase4GDemo` |
| `FinalIntegrationDemo` | Phase 5: Complete System Audit | `java -cp "target\classes;..." com.nutriguide.demo.FinalIntegrationDemo` |
| `TransactionDemo` | DBMS Exp 10: ACID & MVCC | `java -cp "target\classes;..." com.nutriguide.demo.TransactionDemo` |

---

## 12. DBMS Lab Concepts Demonstrated
- **Exp 1**: ER/EER Modeling, cardinalities, M:N resolution via `meal_item` junction table.
- **Exp 2**: DDL (CREATE, ALTER, DROP, constraints) and DML (INSERT, UPDATE, DELETE).
- **Exp 3**: SQL Built-in Functions (`ROUND`, `DATE_FORMAT`, `SUM`, `AVG`, `COUNT`, `MAX`, `MIN`).
- **Exp 4**: GROUP BY, HAVING, ORDER BY, and SQL Views (`daily_nutrition_summary`, `food_category_details`).
- **Exp 5**: SQL Joins (INNER, LEFT, RIGHT, correlated subqueries with EXISTS/NOT EXISTS).
- **Exp 6**: Stored Functions (`calculate_bmi`, `get_total_calories`).
- **Exp 7**: Stored Procedures with IN and OUT parameters (`get_daily_summary`).
- **Exp 8**: Database Triggers (`trg_after_meal_insert` for auditing, `trg_validate_meal_quantity` for SIGNAL validation).
- **Exp 9**: Functional Dependencies and 1NF, 2NF, 3NF Normalization.
- **Exp 10**: ACID Transactions, explicit COMMIT/ROLLBACK, and MySQL InnoDB REPEATABLE READ MVCC snapshot isolation (`TransactionDemo.java`).

---

## 13. Java Lab Concepts Demonstrated
- **Exp 1–2**: OOP Fundamentals, Variables, Encapsulation, Constructors (Default, Parameterized, Copy), Method Overriding (`toString`, `equals`, `hashCode`).
- **Exp 3**: Method Overloading & Static Utilities (`ValidationUtil`).
- **Exp 4**: Type Conversion and Wrapper Classes (`Integer.parseInt`, `Double.parseDouble`, `BigDecimal`).
- **Exp 5**: Collections Framework (`ArrayList`, `Vector`, `StringBuffer`).
- **Exp 6**: Anonymous Inner Classes for GUI event handling and thread execution.
- **Exp 7 & 11**: Interfaces and Polymorphism (`RecommendationEngine` interface implemented by `RuleBasedRecommendationEngine`).
- **Exp 8**: `super` keyword in custom exception hierarchies.
- **Exp 9**: `final` keyword on immutable utility classes and database constants.
- **Exp 12**: Package Architecture (`com.nutriguide.*`).
- **Exp 14–16**: Exception Handling, nested try-catch, resource management, and custom exception classes.
- **Exp 17–18**: Java Swing components and Layout Managers (`BorderLayout`, `FlowLayout`, `GridLayout`, `BoxLayout`).
- **Exp 19–24**: JDBC Database Integration, `PreparedStatement`, `CallableStatement`, `ResultSet` navigation, and strict separation of presentation from persistence.
- **Exp 25–26**: Multi-threading via Event Dispatch Thread (`SwingUtilities.invokeLater`) and concurrent connection isolation.

---

## 14. Security Design Principles
- **No Plaintext Passwords**: Passwords hashed using SHA-256 with standard Java `MessageDigest`.
- **SQL Injection Immune**: All database queries use parameterized `PreparedStatement` or `CallableStatement` with `?` place-holders; zero string concatenation for SQL statements.
- **Zero Leakage**: Password hashes are never returned to UI fields, and email identifiers remain read-only in profile management.
- **Architectural Isolation**: Swing frames contain 0 JDBC imports and 0 SQL query strings.
