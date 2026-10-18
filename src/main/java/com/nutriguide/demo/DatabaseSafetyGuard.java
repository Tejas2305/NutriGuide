package com.nutriguide.demo;

import com.nutriguide.db.DBConnectionFactory;

import java.sql.Connection;

/**
 * DatabaseSafetyGuard: Protects user databases from accidental writes or test mutation.
 *
 * Enforces:
 * - Any demo or integration test that writes, updates, or deletes records MUST verify
 *   that the active database catalog EXACTLY matches an explicitly configured disposable test database name.
 * - Substring matching and boolean flag bypasses are strictly prohibited.
 * - Fails closed if the database cannot be positively identified as the exact disposable test database.
 */
public final class DatabaseSafetyGuard {

    /**
     * JVM system property specifying the exact expected disposable test database name.
     * Example: -Dnutriguide.test.database.name=nutriguide_test
     */
    public static final String TEST_DB_PROPERTY = "nutriguide.test.database.name";

    private DatabaseSafetyGuard() {
        // Prevent instantiation
    }

    /**
     * Checks whether the active database configuration is confirmed to be an explicitly configured disposable test database.
     *
     * Strict verification rule:
     * 1. An exact expected test database name must be provided via the system property
     *    {@code nutriguide.test.database.name} (or {@code nutriguide.disposable.database.name}).
     * 2. The active JDBC catalog retrieved from {@link Connection#getCatalog()} must EXACTLY match that name.
     * Substring matching and flag-only bypasses are not permitted.
     *
     * @return true if positively verified as an exact match to the configured test database, false otherwise
     */
    public static boolean isDisposableDatabaseConfigured() {
        String expectedTestDb = System.getProperty(TEST_DB_PROPERTY);
        if (expectedTestDb == null || expectedTestDb.trim().isEmpty()) {
            expectedTestDb = System.getProperty("nutriguide.disposable.database.name");
        }

        // Fail closed if no explicit disposable test database name is configured
        if (expectedTestDb == null || expectedTestDb.trim().isEmpty()) {
            return false;
        }

        expectedTestDb = expectedTestDb.trim();

        try (Connection conn = DBConnectionFactory.getConnection()) {
            if (conn == null) {
                return false;
            }
            String actualCatalog = conn.getCatalog();
            if (actualCatalog == null || actualCatalog.trim().isEmpty()) {
                return false;
            }

            // Must match exactly (case-insensitive for cross-platform MySQL database name compatibility)
            return expectedTestDb.equalsIgnoreCase(actualCatalog.trim());

        } catch (Exception ignored) {
            // Fail closed on any connection or metadata retrieval exception
            return false;
        }
    }

    /**
     * Ensures that the environment is configured with an explicitly disposable test database.
     * Throws IllegalStateException and halts execution if the database is not disposable.
     *
     * @param demoOrTestName Name of the demo or test suite requesting database write permission
     */
    public static void requireDisposableDatabase(String demoOrTestName) {
        if (!isDisposableDatabaseConfigured()) {
            String expected = System.getProperty(TEST_DB_PROPERTY);
            if (expected == null || expected.trim().isEmpty()) {
                expected = System.getProperty("nutriguide.disposable.database.name");
            }

            String errorMsg = "\n" +
                    "================================================================================\n" +
                    "   SAFETY REFUSAL: DATABASE WRITE PROHIBITED\n" +
                    "================================================================================\n" +
                    " Demo/Suite '" + demoOrTestName + "' requires writing, updating, or deleting database data.\n" +
                    " To protect your configured development or personal database from accidental modification,\n" +
                    " this suite refuses to execute against non-disposable databases.\n\n" +
                    " Safety requirements (Both must be met):\n" +
                    "   1. Specify exact disposable database name via JVM argument:\n" +
                    "      -D" + TEST_DB_PROPERTY + "=<exact_disposable_db_name>\n" +
                    "   2. Configure database.properties so JDBC catalog exactly matches that name.\n" +
                    " Currently configured property: " + (expected != null ? expected : "<NOT CONFIGURED>") + "\n" +
                    " Substring matching and flag-only bypasses are prohibited.\n" +
                    "================================================================================\n";
            System.err.println(errorMsg);
            throw new IllegalStateException("Refusing to run database-writing demo '" + demoOrTestName +
                    "' on non-disposable database.");
        }
    }
}
