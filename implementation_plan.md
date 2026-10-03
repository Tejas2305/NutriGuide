# NutriGuide – Project Implementation Plan & Progress Tracking

## Project Summary
- **Title**: NutriGuide – Personalized Nutrition and Diet Management System
- **Academic Purpose**: College B.Tech Practical Evaluation covering Java Lab Manual (Exp 1–28) & DBMS Lab Manual (Exp 1–10).
- **Core Stack**: Java Swing, Core Java/OOP/Services/Rule-based engine, JDBC (PreparedStatement, Transactions), MySQL 8.x, Maven.

---

## Phase Status Summary

| Phase | Description | Status | Verification Detail |
|:---|:---|:---|:---|
| **Phase 1** | Project Initialization & Infrastructure | ✅ **COMPLETED** | Maven configured, JDBC connected to `nutriguide`, `SELECT 1` returned 1. |
| **Phase 2** | Database Engineering & Lab Validation | ✅ **COMPLETED** | 10 3NF tables created, views active, stored functions & procedures tested, triggers verified, sample data loaded, Exp 1–10 fully executed, Java TransactionDemo (ACID + MVCC) passed. |
| **Phase 3A** | Java Model POJO Layer | ✅ **COMPLETED** | 10 POJOs created under `com.nutriguide.model` with default/parameterized/copy constructors, encapsulation, toString/equals/hashCode, compiled and verified via `ModelLayerDemo.java`. |
| **Phase 3B** | Custom Exceptions & Utilities | ✅ **COMPLETED** | 4 custom exceptions (`DatabaseOperationException`, `ValidationException`, `AuthenticationException`, `ResourceNotFoundException`), `PasswordUtil` (SHA-256 matching MySQL `SHA2`), `ValidationUtil` (viva-friendly static validations), 46/46 passed via `UtilityLayerDemo.java`. |
| **Phase 3C** | DAO Layer (JDBC) | ✅ **COMPLETED** | 10 DAO classes under `com.nutriguide.dao` using `PreparedStatement`, `ResultSet`, and `DBConnectionFactory`, verified with 25/25 live tests via `DaoLayerDemo.java`. |
| **Phase 3D** | Service Layer & Transactions | ✅ **COMPLETED** | 7 Service classes (`AuthenticationService`, `UserService`, `NutritionService`, `MealService`, `MealPlanService`, `RecommendationService`, `ReportService`), ACID commit/rollback transaction (`addMealWithItems`), DB function & procedure integration (`calculate_bmi`, `get_daily_summary`), 27/27 tests passed via `ServiceLayerDemo.java`. |
| **Phase 3E** | Rule-Based Recommendation Engine | ✅ **COMPLETED** | `RecommendationEngine` interface, `RuleBasedRecommendationEngine` implementation, deterministic if/else rules for BMI, calories, and protein, polymorphism demonstrated, 17/17 tests passed via `RecommendationEngineDemo.java`. |
| **Phase 3F** | Backend Integration Testing | ✅ **COMPLETED** | End-to-end integration verified: Auth, registration, BMI function, procedure vs view consistency, ACID transactions, 50/50 tests passed via `BackendIntegrationDemo.java`, all 6 demo suites passing (175/175 tests total). |
| **Phase 4A** | Swing GUI: Login + Registration | ✅ **COMPLETED** | Standard textbook Swing (`LoginFrame`, `RegisterFrame`, `NutriGuideGUI`), anonymous inner class ActionListeners (0 lambdas), exclusive delegation to `AuthenticationService`, verified via `GuiPhase4ADemo.java` (43/43 passed). |
| **Phase 4B** | Swing GUI: Main Dashboard & Navigation | ✅ **COMPLETED** | Standard textbook Swing (`DashboardFrame`), header, BMI, nutrition summary, active goal, recommendation preview, navigation & logout, 27/27 tests passed via `GuiPhase4BDemo.java`. |
| **Phase 4C** | Swing GUI: Meal Logging & Food Management | ✅ **COMPLETED** | Standard textbook Swing (`MealLogFrame`, `FoodService`), food combo, temporary table, nutrient calculations, totals, transactional save (`addMealWithItems`), existing meals, deletion with FK cascade, 48/48 tests passed via `GuiPhase4CDemo.java`. |
| **Phase 4D** | Swing GUI: Nutrition Goal Management | ✅ **COMPLETED** | Standard textbook Swing (`NutritionGoalFrame`, `NutritionService`), active goal display, goal creation form, update goal, deactivate goal, goal history `JTable`, active goal replacement with ACID transaction, user isolation, 39/39 tests passed via `GuiPhase4DDemo.java`. |
| **Phase 4E** | Swing GUI: Rule-Based Recommendations Screen | ✅ **COMPLETED** | Standard textbook Swing (`RecommendationFrame`), profile summary, live recommendation generation, recommendation history `JTable`, non-blocking validation, 35/35 tests passed via `GuiPhase4EDemo.java`. |
| **Phase 4F** | Swing GUI: Reports & Nutrition History Screen | ✅ **COMPLETED** | Standard textbook Swing (`ReportFrame`), daily report via stored procedure `get_daily_summary`, nutrition history `JTable` via view `daily_nutrition_summary`, target goal comparison, formatted text report, user isolation, 47/47 tests passed via `GuiPhase4FDemo.java`. |
| **Phase 4G** | Swing GUI: User Profile Screen | ✅ **COMPLETED** | Standard textbook Swing (`ProfileFrame`, `UserService`), view/edit profile (Name, Age, Gender, Height, Weight, Activity), read-only Email, password change with SHA-256 and current password verification, BMI refresh on Dashboard, user isolation, 49/49 tests passed via `GuiPhase4GDemo.java`. |
| **Phase 5** | Final Integration, Audit & Submission Preparation | ✅ **COMPLETED** | Full system workflow verification, database audit (10 tables, 2 views, 2 functions, 1 procedure, 2 triggers), normalization audit (1NF–3NF), security audit, syntax audit (0 stream/lambda/::/Optional), documentation (`README.md`, `LAB_COVERAGE.md`, `DBMS_COVERAGE.md`), 46/46 audits passed via `FinalIntegrationDemo.java`. All 14 test suites pass (100% rate). Project is complete and ready for submission. |

---

## Phase 2 Action Plan (Current Phase)

1. **SQL Artifact Generation**:
   - `src/main/resources/sql/tables.sql` (10 tables, PKs, FKs, CHECKs, UNIQUEs, Indexes)
   - `src/main/resources/sql/views.sql` (`daily_nutrition_summary`, `food_category_details`)
   - `src/main/resources/sql/functions.sql` (`calculate_bmi`, `get_total_calories`)
   - `src/main/resources/sql/procedures.sql` (`get_daily_summary`)
   - `src/main/resources/sql/triggers.sql` (`trg_after_meal_insert`, `trg_validate_meal_quantity`)
   - `src/main/resources/sql/sample_data.sql` (Categories, ~15 foods, 2 users with password hashes, meals, items, plans, recs, logs)
   - `src/main/resources/sql/queries.sql` (Demonstration queries for Exp 3, 4, 5)
   - `src/main/resources/sql/transactions_demo.sql` (Exp 10 scripts)
   - `src/main/java/com/nutriguide/demo/TransactionDemo.java` (Java ACID commit/rollback/MVCC demo)

2. **Execution & Validation**:
   - Execute DDL tables, views, functions, procedures, triggers, and sample data against MySQL `nutriguide`.
   - Verify table counts, constraints, row counts.
   - Run Exp 3–8 demonstrations (queries, function calls, procedure calls, trigger actions).
   - Run Exp 10 Java Transaction demo (`mvn test-compile` / `exec:java`).
