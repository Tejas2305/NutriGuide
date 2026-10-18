package com.nutriguide.gui;

import com.nutriguide.client.NutriGuideApiClient;
import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.User;
import com.nutriguide.service.NutritionService;
import com.nutriguide.service.UserService;
import com.nutriguide.util.ValidationUtil;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

/**
 * ProfileFrame: Java Swing GUI for user profile management and credential updates.
 *
 * Demonstrates:
 * - Decoupled client communicating with localhost REST API (with direct service fallback)
 * - SwingWorker for background profile and password updates
 * - Exp 6: Anonymous Inner Classes for ActionListeners
 * - Exp 17-18: Swing UI Components (JFrame, JPanel, JLabel, JTextField, JPasswordField, JComboBox, JButton, JOptionPane)
 * - Layered Architecture: Delegates strictly to UserService and NutritionService
 *   (Strictly ZERO java.sql imports and ZERO SQL strings in GUI)
 * - Security: No plaintext passwords stored, password hashes never displayed, current password verification
 */
public class ProfileFrame extends JFrame {

    private final User user;
    private final NutriGuideApiClient apiClient;
    private final UserService userService;
    private final NutritionService nutritionService;

    // Profile Information Fields
    private JTextField nameField;
    private JTextField emailField;
    private JTextField ageField;
    private JComboBox<String> genderComboBox;
    private JTextField heightField;
    private JTextField weightField;
    private JComboBox<String> activityComboBox;
    private JButton saveProfileButton;

    // Password Change Fields
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton changePasswordButton;

    // Navigation
    private JButton backButton;

    public ProfileFrame(User user) {
        this(user, NutriGuideApiClient.getInstance(), null, null);
    }

    public ProfileFrame(User user, NutriGuideApiClient apiClient) {
        this(user, apiClient, null, null);
    }

    /**
     * For test and demo harness execution only: allows direct service injection
     * without requiring a running localhost HTTP backend server.
     *
     * @param user Active user model
     * @param userService Directly injected UserService for offline/integration testing
     * @param nutritionService Directly injected NutritionService for offline/integration testing
     */
    public ProfileFrame(User user, UserService userService, NutritionService nutritionService) {
        this(user, null, userService, nutritionService);
    }

    /**
     * For test and demo harness execution only: allows explicit injection of both
     * client and service implementations.
     *
     * @param user Active user model
     * @param apiClient API client implementation or null if operating in test harness mode
     * @param userService Directly injected UserService or null for standard client mode
     * @param nutritionService Directly injected NutritionService or null for standard client mode
     */
    public ProfileFrame(User user, NutriGuideApiClient apiClient, UserService userService, NutritionService nutritionService) {
        this.user = user;
        this.apiClient = apiClient;
        this.userService = userService;
        this.nutritionService = nutritionService;

        initializeUI();
        loadProfileData();
    }

    private void initializeUI() {
        setTitle("NutriGuide - User Profile");
        UITheme.configureFrame(this, 1024, 750, 850, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 10));

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.CARD_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 20, 12, 20)
        ));

        JLabel titleLabel = new JLabel("User Profile Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        String subtitleText = (user != null) ? "Account ID: #" + user.getUserId() : "Guest Account";
        JLabel subtitleLabel = new JLabel(subtitleText);
        subtitleLabel.setFont(UITheme.FONT_REGULAR);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(subtitleLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content Panel (Scrollable container for Profile Form + Password Change)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UITheme.BG_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 20, 10, 20));

        // Section A: Personal Information Form
        JPanel profilePanel = new JPanel(new GridLayout(7, 2, 12, 8));
        profilePanel.setBackground(UITheme.CARD_BG);
        profilePanel.setBorder(UITheme.createTitledCardBorder("Personal Profile Information"));

        nameField = new JTextField();
        UITheme.styleTextField(nameField);

        emailField = new JTextField();
        UITheme.styleTextField(emailField);
        emailField.setEditable(false);
        emailField.setBackground(new Color(245, 247, 250));
        emailField.setToolTipText("Email address is your unique account identifier and cannot be modified.");

        ageField = new JTextField();
        UITheme.styleTextField(ageField);

        String[] genders = {"Male", "Female", "Other"};
        genderComboBox = new JComboBox<String>(genders);
        UITheme.styleComboBox(genderComboBox);

        heightField = new JTextField();
        UITheme.styleTextField(heightField);

        weightField = new JTextField();
        UITheme.styleTextField(weightField);

        String[] activityLevels = {
                "Sedentary",
                "Lightly Active",
                "Moderately Active",
                "Very Active"
        };
        activityComboBox = new JComboBox<String>(activityLevels);
        UITheme.styleComboBox(activityComboBox);

        JLabel lName = new JLabel("Full Name:"); lName.setFont(UITheme.FONT_BOLD); lName.setForeground(UITheme.TEXT_MAIN);
        profilePanel.add(lName);
        profilePanel.add(nameField);

        JLabel lEmail = new JLabel("Email Address (Login ID):"); lEmail.setFont(UITheme.FONT_BOLD); lEmail.setForeground(UITheme.TEXT_MAIN);
        profilePanel.add(lEmail);
        profilePanel.add(emailField);

        JLabel lAge = new JLabel("Age (years):"); lAge.setFont(UITheme.FONT_BOLD); lAge.setForeground(UITheme.TEXT_MAIN);
        profilePanel.add(lAge);
        profilePanel.add(ageField);

        JLabel lGen = new JLabel("Gender:"); lGen.setFont(UITheme.FONT_BOLD); lGen.setForeground(UITheme.TEXT_MAIN);
        profilePanel.add(lGen);
        profilePanel.add(genderComboBox);

        JLabel lHt = new JLabel("Height (cm):"); lHt.setFont(UITheme.FONT_BOLD); lHt.setForeground(UITheme.TEXT_MAIN);
        profilePanel.add(lHt);
        profilePanel.add(heightField);

        JLabel lWt = new JLabel("Weight (kg):"); lWt.setFont(UITheme.FONT_BOLD); lWt.setForeground(UITheme.TEXT_MAIN);
        profilePanel.add(lWt);
        profilePanel.add(weightField);

        JLabel lAct = new JLabel("Activity Level:"); lAct.setFont(UITheme.FONT_BOLD); lAct.setForeground(UITheme.TEXT_MAIN);
        profilePanel.add(lAct);
        profilePanel.add(activityComboBox);

        contentPanel.add(profilePanel);

        // Save Profile Button Panel
        JPanel saveProfileBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        saveProfileBtnPanel.setBackground(UITheme.BG_COLOR);
        saveProfileButton = new JButton("Save Profile");
        UITheme.stylePrimaryButton(saveProfileButton);
        saveProfileBtnPanel.add(saveProfileButton);
        contentPanel.add(saveProfileBtnPanel);

        // Section B: Password Change Panel
        JPanel passwordPanel = new JPanel(new GridLayout(3, 2, 12, 8));
        passwordPanel.setBackground(UITheme.CARD_BG);
        passwordPanel.setBorder(UITheme.createTitledCardBorder("Change Password"));

        currentPasswordField = new JPasswordField();
        UITheme.styleTextField(currentPasswordField);

        newPasswordField = new JPasswordField();
        UITheme.styleTextField(newPasswordField);

        confirmPasswordField = new JPasswordField();
        UITheme.styleTextField(confirmPasswordField);

        JLabel lCurr = new JLabel("Current Password:"); lCurr.setFont(UITheme.FONT_BOLD); lCurr.setForeground(UITheme.TEXT_MAIN);
        passwordPanel.add(lCurr);
        passwordPanel.add(currentPasswordField);

        JLabel lNew = new JLabel("New Password (min 6 chars):"); lNew.setFont(UITheme.FONT_BOLD); lNew.setForeground(UITheme.TEXT_MAIN);
        passwordPanel.add(lNew);
        passwordPanel.add(newPasswordField);

        JLabel lConf = new JLabel("Confirm New Password:"); lConf.setFont(UITheme.FONT_BOLD); lConf.setForeground(UITheme.TEXT_MAIN);
        passwordPanel.add(lConf);
        passwordPanel.add(confirmPasswordField);

        contentPanel.add(passwordPanel);

        // Change Password Button Panel
        JPanel changePasswordBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        changePasswordBtnPanel.setBackground(UITheme.BG_COLOR);
        changePasswordButton = new JButton("Change Password");
        UITheme.stylePrimaryButton(changePasswordButton);
        changePasswordBtnPanel.add(changePasswordButton);
        contentPanel.add(changePasswordBtnPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UITheme.BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Navigation Panel
        backButton = new JButton("Back to Dashboard");
        JPanel bottomPanel = UITheme.createSecondaryNavBar(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event Listeners (Anonymous Inner Classes - Java Lab Exp 6)
        saveProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProfile(true);
            }
        });

        changePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePassword(true);
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBackToDashboard();
            }
        });
    }

    /**
     * Loads the existing profile data into the GUI fields.
     */
    public void loadProfileData() {
        if (user == null) {
            return;
        }

        nameField.setText(user.getName() != null ? user.getName() : "");
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        ageField.setText(user.getAge() != null ? user.getAge().toString() : "");

        if (user.getGender() != null) {
            genderComboBox.setSelectedItem(user.getGender());
        }

        heightField.setText(user.getHeightCm() != null ? user.getHeightCm().toString() : "");
        weightField.setText(user.getWeightKg() != null ? user.getWeightKg().toString() : "");

        if (user.getActivityLevel() != null) {
            activityComboBox.setSelectedItem(user.getActivityLevel());
        }
    }

    /**
     * Validates profile input fields.
     * Non-blocking overload for test suites when showDialog is false.
     */
    public boolean validateProfileInputs(boolean showDialog) {
        String name = nameField.getText() != null ? nameField.getText().trim() : "";
        if (name.isEmpty()) {
            if (showDialog) {
                UITheme.showWarning(this, "Name cannot be empty.", "Validation Error");
            }
            throw new ValidationException("name", "Name cannot be empty.");
        }

        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
            ValidationUtil.requireValidAge(age);
        } catch (NumberFormatException ex) {
            if (showDialog) {
                UITheme.showWarning(this, "Age must be a valid integer.", "Validation Error");
            }
            throw new ValidationException("age", "Age must be a valid integer.");
        } catch (ValidationException ex) {
            if (showDialog) {
                UITheme.showWarning(this, ex.getMessage(), "Validation Error");
            }
            throw ex;
        }

        double height;
        try {
            height = Double.parseDouble(heightField.getText().trim());
            ValidationUtil.requireValidHeight(height);
        } catch (NumberFormatException ex) {
            if (showDialog) {
                UITheme.showWarning(this, "Height must be a valid number.", "Validation Error");
            }
            throw new ValidationException("height", "Height must be a valid number.");
        } catch (ValidationException ex) {
            if (showDialog) {
                UITheme.showWarning(this, ex.getMessage(), "Validation Error");
            }
            throw ex;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightField.getText().trim());
            ValidationUtil.requireValidWeight(weight);
        } catch (NumberFormatException ex) {
            if (showDialog) {
                UITheme.showWarning(this, "Weight must be a valid number.", "Validation Error");
            }
            throw new ValidationException("weight", "Weight must be a valid number.");
        } catch (ValidationException ex) {
            if (showDialog) {
                UITheme.showWarning(this, ex.getMessage(), "Validation Error");
            }
            throw ex;
        }

        String gender = (String) genderComboBox.getSelectedItem();
        if (gender == null || gender.trim().isEmpty()) {
            if (showDialog) {
                UITheme.showWarning(this, "Please select a valid gender.", "Validation Error");
            }
            throw new ValidationException("gender", "Please select a valid gender.");
        }

        String activity = (String) activityComboBox.getSelectedItem();
        if (activity == null || activity.trim().isEmpty()) {
            if (showDialog) {
                UITheme.showWarning(this, "Please select a valid activity level.", "Validation Error");
            }
            throw new ValidationException("activityLevel", "Please select a valid activity level.");
        }

        return true;
    }

    public boolean validateProfileInputs() {
        return validateProfileInputs(true);
    }

    /**
     * Saves updated profile attributes through API or UserService.
     */
    public boolean saveProfile(final boolean showDialog) {
        try {
            validateProfileInputs(showDialog);
        } catch (ValidationException ex) {
            return false;
        }

        final String name = nameField.getText().trim();
        final int age = Integer.parseInt(ageField.getText().trim());
        final String gender = (String) genderComboBox.getSelectedItem();
        final BigDecimal height = new BigDecimal(heightField.getText().trim());
        final BigDecimal weight = new BigDecimal(weightField.getText().trim());
        final String activity = (String) activityComboBox.getSelectedItem();

        if (apiClient != null) {
            saveProfileButton.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
                @Override
                protected User doInBackground() throws Exception {
                    User updatedUser = new User();
                    updatedUser.setUserId(user.getUserId());
                    updatedUser.setEmail(user.getEmail());
                    updatedUser.setName(name);
                    updatedUser.setAge(age);
                    updatedUser.setGender(gender);
                    updatedUser.setHeightCm(height);
                    updatedUser.setWeightKg(weight);
                    updatedUser.setActivityLevel(activity);
                    return apiClient.updateProfile(updatedUser);
                }

                @Override
                protected void done() {
                    saveProfileButton.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        User res = get();
                        if (res != null) {
                            user.setName(res.getName());
                            user.setAge(res.getAge());
                            user.setGender(res.getGender());
                            user.setHeightCm(res.getHeightCm());
                            user.setWeightKg(res.getWeightKg());
                            user.setActivityLevel(res.getActivityLevel());
                        }
                        if (showDialog) {
                            UITheme.showInfo(ProfileFrame.this, "Profile updated successfully!", "Success");
                        }
                    } catch (Exception ex) {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        if (showDialog) {
                            UITheme.showError(ProfileFrame.this, "Failed to update profile: " + msg, "Error");
                        }
                    }
                }
            };
            worker.execute();
            return true;
        }

        if (userService == null) {
            if (showDialog) {
                UITheme.showError(this,
                        "NutriGuide Localhost API Client is not connected, and direct database service fallback is disabled.",
                        "Connection Unavailable");
            }
            return false;
        }

        try {
            // Update in-memory user entity
            user.setName(name);
            user.setAge(age);
            user.setGender(gender);
            user.setHeightCm(height);
            user.setWeightKg(weight);
            user.setActivityLevel(activity);

            // Persist through UserService
            boolean updated = userService.updateProfile(user);

            if (updated && showDialog) {
                UITheme.showInfo(this, "Profile updated successfully!", "Success");
            }
            return updated;
        } catch (ResourceNotFoundException ex) {
            if (showDialog) {
                UITheme.showError(this, "User not found: " + ex.getMessage(), "Error");
            }
            return false;
        } catch (DatabaseOperationException ex) {
            if (showDialog) {
                UITheme.showError(this, "Database update error: " + ex.getMessage(), "Database Error");
            }
            return false;
        }
    }

    public boolean saveProfile() {
        return saveProfile(true);
    }

    /**
     * Validates password change input fields.
     */
    public boolean validatePasswordInputs(boolean showDialog) {
        String currentPass = new String(currentPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (currentPass.trim().isEmpty()) {
            if (showDialog) {
                UITheme.showWarning(this, "Current password is required.", "Validation Error");
            }
            throw new ValidationException("currentPassword", "Current password is required.");
        }

        if (newPass.trim().isEmpty()) {
            if (showDialog) {
                UITheme.showWarning(this, "New password is required.", "Validation Error");
            }
            throw new ValidationException("newPassword", "New password is required.");
        }

        if (newPass.length() < 6) {
            if (showDialog) {
                UITheme.showWarning(this, "New password must be at least 6 characters long.", "Validation Error");
            }
            throw new ValidationException("newPassword", "New password must be at least 6 characters long.");
        }

        if (!newPass.equals(confirmPass)) {
            if (showDialog) {
                UITheme.showWarning(this, "New password and confirmation do not match.", "Validation Error");
            }
            throw new ValidationException("confirmPassword", "New password and confirmation do not match.");
        }

        return true;
    }

    public boolean validatePasswordInputs() {
        return validatePasswordInputs(true);
    }

    /**
     * Changes user password after verifying current credentials.
     */
    public boolean changePassword(final boolean showDialog) {
        try {
            validatePasswordInputs(showDialog);
        } catch (ValidationException ex) {
            return false;
        }

        final String currentPass = new String(currentPasswordField.getPassword());
        final String newPass = new String(newPasswordField.getPassword());
        final String confirmPass = new String(confirmPasswordField.getPassword());

        if (apiClient != null) {
            changePasswordButton.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return apiClient.changePassword(currentPass, newPass, confirmPass);
                }

                @Override
                protected void done() {
                    changePasswordButton.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        get();
                        currentPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");
                        if (showDialog) {
                            UITheme.showInfo(ProfileFrame.this, "Password changed successfully!", "Success");
                        }
                    } catch (Exception ex) {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        if (showDialog) {
                            UITheme.showError(ProfileFrame.this, "Failed to change password: " + msg, "Error");
                        }
                    }
                }
            };
            worker.execute();
            return true;
        }

        if (userService == null) {
            if (showDialog) {
                UITheme.showError(this,
                        "NutriGuide Localhost API Client is not connected, and direct database service fallback is disabled.",
                        "Connection Unavailable");
            }
            return false;
        }

        try {
            boolean success = userService.changePassword(user.getUserId(), currentPass, newPass, confirmPass);

            if (success) {
                currentPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");

                if (showDialog) {
                    UITheme.showInfo(this, "Password changed successfully!", "Success");
                }
            }
            return success;
        } catch (AuthenticationException ex) {
            if (showDialog) {
                UITheme.showError(this, ex.getMessage(), "Authentication Error");
            }
            return false;
        } catch (ResourceNotFoundException ex) {
            if (showDialog) {
                UITheme.showError(this, "User not found: " + ex.getMessage(), "Error");
            }
            return false;
        } catch (DatabaseOperationException ex) {
            if (showDialog) {
                UITheme.showError(this, "Database update error: " + ex.getMessage(), "Database Error");
            }
            return false;
        }
    }

    public boolean changePassword() {
        return changePassword(true);
    }

    /**
     * Disposes this frame and returns to DashboardFrame.
     */
    public void handleBackToDashboard() {
        DashboardFrame dashboardFrame = apiClient != null ? new DashboardFrame(user, apiClient) : new DashboardFrame(user);
        UITheme.transition(this, dashboardFrame);
    }

    // ========================================================================
    // Programmatic Accessors for Testing & Verification
    // ========================================================================
    public User getUser() {
        return user;
    }

    public UserService getUserService() {
        return userService;
    }

    public NutritionService getNutritionService() {
        return nutritionService;
    }

    public JTextField getNameField() {
        return nameField;
    }

    public JTextField getEmailField() {
        return emailField;
    }

    public JTextField getAgeField() {
        return ageField;
    }

    public JComboBox<String> getGenderComboBox() {
        return genderComboBox;
    }

    public JTextField getHeightField() {
        return heightField;
    }

    public JTextField getWeightField() {
        return weightField;
    }

    public JComboBox<String> getActivityComboBox() {
        return activityComboBox;
    }

    public JButton getSaveProfileButton() {
        return saveProfileButton;
    }

    public JPasswordField getCurrentPasswordField() {
        return currentPasswordField;
    }

    public JPasswordField getNewPasswordField() {
        return newPasswordField;
    }

    public JPasswordField getConfirmPasswordField() {
        return confirmPasswordField;
    }

    public JButton getChangePasswordButton() {
        return changePasswordButton;
    }

    public JButton getBackButton() {
        return backButton;
    }
}
