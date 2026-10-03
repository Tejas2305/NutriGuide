package com.nutriguide.gui;

import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.Recommendation;
import com.nutriguide.model.User;
import com.nutriguide.service.NutritionService;
import com.nutriguide.service.RecommendationService;
import com.nutriguide.service.UserService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * DashboardFrame: Main user dashboard presenting user profile, BMI, actual nutrition,
 * daily nutrition targets, and rule-based recommendation previews.
 *
 * Demonstrates:
 * - Java Lab Exp 6: Anonymous Inner Classes for ActionListeners
 * - Java Lab Exp 17-18: Swing UI Components (JFrame, JPanel, JLabel, JButton, JOptionPane)
 * - Layered Architecture: GUI delegates to NutritionService & RecommendationService
 *   (Strictly NO SQL, Connection, PreparedStatement, or ResultSet in GUI)
 */
public class DashboardFrame extends JFrame {

    private final User user;
    private final NutritionService nutritionService;
    private final RecommendationService recommendationService;

    // Data models loaded via Services
    private BigDecimal bmi;
    private DailyNutritionSummary dailySummary;
    private NutritionGoal activeGoal;
    private ArrayList<Recommendation> recommendations;

    // UI Labels for verification
    private JLabel welcomeLabel;
    private JLabel userDetailsLabel;
    private JLabel physicalStatsLabel;
    private JLabel bmiLabel;
    private JLabel caloriesLabel;
    private JLabel proteinLabel;
    private JLabel carbsLabel;
    private JLabel fatLabel;
    private JLabel targetCaloriesLabel;
    private JLabel targetProteinLabel;
    private JLabel targetCarbsLabel;
    private JLabel targetFatLabel;
    private JLabel noGoalLabel;

    // Navigation Buttons
    private JButton mealLogButton;
    private JButton goalButton;
    private JButton recButton;
    private JButton reportsButton;
    private JButton profileButton;
    private JButton logoutButton;

    public DashboardFrame(User user) {
        this(user, new NutritionService(), new RecommendationService());
    }

    public DashboardFrame(User user, NutritionService nutritionService, RecommendationService recommendationService) {
        this.user = user;
        this.nutritionService = nutritionService;
        this.recommendationService = recommendationService;

        loadDashboardData();
        initializeUI();
    }

    /**
     * Loads live data by delegating to Service Layer.
     * GUI does not write any SQL or access DB directly.
     */
    public void loadDashboardData() {
        if (user == null) {
            return;
        }

        // 0. Refresh User from DB if userId is available
        try {
            if (user.getUserId() != null) {
                UserService uService = new UserService();
                User fresh = uService.getUserById(user.getUserId());
                if (fresh != null) {
                    this.user.setName(fresh.getName());
                    this.user.setAge(fresh.getAge());
                    this.user.setGender(fresh.getGender());
                    this.user.setHeightCm(fresh.getHeightCm());
                    this.user.setWeightKg(fresh.getWeightKg());
                    this.user.setActivityLevel(fresh.getActivityLevel());
                    this.user.setPasswordHash(fresh.getPasswordHash());
                }
            }
        } catch (Exception ignored) {}

        LocalDate today = LocalDate.now();

        // 1. Calculate BMI via NutritionService (MySQL stored function calculate_bmi)
        try {
            if (user.getWeightKg() != null && user.getHeightCm() != null) {
                this.bmi = nutritionService.calculateBmi(user.getWeightKg(), user.getHeightCm());
            }
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid height/weight data: " + ex.getMessage(),
                    "BMI Calculation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while calculating BMI: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // 2. Retrieve Today's Nutrition Summary via NutritionService (view or procedure)
        try {
            if (user.getUserId() != null) {
                this.dailySummary = nutritionService.getDailySummaryFromView(user.getUserId(), today);
            }
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load today's nutrition summary: " + ex.getMessage(),
                    "Data Load Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
        if (this.dailySummary == null) {
            this.dailySummary = new DailyNutritionSummary();
        }

        // 3. Retrieve Active Goal via NutritionService
        try {
            if (user.getUserId() != null) {
                this.activeGoal = nutritionService.getActiveGoal(user.getUserId());
            }
        } catch (ResourceNotFoundException ex) {
            // Normal case when user has not set an active goal yet
            this.activeGoal = null;
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load active nutrition goal: " + ex.getMessage(),
                    "Data Load Warning",
                    JOptionPane.WARNING_MESSAGE);
            this.activeGoal = null;
        }

        // 4. Retrieve Today's Recommendations via RecommendationService (rule-based preview)
        try {
            if (user.getUserId() != null) {
                this.recommendations = recommendationService.generateRecommendations(user.getUserId(), today);
            }
        } catch (Exception ex) {
            this.recommendations = new ArrayList<Recommendation>();
        }
        if (this.recommendations == null) {
            this.recommendations = new ArrayList<Recommendation>();
        }
    }

    private void initializeUI() {
        setTitle("NutriGuide Dashboard - " + (user != null ? user.getName() : "User"));
        setSize(720, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ====================================================================
        // Section 1: Header Panel (North)
        // ====================================================================
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 15, 8, 15),
                BorderFactory.createEtchedBorder()
        ));

        String userName = (user != null && user.getName() != null) ? user.getName() : "User";
        welcomeLabel = new JLabel("Welcome, " + userName + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        String email = (user != null && user.getEmail() != null) ? user.getEmail() : "N/A";
        String age = (user != null && user.getAge() != null) ? user.getAge() + " yrs" : "N/A";
        String gender = (user != null && user.getGender() != null) ? user.getGender() : "N/A";
        String activity = (user != null && user.getActivityLevel() != null) ? user.getActivityLevel() : "N/A";

        userDetailsLabel = new JLabel("Email: " + email + "  |  Age: " + age + "  |  Gender: " + gender + "  |  Activity: " + activity);
        userDetailsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        headerPanel.add(welcomeLabel);
        headerPanel.add(userDetailsLabel);
        add(headerPanel, BorderLayout.NORTH);

        // ====================================================================
        // Section 2: Center Content Panel
        // ====================================================================
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        // Subpanel A: Health Profile & BMI Section
        JPanel bmiPanel = new JPanel(new GridLayout(1, 2, 10, 5));
        bmiPanel.setBorder(BorderFactory.createTitledBorder("User Health Profile & BMI"));

        String heightText = (user != null && user.getHeightCm() != null) ? user.getHeightCm() + " cm" : "N/A";
        String weightText = (user != null && user.getWeightKg() != null) ? user.getWeightKg() + " kg" : "N/A";
        physicalStatsLabel = new JLabel("Height: " + heightText + "   |   Weight: " + weightText);
        physicalStatsLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        String bmiText = (bmi != null) ? bmi.toPlainString() : "N/A";
        bmiLabel = new JLabel("BMI: " + bmiText);
        bmiLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        bmiPanel.add(physicalStatsLabel);
        bmiPanel.add(bmiLabel);
        centerPanel.add(bmiPanel);

        // Subpanel B: Today's Actual Nutrition Intake
        JPanel nutritionPanel = new JPanel(new GridLayout(1, 4, 10, 5));
        LocalDate today = LocalDate.now();
        nutritionPanel.setBorder(BorderFactory.createTitledBorder("Today's Nutrition (" + today + ")"));

        BigDecimal cal = (dailySummary != null && dailySummary.getDailyCalories() != null) ? dailySummary.getDailyCalories() : BigDecimal.ZERO;
        BigDecimal prot = (dailySummary != null && dailySummary.getDailyProtein() != null) ? dailySummary.getDailyProtein() : BigDecimal.ZERO;
        BigDecimal carb = (dailySummary != null && dailySummary.getDailyCarbs() != null) ? dailySummary.getDailyCarbs() : BigDecimal.ZERO;
        BigDecimal fat = (dailySummary != null && dailySummary.getDailyFat() != null) ? dailySummary.getDailyFat() : BigDecimal.ZERO;

        caloriesLabel = new JLabel("Calories: " + cal.toPlainString() + " kcal");
        proteinLabel = new JLabel("Protein: " + prot.toPlainString() + " g");
        carbsLabel = new JLabel("Carbs: " + carb.toPlainString() + " g");
        fatLabel = new JLabel("Fat: " + fat.toPlainString() + " g");

        nutritionPanel.add(caloriesLabel);
        nutritionPanel.add(proteinLabel);
        nutritionPanel.add(carbsLabel);
        nutritionPanel.add(fatLabel);
        centerPanel.add(nutritionPanel);

        // Subpanel C: Daily Target (Active Nutrition Goal)
        JPanel goalPanel = new JPanel();
        goalPanel.setBorder(BorderFactory.createTitledBorder("Daily Target (Active Nutrition Goal)"));

        if (activeGoal != null) {
            goalPanel.setLayout(new GridLayout(1, 4, 10, 5));
            targetCaloriesLabel = new JLabel("Target Cal: " + activeGoal.getTargetCalories() + " kcal");
            targetProteinLabel = new JLabel("Target Protein: " + activeGoal.getTargetProtein() + " g");
            targetCarbsLabel = new JLabel("Target Carbs: " + activeGoal.getTargetCarbs() + " g");
            targetFatLabel = new JLabel("Target Fat: " + activeGoal.getTargetFats() + " g");

            goalPanel.add(targetCaloriesLabel);
            goalPanel.add(targetProteinLabel);
            goalPanel.add(targetCarbsLabel);
            goalPanel.add(targetFatLabel);
        } else {
            goalPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            noGoalLabel = new JLabel("No active nutrition goal.");
            noGoalLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
            goalPanel.add(noGoalLabel);
        }
        centerPanel.add(goalPanel);

        // Subpanel D: Today's Recommendations Preview (Up to 3 rules)
        JPanel recPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        recPanel.setBorder(BorderFactory.createTitledBorder("Today's Recommendations"));

        if (recommendations != null && !recommendations.isEmpty()) {
            int count = Math.min(3, recommendations.size());
            for (int i = 0; i < count; i++) {
                Recommendation rec = recommendations.get(i);
                JLabel recLabel = new JLabel("• [" + rec.getRecType() + "] " + rec.getMessage());
                recLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                recPanel.add(recLabel);
            }
            // Fill remaining slots if less than 3
            for (int i = count; i < 3; i++) {
                JLabel emptyLabel = new JLabel("");
                recPanel.add(emptyLabel);
            }
        } else {
            JLabel noRecLabel = new JLabel("• No dietary recommendations generated for today.");
            noRecLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
            recPanel.add(noRecLabel);
            recPanel.add(new JLabel(""));
            recPanel.add(new JLabel(""));
        }
        centerPanel.add(recPanel);

        add(centerPanel, BorderLayout.CENTER);

        // ====================================================================
        // Section 3: Navigation Buttons Panel (South)
        // ====================================================================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));

        mealLogButton = new JButton("Meal Log");
        goalButton = new JButton("Nutrition Goal");
        recButton = new JButton("Recommendations");
        reportsButton = new JButton("Reports");
        profileButton = new JButton("Profile");
        logoutButton = new JButton("Logout");

        buttonPanel.add(mealLogButton);
        buttonPanel.add(goalButton);
        buttonPanel.add(recButton);
        buttonPanel.add(reportsButton);
        buttonPanel.add(profileButton);
        buttonPanel.add(logoutButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // ====================================================================
        // Section 4: Action Listeners (Anonymous Inner Classes - Java Lab Exp 6)
        // ====================================================================
        mealLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openMealLogFrame();
            }
        });

        goalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNutritionGoalFrame();
            }
        });

        recButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRecommendationFrame();
            }
        });

        reportsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openReportFrame();
            }
        });

        profileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openProfileFrame();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });
    }

    private void showNextPhaseDialog(String featureName) {
        JOptionPane.showMessageDialog(this,
                featureName + " screen will be implemented in the next phase.",
                "NutriGuide",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Disposes the dashboard and opens ProfileFrame for the logged-in User.
     */
    public void openProfileFrame() {
        this.dispose();
        ProfileFrame profileFrame = new ProfileFrame(user);
        profileFrame.setVisible(true);
    }

    /**
     * Disposes the dashboard and opens ReportFrame for the logged-in User.
     */
    public void openReportFrame() {
        this.dispose();
        ReportFrame reportFrame = new ReportFrame(user);
        reportFrame.setVisible(true);
    }

    /**
     * Disposes the dashboard and opens RecommendationFrame for the logged-in User.
     */
    public void openRecommendationFrame() {
        this.dispose();
        RecommendationFrame recFrame = new RecommendationFrame(user);
        recFrame.setVisible(true);
    }

    /**
     * Disposes the dashboard and opens NutritionGoalFrame for the logged-in User.
     */
    public void openNutritionGoalFrame() {
        this.dispose();
        NutritionGoalFrame goalFrame = new NutritionGoalFrame(user);
        goalFrame.setVisible(true);
    }

    /**
     * Disposes the dashboard and opens MealLogFrame for the logged-in User.
     */
    public void openMealLogFrame() {
        this.dispose();
        MealLogFrame mealLogFrame = new MealLogFrame(user);
        mealLogFrame.setVisible(true);
    }

    /**
     * Handles logout by disposing the dashboard and opening LoginFrame.
     */
    public void handleLogout() {
        this.dispose();
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }

    /**
     * Refreshes the dashboard data and UI labels.
     */
    public void refreshDashboardData() {
        loadDashboardData();
        if (welcomeLabel != null && user != null) {
            welcomeLabel.setText("Welcome, " + user.getName() + "!");
        }
        if (userDetailsLabel != null && user != null) {
            String email = (user.getEmail() != null) ? user.getEmail() : "N/A";
            String age = (user.getAge() != null) ? user.getAge() + " yrs" : "N/A";
            String gender = (user.getGender() != null) ? user.getGender() : "N/A";
            String activity = (user.getActivityLevel() != null) ? user.getActivityLevel() : "N/A";
            userDetailsLabel.setText("Email: " + email + "  |  Age: " + age + "  |  Gender: " + gender + "  |  Activity: " + activity);
        }
        if (physicalStatsLabel != null && user != null) {
            String heightText = (user.getHeightCm() != null) ? user.getHeightCm() + " cm" : "N/A";
            String weightText = (user.getWeightKg() != null) ? user.getWeightKg() + " kg" : "N/A";
            physicalStatsLabel.setText("Height: " + heightText + "   |   Weight: " + weightText);
        }
        if (bmiLabel != null) {
            bmiLabel.setText("BMI: " + (bmi != null ? bmi.toPlainString() : "N/A"));
        }
    }

    // ========================================================================
    // Programmatic Accessors for Testing & Verification
    // ========================================================================
    public User getUser() {
        return user;
    }

    public BigDecimal getBmi() {
        return bmi;
    }

    public DailyNutritionSummary getDailySummary() {
        return dailySummary;
    }

    public NutritionGoal getActiveGoal() {
        return activeGoal;
    }

    public ArrayList<Recommendation> getRecommendations() {
        return recommendations;
    }

    public NutritionService getNutritionService() {
        return nutritionService;
    }

    public RecommendationService getRecommendationService() {
        return recommendationService;
    }

    public JLabel getWelcomeLabel() {
        return welcomeLabel;
    }

    public JLabel getBmiLabel() {
        return bmiLabel;
    }

    public JLabel getCaloriesLabel() {
        return caloriesLabel;
    }

    public JLabel getTargetCaloriesLabel() {
        return targetCaloriesLabel;
    }

    public JLabel getNoGoalLabel() {
        return noGoalLabel;
    }

    public JButton getMealLogButton() {
        return mealLogButton;
    }

    public JButton getGoalButton() {
        return goalButton;
    }

    public JButton getRecButton() {
        return recButton;
    }

    public JButton getReportsButton() {
        return reportsButton;
    }

    public JButton getProfileButton() {
        return profileButton;
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }
}
