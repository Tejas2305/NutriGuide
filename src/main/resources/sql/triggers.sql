-- ============================================================================
-- NutriGuide Database Triggers: triggers.sql
-- Covers DBMS Exp 8 (Triggers: AFTER INSERT audit, BEFORE INSERT validation with SIGNAL)
-- ============================================================================

USE nutriguide;

DROP TRIGGER IF EXISTS trg_after_meal_insert;
DROP TRIGGER IF EXISTS trg_validate_meal_quantity;
DROP TRIGGER IF EXISTS trg_validate_meal_quantity_update;

DELIMITER $$

-- Trigger 1: trg_after_meal_insert
-- Audit Trigger: Automatically records an audit event in activity_log upon logging a meal
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
END$$

-- Trigger 2: trg_validate_meal_quantity
-- Validation Trigger: Demonstrates SIGNAL SQLSTATE '45000' to abort invalid INSERT operations
CREATE TRIGGER trg_validate_meal_quantity
BEFORE INSERT ON meal_item
FOR EACH ROW
BEGIN
    IF NEW.quantity <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Validation Trigger Error: Meal item quantity must be strictly greater than zero.';
    END IF;
END$$

-- Trigger 3: trg_validate_meal_quantity_update
-- Validation Trigger for UPDATE operations
CREATE TRIGGER trg_validate_meal_quantity_update
BEFORE UPDATE ON meal_item
FOR EACH ROW
BEGIN
    IF NEW.quantity <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Validation Trigger Error: Updated meal item quantity must be strictly greater than zero.';
    END IF;
END$$

DELIMITER ;
