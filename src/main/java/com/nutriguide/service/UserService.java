package com.nutriguide.service;

import com.nutriguide.dao.UserDAO;
import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.User;
import com.nutriguide.util.PasswordUtil;
import com.nutriguide.util.ValidationUtil;

import java.util.ArrayList;

/**
 * UserService - handles user profile operations like getting, updating, and deleting users.
 * For login/register, it delegates to AuthenticationService.
 */
public class UserService {

    private UserDAO userDAO;
    private AuthenticationService authenticationService;

    public UserService() {
        this.userDAO = new UserDAO();
        this.authenticationService = new AuthenticationService(userDAO);
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.authenticationService = new AuthenticationService(userDAO);
    }

    /**
     * Registers a new user. Delegates to AuthenticationService.
     */
    public User registerUser(User user, String plainPassword)
            throws ValidationException, DatabaseOperationException {
        return authenticationService.registerUser(user, plainPassword);
    }

    /**
     * Gets a user by their user_id.
     * Throws ResourceNotFoundException if the user doesn't exist.
     */
    public User getUserById(int userId) throws ResourceNotFoundException, DatabaseOperationException {
        if (userId <= 0) {
            throw new ValidationException("userId", "User ID must be positive.");
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User", userId);
        }
        return user;
    }

    /**
     * Gets a user by their email address.
     * Throws ResourceNotFoundException if the user doesn't exist.
     */
    public User getUserByEmail(String email) throws ResourceNotFoundException, DatabaseOperationException {
        ValidationUtil.requireValidEmail(email);

        User user = userDAO.getUserByEmail(email.trim());
        if (user == null) {
            throw new ResourceNotFoundException("User", email);
        }
        return user;
    }

    /**
     * Updates a user's profile information.
     * Validates all fields before updating.
     */
    public boolean updateProfile(User user)
            throws ValidationException, ResourceNotFoundException, DatabaseOperationException {

        if (user == null || user.getUserId() == null) {
            throw new ValidationException("user", "User and User ID must not be null.");
        }

        // Validate all profile fields
        ValidationUtil.requireNonBlank(user.getName(), "name");
        ValidationUtil.requireValidEmail(user.getEmail());
        ValidationUtil.requireValidAge(user.getAge());
        ValidationUtil.requireValidHeight(user.getHeightCm().doubleValue());
        ValidationUtil.requireValidWeight(user.getWeightKg().doubleValue());
        ValidationUtil.requireNonBlank(user.getGender(), "gender");
        ValidationUtil.requireNonBlank(user.getActivityLevel(), "activityLevel");

        // Make sure the user actually exists in the database
        User existing = userDAO.getUserById(user.getUserId());
        if (existing == null) {
            throw new ResourceNotFoundException("User", user.getUserId());
        }

        // If no password hash provided, keep the existing one
        if (user.getPasswordHash() == null) {
            user.setPasswordHash(existing.getPasswordHash());
        }

        return userDAO.updateUser(user);
    }

    /**
     * Changes a user's password.
     * Requires the current password for verification.
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword, String confirmPassword)
            throws AuthenticationException, ValidationException, ResourceNotFoundException, DatabaseOperationException {

        if (userId <= 0) {
            throw new ValidationException("userId", "User ID must be positive.");
        }
        if (!ValidationUtil.isNotBlank(currentPassword)) {
            throw new ValidationException("currentPassword", "Current password is required.");
        }
        if (!ValidationUtil.isNotBlank(newPassword)) {
            throw new ValidationException("newPassword", "New password is required.");
        }
        if (newPassword.length() < 6) {
            throw new ValidationException("newPassword", "New password must be at least 6 characters.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("confirmPassword", "New password and confirmation do not match.");
        }

        // Find the user
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User", userId);
        }

        // Verify the current password is correct
        boolean matches = PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash());
        if (!matches) {
            throw new AuthenticationException("Current password is incorrect.");
        }

        // Hash the new password and save
        String newHash = PasswordUtil.hashPassword(newPassword);
        user.setPasswordHash(newHash);

        return userDAO.updateUser(user);
    }

    /**
     * Deletes a user from the database.
     */
    public boolean deleteUser(int userId) throws ResourceNotFoundException, DatabaseOperationException {
        if (userId <= 0) {
            throw new ValidationException("userId", "User ID must be positive.");
        }

        User existing = userDAO.getUserById(userId);
        if (existing == null) {
            throw new ResourceNotFoundException("User", userId);
        }

        return userDAO.deleteUser(userId);
    }

    /**
     * Gets all registered users.
     */
    public ArrayList<User> getAllUsers() throws DatabaseOperationException {
        return userDAO.getAllUsers();
    }
}
