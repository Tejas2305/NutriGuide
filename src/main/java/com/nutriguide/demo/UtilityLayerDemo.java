package com.nutriguide.demo;

import com.nutriguide.dao.UserDAO;
import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.gui.DashboardFrame;
import com.nutriguide.model.User;
import com.nutriguide.service.AuthenticationService;
import com.nutriguide.util.PasswordUtil;
import com.nutriguide.util.ValidationUtil;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * UtilityLayerDemo: Verifies the custom exception classes, PasswordUtil, and ValidationUtil
 * created in Phase 3B.
 *
 * Demonstrates:
 * - SHA-256 password hashing compatible with MySQL SHA2()
 * - Password verification (correct and incorrect)
 * - Static utility validation methods
 * - User-defined exception creation and handling (Java Lab Exp 14, 15, 16)
 */
public class UtilityLayerDemo {

    /** Expected SHA-256 hash of 'Password@123' — matches MySQL: SHA2('Password@123', 256) */
    private static final String EXPECTED_HASH =
            "ff7bd97b1a7789ddd2775122fd6817f3173672da9f802ceec57f284325bf589f";

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   NutriGuide Phase 3B: Utility Layer Verification");
        System.out.println("================================================================================");

        testPasswordHashing();
        testPasswordVerification();
        testValidationUtil();
        testExceptions();
        testPasswordUpgradeAndDashboardStatus();

        System.out.println("\n================================================================================");
        System.out.println("   Results: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    // ========================================================================
    // ========================================================================
    // Section A: Password Hashing
    // ========================================================================
    private static void testPasswordHashing() {
        System.out.println("\n--- [A] Password Hashing (Salted PBKDF2 & Legacy SHA-256) ---");

        // A1: Modern PBKDF2 salted hash contains algorithm prefix and 600,000 iterations (OWASP recommendation)
        String hash = PasswordUtil.hashPassword("Password@123");
        assertCondition("A1: Modern salted hash uses PBKDF2 with 600,000 iterations",
                hash != null && hash.startsWith("PBKDF2$600000$"));

        // A2: Format parsing validation: PBKDF2$<iterations>$<saltHex>$<hashHex>
        String[] parts = hash != null ? hash.split("\\$") : new String[0];
        boolean formatValid = parts.length == 4 &&
                "PBKDF2".equals(parts[0]) &&
                "600000".equals(parts[1]) &&
                parts[2].length() == 32 && // 16 bytes = 32 hex chars
                parts[3].length() == 64;   // 32 bytes = 64 hex chars
        assertCondition("A2: Format parsing matches PBKDF2$<iterations>$<saltHex>$<hashHex> with 16-byte salt and 32-byte hash",
                formatValid);

        // A3: Cryptographic salt ensures repeated hashing produces distinct hashes
        String hash2 = PasswordUtil.hashPassword("Password@123");
        assertCondition("A3: Repeated hashing produces unique salted digests (rainbow table defense)",
                !hash.equals(hash2));

        // A4: Different input produces different hash
        String hashAdmin = PasswordUtil.hashPassword("Admin@123");
        assertCondition("A4: Different password produces different hash",
                !hash.equals(hashAdmin));

        // A5: Null/empty input throws IllegalArgumentException
        boolean nullRejected = false;
        try {
            PasswordUtil.hashPassword(null);
        } catch (IllegalArgumentException e) {
            nullRejected = true;
        }
        assertCondition("A5: Null password is rejected with IllegalArgumentException", nullRejected);

        boolean emptyRejected = false;
        try {
            PasswordUtil.hashPassword("");
        } catch (IllegalArgumentException e) {
            emptyRejected = true;
        }
        assertCondition("A6: Empty password is rejected with IllegalArgumentException", emptyRejected);

        // A7: Legacy deterministic SHA-256 hash matches MySQL SHA2('Password@123', 256)
        String legacyHash = PasswordUtil.hashLegacySha256("Password@123");
        assertCondition("A7: Legacy SHA-256 matches MySQL SHA2('Password@123', 256)",
                EXPECTED_HASH.equals(legacyHash));
    }

    // ========================================================================
    // Section B: Password Verification
    // ========================================================================
    private static void testPasswordVerification() {
        System.out.println("\n--- [B] Password Verification (PBKDF2 & Legacy Migration) ---");

        // B1: Correct password verifies successfully against legacy SHA-256
        assertCondition("B1: Correct password verifies against legacy stored hash",
                PasswordUtil.verifyPassword("Password@123", EXPECTED_HASH));

        // B2: Correct password verifies against modern 600,000-iteration PBKDF2 salted hash
        String modernHash = PasswordUtil.hashPassword("Password@123");
        assertCondition("B2: Correct password verifies against modern 600,000-iteration PBKDF2 hash",
                PasswordUtil.verifyPassword("Password@123", modernHash));

        // B3: Backward compatibility: Existing 10,000-iteration PBKDF2 hash verifies correctly (no lockout)
        String existing10kHash = "PBKDF2$10000$00112233445566778899aabbccddeeff$86a93946654a6c68324dbd5e3af32055eefe2bd57efa540c2d27508e2a221cbc";
        assertCondition("B3: Existing 10,000-iteration PBKDF2 hash verifies successfully (preserves existing accounts)",
                PasswordUtil.verifyPassword("Password@123", existing10kHash));

        // B4: Incorrect password does NOT verify against legacy, 10k, or modern hashes
        assertCondition("B4: Incorrect password is rejected across legacy, 10k PBKDF2, and 600k PBKDF2 hashes",
                !PasswordUtil.verifyPassword("WrongPassword", EXPECTED_HASH) &&
                !PasswordUtil.verifyPassword("WrongPassword", existing10kHash) &&
                !PasswordUtil.verifyPassword("WrongPassword", modernHash));

        // B5: Null/empty inputs return false safely without throwing exceptions
        assertCondition("B5: Null password returns false (safe)",
                !PasswordUtil.verifyPassword(null, EXPECTED_HASH));
        assertCondition("B6: Null stored hash returns false (safe)",
                !PasswordUtil.verifyPassword("Password@123", null));
        assertCondition("B7: Empty password returns false (safe)",
                !PasswordUtil.verifyPassword("", modernHash));
        assertCondition("B8: Empty stored hash returns false (safe)",
                !PasswordUtil.verifyPassword("Password@123", ""));

        // B9: Malformed hash parsing safety checks
        assertCondition("B9: Malformed hash (insufficient parts) rejected safely",
                !PasswordUtil.verifyPassword("Password@123", "PBKDF2$600000$notenoughparts"));
        assertCondition("B10: Malformed hash (non-numeric iterations) rejected safely",
                !PasswordUtil.verifyPassword("Password@123", "PBKDF2$invalid$00112233445566778899aabbccddeeff$86a93946654a6c68324dbd5e3af32055eefe2bd57efa540c2d27508e2a221cbc"));
        assertCondition("B11: Malformed hash (iterations < MIN_ITERATIONS 1,000) rejected safely",
                !PasswordUtil.verifyPassword("Password@123", "PBKDF2$500$00112233445566778899aabbccddeeff$86a93946654a6c68324dbd5e3af32055eefe2bd57efa540c2d27508e2a221cbc"));
        assertCondition("B12: Malformed hash (iterations > MAX_ITERATIONS 2,000,000) rejected safely (DoS defense)",
                !PasswordUtil.verifyPassword("Password@123", "PBKDF2$5000000$00112233445566778899aabbccddeeff$86a93946654a6c68324dbd5e3af32055eefe2bd57efa540c2d27508e2a221cbc"));
        assertCondition("B13: Malformed hash (invalid salt hex) rejected safely",
                !PasswordUtil.verifyPassword("Password@123", "PBKDF2$600000$invalid_salt_hex_zzzzzzzzzzzzzzz$86a93946654a6c68324dbd5e3af32055eefe2bd57efa540c2d27508e2a221cbc"));
        assertCondition("B14: Malformed hash (truncated hash hex) rejected safely",
                !PasswordUtil.verifyPassword("Password@123", "PBKDF2$600000$00112233445566778899aabbccddeeff$tooshort"));
        assertCondition("B15: Unrecognized hash format string rejected safely",
                !PasswordUtil.verifyPassword("Password@123", "unrecognized_arbitrary_format_string"));

        // B16: Legacy migration detection via needsRehash()
        assertCondition("B16: needsRehash() returns true for legacy unsalted SHA-256",
                PasswordUtil.needsRehash(EXPECTED_HASH));
        assertCondition("B17: needsRehash() returns true for null, empty, or malformed hashes",
                PasswordUtil.needsRehash(null) && PasswordUtil.needsRehash("") && PasswordUtil.needsRehash("invalid$hash"));

        // B18: Work-factor upgrade detection via needsRehash()
        assertCondition("B18: needsRehash() returns true for lower-iteration PBKDF2 (10,000 iterations)",
                PasswordUtil.needsRehash(existing10kHash));
        assertCondition("B19: needsRehash() returns true for 100,000-iteration PBKDF2 hash",
                PasswordUtil.needsRehash("PBKDF2$100000$00112233445566778899aabbccddeeff$86a93946654a6c68324dbd5e3af32055eefe2bd57efa540c2d27508e2a221cbc"));
        assertCondition("B20: needsRehash() returns false for target 600,000-iteration PBKDF2 hash",
                !PasswordUtil.needsRehash(modernHash) &&
                !PasswordUtil.needsRehash("PBKDF2$600000$00112233445566778899aabbccddeeff$86a93946654a6c68324dbd5e3af32055eefe2bd57efa540c2d27508e2a221cbc"));
    }

    // ========================================================================
    // Section C: Validation Utility
    // ========================================================================
    private static void testValidationUtil() {
        System.out.println("\n--- [C] ValidationUtil (Static Methods) ---");

        // C1: Email validation
        assertCondition("C1: Valid email 'user@example.com' accepted",
                ValidationUtil.isValidEmail("user@example.com"));
        assertCondition("C2: Invalid email 'not-an-email' rejected",
                !ValidationUtil.isValidEmail("not-an-email"));
        assertCondition("C3: Invalid email '@missing-local.com' rejected",
                !ValidationUtil.isValidEmail("@missing-local.com"));
        assertCondition("C4: Null email rejected",
                !ValidationUtil.isValidEmail(null));

        // C5: Non-blank validation
        assertCondition("C5: Non-blank 'hello' accepted",
                ValidationUtil.isNotBlank("hello"));
        assertCondition("C6: Blank '   ' rejected",
                !ValidationUtil.isNotBlank("   "));
        assertCondition("C7: Null rejected",
                !ValidationUtil.isNotBlank(null));

        // C8: Positive integer validation
        assertCondition("C8: Positive int 25 accepted",
                ValidationUtil.isPositiveInt(25));
        assertCondition("C9: Zero int 0 rejected",
                !ValidationUtil.isPositiveInt(0));
        assertCondition("C10: Negative int -5 rejected",
                !ValidationUtil.isPositiveInt(-5));

        // C11: Positive double validation
        assertCondition("C11: Positive double 3.14 accepted",
                ValidationUtil.isPositiveDouble(3.14));
        assertCondition("C12: Negative double -1.5 rejected",
                !ValidationUtil.isPositiveDouble(-1.5));

        // C13: Non-negative validation (for nutritional values)
        assertCondition("C13: Non-negative 0.0 accepted (valid for 0g carbs)",
                ValidationUtil.isNonNegativeDouble(0.0));
        assertCondition("C14: Non-negative -0.1 rejected",
                !ValidationUtil.isNonNegativeDouble(-0.1));

        // C15: Domain-specific age validation
        assertCondition("C15: Valid age 24 accepted",
                ValidationUtil.isValidAge(24));
        assertCondition("C16: Invalid age 0 rejected",
                !ValidationUtil.isValidAge(0));
        assertCondition("C17: Invalid age 200 rejected",
                !ValidationUtil.isValidAge(200));

        // C18: Domain-specific height validation
        assertCondition("C18: Valid height 175.0 cm accepted",
                ValidationUtil.isValidHeight(175.0));
        assertCondition("C19: Invalid height 10.0 cm rejected",
                !ValidationUtil.isValidHeight(10.0));

        // C20: Domain-specific weight validation
        assertCondition("C20: Valid weight 70.0 kg accepted",
                ValidationUtil.isValidWeight(70.0));
        assertCondition("C21: Invalid weight 0.0 kg rejected",
                !ValidationUtil.isValidWeight(0.0));

        // C22: Quantity validation
        assertCondition("C22: Valid quantity 1.5 accepted",
                ValidationUtil.isValidQuantity(1.5));
        assertCondition("C23: Invalid quantity 0.0 rejected",
                !ValidationUtil.isValidQuantity(0.0));
        assertCondition("C24: Invalid quantity -2.0 rejected",
                !ValidationUtil.isValidQuantity(-2.0));

        // C25: requireValidEmail throws ValidationException for bad email
        boolean emailValidationThrown = false;
        try {
            ValidationUtil.requireValidEmail("bad-email");
        } catch (ValidationException e) {
            emailValidationThrown = true;
            System.out.println("    Caught expected: " + e.getClass().getSimpleName()
                    + " [field=" + e.getFieldName() + "]: " + e.getMessage());
        }
        assertCondition("C25: requireValidEmail throws ValidationException for invalid email",
                emailValidationThrown);

        // C26: requireValidAge throws for invalid age
        boolean ageValidationThrown = false;
        try {
            ValidationUtil.requireValidAge(-5);
        } catch (ValidationException e) {
            ageValidationThrown = true;
            System.out.println("    Caught expected: " + e.getClass().getSimpleName()
                    + " [field=" + e.getFieldName() + "]: " + e.getMessage());
        }
        assertCondition("C26: requireValidAge throws ValidationException for negative age",
                ageValidationThrown);
    }

    // ========================================================================
    // Section D: Custom Exceptions
    // ========================================================================
    private static void testExceptions() {
        System.out.println("\n--- [D] Custom Exception Classes (Java Lab Exp 8, 14, 15, 16) ---");

        // D1: DatabaseOperationException with cause (demonstrating super(message, cause))
        try {
            // Simulate a DAO method wrapping a low-level SQLException
            SQLException sqlCause = new SQLException("Duplicate entry 'user@example.com' for key 'email'",
                    "23000", 1062);
            throw new DatabaseOperationException("Failed to insert user profile", sqlCause);
        } catch (DatabaseOperationException e) {
            assertCondition("D1: DatabaseOperationException caught with message",
                    e.getMessage().contains("Failed to insert user profile"));
            assertCondition("D2: Original SQLException preserved as cause",
                    e.getCause() instanceof SQLException);
            assertCondition("D3: getSqlErrorCode() returns 1062 (duplicate key)",
                    e.getSqlErrorCode() == 1062);
            assertCondition("D4: getSqlState() returns '23000' (integrity constraint)",
                    "23000".equals(e.getSqlState()));
            System.out.println("    Exception chain: " + e.getClass().getSimpleName()
                    + " => " + e.getCause().getClass().getSimpleName()
                    + " [code=" + e.getSqlErrorCode() + ", state=" + e.getSqlState() + "]");
        }

        // D5: AuthenticationException
        try {
            throw new AuthenticationException("Invalid email or password");
        } catch (AuthenticationException e) {
            assertCondition("D5: AuthenticationException caught",
                    e.getMessage().equals("Invalid email or password"));
            assertCondition("D6: AuthenticationException is unchecked (RuntimeException subclass)",
                    e instanceof RuntimeException);
            System.out.println("    Caught: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        // D7: ValidationException with field name
        try {
            throw new ValidationException("quantity", "Quantity must be greater than 0. Got: -2.0");
        } catch (ValidationException e) {
            assertCondition("D7: ValidationException caught with field name 'quantity'",
                    "quantity".equals(e.getFieldName()));
            assertCondition("D8: ValidationException message preserved",
                    e.getMessage().contains("-2.0"));
            System.out.println("    Caught: " + e.getClass().getSimpleName()
                    + " [field=" + e.getFieldName() + "]: " + e.getMessage());
        }

        // D9: ResourceNotFoundException with type and ID
        try {
            throw new ResourceNotFoundException("User", 999);
        } catch (ResourceNotFoundException e) {
            assertCondition("D9: ResourceNotFoundException caught",
                    e.getMessage().contains("User not found with identifier: 999"));
            assertCondition("D10: getResourceType() returns 'User'",
                    "User".equals(e.getResourceType()));
            assertCondition("D11: getResourceId() returns 999",
                    Integer.valueOf(999).equals(e.getResourceId()));
            System.out.println("    Caught: " + e.getClass().getSimpleName()
                    + " [type=" + e.getResourceType() + ", id=" + e.getResourceId() + "]");
        }
    }

    // ========================================================================
    // Section E: Password Upgrade Reversion & Dashboard Partial Failure Status
    // ========================================================================
    private static void testPasswordUpgradeAndDashboardStatus() {
        System.out.println("\n--- [E] Password Upgrade Save Failure & Dashboard Partial Failure Status ---");

        // E1: Verify legacy SHA-256 hash needs upgrade
        final String legacyHash = EXPECTED_HASH; // SHA-256 of 'Password@123'
        final User userModel = new User();
        userModel.setUserId(42);
        userModel.setName("Test Student");
        userModel.setEmail("student@nutriguide.com");
        userModel.setPasswordHash(legacyHash);
        userModel.setAge(22);
        userModel.setGender("Female");
        userModel.setHeightCm(new BigDecimal("165.00"));
        userModel.setWeightKg(new BigDecimal("55.00"));
        userModel.setActivityLevel("Moderate");

        assertCondition("E1: Legacy SHA-256 hash flagged for rehash upgrade",
                PasswordUtil.needsRehash(legacyHash));

        // E2: AuthenticationService: updateUser throws DatabaseOperationException
        UserDAO throwingDao = new UserDAO() {
            @Override
            public User getUserByEmail(String email) {
                return userModel;
            }

            @Override
            public boolean updateUser(User u) throws DatabaseOperationException {
                throw new DatabaseOperationException("Simulated disk error or broken connection during password rehash save");
            }
        };

        AuthenticationService authThrowing = new AuthenticationService(throwingDao);
        User loggedIn1 = null;
        try {
            loggedIn1 = authThrowing.login("student@nutriguide.com", "Password@123");
        } catch (Exception ex) {
            assertCondition("E2: Unexpected exception thrown from login", false);
        }

        assertCondition("E2: Login succeeds even when password upgrade save fails (safe & responsive)",
                loggedIn1 != null);
        assertCondition("E3: In-memory hash reverts to original legacy hash when database update throws",
                loggedIn1 != null && legacyHash.equals(loggedIn1.getPasswordHash()));

        // E4: AuthenticationService: updateUser returns false (0 rows modified)
        UserDAO zeroRowDao = new UserDAO() {
            @Override
            public User getUserByEmail(String email) {
                return userModel;
            }

            @Override
            public boolean updateUser(User u) {
                return false;
            }
        };

        AuthenticationService authZeroRow = new AuthenticationService(zeroRowDao);
        User loggedIn2 = null;
        try {
            loggedIn2 = authZeroRow.login("student@nutriguide.com", "Password@123");
        } catch (Exception ex) {
            assertCondition("E4: Unexpected exception thrown from login", false);
        }

        assertCondition("E4: In-memory hash reverts to original legacy hash when updateUser returns false",
                loggedIn2 != null && legacyHash.equals(loggedIn2.getPasswordHash()));

        // E5: AuthenticationService: updateUser succeeds
        final boolean[] saveCalled = new boolean[]{false};
        UserDAO successfulDao = new UserDAO() {
            @Override
            public User getUserByEmail(String email) {
                return userModel;
            }

            @Override
            public boolean updateUser(User u) {
                saveCalled[0] = true;
                return true;
            }
        };

        AuthenticationService authSuccess = new AuthenticationService(successfulDao);
        User loggedIn3 = null;
        try {
            loggedIn3 = authSuccess.login("student@nutriguide.com", "Password@123");
        } catch (Exception ex) {
            assertCondition("E5: Unexpected exception thrown from login", false);
        }

        assertCondition("E5: Password hash successfully upgraded to 600,000-iteration PBKDF2 when save succeeds",
                saveCalled[0] && loggedIn3 != null && loggedIn3.getPasswordHash().startsWith("PBKDF2$600000$"));

        // E6-E10: DashboardFrame partial request failure indicators
        DashboardFrame frame = new DashboardFrame(userModel, null, null, null);
        try {
            // Simulate: Profile loaded, but BMI failed
            frame.setPartialFailureForTesting(true, false, false);
            assertCondition("E6: BMI request failure reflected in isBmiLoadFailed()",
                    frame.isBmiLoadFailed());
            assertCondition("E7: BMI label displays 'BMI: Request failed' instead of 'N/A'",
                    "BMI: Request failed".equals(frame.getBmiLabel().getText()));
            assertCondition("E8: Dashboard header displays clear notice for BMI request failure",
                    frame.getPartialStatusMessage() != null && frame.getPartialStatusMessage().contains("BMI calculation failed"));

            // Simulate: Profile loaded, but daily summary / goal request failed
            frame.setPartialFailureForTesting(false, true, false);
            assertCondition("E9: Calories label displays 'Calories: Request failed' instead of empty 0 kcal",
                    "Calories: Request failed".equals(frame.getCaloriesLabel().getText()));
            assertCondition("E10: Goal label displays server failure instead of 'No active goal set'",
                    "Failed to load daily goal from server.".equals(frame.getNoGoalLabel().getText()));

            // Simulate: Profile loaded, but all 3 sub-requests failed
            frame.setPartialFailureForTesting(true, true, true);
            assertCondition("E11: Consolidated notice reports all failed sections without popup cascades",
                    frame.getPartialStatusMessage().contains("BMI calculation failed") &&
                    frame.getPartialStatusMessage().contains("Daily nutrition summary and goal request failed") &&
                    frame.getPartialStatusMessage().contains("Recommendations request failed"));
        } finally {
            frame.dispose();
        }
    }

    // ========================================================================
    // Assertion Helper
    // ========================================================================
    private static void assertCondition(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.out.println("  [FAIL] " + testName);
            failed++;
        }
    }
}
