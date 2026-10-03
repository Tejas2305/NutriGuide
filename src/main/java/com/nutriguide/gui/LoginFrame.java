package com.nutriguide.gui;

import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.User;
import com.nutriguide.service.AuthenticationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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

/**
 * LoginFrame: Provides user authentication interface using Java Swing.
 *
 * Demonstrates Java Lab Manual concepts:
 * - Exp 6: Anonymous Inner Classes implementing ActionListener
 * - Exp 17-18: Swing components (JFrame, JPanel, JLabel, JTextField, JPasswordField, JButton)
 *   and Event Handling (actionPerformed)
 * - Layered architecture: GUI delegates directly to AuthenticationService
 */
public class LoginFrame extends JFrame {

    private final AuthenticationService authService;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton exitButton;

    public LoginFrame() {
        this.authService = new AuthenticationService();
        initializeUI();
    }

    public LoginFrame(AuthenticationService authService) {
        this.authService = authService;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("NutriGuide - User Login");
        setSize(480, 270);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("NutriGuide Management System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Form Panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 10, 30));

        JLabel emailLabel = new JLabel("Email Address:");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emailField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField = new JPasswordField(20);

        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        add(formPanel, BorderLayout.CENTER);

        // 3. Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        exitButton = new JButton("Exit");

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 4. Action Listeners using Anonymous Inner Classes (Java Lab Exp 6)
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
     * Handles login execution by calling AuthenticationService.
     */
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both email and password.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = authService.login(email, password);
            JOptionPane.showMessageDialog(this,
                    "Login successful!\nWelcome, " + user.getName(),
                    "NutriGuide - Login Success",
                    JOptionPane.INFORMATION_MESSAGE);

            DashboardFrame dashboard = new DashboardFrame(user);
            dashboard.setVisible(true);
            this.dispose();

        } catch (AuthenticationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Authentication Failed",
                    JOptionPane.ERROR_MESSAGE);

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);

        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database operation failed: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterFrame() {
        RegisterFrame registerFrame = new RegisterFrame(this, authService);
        registerFrame.setVisible(true);
        this.setVisible(false);
    }

    // Getters for programmatic testing
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
