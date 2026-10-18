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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * LoginFrame: Provides user authentication interface using Java Swing.
 *
 * Demonstrates:
 * - SwingWorker for off-EDT network and authentication requests (zero UI freezing)
 * - Decoupled client architecture communicating with localhost REST API
 * - Java Lab Exp 6: Anonymous Inner Classes implementing ActionListener
 * - Exp 17-18: Swing components (JFrame, JPanel, JLabel, JTextField, JPasswordField, JButton)
 */
public class LoginFrame extends JFrame {

    private final NutriGuideApiClient apiClient;
    private final AuthenticationService authService;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton exitButton;

    public LoginFrame() {
        this(NutriGuideApiClient.getInstance(), null);
    }

    public LoginFrame(NutriGuideApiClient apiClient) {
        this(apiClient, null);
    }

    /**
     * For test and demo harness execution only: allows direct service injection
     * without requiring a running localhost HTTP backend server.
     *
     * @param authService Directly injected AuthenticationService for offline/integration testing
     */
    public LoginFrame(AuthenticationService authService) {
        this(null, authService);
    }

    /**
     * For test and demo harness execution only: allows explicit injection of both
     * client and service implementations.
     *
     * @param apiClient API client implementation or null if operating in test harness mode
     * @param authService Directly injected AuthenticationService or null for standard client mode
     */
    public LoginFrame(NutriGuideApiClient apiClient, AuthenticationService authService) {
        this.apiClient = apiClient;
        this.authService = authService;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("NutriGuide - User Login");
        UITheme.configureFrame(this, 1024, 720, 500, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        // Centered Card Container Panel
        JPanel cardPanel = UITheme.createCardPanel();
        cardPanel.setLayout(new BorderLayout(15, 15));
        cardPanel.setPreferredSize(new Dimension(480, 310));
        cardPanel.setMinimumSize(new Dimension(480, 310));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 2, 4));
        headerPanel.setBackground(UITheme.CARD_BG);

        JLabel titleLabel = new JLabel("NutriGuide Management System", JLabel.CENTER);
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_DARK);

        JLabel subtitleLabel = new JLabel("Enter your credentials to access your nutrition profile", JLabel.CENTER);
        subtitleLabel.setFont(UITheme.FONT_SMALL);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        cardPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Form Panel using GridBagLayout for aligned, consistent field widths
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);

        JLabel emailLabel = new JLabel("Email Address:");
        emailLabel.setFont(UITheme.FONT_BOLD);
        emailLabel.setForeground(UITheme.TEXT_MAIN);
        emailLabel.setPreferredSize(new Dimension(110, 32));

        emailField = new JTextField();
        UITheme.styleTextField(emailField);
        emailField.setPreferredSize(new Dimension(300, 32));
        emailField.setMinimumSize(new Dimension(280, 32));

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(UITheme.FONT_BOLD);
        passwordLabel.setForeground(UITheme.TEXT_MAIN);
        passwordLabel.setPreferredSize(new Dimension(110, 32));

        passwordField = new JPasswordField();
        UITheme.styleTextField(passwordField);
        passwordField.setPreferredSize(new Dimension(300, 32));
        passwordField.setMinimumSize(new Dimension(280, 32));

        // Row 0: Email
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(emailField, gbc);

        // Row 1: Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(passwordField, gbc);

        cardPanel.add(formPanel, BorderLayout.CENTER);

        // 3. Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttonPanel.setBackground(UITheme.CARD_BG);

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        exitButton = new JButton("Exit");

        UITheme.stylePrimaryButton(loginButton);
        UITheme.styleSecondaryButton(registerButton);
        UITheme.styleSecondaryButton(exitButton);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(exitButton);
        cardPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(cardPanel);

        // 4. Action Listeners using Anonymous Inner Classes
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegisterFrame();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    /**
     * Handles login execution asynchronously using SwingWorker to prevent UI freezing.
     */
    public void handleLogin() {
        final String email = emailField.getText().trim();
        final String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            UITheme.showWarning(this, "Please enter both email and password.", "Input Error");
            return;
        }

        loginButton.setEnabled(false);
        registerButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                if (apiClient != null) {
                    return apiClient.login(email, password);
                } else if (authService != null) {
                    return authService.login(email, password);
                } else {
                    throw new IllegalStateException("NutriGuide Localhost API Client is unavailable, and direct database service fallback is disabled for standard desktop operation.");
                }
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());

                try {
                    User user = get();
                    UITheme.showInfo(LoginFrame.this,
                            "Login successful!\nWelcome, " + user.getName(),
                            "NutriGuide - Login Success");

                    DashboardFrame dashboard = (apiClient != null)
                            ? new DashboardFrame(user, apiClient)
                            : new DashboardFrame(user);
                    UITheme.transition(LoginFrame.this, dashboard);

                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String message = cause.getMessage();
                    if (cause instanceof AuthenticationException || cause instanceof ApiClientException) {
                        UITheme.showError(LoginFrame.this, message, "Authentication Failed");
                    } else if (cause instanceof ValidationException) {
                        UITheme.showWarning(LoginFrame.this, message, "Validation Error");
                    } else if (cause instanceof DatabaseOperationException) {
                        UITheme.showError(LoginFrame.this, "Database error: " + message, "Database Error");
                    } else {
                        UITheme.showError(LoginFrame.this, "Login error: " + message, "Error");
                    }
                }
            }
        };
        worker.execute();
    }

    private void openRegisterFrame() {
        RegisterFrame registerFrame = new RegisterFrame(this, apiClient, authService);
        registerFrame.setExtendedState(this.getExtendedState());
        registerFrame.setVisible(true);
        this.setVisible(false);
    }

    // Getters for programmatic testing
    public NutriGuideApiClient getApiClient() {
        return apiClient;
    }

    public AuthenticationService getAuthService() {
        return authService;
    }

    public JTextField getEmailField() {
        return emailField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JButton getExitButton() {
        return exitButton;
    }
}
