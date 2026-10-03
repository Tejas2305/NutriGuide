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
 * UserService: Handles user profile retrieval, update, and management.
 */
public class UserService {

    private final UserDAO userDAO;
    private final AuthenticationService authenticationService;

    public UserService() {
        this.userDAO = new UserDAO();
        this.authenticationService = new AuthenticationService(userDAO);
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.authenticationService = new AuthenticationService(userDAO);
    }

    /**
     * Registers a new user through the authentication service.
     */
    public User registerUser(User user, String plainPassword)
            throws ValidationException, DatabaseOperationException {
        return authenticationService.registerUser(user, plainPassword);
    }

    /**
     * Retrieves a user by ID, throwing ResourceNotFoundException if absent.
     *
     * @param userId Primary key ID
     * @return User entity
     * @throws ResourceNotFoundException if user does not exist
     * @throws DatabaseOperationException if database access fails
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
     * Retrieves a user by email, throwing ResourceNotFoundException if absent.
     *
     * @param email User email
     * @return User entity
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
     * Updates an existing user's profile information.
     *
     * @param user User with updated attributes
     * @return true if updated successfully
     * @throws ValidationException if updated values are invalid
     * @throws ResourceNotFoundException if user does not exist
     * @throws DatabaseOperationException if database update fails
     */
    public boolean updateProfile(User user)
            throws ValidationException, ResourceNotFoundException, DatabaseOperationException {

        if (user == null || user.getUserId() == null) {
            throw new ValidationException("user", "User and User ID must not be null.");
        }

        // Validate updated profile attributes
        ValidationUtil.requireNonBlank(user.getName(), "name");
        ValidationUtil.requireValidEmail(user.getEmail());
        ValidationUtil.requireValidAge(user.getAge());
        ValidationUtil.requireValidHeight(user.getHeightCm().doubleValue());
        ValidationUtil.requireValidWeight(user.getWeightKg().doubleValue());
        ValidationUtil.requireNonBlank(user.getGender(), "gender");
        ValidationUtil.requireNonBlank(user.getActivityLevel(), "activityLevel");

        // Verify user exists first
        User existing = userDAO.getUserById(user.getUserId());
        if (existing == null) {
            throw new ResourceNotFoundException("User", user.getUserId());
        }

        if (user.getPasswordHash() == null) {
            user.setPasswordHash(existing.getPasswordHash());
        }

        return userDAO.updateUser(user);
    }

    /**
     * Changes a user's password after verifying the current password (Java Lab Exp 1-2 & Security).
     *
     * @param userId User ID
     * @param currentPassword Current plain password
     * @param newPassword New plain password
     * @param confirmPassword Confirmation of new password
     * @return true if updated successfully
     * @throws AuthenticationException if current password is incorrect
     * @throws ValidationException if password constraints are violated
     * @throws ResourceNotFoundException if user does not exist
     * @throws DatabaseOperationException if database update fails
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
            throw new ValidationException("newPassword", "New password must be at least 6 characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("confirmPassword", "New password and confirmation do not match.");
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User", userId);
        }

        boolean matches = PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash());
        if (!matches) {
            throw new AuthenticationException("Current password is incorrect.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        user.setPasswordHash(newHash);

        return userDAO.updateUser(user);
    }

    /**
     * Deletes a user by ID.
     *
     * @param userId User ID
     * @return true if deleted
     * @throws ResourceNotFoundException if user does not exist
     * @throws DatabaseOperationException if database deletion fails
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
     * Retrieves all registered users.
     */
    public ArrayList<User> getAllUsers() throws DatabaseOperationException {
        return userDAO.getAllUsers();
    }
}
