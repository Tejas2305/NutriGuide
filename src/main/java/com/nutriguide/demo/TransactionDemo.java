package com.nutriguide.demo;

import com.nutriguide.db.DBConnectionFactory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * TransactionDemo: Demonstrates ACID Transaction Management & Concurrency Control
 * for DBMS Experiment 10 and Java JDBC curriculum.
 *
 * Covers:
 * Part A: Successful Transaction (Atomic commit of meal + meal_items)
 * Part B: Failed Transaction (Rollback on Foreign Key violation, atomicity preserved)
 * Part C: Two-Connection Concurrency & MVCC Snapshot Isolation (REPEATABLE READ)
 */
public class TransactionDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   NutriGuide DBMS Exp 10: ACID Transactions & Concurrency Demonstration");
        System.out.println("================================================================================");

        try {
            // Part A
            runPartA_SuccessfulCommit();

            // Part B
            runPartB_RollbackOnFailure();

            // Part C
            runPartC_ConcurrencyAndMVCC();

        } catch (Exception e) {
            System.err.println("Fatal execution error in TransactionDemo: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("================================================================================");
        System.out.println("   All Exp 10 Transaction Demonstrations Completed Successfully");
        System.out.println("================================================================================");
    }

    /**
     * PART A: Atomic Insert with COMMIT
     */
    public static void runPartA_SuccessfulCommit() throws SQLException {
        System.out.println("\n--- [PART A] Successful Transaction with COMMIT ---");

        try (Connection conn = DBConnectionFactory.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            int newMealId = -1;
            String insertMealSql = "INSERT INTO meal (user_id, meal_type, meal_date) VALUES (?, ?, ?)";
            try (PreparedStatement psMeal = conn.prepareStatement(insertMealSql, Statement.RETURN_GENERATED_KEYS)) {
                psMeal.setInt(1, 1);
                psMeal.setString(2, "Post-Workout Snack");
                psMeal.setDate(3, Date.valueOf(LocalDate.now()));
                psMeal.executeUpdate();

                try (ResultSet rs = psMeal.getGeneratedKeys()) {
                    if (rs.next()) {
                        newMealId = rs.getInt(1);
                        System.out.println("1. Inserted Meal Header. Generated meal_id = " + newMealId);
                    }
                }
            }

            String insertItemSql = "INSERT INTO meal_item (meal_id, food_id, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                // Item 1: Greek Yogurt (food_id = 13, qty = 1.0)
                psItem.setInt(1, newMealId);
                psItem.setInt(2, 13);
                psItem.setDouble(3, 1.0);
                psItem.executeUpdate();
                System.out.println("2. Inserted Meal Item: Greek Yogurt (food_id=13, qty=1.0)");

                // Item 2: Apple (food_id = 8, qty = 1.0)
                psItem.setInt(1, newMealId);
                psItem.setInt(2, 8);
                psItem.setDouble(3, 1.0);
                psItem.executeUpdate();
                System.out.println("3. Inserted Meal Item: Apple (food_id=8, qty=1.0)");
            }

            conn.commit(); // Commit transaction atomically
            System.out.println("4. conn.commit() executed successfully!");

            // Verify row persistence
            try (PreparedStatement psVerify = conn.prepareStatement(
                    "SELECT COUNT(*) FROM meal_item WHERE meal_id = ?")) {
                psVerify.setInt(1, newMealId);
                try (ResultSet rs = psVerify.executeQuery()) {
                    if (rs.next()) {
                        System.out.println(">> Verification: " + rs.getInt(1) + " meal_items confirmed in database for meal_id " + newMealId);
                    }
                }
            }
        }
    }

    /**
     * PART B: Controlled Failure & ROLLBACK (Atomicity Guarantee)
     */
    public static void runPartB_RollbackOnFailure() throws SQLException {
        System.out.println("\n--- [PART B] Controlled Failure & ROLLBACK Demonstration ---");

        int failedMealId = -1;
        try (Connection conn = DBConnectionFactory.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            try {
                String insertMealSql = "INSERT INTO meal (user_id, meal_type, meal_date) VALUES (?, ?, ?)";
                try (PreparedStatement psMeal = conn.prepareStatement(insertMealSql, Statement.RETURN_GENERATED_KEYS)) {
                    psMeal.setInt(1, 1);
                    psMeal.setString(2, "Test Rollback Meal");
                    psMeal.setDate(3, Date.valueOf(LocalDate.now()));
                    psMeal.executeUpdate();

                    try (ResultSet rs = psMeal.getGeneratedKeys()) {
                        if (rs.next()) {
                            failedMealId = rs.getInt(1);
                            System.out.println("1. Inserted temporary Meal Header. Generated meal_id = " + failedMealId);
                        }
                    }
                }

                String insertItemSql = "INSERT INTO meal_item (meal_id, food_id, quantity) VALUES (?, ?, ?)";
                try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                    // Valid item
                    psItem.setInt(1, failedMealId);
                    psItem.setInt(2, 1); // Cooked Rice
                    psItem.setDouble(3, 1.0);
                    psItem.executeUpdate();
                    System.out.println("2. Inserted valid item (food_id=1)");

                    // Deliberately invalid item with non-existent food_id = 99999
                    System.out.println("3. Attempting to insert invalid item with food_id = 99999 (FK constraint violation)...");
                    psItem.setInt(1, failedMealId);
                    psItem.setInt(2, 99999);
                    psItem.setDouble(3, 1.0);
                    psItem.executeUpdate(); // Will throw SQLException
                }

                conn.commit();
            } catch (SQLException ex) {
                System.out.println(">> Caught expected SQLException: " + ex.getMessage());
                System.out.println("4. Executing conn.rollback() to preserve atomicity...");
                conn.rollback();
                System.out.println("5. Rollback completed successfully.");
            }
        }

        // Verify that the meal header was completely rolled back and not persisted
        if (failedMealId != -1) {
            try (Connection conn = DBConnectionFactory.getConnection();
                 PreparedStatement psCheck = conn.prepareStatement("SELECT COUNT(*) FROM meal WHERE meal_id = ?")) {
                psCheck.setInt(1, failedMealId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println(">> Atomicity Verification: Meal ID " + failedMealId + " exists in DB? " + (count > 0 ? "YES (FAIL)" : "NO (PASSED - Row was fully erased by rollback)"));
                    }
                }
            }
        }
    }

    /**
     * PART C: Two-Connection Concurrency & MVCC Snapshot Isolation (REPEATABLE READ)
     */
    public static void runPartC_ConcurrencyAndMVCC() throws SQLException {
        System.out.println("\n--- [PART C] Two-Connection Concurrency & MVCC Isolation Demo ---");
        System.out.println("Demonstrating MySQL InnoDB REPEATABLE READ snapshot isolation using 2 independent connections.");

        try (Connection connA = DBConnectionFactory.getConnection();
             Connection connB = DBConnectionFactory.getConnection()) {

            connA.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connA.setAutoCommit(false);

            connB.setAutoCommit(false);

            // Step 1: Connection A reads current calorie total
            double calsRead1 = queryTotalCalories(connA, 1, LocalDate.now());
            System.out.println("1. [Connection A - Tx 1] Initial daily calories read: " + calsRead1 + " kcal");

            // Step 2: Connection B logs a new meal and commits
            System.out.println("2. [Connection B - Tx 2] Inserting new snack with 2 Apples (104.0 kcal)...");
            int snackMealId = -1;
            String insertMealSql = "INSERT INTO meal (user_id, meal_type, meal_date) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connB.prepareStatement(insertMealSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, 1);
                ps.setString(2, "Concurrency Test Snack");
                ps.setDate(3, Date.valueOf(LocalDate.now()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) snackMealId = rs.getInt(1);
                }
            }

            String insertItemSql = "INSERT INTO meal_item (meal_id, food_id, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement ps = connB.prepareStatement(insertItemSql)) {
                ps.setInt(1, snackMealId);
                ps.setInt(2, 8); // Apple = 52 kcal
                ps.setDouble(3, 2.0); // 2 * 52 = 104 kcal
                ps.executeUpdate();
            }

            connB.commit();
            System.out.println("3. [Connection B - Tx 2] Committed changes to database.");

            // Step 3: Connection A re-reads in the same ongoing transaction
            double calsRead2 = queryTotalCalories(connA, 1, LocalDate.now());
            System.out.println("4. [Connection A - Tx 1] Repeat Read in active transaction: " + calsRead2 + " kcal");

            if (Double.compare(calsRead1, calsRead2) == 0) {
                System.out.println(">> MVCC PASSED: Connection A maintains its consistent snapshot despite Connection B's commit!");
            } else {
                System.out.println(">> MVCC Non-repeatable read observed.");
            }

            // Step 4: Connection A commits transaction and reads again in a fresh transaction
            connA.commit();
            connA.setAutoCommit(true);

            double calsRead3 = queryTotalCalories(connA, 1, LocalDate.now());
            System.out.println("5. [Connection A - Tx 2] After Commit & New Read: " + calsRead3 + " kcal");
            System.out.println(">> Expected increase: +104.00 kcal (Reflecting Connection B's committed data).");
        }
    }

    private static double queryTotalCalories(Connection conn, int userId, LocalDate date) throws SQLException {
        String sql = "SELECT COALESCE(SUM(f.calories * mi.quantity), 0.0) " +
                     "FROM meal m " +
                     "JOIN meal_item mi ON m.meal_id = mi.meal_id " +
                     "JOIN food f ON mi.food_id = f.food_id " +
                     "WHERE m.user_id = ? AND m.meal_date = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }
}
