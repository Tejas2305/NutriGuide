package com.nutriguide.demo;

import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.util.PasswordUtil;
import com.nutriguide.util.ValidationUtil;
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

        System.out.println("\n================================================================================");
        System.out.println("   Results: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    // ========================================================================
    // Section A: Password Hashing
    // ========================================================================
    private static void testPasswordHashing() {
        System.out.println("\n--- [A] Password Hashing (SHA-256 via MessageDigest) ---");

        // A1: Deterministic SHA-256 hash must match MySQL SHA2('Password@123', 256)
        String hash = PasswordUtil.hashPassword("Password@123");
        assertCondition("A1: SHA-256 hash matches MySQL SHA2('Password@123', 256)",
                EXPECTED_HASH.equals(hash));
        System.out.println("    Computed hash length: " + hash.length() + " chars (expected 64)");

        // A2: Same input always produces same hash (deterministic)
        String hash2 = PasswordUtil.hashPassword("Password@123");
        assertCondition("A2: Repeated hashing produces identical output (deterministic)",
                hash.equals(hash2));

        // A3: Different input produces different hash
        String hashAdmin = PasswordUtil.hashPassword("Admin@123");
        assertCondition("A3: Different password produces different hash",
                !hash.equals(hashAdmin));

        // A4: Null/empty input throws IllegalArgumentException
        boolean nullRejected = false;
        try {
            PasswordUtil.hashPassword(null);
        } catch (IllegalArgumentException e) {
            nullRejected = true;
        }
        assertCondition("A4: Null password is rejected with IllegalArgumentException", nullRejected);

        boolean emptyRejected = false;
        try {
            PasswordUtil.hashPassword("");
        } catch (IllegalArgumentException e) {
            emptyRejected = true;
        }
        assertCondition("A5: Empty password is rejected with IllegalArgumentException", emptyRejected);
    }

    // ========================================================================
    // Section B: Password Verification
    // ========================================================================
    private static void testPasswordVerification() {
        System.out.println("\n--- [B] Password Verification ---");

        // B1: Correct password verifies successfully
        assertCondition("B1: Correct password verifies against stored hash",
                PasswordUtil.verifyPassword("Password@123", EXPECTED_HASH));

        // B2: Incorrect password does NOT verify
        assertCondition("B2: Incorrect password is rejected",
                !PasswordUtil.verifyPassword("WrongPassword", EXPECTED_HASH));

        // B3: Null password returns false (no crash)
        assertCondition("B3: Null password returns false (safe)",
                !PasswordUtil.verifyPassword(null, EXPECTED_HASH));

        // B4: Null stored hash returns false (no crash)
        assertCondition("B4: Null stored hash returns false (safe)",
                !PasswordUtil.verifyPassword("Password@123", null));
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
