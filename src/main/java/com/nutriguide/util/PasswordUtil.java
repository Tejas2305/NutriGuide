package com.nutriguide.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil - Utility class to hash and verify passwords.
 * Uses SHA-256 hashing (a standard one-way hashing algorithm).
 * All methods are static so you don't need to create an object.
 */
public class PasswordUtil {

    // Private constructor: this class should not be instantiated
    private PasswordUtil() {
    }

    /**
     * Converts a plain text password into a SHA-256 hash string.
     * The hash is a 64-character hexadecimal string.
     *
     * @param plainPassword The plain text password entered by the user
     * @return SHA-256 hash of the password as a hex string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        try {
            // Create a SHA-256 MessageDigest object
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Convert the password string to bytes and compute the hash
            byte[] hashBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));

            // Convert the byte array into a hex string
            return bytesToHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available in Java, so this should never happen
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Checks if the entered plain password matches the stored hash.
     * We hash the entered password and compare with the stored hash.
     *
     * @param plainPassword The password the user typed in
     * @param storedHash    The hash stored in the database
     * @return true if the password is correct, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }

        // Hash the entered password and compare with stored hash
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equals(storedHash);
    }

    /**
     * Helper method: converts a byte array to a hexadecimal string.
     * For example: byte {0x1F, 0xAB} -> "1fab"
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0'); // pad with 0 if needed
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
