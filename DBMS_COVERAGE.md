# DBMS Lab Manual Coverage (Experiments 1–10)

This document maps every experiment from the DBMS Lab Manual AY 2026-27 to its implementation within the NutriGuide database (`nutriguide`), including the exact SQL files and viva demonstration steps.

| Exp # | Topic | SQL File(s) | Implementation Detail | Viva Demonstration |
| :--- | :--- | :--- | :--- | :--- |
| **1** | ER/EER Diagrams & Conversion to Relational Schema | `DATABASE_DESIGN.md` | 10 entities with 1:N relationships. M:N between meals and foods resolved via `meal_item` junction table. | Draw the ER diagram. Explain entity identification, relationship cardinalities, and how the junction table resolves M:N. |
| **2** | DDL & DML Commands (CREATE, ALTER, DROP, INSERT, UPDATE, DELETE) | `tables.sql`, `sample_data.sql` | CREATE TABLE with PK, FK, UNIQUE, NOT NULL, CHECK constraints. INSERT of 20+ food records, 2 users, sample meals. | Execute `tables.sql`. Show constraints preventing invalid data. Run INSERT/UPDATE/DELETE from `sample_data.sql`. |
| **3** | SQL Functions (String, Numeric, Date, Aggregate) | `queries.sql` | `UPPER()`, `LOWER()`, `ROUND()`, `DATE_FORMAT()`, `CURDATE()`, `SUM()`, `AVG()`, `COUNT()`, `MAX()`, `MIN()`. | Run queries from `queries.sql` showing string manipulation on food names, date arithmetic on meal dates, and aggregate functions on nutritional data. |
| **4** | GROUP BY / HAVING / ORDER BY / Views | `views.sql`, `queries.sql` | `daily_nutrition_summary` VIEW uses GROUP BY (user_id, meal_date) with SUM aggregates and COALESCE. Queries use HAVING and ORDER BY. | Query the view: `SELECT * FROM daily_nutrition_summary WHERE daily_calories > 500 ORDER BY meal_date DESC`. Show HAVING filtering. |
| **5** | SQL Joins (INNER, LEFT, RIGHT, EXISTS, NOT EXISTS) | `queries.sql` | INNER JOIN in `food_category_details` view. LEFT JOIN in `daily_nutrition_summary` to include meals without items. RIGHT JOIN demo. EXISTS/NOT EXISTS subqueries. | Execute each join type from `queries.sql`. Explain why LEFT JOIN is used in the summary view (to capture empty meals). |
| **6** | Stored Functions (PL/SQL-style subprograms) | `functions.sql` | `calculate_bmi(weight, height)` with IF/ELSEIF logic. `get_total_calories(user_id, date)` with SELECT INTO and RETURN. | Execute `SELECT calculate_bmi(70, 175);` and `SELECT get_total_calories(1, CURDATE());` directly in MySQL CLI. Explain DELIMITER, BEGIN...END, RETURNS, DETERMINISTIC. |
| **7** | Stored Procedures (IN, OUT, INOUT parameters) | `procedures.sql` | `get_daily_summary(IN user_id, IN date, OUT cal, OUT pro, OUT carbs, OUT fat)` uses SELECT INTO for OUT parameters. | Execute `CALL get_daily_summary(1, '2026-10-03', @c, @p, @cb, @f); SELECT @c, @p, @cb, @f;` Show IN/OUT parameter flow. |
| **8** | Triggers (BEFORE, AFTER, SIGNAL) | `triggers.sql` | `trg_after_meal_insert`: AFTER INSERT on meal → logs to activity_log. `trg_validate_meal_quantity`: BEFORE INSERT on meal_item → SIGNAL SQLSTATE '45000' if quantity ≤ 0. | Insert a meal and query `activity_log` to show automatic audit. Insert a meal_item with quantity = -1 and show the SIGNAL error. Explain that CHECK constraint is the declarative rule; the trigger demonstrates SIGNAL behavior. |
| **9** | Functional Dependencies & Normalization (1NF/2NF/3NF) | `DATABASE_DESIGN.md` | Full FD documentation for all 10 relations. 3NF proven by absence of stored derived fields (total calories computed via VIEW, not stored in meal). | Explain FDs for each table. Show that `meal` has no `total_calories` column. Show the VIEW that computes it. Explain why storing it would violate 3NF (transitive dependency through meal_item → food). |
| **10** | Transactions, Serializability & Concurrency | `transactions_demo.sql`, `TransactionDemo.java` | **Part A**: Successful COMMIT of meal + items. **Part B**: Deliberate ROLLBACK on FK violation. **Part C**: Two-connection REPEATABLE READ demo showing MVCC snapshot isolation. | Run `TransactionDemo.java`. Show Part A (data persists after commit). Show Part B (data vanishes after rollback). Show Part C (Connection A's repeated read is stable despite Connection B's committed insert). Explain MySQL's REPEATABLE READ isolation level and MVCC. |

## Exp 10 Detailed Demonstration Plan

### Transaction A — Successful COMMIT
1. `connection.setAutoCommit(false)`
2. INSERT meal → capture generated `meal_id`
3. INSERT 2 meal_items using that `meal_id`
4. `connection.commit()`
5. Verify: SELECT shows the new meal and items

### Transaction B — Deliberate ROLLBACK
1. `connection.setAutoCommit(false)`
2. INSERT meal → capture generated `meal_id`
3. INSERT 1 valid meal_item (succeeds)
4. INSERT 1 invalid meal_item with `food_id = 9999` (FK violation → SQLException)
5. `connection.rollback()`
6. Verify: SELECT shows NO new meal or items (atomicity preserved)

### Transaction C — Concurrency / Serializability
- **Isolation level**: MySQL default `REPEATABLE READ`
- **Mechanism**: MVCC (Multi-Version Concurrency Control)
- Two separate JDBC connections (connA, connB) in two threads:
  1. connA starts transaction, reads SUM of daily calories → gets 1500
  2. connB starts transaction, inserts new meal+items, commits
  3. connA re-reads SUM of daily calories → **still gets 1500** (snapshot consistency)
  4. connA commits, starts new transaction, reads again → **now gets updated total**
- Console output proves the isolation behavior

### Viva talking points for Exp 10
- Explain ACID properties (Atomicity via rollback, Consistency via constraints, Isolation via REPEATABLE READ, Durability via commit)
- Explain that REPEATABLE READ prevents non-repeatable reads and phantom reads (in InnoDB)
- Explain MVCC: each transaction sees a snapshot taken at the time of its first read
- Optionally: `SET TRANSACTION ISOLATION LEVEL SERIALIZABLE` to show stricter locking

*Note: MySQL syntax is used throughout. PL/SQL terminology from the lab manual is mapped to MySQL equivalents (DELIMITER, BEGIN...END, IF/THEN/ELSE).*
