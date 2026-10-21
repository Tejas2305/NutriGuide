package com.nutriguide.service;

import com.nutriguide.dao.UserDAO;
import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.User;
import com.nutriguide.util.PasswordUtil;
import com.nutriguide.util.ValidationUtil;

/**
 * AuthenticationService - handles user login and registration.
 *
 * This service:
 * - Validates user input (email format, password length, etc.)
 * - Hashes passwords before saving to the database
 * - Verifies passwords during login
 * - Delegates database work to UserDAO
 */
public class AuthenticationService {

    private UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Logs in a user with their email and password.
     *
     * Steps:
     * 1. Validate email and password format
     * 2. Look up the user by email in the database
     * 3. Compare the entered password with the stored hash
     * 4. Return the user if login is successful
     *
     * @param email         The user's email
     * @param plainPassword The password entered by the user (plain text)
     * @return The authenticated User object
     * @throws AuthenticationException    if credentials are wrong
     * @throws ValidationException        if email/password format is bad
     * @throws DatabaseOperationException if database access fails
     */
    public User login(String email, String plainPassword)
            throws AuthenticationException, ValidationException, DatabaseOperationException {

        // Step 1: Validate inputs
        ValidationUtil.requireValidEmail(email);
        if (!ValidationUtil.isNotBlank(plainPassword)) {
            throw new ValidationException("password", "Password cannot be blank.");
        }

        // Step 2: Find the user by email
        User user = userDAO.getUserByEmail(email.trim());
        if (user == null) {
            // Use a generic message so hackers can't tell if email exists or not
            throw new AuthenticationException("Invalid email or password.");
        }

        // Step 3: Verify the password
        boolean passwordMatches = PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash());
        if (!passwordMatches) {
            throw new AuthenticationException("Invalid email or password.");
        }

        // Step 4: Return the authenticated user
        return user;
    }

    /**
     * Registers a new user.
     *
     * Steps:
     * 1. Validate all fields
     * 2. Check if email is already registered
     * 3. Hash the password (never store plain text passwords)
     * 4. Save the user to the database
     *
     * @param user          The user's profile details
     * @param plainPassword The plain text password to hash and save
     * @return The registered User with their new user_id
     * @throws ValidationException        if any input is invalid
     * @throws DatabaseOperationException if database insert fails
     */
    public User registerUser(User user, String plainPassword)
            throws ValidationException, DatabaseOperationException {

        if (user == null) {
            throw new ValidationException("user", "User details cannot be null.");
        }

        // Step 1: Validate all profile fields
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
            throw new ValidationException("password", "Password must be at least 6 characters.");
        }

        // Step 2: Check if email is already taken
        User existing = userDAO.getUserByEmail(user.getEmail().trim());
        if (existing != null) {
            throw new ValidationException("email", "Email is already registered: " + user.getEmail());
        }

        // Step 3: Hash the password (SHA-256)
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        user.setPasswordHash(hashedPassword);

        // Step 4: Save to database
        int generatedId = userDAO.addUser(user);
        user.setUserId(generatedId);

        return user;
    }
}
