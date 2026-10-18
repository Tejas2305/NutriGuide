package com.nutriguide.service;

import com.nutriguide.dao.UserDAO;
import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.User;
import com.nutriguide.util.PasswordUtil;
import com.nutriguide.util.ValidationUtil;

/**
 * AuthenticationService: Handles user login and registration business logic.
 *
 * Demonstrates:
 * - Simple business logic and input validation (Java Lab Exp 1-2)
 * - Static utility usage (PasswordUtil, ValidationUtil)
 * - Custom exception throwing (AuthenticationException, ValidationException)
 * - Clean delegation to UserDAO without embedded SQL
 */
public class AuthenticationService {

    private final UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user with email and plain password.
     *
     * @param email User's email
     * @param plainPassword Plaintext password entered by user
     * @return Authenticated User object
     * @throws AuthenticationException if credentials are invalid or user not found
     * @throws ValidationException if inputs are malformed
     * @throws DatabaseOperationException if database access fails
     */
    public User login(String email, String plainPassword)
            throws AuthenticationException, ValidationException, DatabaseOperationException {

        // 1. Validate inputs
        ValidationUtil.requireValidEmail(email);
        if (!ValidationUtil.isNotBlank(plainPassword)) {
            throw new ValidationException("password", "Password cannot be blank.");
        }

        // 2. Fetch user by email
        User user = userDAO.getUserByEmail(email.trim());
        if (user == null) {
            // Use generic message to prevent username enumeration attacks
            throw new AuthenticationException("Invalid email or password.");
        }

        // 3. Verify password against stored hash (PBKDF2 or legacy SHA-256)
        boolean matches = PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash());
        if (!matches) {
            throw new AuthenticationException("Invalid email or password.");
        }

        // 4. Automatic migration: upgrade legacy unsalted SHA-256 or lower-iteration PBKDF2 to modern PBKDF2
        String currentHash = user.getPasswordHash();
        if (PasswordUtil.needsRehash(currentHash)) {
            String upgradedHash = PasswordUtil.hashPassword(plainPassword);
            user.setPasswordHash(upgradedHash);
            try {
                boolean saved = userDAO.updateUser(user);
                if (!saved) {
                    // Update affected 0 rows; restore original hash so in-memory object accurately reflects persistence
                    user.setPasswordHash(currentHash);
                }
            } catch (Exception ex) {
                // Database update failed; revert in-memory hash to avoid falsely claiming an upgrade
                user.setPasswordHash(currentHash);
                // Non-fatal for authentication: login succeeds safely with verified existing credentials
            }
        }

        // 5. Return authenticated user
        return user;
    }

    /**
     * Registers a new user with profile information and a plaintext password.
     *
     * @param user User profile details
     * @param plainPassword Plain password to be hashed
     * @return Registered User with generated ID
     * @throws ValidationException if input validation fails
     * @throws DatabaseOperationException if database insertion fails
     */
    public User registerUser(User user, String plainPassword)
            throws ValidationException, DatabaseOperationException {

        if (user == null) {
            throw new ValidationException("user", "User details cannot be null.");
        }

        // Validate fields
        ValidationUtil.requireNonBlank(user.getName(), "name");
        ValidationUtil.requireValidEmail(user.getEmail());
        ValidationUtil.requireValidAge(user.getAge());
        ValidationUtil.requireValidHeight(user.getHeightCm().doubleValue());
        ValidationUtil.requireValidWeight(user.getWeightKg().doubleValue());
        ValidationUtil.requireNonBlank(user.getGender(), "gender");
        ValidationUtil.requireNonBlank(user.getActivityLevel(), "activityLevel");

        if (!ValidationUtil.isNotBlank(plainPassword)) {
            throw new ValidationException("password", "Password cannot be blank.");
        }
        if (plainPassword.length() < 6) {
            throw new ValidationException("password", "Password must be at least 6 characters long.");
        }

        // Check for existing email
        User existing = userDAO.getUserByEmail(user.getEmail().trim());
        if (existing != null) {
            throw new ValidationException("email", "Email is already registered: " + user.getEmail());
        }

        // Hash password before saving to database
        String hash = PasswordUtil.hashPassword(plainPassword);
        user.setPasswordHash(hash);

        // Persist via DAO
        int generatedId = userDAO.addUser(user);
        user.setUserId(generatedId);

        return user;
    }
}
