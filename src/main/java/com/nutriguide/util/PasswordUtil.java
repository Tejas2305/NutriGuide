package com.nutriguide.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil: Secure password hashing and verification utility using SHA-256.
 *
 * <h3>Why passwords are never stored in plaintext:</h3>
 * <p>If the database is compromised, stored plaintext passwords would directly expose
 * every user's credentials. Instead, we store a <b>one-way cryptographic hash</b> —
 * a fixed-length fingerprint derived from the password. The hash cannot be reversed
 * back to the original password, so even a database breach does not reveal credentials.</p>
 *
 * <h3>How SHA-256 hashing works:</h3>
 * <ol>
 *   <li>The plaintext password bytes (UTF-8) are fed into the SHA-256 algorithm.</li>
 *   <li>SHA-256 produces a deterministic 256-bit (32-byte) digest.</li>
 *   <li>The digest is converted to a 64-character lowercase hexadecimal string for storage.</li>
 *   <li>The same input always produces the same hash — this enables verification.</li>
 * </ol>
 *
 * <h3>How password verification works:</h3>
 * <p>When a user logs in, we hash the entered password with the same algorithm and compare
 * the resulting hash against the stored hash. If they match, the password is correct.
 * We never need to decrypt or recover the original password.</p>
 *
 * <h3>Compatibility with MySQL:</h3>
 * <p>This implementation produces output identical to MySQL's {@code SHA2('input', 256)}
 * function, which is used in {@code sample_data.sql} to insert user password hashes.
 * Both produce lowercase hex strings from UTF-8 encoded input.</p>
 *
 * <p>Demonstrates Java Lab Exp 3 (static methods) and Exp 12 (user-defined packages).</p>
 */
public final class PasswordUtil {

    /** The hashing algorithm used, matching MySQL's SHA2(input, 256) */
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Private constructor prevents instantiation of this utility class.
     * All methods are static — no instance is ever needed.
     */
    private PasswordUtil() {
        // Utility class: not meant to be instantiated
    }

    /**
     * Hashes a plaintext password using SHA-256 and returns the result
     * as a 64-character lowercase hexadecimal string.
     *
     * <p>This output is directly compatible with MySQL's {@code SHA2('password', 256)}.</p>
     *
     * @param plainPassword The plaintext password to hash
     * @return 64-character lowercase hex string of the SHA-256 digest
     * @throws IllegalArgumentException if the input is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available in every Java SE implementation
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies a plaintext password against a stored SHA-256 hash.
     *
     * <p>Hashes the input password and compares the result to the stored hash
     * using a constant-time comparison to prevent timing attacks.</p>
     *
     * @param plainPassword The plaintext password entered by the user
     * @param storedHash    The stored SHA-256 hash from the database
     * @return {@code true} if the password matches the stored hash, {@code false} otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || plainPassword.isEmpty() || storedHash == null || storedHash.isEmpty()) {
            return false;
        }

        String computedHash = hashPassword(plainPassword);
        return constantTimeEquals(computedHash, storedHash);
    }

    /**
     * Converts a byte array to a lowercase hexadecimal string.
     *
     * <p>Each byte is represented by exactly two hex characters (zero-padded).</p>
     *
     * @param bytes The byte array to convert
     * @return Lowercase hex string representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            // 0xff mask ensures unsigned interpretation; zero-pad to 2 characters
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Constant-time string comparison to prevent timing side-channel attacks.
     *
     * <p>A naive {@code String.equals()} returns early on the first mismatched character,
     * which can leak information about how many characters matched. This method always
     * compares every character regardless of mismatches.</p>
     *
     * @param a First string
     * @param b Second string
     * @return {@code true} if both strings are identical, {@code false} otherwise
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
