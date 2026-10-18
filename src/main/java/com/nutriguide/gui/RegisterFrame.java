package com.nutriguide.gui;

import com.nutriguide.client.ApiClientException;
import com.nutriguide.client.NutriGuideApiClient;
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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;

/**
 * RegisterFrame: Provides new user registration interface using Java Swing.
 *
 * Demonstrates:
 * - SwingWorker for off-EDT network and registration execution
 * - Clean validation on both client and localhost server
 * - Exp 6: Anonymous inner classes implementing ActionListener
 * - Exp 17-18: Swing components (JComboBox, JTextField, JPasswordField, JButton) and layouts
 */
public class RegisterFrame extends JFrame {

    private final LoginFrame loginFrame;
    private final NutriGuideApiClient apiClient;
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
        this(loginFrame, NutriGuideApiClient.getInstance(), null);
    }

    public RegisterFrame(LoginFrame loginFrame, NutriGuideApiClient apiClient) {
        this(loginFrame, apiClient, null);
    }

    /**
     * For test and demo harness execution only: allows direct service injection
     * without requiring a running localhost HTTP backend server.
     *
     * @param loginFrame Parent LoginFrame
     * @param authService Directly injected AuthenticationService for offline/integration testing
     */
    public RegisterFrame(LoginFrame loginFrame, AuthenticationService authService) {
        this(loginFrame, null, authService);
    }

    /**
     * For test and demo harness execution only: allows explicit injection of both
     * client and service implementations.
     *
     * @param loginFrame Parent LoginFrame
     * @param apiClient API client implementation or null if operating in test harness mode
     * @param authService Directly injected AuthenticationService or null for standard client mode
     */
    public RegisterFrame(LoginFrame loginFrame, NutriGuideApiClient apiClient, AuthenticationService authService) {
        this.loginFrame = loginFrame;
        this.apiClient = apiClient;
        this.authService = authService;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("NutriGuide - User Registration");
        UITheme.configureFrame(this, 1024, 760, 560, 560);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                returnToLogin();
            }
        });
        setLayout(new GridBagLayout());

        // Centered Card Container Panel
        JPanel cardPanel = UITheme.createCardPanel();
        cardPanel.setLayout(new BorderLayout(15, 15));
        cardPanel.setPreferredSize(new Dimension(540, 560));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(18, 25, 18, 25)
        ));

        // 1. Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 2, 4));
        headerPanel.setBackground(UITheme.CARD_BG);

        JLabel titleLabel = new JLabel("Create a New NutriGuide Account", JLabel.CENTER);
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_DARK);

        JLabel subtitleLabel = new JLabel("Enter your physical stats to enable personalized diet recommendations", JLabel.CENTER);
        subtitleLabel.setFont(UITheme.FONT_SMALL);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        cardPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Form Grid Panel
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 12, 10));
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

        nameField = new JTextField();
        UITheme.styleTextField(nameField);

        emailField = new JTextField();
        UITheme.styleTextField(emailField);

        passwordField = new JPasswordField();
        UITheme.styleTextField(passwordField);

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

        JLabel l1 = new JLabel("Full Name:"); l1.setFont(UITheme.FONT_BOLD); l1.setForeground(UITheme.TEXT_MAIN);
        formPanel.add(l1);
        formPanel.add(nameField);

        JLabel l2 = new JLabel("Email Address:"); l2.setFont(UITheme.FONT_BOLD); l2.setForeground(UITheme.TEXT_MAIN);
        formPanel.add(l2);
        formPanel.add(emailField);

        JLabel l3 = new JLabel("Password (min 6 chars):"); l3.setFont(UITheme.FONT_BOLD); l3.setForeground(UITheme.TEXT_MAIN);
        formPanel.add(l3);
        formPanel.add(passwordField);

        JLabel l4 = new JLabel("Age (years):"); l4.setFont(UITheme.FONT_BOLD); l4.setForeground(UITheme.TEXT_MAIN);
        formPanel.add(l4);
        formPanel.add(ageField);

        JLabel l5 = new JLabel("Gender:"); l5.setFont(UITheme.FONT_BOLD); l5.setForeground(UITheme.TEXT_MAIN);
        formPanel.add(l5);
        formPanel.add(genderComboBox);

        JLabel l6 = new JLabel("Height (cm):"); l6.setFont(UITheme.FONT_BOLD); l6.setForeground(UITheme.TEXT_MAIN);
        formPanel.add(l6);
        formPanel.add(heightField);

        JLabel l7 = new JLabel("Weight (kg):"); l7.setFont(UITheme.FONT_BOLD); l7.setForeground(UITheme.TEXT_MAIN);
        formPanel.add(l7);
        formPanel.add(weightField);

        JLabel l8 = new JLabel("Activity Level:"); l8.setFont(UITheme.FONT_BOLD); l8.setForeground(UITheme.TEXT_MAIN);
        formPanel.add(l8);
        formPanel.add(activityComboBox);

        cardPanel.add(formPanel, BorderLayout.CENTER);

        // 3. Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttonPanel.setBackground(UITheme.CARD_BG);

        registerButton = new JButton("Register");
        clearButton = new JButton("Clear Form");
        backButton = new JButton("Back to Login");

        UITheme.stylePrimaryButton(registerButton);
        UITheme.styleSecondaryButton(clearButton);
        UITheme.styleSecondaryButton(backButton);

        buttonPanel.add(registerButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(backButton);
        cardPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.BG_COLOR);
        wrapper.add(cardPanel);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        // 4. Action Listeners using Anonymous Inner Classes
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
     * Handles registration submission asynchronously using SwingWorker.
     */
    public void handleRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        final String password = new String(passwordField.getPassword());
        String ageStr = ageField.getText().trim();
        String gender = (String) genderComboBox.getSelectedItem();
        String heightStr = heightField.getText().trim();
        String weightStr = weightField.getText().trim();
        String activity = (String) activityComboBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() ||
                ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            UITheme.showWarning(this, "Please fill in all registration fields.", "Input Warning");
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
            UITheme.showError(this, "Age, height, and weight must be valid numeric values.", "Format Error");
            return;
        }

        final User newUser = new User(name, email, null, age, gender, heightCm, weightKg, activity);

        registerButton.setEnabled(false);
        clearButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                if (apiClient != null) {
                    return apiClient.register(newUser, password);
                } else if (authService != null) {
                    return authService.registerUser(newUser, password);
                } else {
                    throw new IllegalStateException("NutriGuide Localhost API Client is unavailable, and direct database service fallback is disabled for standard desktop operation.");
                }
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                clearButton.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());

                try {
                    User registered = get();
                    UITheme.showInfo(RegisterFrame.this,
                            "Registration successful!\nWelcome, " + registered.getName() + ".\nYou can now log in.",
                            "Registration Complete");
                    returnToLogin();

                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String message = cause.getMessage();

                    if (cause instanceof ValidationException) {
                        UITheme.showWarning(RegisterFrame.this, message, "Validation Error");
                    } else if (cause instanceof AuthenticationException || cause instanceof ApiClientException) {
                        UITheme.showError(RegisterFrame.this, message, "Registration Error");
                    } else if (cause instanceof DatabaseOperationException) {
                        UITheme.showError(RegisterFrame.this, "Database error: " + message, "Database Error");
                    } else {
                        UITheme.showError(RegisterFrame.this, "Registration error: " + message, "Error");
                    }
                }
            }
        };
        worker.execute();
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
        if (loginFrame != null) {
            loginFrame.setExtendedState(this.getExtendedState());
            loginFrame.setVisible(true);
            this.dispose();
        } else {
            LoginFrame newLogin = new LoginFrame(apiClient, authService);
            UITheme.transition(this, newLogin);
        }
    }

    // Accessors for testing
    public NutriGuideApiClient getApiClient() {
        return apiClient;
    }

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
