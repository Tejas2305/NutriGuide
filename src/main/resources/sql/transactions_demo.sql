-- ============================================================================
-- NutriGuide Transaction & Concurrency Demos: transactions_demo.sql
-- Covers DBMS Exp 10 (ACID Transactions, COMMIT, ROLLBACK, Concurrency & MVCC)
-- ============================================================================

USE nutriguide;

-- ============================================================================
-- PART A: SUCCESSFUL TRANSACTION (COMMIT)
-- Atomically inserting a new meal header and associated meal items
-- ============================================================================

START TRANSACTION;

-- Step 1: Insert meal header
INSERT INTO meal (user_id, meal_type, meal_date) 
VALUES (1, 'Dinner', CURDATE());

-- Step 2: Capture generated ID
SET @new_meal_id = LAST_INSERT_ID();

-- Step 3: Insert meal items associated with this meal
INSERT INTO meal_item (meal_id, food_id, quantity) 
VALUES (@new_meal_id, 1, 1.50); -- Cooked Rice

INSERT INTO meal_item (meal_id, food_id, quantity) 
VALUES (@new_meal_id, 5, 1.00); -- Yellow Dal

-- Step 4: Commit both operations atomically
COMMIT;

-- Verify persistence:
SELECT m.meal_id, m.user_id, m.meal_type, m.meal_date, mi.meal_item_id, mi.food_id, mi.quantity
FROM meal m
JOIN meal_item mi ON m.meal_id = mi.meal_id
WHERE m.meal_id = @new_meal_id;


-- ============================================================================
-- PART B: FAILED TRANSACTION WITH ROLLBACK (Atomicity Guarantee)
-- Simulates a partial failure (foreign key violation) and rolls back cleanly
-- ============================================================================

START TRANSACTION;

-- Step 1: Insert meal header
INSERT INTO meal (user_id, meal_type, meal_date) 
VALUES (1, 'Late Night Snack', CURDATE());

SET @failed_meal_id = LAST_INSERT_ID();

-- Step 2: Insert valid meal item
INSERT INTO meal_item (meal_id, food_id, quantity) 
VALUES (@failed_meal_id, 8, 1.00); -- Apple

-- Step 3: Attempt inserting invalid meal item referencing non-existent food_id = 99999
-- Note: In a script or Java code, this throws Error 1452 (Cannot add or update a child row: a foreign key constraint fails).
-- Upon catching error, the application issues ROLLBACK:
ROLLBACK;

-- Verify Atomicity:
-- The meal record @failed_meal_id must NOT exist in the database:
SELECT * FROM meal WHERE meal_id = @failed_meal_id;
-- Returns 0 rows, proving no partial data was committed!


-- ============================================================================
-- PART C: TWO-CONNECTION CONCURRENCY / MVCC (REPEATABLE READ) DEMO
-- Demonstrates snapshot isolation in InnoDB:
-- ============================================================================

/*
--- SESSION 1 (Connection A) ---
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
START TRANSACTION;

-- Read initial sum of calories for User 1 today:
SELECT COALESCE(SUM(f.calories * mi.quantity), 0) AS initial_cals
FROM meal m
JOIN meal_item mi ON m.meal_id = mi.meal_id
JOIN food f ON mi.food_id = f.food_id
WHERE m.user_id = 1 AND m.meal_date = CURDATE();
-- Note this value: e.g. 2150.00

--- SESSION 2 (Connection B) ---
START TRANSACTION;
INSERT INTO meal (user_id, meal_type, meal_date) VALUES (1, 'Midnight Snack', CURDATE());
SET @snack_id = LAST_INSERT_ID();
INSERT INTO meal_item (meal_id, food_id, quantity) VALUES (@snack_id, 8, 2.00); -- 2 apples (104 cal)
COMMIT; -- Committed in Session 2!

--- SESSION 1 (Connection A re-reads) ---
-- In REPEATABLE READ, MVCC provides consistent read snapshot:
SELECT COALESCE(SUM(f.calories * mi.quantity), 0) AS repeat_cals
FROM meal m
JOIN meal_item mi ON m.meal_id = mi.meal_id
JOIN food f ON mi.food_id = f.food_id
WHERE m.user_id = 1 AND m.meal_date = CURDATE();
-- Proves MVCC: repeat_cals is STILL identical to initial_cals (e.g. 2150.00),
-- completely isolated from Session 2's committed changes until Session 1 completes!

COMMIT; -- Session 1 ends transaction.

-- Subsequent read in Session 1 now sees the updated value:
SELECT COALESCE(SUM(f.calories * mi.quantity), 0) AS post_commit_cals
FROM meal m
JOIN meal_item mi ON m.meal_id = mi.meal_id
JOIN food f ON mi.food_id = f.food_id
WHERE m.user_id = 1 AND m.meal_date = CURDATE();
*/
