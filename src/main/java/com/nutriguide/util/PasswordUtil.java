package com.nutriguide.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * PasswordUtil: Secure password hashing and verification utility.
 *
 * Provides:
 * 1. Adaptive, salted PBKDF2WithHmacSHA256 password hashing for modern security.
 * 2. Backwards-compatible verification for legacy unsalted SHA-256 hashes (matching sample_data.sql).
 * 3. Rehash detection to enable seamless, automatic migration upon successful user login.
 *
 * Demonstrates:
 * - Java Lab Exp 3 (static methods)
 * - Exp 12 (user-defined packages)
 * - Standard Java 8 Cryptography Architecture (JCA/JCE) without third-party dependencies.
 */
public final class PasswordUtil {

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String LEGACY_SHA256_ALGORITHM = "SHA-256";
    private static final String PBKDF2_PREFIX = "PBKDF2$";
    private static final int ITERATIONS = 600000; // OWASP recommendation for PBKDF2-HMAC-SHA256
    private static final int MIN_ITERATIONS = 1000;
    private static final int MAX_ITERATIONS = 2000000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int EXPECTED_HASH_HEX_LENGTH = 64; // 256 bits = 32 bytes = 64 hex chars

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordUtil() {
        // Utility class: not meant to be instantiated
    }

    public static int getTargetIterations() {
        return ITERATIONS;
    }

    public static int getMinIterations() {
        return MIN_ITERATIONS;
    }

    public static int getMaxIterations() {
        return MAX_ITERATIONS;
    }

    /**
     * Hashes a plaintext password using salted PBKDF2-HMAC-SHA256 with 600,000 iterations
     * following OWASP guidelines.
     * Returns format: PBKDF2$600000$<saltHex>$<hashHex>
     *
     * @param plainPassword Plaintext password to hash
     * @return Formatted salted hash string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }

        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        return PBKDF2_PREFIX + ITERATIONS + "$" + bytesToHex(salt) + "$" + bytesToHex(hash);
    }

    /**
     * Verifies a plaintext password against a stored hash string.
     * Supports both modern PBKDF2 salted hashes and legacy unsalted SHA-256 hashes.
     *
     * @param plainPassword Plaintext password entered by the user
     * @param storedHash Stored hash from database
     * @return true if password matches stored hash, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || plainPassword.isEmpty() || storedHash == null || storedHash.isEmpty()) {
            return false;
        }

        if (storedHash.startsWith(PBKDF2_PREFIX)) {
            return verifyPbkdf2(plainPassword, storedHash);
        }

        // Legacy fallback: 64-character lowercase hex unsalted SHA-256 (matches sample_data.sql SHA2)
        if (storedHash.length() == 64) {
            String legacyHash = hashLegacySha256(plainPassword);
            return constantTimeEquals(legacyHash, storedHash);
        }

        return false;
    }

    /**
     * Determines whether a stored hash should be upgraded to the modern PBKDF2 salted format
     * with the current recommended work factor (600,000 iterations).
     *
     * Upgrades are triggered for:
     * - Null or empty hash strings
     * - Legacy unsalted SHA-256 hashes (matching sample_data.sql)
     * - Older PBKDF2 hashes whose iteration count is lower than ITERATIONS (e.g. 10,000 iterations)
     * - Malformed hash strings that cannot be safely parsed
     *
     * @param storedHash The current password hash in the database
     * @return true if the hash should be upgraded upon successful authentication, false otherwise
     */
    public static boolean needsRehash(String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return true;
        }
        if (!storedHash.startsWith(PBKDF2_PREFIX)) {
            return true;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4) {
            return true;
        }
        try {
            int storedIterations = Integer.parseInt(parts[1]);
            return storedIterations < ITERATIONS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static boolean verifyPbkdf2(String plainPassword, String storedHash) {
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4) {
            return false;
        }

        if (!"PBKDF2".equals(parts[0])) {
            return false;
        }

        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS) {
            return false;
        }

        byte[] salt = hexToBytes(parts[2]);
        if (salt == null || salt.length != SALT_BYTES) {
            return false;
        }

        String expectedHashHex = parts[3];
        if (expectedHashHex == null || expectedHashHex.length() != EXPECTED_HASH_HEX_LENGTH) {
            return false;
        }

        byte[] computedHash = pbkdf2(plainPassword.toCharArray(), salt, iterations, KEY_LENGTH_BITS);
        String computedHashHex = bytesToHex(computedHash);

        return constantTimeEquals(computedHashHex, expectedHashHex);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("PBKDF2 algorithm not available in Java runtime", e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Legacy unsalted SHA-256 hasher for verifying pre-existing user passwords
     * and computing deterministic digests matching MySQL SHA2(password, 256).
     */
    public static String hashLegacySha256(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance(LEGACY_SHA256_ALGORITHM);
            byte[] hashBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return null;
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                return null;
            }
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
