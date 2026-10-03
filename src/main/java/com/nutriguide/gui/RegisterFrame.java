package com.nutriguide.gui;

import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.User;
import com.nutriguide.service.AuthenticationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

/**
 * RegisterFrame: Provides new user registration interface using Java Swing.
 *
 * Demonstrates:
 * - Exp 6: Anonymous inner classes implementing ActionListener
 * - Exp 17-18: Swing components (JComboBox, JTextField, JPasswordField, JButton) and layouts
 * - Integration with AuthenticationService and backend validation
 */
public class RegisterFrame extends JFrame {

    private final LoginFrame loginFrame;
    private final AuthenticationService authService;

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField ageField;
    private JComboBox<String> genderComboBox;
    private JTextField heightField;
    private JTextField weightField;
    private JComboBox<String> activityComboBox;

    private JButton registerButton;
    private JButton clearButton;
    private JButton backButton;

    public RegisterFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        this.authService = new AuthenticationService();
        initializeUI();
    }

    public RegisterFrame(LoginFrame loginFrame, AuthenticationService authService) {
        this.loginFrame = loginFrame;
        this.authService = authService;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("NutriGuide - User Registration");
        setSize(520, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 1. Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Create a New NutriGuide Account");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Form Grid Panel
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 12));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        nameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
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

        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Email Address:"));
        formPanel.add(emailField);

        formPanel.add(new JLabel("Password (min 6 chars):"));
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Age (years):"));
        formPanel.add(ageField);

        formPanel.add(new JLabel("Gender:"));
        formPanel.add(genderComboBox);

        formPanel.add(new JLabel("Height (cm):"));
        formPanel.add(heightField);

        formPanel.add(new JLabel("Weight (kg):"));
        formPanel.add(weightField);

        formPanel.add(new JLabel("Activity Level:"));
        formPanel.add(activityComboBox);

        add(formPanel, BorderLayout.CENTER);

        // 3. Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        registerButton = new JButton("Register");
        clearButton = new JButton("Clear Form");
        backButton = new JButton("Back to Login");

        buttonPanel.add(registerButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 4. Action Listeners using Anonymous Inner Classes (Java Lab Exp 6)
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegistration();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnToLogin();
            }
        });
    }

    /**
     * Handles registration submission by reading fields and calling AuthenticationService.
     */
    public void handleRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String ageStr = ageField.getText().trim();
        String gender = (String) genderComboBox.getSelectedItem();
        String heightStr = heightField.getText().trim();
        String weightStr = weightField.getText().trim();
        String activity = (String) activityComboBox.getSelectedItem();

        // Basic UI null/empty checks
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() ||
                ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all registration fields.",
                    "Input Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int age;
        BigDecimal heightCm;
        BigDecimal weightKg;

        try {
            age = Integer.parseInt(ageStr);
            heightCm = new BigDecimal(heightStr);
            weightKg = new BigDecimal(weightStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Age, height, and weight must be valid numeric values.",
                    "Format Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        User newUser = new User(name, email, null, age, gender, heightCm, weightKg, activity);

        try {
            User registered = authService.registerUser(newUser, password);
            JOptionPane.showMessageDialog(this,
                    "Registration successful!\nWelcome, " + registered.getName() + ".\nYou can now log in.",
                    "Registration Complete",
                    JOptionPane.INFORMATION_MESSAGE);

            returnToLogin();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);

        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Authentication Error",
                    JOptionPane.ERROR_MESSAGE);

        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error during registration: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearForm() {
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        ageField.setText("");
        heightField.setText("");
        weightField.setText("");
        genderComboBox.setSelectedIndex(0);
        activityComboBox.setSelectedIndex(0);
    }

    private void returnToLogin() {
        this.dispose();
        if (loginFrame != null) {
            loginFrame.setVisible(true);
        }
    }

    // Accessors for testing
    public AuthenticationService getAuthService() {
        return authService;
    }

    public JTextField getNameField() {
        return nameField;
    }

    public JTextField getEmailField() {
        return emailField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
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

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JButton getBackButton() {
        return backButton;
    }
}
