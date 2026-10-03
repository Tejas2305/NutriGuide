package com.nutriguide.gui;

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
import java.awt.BorderLayout;
import java.awt.Color;
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
 * - Exp 6: Anonymous Inner Classes for ActionListeners
 * - Exp 17-18: Swing UI Components (JFrame, JPanel, JLabel, JTextField, JPasswordField, JComboBox, JButton, JOptionPane)
 * - Layered Architecture: Delegates strictly to UserService and NutritionService
 *   (Strictly ZERO java.sql imports and ZERO SQL strings in GUI)
 * - Security: No plaintext passwords stored, password hashes never displayed, current password verification
 */
public class ProfileFrame extends JFrame {

    private final User user;
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
        this(user, new UserService(), new NutritionService());
    }

    public ProfileFrame(User user, UserService userService, NutritionService nutritionService) {
        this.user = user;
        this.userService = userService;
        this.nutritionService = nutritionService;

        initializeUI();
        loadProfileData();
    }

    private void initializeUI() {
        setTitle("NutriGuide - User Profile");
        setSize(720, 650);
        setMinimumSize(new Dimension(680, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 10, 20));

        JLabel titleLabel = new JLabel("User Profile Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(24, 43, 73));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        String subtitleText = (user != null) ? "Account ID: #" + user.getUserId() : "Guest Account";
        JLabel subtitleLabel = new JLabel(subtitleText);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.DARK_GRAY);
        headerPanel.add(subtitleLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content Panel (Scrollable container for Profile Form + Password Change)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        // Section A: Personal Information Form
        JPanel profilePanel = new JPanel(new GridLayout(7, 2, 10, 10));
        profilePanel.setBorder(BorderFactory.createTitledBorder("Personal Profile Information"));

        nameField = new JTextField();
        emailField = new JTextField();
        emailField.setEditable(false);
        emailField.setBackground(new Color(245, 245, 245));
        emailField.setToolTipText("Email address is your unique account identifier and cannot be modified.");

        ageField = new JTextField();

        String[] genders = {"Male", "Female", "Other"};
        genderComboBox = new JComboBox<String>(genders);

        heightField = new JTextField();
        weightField = new JTextField();

        String[] activityLevels = {
                "Sedentary",
                "Lightly Active",
                "Moderately Active",
                "Very Active"
        };
        activityComboBox = new JComboBox<String>(activityLevels);

        profilePanel.add(new JLabel("Full Name:"));
        profilePanel.add(nameField);

        profilePanel.add(new JLabel("Email Address (Login ID):"));
        profilePanel.add(emailField);

        profilePanel.add(new JLabel("Age (years):"));
        profilePanel.add(ageField);

        profilePanel.add(new JLabel("Gender:"));
        profilePanel.add(genderComboBox);

        profilePanel.add(new JLabel("Height (cm):"));
        profilePanel.add(heightField);

        profilePanel.add(new JLabel("Weight (kg):"));
        profilePanel.add(weightField);

        profilePanel.add(new JLabel("Activity Level:"));
        profilePanel.add(activityComboBox);

        contentPanel.add(profilePanel);

        // Save Profile Button Panel
        JPanel saveProfileBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveProfileButton = new JButton("Save Profile");
        saveProfileButton.setFont(new Font("Arial", Font.BOLD, 12));
        saveProfileBtnPanel.add(saveProfileButton);
        contentPanel.add(saveProfileBtnPanel);

        // Section B: Password Change Panel
        JPanel passwordPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        passwordPanel.setBorder(BorderFactory.createTitledBorder("Change Password"));

        currentPasswordField = new JPasswordField();
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        passwordPanel.add(new JLabel("Current Password:"));
        passwordPanel.add(currentPasswordField);

        passwordPanel.add(new JLabel("New Password (min 6 chars):"));
        passwordPanel.add(newPasswordField);

        passwordPanel.add(new JLabel("Confirm New Password:"));
        passwordPanel.add(confirmPasswordField);

        contentPanel.add(passwordPanel);

        // Change Password Button Panel
        JPanel changePasswordBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        changePasswordButton = new JButton("Change Password");
        changePasswordButton.setFont(new Font("Arial", Font.BOLD, 12));
        changePasswordBtnPanel.add(changePasswordButton);
        contentPanel.add(changePasswordBtnPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Navigation Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        backButton = new JButton("Back to Dashboard");
        bottomPanel.add(backButton);
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
                JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw new ValidationException("name", "Name cannot be empty.");
        }

        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
            ValidationUtil.requireValidAge(age);
        } catch (NumberFormatException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "Age must be a valid integer.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw new ValidationException("age", "Age must be a valid integer.");
        } catch (ValidationException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw ex;
        }

        double height;
        try {
            height = Double.parseDouble(heightField.getText().trim());
            ValidationUtil.requireValidHeight(height);
        } catch (NumberFormatException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "Height must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw new ValidationException("height", "Height must be a valid number.");
        } catch (ValidationException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw ex;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightField.getText().trim());
            ValidationUtil.requireValidWeight(weight);
        } catch (NumberFormatException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "Weight must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw new ValidationException("weight", "Weight must be a valid number.");
        } catch (ValidationException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw ex;
        }

        String gender = (String) genderComboBox.getSelectedItem();
        if (gender == null || gender.trim().isEmpty()) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "Please select a valid gender.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw new ValidationException("gender", "Please select a valid gender.");
        }

        String activity = (String) activityComboBox.getSelectedItem();
        if (activity == null || activity.trim().isEmpty()) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "Please select a valid activity level.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw new ValidationException("activityLevel", "Please select a valid activity level.");
        }

        return true;
    }

    public boolean validateProfileInputs() {
        return validateProfileInputs(true);
    }

    /**
     * Saves updated profile attributes through UserService.
     */
    public boolean saveProfile(boolean showDialog) {
        try {
            validateProfileInputs(showDialog);

            String name = nameField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            String gender = (String) genderComboBox.getSelectedItem();
            BigDecimal height = new BigDecimal(heightField.getText().trim());
            BigDecimal weight = new BigDecimal(weightField.getText().trim());
            String activity = (String) activityComboBox.getSelectedItem();

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
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            return updated;
        } catch (ValidationException ex) {
            // Already prompted if showDialog is true
            return false;
        } catch (ResourceNotFoundException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "User not found: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        } catch (DatabaseOperationException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "Database update error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Current password is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw new ValidationException("currentPassword", "Current password is required.");
        }

        if (newPass.trim().isEmpty()) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "New password is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw new ValidationException("newPassword", "New password is required.");
        }

        if (newPass.length() < 6) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "New password must be at least 6 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
            throw new ValidationException("newPassword", "New password must be at least 6 characters long.");
        }

        if (!newPass.equals(confirmPass)) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
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
    public boolean changePassword(boolean showDialog) {
        try {
            validatePasswordInputs(showDialog);

            String currentPass = new String(currentPasswordField.getPassword());
            String newPass = new String(newPasswordField.getPassword());
            String confirmPass = new String(confirmPasswordField.getPassword());

            boolean success = userService.changePassword(user.getUserId(), currentPass, newPass, confirmPass);

            if (success) {
                currentPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");

                if (showDialog) {
                    JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            return success;
        } catch (AuthenticationException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        } catch (ValidationException ex) {
            // Validation exception message already handled or thrown if calling validate directly
            return false;
        } catch (ResourceNotFoundException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "User not found: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        } catch (DatabaseOperationException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this, "Database update error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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
        this.dispose();
        DashboardFrame dashboardFrame = new DashboardFrame(user);
        dashboardFrame.setVisible(true);
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
