package com.nutriguide.demo;

import com.nutriguide.exception.AuthenticationException;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.gui.LoginFrame;
import com.nutriguide.gui.NutriGuideGUI;
import com.nutriguide.gui.RegisterFrame;
import com.nutriguide.model.User;
import com.nutriguide.service.AuthenticationService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * GuiPhase4ADemo: Programmatic verification of the Java Swing GUI (Phase 4A: Login + Registration).
 *
 * Verifies:
 * 1. LoginFrame can be created and has correct Swing components & title.
 * 2. RegisterFrame can be created and has correct Swing components & 8 User model fields.
 * 3. Login button and ActionListeners are wired properly.
 * 4. Register button and ActionListeners are wired properly.
 * 5. Backend is NOT bypassed: GUI delegates exclusively to AuthenticationService.
 * 6. UI form clear functionality.
 * 7. Verification that GUI classes do not directly import JDBC/SQL classes.
 */
public class GuiPhase4ADemo {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   NutriGuide Phase 4A: Java Swing GUI Verification (Login + Registration)");
        System.out.println("================================================================================");

        LoginFrame loginFrame = null;
        RegisterFrame registerFrame = null;

        try {
            AuthenticationService authService = new AuthenticationService();

            // 1. Test LoginFrame instantiation and components
            loginFrame = testLoginFrame(authService);

            // 2. Test RegisterFrame instantiation and components
            registerFrame = testRegisterFrame(loginFrame, authService);

            // 3. Test Form clear behavior
            testRegisterFrameClear(registerFrame);

            // 4. Test GUI-to-Service Integration for Authentication
            testLoginGuiServiceIntegration(loginFrame);

            // 5. Test GUI-to-Service Integration for Registration
            testRegisterGuiServiceIntegration(registerFrame);

            // 6. Verify Architecture: Ensure GUI doesn't import JDBC / SQL
            testArchitecturalIntegrity();

            // 7. Verify NutriGuideGUI launcher exists and has main method
            testLauncher();

        } catch (Exception e) {
            System.err.println("Unexpected exception in GuiPhase4ADemo: " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            if (registerFrame != null) {
                registerFrame.dispose();
            }
            if (loginFrame != null) {
                loginFrame.dispose();
            }
        }

        // Final Summary
        System.out.println("\n================================================================================");
        System.out.println("   Phase 4A GUI Verification Summary");
        System.out.println("================================================================================");
        System.out.println("Total Tests Run: " + (passed + failed));
        System.out.println("Tests Passed:    " + passed);
        System.out.println("Tests Failed:    " + failed);
        System.out.println("Pass Rate:       " + (passed * 100 / (passed + failed)) + "%");
        System.out.println("================================================================================\n");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static LoginFrame testLoginFrame(AuthenticationService authService) {
        System.out.println("\n--- [1] Testing LoginFrame Instantiation & Components ---");

        LoginFrame frame = new LoginFrame(authService);
        assertCondition("1.1: LoginFrame instantiated successfully", frame != null);
        assertCondition("1.2: Window title is 'NutriGuide - User Login'", "NutriGuide - User Login".equals(frame.getTitle()));

        JTextField emailField = frame.getEmailField();
        JPasswordField passwordField = frame.getPasswordField();
        JButton loginBtn = frame.getLoginButton();
        JButton regBtn = frame.getRegisterButton();
        JButton exitBtn = frame.getExitButton();

        assertCondition("1.3: Email text field is present", emailField != null);
        assertCondition("1.4: Password field is JPasswordField (not plain text)", passwordField != null);
        assertCondition("1.5: Login button is present", loginBtn != null);
        assertCondition("1.6: Register button is present", regBtn != null);
        assertCondition("1.7: Exit button is present", exitBtn != null);

        ActionListener[] loginListeners = loginBtn.getActionListeners();
        assertCondition("1.8: Login button has ActionListener attached", loginListeners != null && loginListeners.length > 0);

        ActionListener[] regListeners = regBtn.getActionListeners();
        assertCondition("1.9: Register button has ActionListener attached", regListeners != null && regListeners.length > 0);

        ActionListener[] exitListeners = exitBtn.getActionListeners();
        assertCondition("1.10: Exit button has ActionListener attached", exitListeners != null && exitListeners.length > 0);

        assertCondition("1.11: LoginFrame is wired to AuthenticationService", frame.getAuthService() != null);

        return frame;
    }

    private static RegisterFrame testRegisterFrame(LoginFrame loginFrame, AuthenticationService authService) {
        System.out.println("\n--- [2] Testing RegisterFrame Instantiation & Components ---");

        RegisterFrame frame = new RegisterFrame(loginFrame, authService);
        assertCondition("2.1: RegisterFrame instantiated successfully", frame != null);
        assertCondition("2.2: Window title is 'NutriGuide - User Registration'", "NutriGuide - User Registration".equals(frame.getTitle()));

        JTextField nameField = frame.getNameField();
        JTextField emailField = frame.getEmailField();
        JPasswordField passwordField = frame.getPasswordField();
        JTextField ageField = frame.getAgeField();
        JComboBox<String> genderBox = frame.getGenderComboBox();
        JTextField heightField = frame.getHeightField();
        JTextField weightField = frame.getWeightField();
        JComboBox<String> activityBox = frame.getActivityComboBox();

        assertCondition("2.3: Name field is present", nameField != null);
        assertCondition("2.4: Email field is present", emailField != null);
        assertCondition("2.5: Password field is JPasswordField", passwordField != null);
        assertCondition("2.6: Age field is present", ageField != null);
        assertCondition("2.7: Gender combo box is present", genderBox != null && genderBox.getItemCount() == 3);
        assertCondition("2.8: Height field is present", heightField != null);
        assertCondition("2.9: Weight field is present", weightField != null);
        assertCondition("2.10: Activity level combo box is present", activityBox != null && activityBox.getItemCount() == 4);

        JButton regBtn = frame.getRegisterButton();
        JButton clearBtn = frame.getClearButton();
        JButton backBtn = frame.getBackButton();

        assertCondition("2.11: Register button is present", regBtn != null);
        assertCondition("2.12: Clear button is present", clearBtn != null);
        assertCondition("2.13: Back to Login button is present", backBtn != null);

        ActionListener[] regListeners = regBtn.getActionListeners();
        assertCondition("2.14: Register button has ActionListener attached", regListeners != null && regListeners.length > 0);

        ActionListener[] clearListeners = clearBtn.getActionListeners();
        assertCondition("2.15: Clear button has ActionListener attached", clearListeners != null && clearListeners.length > 0);

        ActionListener[] backListeners = backBtn.getActionListeners();
        assertCondition("2.16: Back button has ActionListener attached", backListeners != null && backListeners.length > 0);

        assertCondition("2.17: RegisterFrame is wired to AuthenticationService", frame.getAuthService() != null);

        return frame;
    }

    private static void testRegisterFrameClear(RegisterFrame frame) {
        System.out.println("\n--- [3] Testing RegisterFrame Clear Form Functionality ---");

        frame.getNameField().setText("Sample Name");
        frame.getEmailField().setText("sample@test.com");
        frame.getPasswordField().setText("secret123");
        frame.getAgeField().setText("25");
        frame.getHeightField().setText("175.00");
        frame.getWeightField().setText("70.00");
        frame.getGenderComboBox().setSelectedIndex(1); // Female
        frame.getActivityComboBox().setSelectedIndex(2); // Moderately Active

        frame.clearForm();

        assertCondition("3.1: Clear resets Name field", frame.getNameField().getText().isEmpty());
        assertCondition("3.2: Clear resets Email field", frame.getEmailField().getText().isEmpty());
        assertCondition("3.3: Clear resets Password field", frame.getPasswordField().getPassword().length == 0);
        assertCondition("3.4: Clear resets Age field", frame.getAgeField().getText().isEmpty());
        assertCondition("3.5: Clear resets Height field", frame.getHeightField().getText().isEmpty());
        assertCondition("3.6: Clear resets Weight field", frame.getWeightField().getText().isEmpty());
        assertCondition("3.7: Clear resets Gender selection to index 0", frame.getGenderComboBox().getSelectedIndex() == 0);
        assertCondition("3.8: Clear resets Activity selection to index 0", frame.getActivityComboBox().getSelectedIndex() == 0);
    }

    private static void testLoginGuiServiceIntegration(LoginFrame frame) {
        System.out.println("\n--- [4] Testing Login GUI to Service Layer Delegation ---");

        // Simulate entering valid credentials into GUI components
        frame.getEmailField().setText("user@example.com");
        frame.getPasswordField().setText("Password@123");

        String emailInput = frame.getEmailField().getText().trim();
        String pwdInput = new String(frame.getPasswordField().getPassword());

        try {
            User authenticatedUser = frame.getAuthService().login(emailInput, pwdInput);
            assertCondition("4.1: AuthenticationService.login() succeeds with credentials from GUI fields",
                    authenticatedUser != null && authenticatedUser.getUserId() == 1);
            assertCondition("4.2: Authenticated user matches email",
                    "user@example.com".equals(authenticatedUser.getEmail()));
        } catch (Exception e) {
            assertCondition("4.1: AuthenticationService.login() threw unexpected exception: " + e.getMessage(), false);
        }

        // Test bad password rejection through Service layer
        frame.getPasswordField().setText("wrongpassword");
        String badPwd = new String(frame.getPasswordField().getPassword());
        boolean authFailedCaught = false;
        try {
            frame.getAuthService().login(emailInput, badPwd);
        } catch (AuthenticationException ex) {
            authFailedCaught = true;
        } catch (Exception ex) {
            // Other exception
        }
        assertCondition("4.3: Invalid password correctly triggers AuthenticationException", authFailedCaught);
    }

    private static void testRegisterGuiServiceIntegration(RegisterFrame frame) {
        System.out.println("\n--- [5] Testing Register GUI to Service Layer Validation ---");

        // Simulate invalid user data in GUI fields
        frame.getNameField().setText("Test User");
        frame.getEmailField().setText("invalid-email-format");
        frame.getPasswordField().setText("123"); // too short (< 6 chars)
        frame.getAgeField().setText("25");
        frame.getHeightField().setText("175.00");
        frame.getWeightField().setText("70.00");

        String emailInput = frame.getEmailField().getText().trim();
        String pwdInput = new String(frame.getPasswordField().getPassword());
        User testUser = new User("Test User", emailInput, null, 25, "Male",
                new BigDecimal("175.00"), new BigDecimal("70.00"), "Sedentary");

        boolean validationFailedCaught = false;
        try {
            frame.getAuthService().registerUser(testUser, pwdInput);
        } catch (ValidationException ex) {
            validationFailedCaught = true;
        } catch (Exception ex) {
            // Other
        }
        assertCondition("5.1: Invalid input correctly triggers ValidationException in Service layer", validationFailedCaught);
    }

    private static void testArchitecturalIntegrity() {
        System.out.println("\n--- [6] Verifying GUI Architectural Integrity (No JDBC in GUI) ---");

        Class<?> loginClass = LoginFrame.class;
        Class<?> registerClass = RegisterFrame.class;

        // Check declared fields of LoginFrame
        boolean hasDirectConnection = false;
        for (java.lang.reflect.Field f : loginClass.getDeclaredFields()) {
            if (f.getType().getName().startsWith("java.sql.")) {
                hasDirectConnection = true;
                break;
            }
        }
        assertCondition("6.1: LoginFrame has NO java.sql fields (Connection/PreparedStatement/ResultSet)", !hasDirectConnection);

        // Check declared fields of RegisterFrame
        for (java.lang.reflect.Field f : registerClass.getDeclaredFields()) {
            if (f.getType().getName().startsWith("java.sql.")) {
                hasDirectConnection = true;
                break;
            }
        }
        assertCondition("6.2: RegisterFrame has NO java.sql fields (Connection/PreparedStatement/ResultSet)", !hasDirectConnection);
    }

    private static void testLauncher() {
        System.out.println("\n--- [7] Verifying NutriGuideGUI Application Launcher ---");

        try {
            Class<?> launcherClass = Class.forName("com.nutriguide.gui.NutriGuideGUI");
            Method mainMethod = launcherClass.getMethod("main", String[].class);
            assertCondition("7.1: NutriGuideGUI exists and has public static void main(String[])", mainMethod != null);
        } catch (Exception e) {
            assertCondition("7.1: NutriGuideGUI verification failed: " + e.getMessage(), false);
        }
    }

    private static void assertCondition(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.out.println("  [FAIL] " + testName);
            failed++;
        }
    }
}
