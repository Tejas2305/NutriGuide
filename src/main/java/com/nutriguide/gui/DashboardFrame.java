package com.nutriguide.gui;

import com.nutriguide.client.NutriGuideApiClient;
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
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

/**
 * DashboardFrame: Main user dashboard presenting user profile, BMI, actual nutrition,
 * daily nutrition targets, and rule-based recommendation previews.
 *
 * Demonstrates:
 * - Localhost API client communication (or fallback service delegation)
 * - Decoupled client architecture
 * - Java Lab Exp 6: Anonymous Inner Classes for ActionListeners
 * - Java Lab Exp 17-18: Swing UI Components (JFrame, JPanel, JLabel, JButton, JOptionPane)
 */
public class DashboardFrame extends JFrame {

    private final User user;
    private final NutriGuideApiClient apiClient;
    private final NutritionService nutritionService;
    private final RecommendationService recommendationService;

    // Data models loaded via Services or API
    private BigDecimal bmi;
    private DailyNutritionSummary dailySummary;
    private NutritionGoal activeGoal;
    private ArrayList<Recommendation> recommendations;

    // Dynamic containers for smooth async UI updating
    private JPanel goalContainer;
    private JPanel recContainer;
    private boolean isLoading = false;
    private boolean bmiLoadFailed = false;
    private boolean summaryLoadFailed = false;
    private boolean recsLoadFailed = false;
    private String partialStatusMessage = null;

    // UI Labels for verification
    private JLabel welcomeLabel;
    private JLabel userDetailsLabel;
    private JLabel statusNoticeLabel;
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
        this(user, NutriGuideApiClient.getInstance(), null, null);
    }

    public DashboardFrame(User user, NutriGuideApiClient apiClient) {
        this(user, apiClient, null, null);
    }

    /**
     * For test and demo harness execution only: allows direct service injection
     * without requiring a running localhost HTTP backend server.
     *
     * @param user Authenticated user model
     * @param nutritionService Directly injected NutritionService for offline/integration testing
     * @param recommendationService Directly injected RecommendationService for offline/integration testing
     */
    public DashboardFrame(User user, NutritionService nutritionService, RecommendationService recommendationService) {
        this(user, null, nutritionService, recommendationService);
    }

    /**
     * For test and demo harness execution only: allows explicit injection of both
     * client and service implementations.
     *
     * @param user Authenticated user model
     * @param apiClient API client implementation or null if operating in test harness mode
     * @param nutritionService Directly injected NutritionService or null for standard client mode
     * @param recommendationService Directly injected RecommendationService or null for standard client mode
     */
    public DashboardFrame(User user, NutriGuideApiClient apiClient, NutritionService nutritionService, RecommendationService recommendationService) {
        this.user = user;
        this.apiClient = apiClient;
        this.nutritionService = nutritionService;
        this.recommendationService = recommendationService;

        // If test harness injected direct services without an API client, load synchronously for test harness assertions
        if (apiClient == null && (nutritionService != null || recommendationService != null)) {
            this.isLoading = false;
            loadDashboardData();
        } else {
            this.isLoading = true;
        }

        initializeUI();

        // Standard application execution: load API data asynchronously using SwingWorker
        if (apiClient != null) {
            loadDashboardDataAsync();
        } else if (nutritionService == null && recommendationService == null) {
            this.isLoading = false;
            this.bmiLoadFailed = true;
            this.summaryLoadFailed = true;
            this.recsLoadFailed = true;
            this.partialStatusMessage = "NutriGuide Localhost API Client is not connected, and direct database service fallback is disabled.";
            updateAllDisplays();
            if (!GraphicsEnvironment.isHeadless() && isVisible()) {
                UITheme.showError(this,
                        "NutriGuide Localhost API Client is not connected, and direct database service fallback is disabled.",
                        "Connection Unavailable");
            }
        }
    }

    /**
     * For unit test and demo verification only: programmatically simulates partial request failure states.
     *
     * @param bmiFailed Whether BMI request failed
     * @param summaryFailed Whether daily nutrition summary & goal request failed
     * @param recsFailed Whether recommendations request failed
     */
    public void setPartialFailureForTesting(boolean bmiFailed, boolean summaryFailed, boolean recsFailed) {
        this.isLoading = false;
        this.bmiLoadFailed = bmiFailed;
        this.summaryLoadFailed = summaryFailed;
        this.recsLoadFailed = recsFailed;

        if (bmiFailed) {
            this.bmi = null;
        }
        if (summaryFailed) {
            this.dailySummary = null;
            this.activeGoal = null;
        }
        if (recsFailed) {
            this.recommendations = null;
        }

        if (bmiLoadFailed || summaryLoadFailed || recsLoadFailed) {
            StringBuilder sb = new StringBuilder("Notice: ");
            boolean first = true;
            if (bmiLoadFailed) {
                sb.append("BMI calculation failed");
                first = false;
            }
            if (summaryLoadFailed) {
                if (!first) sb.append("; ");
                sb.append("Daily nutrition summary and goal request failed");
                first = false;
            }
            if (recsLoadFailed) {
                if (!first) sb.append("; ");
                sb.append("Recommendations request failed");
            }
            sb.append(". Showing available profile data.");
            this.partialStatusMessage = sb.toString();
        } else {
            this.partialStatusMessage = null;
        }

        updateAllDisplays();
    }

    /**
     * Data carrier container for background dashboard loading.
     */
    private static class DashboardData {
        private User profile;
        private BigDecimal bmi;
        private DailyNutritionSummary dailySummary;
        private NutritionGoal activeGoal;
        private ArrayList<Recommendation> recommendations;
        private boolean backendAvailable = true;
        private String errorMessage;
        private boolean bmiLoadFailed = false;
        private boolean summaryLoadFailed = false;
        private boolean recsLoadFailed = false;
    }

    /**
     * Asynchronously loads dashboard profile, BMI, daily summary, and recommendations
     * using SwingWorker to keep the Swing Event Dispatch Thread (EDT) completely responsive.
     */
    public void loadDashboardDataAsync() {
        this.isLoading = true;
        this.bmiLoadFailed = false;
        this.summaryLoadFailed = false;
        this.recsLoadFailed = false;
        this.partialStatusMessage = null;
        updateAllDisplays();

        SwingWorker<DashboardData, Void> worker = new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardData data = new DashboardData();
                LocalDate today = LocalDate.now();

                if (apiClient == null || !apiClient.isAuthenticated()) {
                    data.backendAvailable = false;
                    data.errorMessage = "NutriGuide API client is not authenticated.";
                    return data;
                }

                // 1. Fetch profile
                try {
                    data.profile = apiClient.getProfile();
                } catch (Exception ex) {
                    data.backendAvailable = false;
                    data.errorMessage = ex.getMessage();
                    return data;
                }

                // 2. Calculate BMI
                try {
                    data.bmi = apiClient.calculateBmi();
                    data.bmiLoadFailed = false;
                } catch (Exception ex) {
                    data.bmi = null;
                    data.bmiLoadFailed = true;
                }

                // 3. Load daily nutrition summary and goal
                try {
                    Map<String, Object> daily = apiClient.getDailyReport(today);
                    if (daily != null) {
                        data.dailySummary = (DailyNutritionSummary) daily.get("summary");
                        data.activeGoal = (NutritionGoal) daily.get("activeGoal");
                    }
                    data.summaryLoadFailed = false;
                } catch (Exception ex) {
                    data.dailySummary = null;
                    data.activeGoal = null;
                    data.summaryLoadFailed = true;
                }

                // 4. Load recommendations
                try {
                    data.recommendations = apiClient.getRecommendations(today);
                    data.recsLoadFailed = false;
                } catch (Exception ex) {
                    data.recommendations = null;
                    data.recsLoadFailed = true;
                }

                return data;
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    applyDashboardData(data);
                } catch (Exception ex) {
                    DashboardData fallback = new DashboardData();
                    fallback.backendAvailable = false;
                    fallback.errorMessage = ex.getMessage();
                    applyDashboardData(fallback);
                }
            }
        };

        worker.execute();
    }

    /**
     * Applies loaded dashboard data and updates Swing components exclusively on the EDT.
     */
    private void applyDashboardData(DashboardData data) {
        this.isLoading = false;

        if (data == null || !data.backendAvailable) {
            this.dailySummary = null;
            this.recommendations = null;
            this.bmi = null;
            this.activeGoal = null;
            this.bmiLoadFailed = true;
            this.summaryLoadFailed = true;
            this.recsLoadFailed = true;
            this.partialStatusMessage = "NutriGuide backend server is unavailable. Offline mode.";
            updateAllDisplays();

            String msg = (data != null && data.errorMessage != null)
                    ? data.errorMessage
                    : "NutriGuide backend server is unreachable.";
            UITheme.showWarning(this,
                    "NutriGuide backend server is unavailable (" + msg + "). Dashboard is in offline mode.",
                    "Backend Unavailable");
            return;
        }

        if (data.profile != null && user != null) {
            user.setName(data.profile.getName());
            user.setAge(data.profile.getAge());
            user.setGender(data.profile.getGender());
            user.setHeightCm(data.profile.getHeightCm());
            user.setWeightKg(data.profile.getWeightKg());
            user.setActivityLevel(data.profile.getActivityLevel());
        }

        this.bmi = data.bmi;
        this.dailySummary = data.dailySummary;
        this.activeGoal = data.activeGoal;
        this.recommendations = data.recommendations;
        this.bmiLoadFailed = data.bmiLoadFailed;
        this.summaryLoadFailed = data.summaryLoadFailed;
        this.recsLoadFailed = data.recsLoadFailed;

        if (bmiLoadFailed || summaryLoadFailed || recsLoadFailed) {
            StringBuilder sb = new StringBuilder("Notice: ");
            boolean first = true;
            if (bmiLoadFailed) {
                sb.append("BMI calculation failed");
                first = false;
            }
            if (summaryLoadFailed) {
                if (!first) sb.append("; ");
                sb.append("Daily nutrition summary and goal request failed");
                first = false;
            }
            if (recsLoadFailed) {
                if (!first) sb.append("; ");
                sb.append("Recommendations request failed");
            }
            sb.append(". Showing available profile data.");
            this.partialStatusMessage = sb.toString();
        } else {
            this.partialStatusMessage = null;
        }

        updateAllDisplays();
    }

    /**
     * Loads live data by delegating to Localhost API client or Service Layer.
     * Consolidates backend errors into a single clear warning message.
     */
    public void loadDashboardData() {
        if (user == null) {
            return;
        }

        LocalDate today = LocalDate.now();

        // 1. Primary path: Fetch via NutriGuide Localhost API Client
        if (apiClient != null && apiClient.isAuthenticated()) {
            boolean serverFailed = false;
            String errorMsg = null;
            try {
                User fresh = apiClient.getProfile();
                if (fresh != null) {
                    this.user.setName(fresh.getName());
                    this.user.setAge(fresh.getAge());
                    this.user.setGender(fresh.getGender());
                    this.user.setHeightCm(fresh.getHeightCm());
                    this.user.setWeightKg(fresh.getWeightKg());
                    this.user.setActivityLevel(fresh.getActivityLevel());
                }
            } catch (Exception ex) {
                serverFailed = true;
                errorMsg = ex.getMessage();
            }

            if (!serverFailed) {
                try {
                    this.bmi = apiClient.calculateBmi();
                    this.bmiLoadFailed = false;
                } catch (Exception ex) {
                    this.bmi = null;
                    this.bmiLoadFailed = true;
                }

                try {
                    Map<String, Object> daily = apiClient.getDailyReport(today);
                    if (daily != null) {
                        this.dailySummary = (DailyNutritionSummary) daily.get("summary");
                        this.activeGoal = (NutritionGoal) daily.get("activeGoal");
                    }
                    this.summaryLoadFailed = false;
                } catch (Exception ex) {
                    this.dailySummary = null;
                    this.activeGoal = null;
                    this.summaryLoadFailed = true;
                }

                try {
                    this.recommendations = apiClient.getRecommendations(today);
                    this.recsLoadFailed = false;
                } catch (Exception ex) {
                    this.recommendations = null;
                    this.recsLoadFailed = true;
                }

                if (bmiLoadFailed || summaryLoadFailed || recsLoadFailed) {
                    StringBuilder sb = new StringBuilder("Notice: ");
                    boolean first = true;
                    if (bmiLoadFailed) {
                        sb.append("BMI calculation failed");
                        first = false;
                    }
                    if (summaryLoadFailed) {
                        if (!first) sb.append("; ");
                        sb.append("Daily nutrition summary and goal request failed");
                        first = false;
                    }
                    if (recsLoadFailed) {
                        if (!first) sb.append("; ");
                        sb.append("Recommendations request failed");
                    }
                    sb.append(". Showing available profile data.");
                    this.partialStatusMessage = sb.toString();
                } else {
                    this.partialStatusMessage = null;
                }
            } else {
                this.bmi = null;
                this.dailySummary = null;
                this.activeGoal = null;
                this.recommendations = null;
                this.bmiLoadFailed = true;
                this.summaryLoadFailed = true;
                this.recsLoadFailed = true;
                this.partialStatusMessage = "NutriGuide backend server is unavailable. Offline mode.";
                UITheme.showWarning(this,
                        "NutriGuide backend server is unavailable (" + errorMsg + "). Dashboard is in offline mode.",
                        "Backend Unavailable");
            }

            if (this.dailySummary == null && !this.summaryLoadFailed) {
                this.dailySummary = new DailyNutritionSummary();
            }
            if (this.recommendations == null && !this.recsLoadFailed) {
                this.recommendations = new ArrayList<Recommendation>();
            }
            return;
        }

        // Standard desktop execution guard: prevent fallback to direct database service calls when API client is absent
        if (nutritionService == null && recommendationService == null) {
            UITheme.showError(this,
                    "NutriGuide Localhost API Client is not connected, and direct database service fallback is disabled.",
                    "Connection Unavailable");
            this.bmi = null;
            this.dailySummary = null;
            this.activeGoal = null;
            this.recommendations = null;
            this.bmiLoadFailed = true;
            this.summaryLoadFailed = true;
            this.recsLoadFailed = true;
            this.partialStatusMessage = "NutriGuide Localhost API Client is not connected, and direct database service fallback is disabled.";
            return;
        }

        // 2. Fallback path for unit tests / direct service demonstration
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
        } catch (Exception ex) {
            UITheme.showWarning(this, "Could not refresh user profile: " + ex.getMessage(), "Profile Warning");
        }

        // Calculate BMI via NutritionService (MySQL stored function calculate_bmi)
        try {
            if (user.getWeightKg() != null && user.getHeightCm() != null && nutritionService != null) {
                this.bmi = nutritionService.calculateBmi(user.getWeightKg(), user.getHeightCm());
                this.bmiLoadFailed = false;
            } else {
                this.bmi = null;
                this.bmiLoadFailed = false;
            }
        } catch (ValidationException ex) {
            this.bmi = null;
            this.bmiLoadFailed = true;
            JOptionPane.showMessageDialog(this,
                    "Invalid height/weight data: " + ex.getMessage(),
                    "BMI Calculation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseOperationException ex) {
            this.bmi = null;
            this.bmiLoadFailed = true;
            JOptionPane.showMessageDialog(this,
                    "Database error while calculating BMI: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Retrieve Today's Nutrition Summary via NutritionService
        try {
            if (user.getUserId() != null && nutritionService != null) {
                this.dailySummary = nutritionService.getDailySummaryFromView(user.getUserId(), today);
                this.summaryLoadFailed = false;
            }
        } catch (DatabaseOperationException ex) {
            this.dailySummary = null;
            this.summaryLoadFailed = true;
            JOptionPane.showMessageDialog(this,
                    "Could not load today's nutrition summary: " + ex.getMessage(),
                    "Data Load Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
        if (this.dailySummary == null && !this.summaryLoadFailed) {
            this.dailySummary = new DailyNutritionSummary();
        }

        // Retrieve Active Goal via NutritionService
        try {
            if (user.getUserId() != null && nutritionService != null) {
                this.activeGoal = nutritionService.getActiveGoal(user.getUserId());
            }
        } catch (ResourceNotFoundException ex) {
            this.activeGoal = null;
        } catch (DatabaseOperationException ex) {
            this.activeGoal = null;
            this.summaryLoadFailed = true;
            JOptionPane.showMessageDialog(this,
                    "Could not load active nutrition goal: " + ex.getMessage(),
                    "Data Load Warning",
                    JOptionPane.WARNING_MESSAGE);
        }

        // Retrieve Today's Recommendations via RecommendationService
        try {
            if (user.getUserId() != null && recommendationService != null) {
                this.recommendations = recommendationService.generateRecommendations(user.getUserId(), today);
                this.recsLoadFailed = false;
            }
        } catch (Exception ex) {
            this.recommendations = null;
            this.recsLoadFailed = true;
            JOptionPane.showMessageDialog(this,
                    "Could not load recommendations: " + ex.getMessage(),
                    "Recommendation Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
        if (this.recommendations == null && !this.recsLoadFailed) {
            this.recommendations = new ArrayList<Recommendation>();
        }
    }

    private void initializeUI() {
        setTitle("NutriGuide Dashboard - " + (user != null ? user.getName() : "User"));
        UITheme.configureFrame(this, 1024, 720, 850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));

        // ====================================================================
        // Section 1: Header Panel (North)
        // ====================================================================
        JPanel headerPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        headerPanel.setBackground(UITheme.CARD_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 20, 12, 20)
        ));

        String userName = (user != null && user.getName() != null) ? user.getName() : "User";
        welcomeLabel = new JLabel("Welcome, " + userName + "!");
        welcomeLabel.setFont(UITheme.FONT_TITLE);
        welcomeLabel.setForeground(UITheme.PRIMARY_DARK);

        String email = (user != null && user.getEmail() != null) ? user.getEmail() : "N/A";
        String age = (user != null && user.getAge() != null) ? user.getAge() + " yrs" : "N/A";
        String gender = (user != null && user.getGender() != null) ? user.getGender() : "N/A";
        String activity = (user != null && user.getActivityLevel() != null) ? user.getActivityLevel() : "N/A";

        userDetailsLabel = new JLabel("Email: " + email + "  |  Age: " + age + "  |  Gender: " + gender + "  |  Activity: " + activity);
        userDetailsLabel.setFont(UITheme.FONT_REGULAR);
        userDetailsLabel.setForeground(UITheme.TEXT_MUTED);

        statusNoticeLabel = new JLabel();
        statusNoticeLabel.setFont(UITheme.FONT_REGULAR);
        statusNoticeLabel.setForeground(UITheme.DANGER_COLOR);
        statusNoticeLabel.setVisible(false);

        headerPanel.add(welcomeLabel);
        headerPanel.add(userDetailsLabel);
        headerPanel.add(statusNoticeLabel);
        add(headerPanel, BorderLayout.NORTH);

        // ====================================================================
        // Section 2: Center Content Panel (Inside JScrollPane for all resolutions)
        // ====================================================================
        // Section 2: Center Content Panel (Inside JScrollPane for all resolutions)
        // ====================================================================
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(UITheme.BG_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel cardsGrid = new JPanel(new GridLayout(2, 2, 14, 14));
        cardsGrid.setBackground(UITheme.BG_COLOR);

        // Card A: Health Profile & BMI Section
        JPanel bmiPanel = new JPanel(new BorderLayout(8, 8));
        bmiPanel.setBackground(UITheme.CARD_BG);
        bmiPanel.setBorder(UITheme.createTitledCardBorder("User Health Profile & BMI Summary"));

        physicalStatsLabel = new JLabel();
        physicalStatsLabel.setFont(UITheme.FONT_REGULAR);
        physicalStatsLabel.setForeground(UITheme.TEXT_MAIN);

        bmiLabel = new JLabel();
        bmiLabel.setFont(UITheme.FONT_METRIC);
        bmiLabel.setForeground(UITheme.PRIMARY_COLOR);

        JPanel bmiStatsSubPanel = new JPanel(new GridLayout(2, 1, 4, 8));
        bmiStatsSubPanel.setBackground(UITheme.CARD_BG);
        bmiStatsSubPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        bmiStatsSubPanel.add(physicalStatsLabel);
        bmiStatsSubPanel.add(bmiLabel);
        bmiPanel.add(bmiStatsSubPanel, BorderLayout.CENTER);
        cardsGrid.add(bmiPanel);

        // Card B: Daily Target (Active Nutrition Goal)
        JPanel goalPanel = new JPanel(new BorderLayout(8, 8));
        goalPanel.setBackground(UITheme.CARD_BG);
        goalPanel.setBorder(UITheme.createTitledCardBorder("Daily Target (Active Nutrition Goal)"));

        targetCaloriesLabel = new JLabel("Target Cal: N/A");
        targetCaloriesLabel.setFont(UITheme.FONT_BOLD);
        targetCaloriesLabel.setForeground(UITheme.PRIMARY_DARK);

        targetProteinLabel = new JLabel("Target Protein: N/A");
        targetProteinLabel.setFont(UITheme.FONT_BOLD);
        targetProteinLabel.setForeground(UITheme.PRIMARY_DARK);

        targetCarbsLabel = new JLabel("Target Carbs: N/A");
        targetCarbsLabel.setFont(UITheme.FONT_BOLD);
        targetCarbsLabel.setForeground(UITheme.PRIMARY_DARK);

        targetFatLabel = new JLabel("Target Fat: N/A");
        targetFatLabel.setFont(UITheme.FONT_BOLD);
        targetFatLabel.setForeground(UITheme.PRIMARY_DARK);

        noGoalLabel = new JLabel("No active goal set. Use 'Nutrition Goal' below to define your daily targets.");
        noGoalLabel.setFont(UITheme.FONT_REGULAR);
        noGoalLabel.setForeground(UITheme.TEXT_MUTED);

        goalContainer = new JPanel(new BorderLayout());
        goalContainer.setBackground(UITheme.CARD_BG);
        goalPanel.add(goalContainer, BorderLayout.CENTER);
        cardsGrid.add(goalPanel);

        // Card C: Today's Actual Nutrition Intake
        LocalDate today = LocalDate.now();
        JPanel nutritionPanel = new JPanel(new BorderLayout(8, 8));
        nutritionPanel.setBackground(UITheme.CARD_BG);
        nutritionPanel.setBorder(UITheme.createTitledCardBorder("Today's Actual Intake (" + today + ")"));

        JPanel intakeGrid = new JPanel(new GridLayout(2, 2, 10, 8));
        intakeGrid.setBackground(UITheme.CARD_BG);
        intakeGrid.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        caloriesLabel = new JLabel();
        caloriesLabel.setFont(UITheme.FONT_BOLD);
        caloriesLabel.setForeground(UITheme.TEXT_MAIN);

        proteinLabel = new JLabel();
        proteinLabel.setFont(UITheme.FONT_BOLD);
        proteinLabel.setForeground(UITheme.TEXT_MAIN);

        carbsLabel = new JLabel();
        carbsLabel.setFont(UITheme.FONT_BOLD);
        carbsLabel.setForeground(UITheme.TEXT_MAIN);

        fatLabel = new JLabel();
        fatLabel.setFont(UITheme.FONT_BOLD);
        fatLabel.setForeground(UITheme.TEXT_MAIN);

        intakeGrid.add(caloriesLabel);
        intakeGrid.add(proteinLabel);
        intakeGrid.add(carbsLabel);
        intakeGrid.add(fatLabel);
        nutritionPanel.add(intakeGrid, BorderLayout.CENTER);
        cardsGrid.add(nutritionPanel);

        // Card D: Today's Recommendations Preview (Up to 3 rules)
        JPanel recPanel = new JPanel(new BorderLayout(8, 8));
        recPanel.setBackground(UITheme.CARD_BG);
        recPanel.setBorder(UITheme.createTitledCardBorder("Today's Dietary Recommendations"));

        recContainer = new JPanel(new BorderLayout());
        recContainer.setBackground(UITheme.CARD_BG);
        recPanel.add(recContainer, BorderLayout.CENTER);
        cardsGrid.add(recPanel);

        // Populate card contents according to current loading/model state
        updateAllDisplays();

        centerPanel.add(cardsGrid, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.getViewport().setBackground(UITheme.BG_COLOR);
        add(scrollPane, BorderLayout.CENTER);

        // ====================================================================
        // Section 3: Navigation Buttons Panel (South)
        // ====================================================================
        mealLogButton = new JButton("Meal Log");
        goalButton = new JButton("Nutrition Goal");
        recButton = new JButton("Recommendations");
        reportsButton = new JButton("Reports");
        profileButton = new JButton("Profile");
        logoutButton = new JButton("Logout");

        JPanel buttonPanel = UITheme.createDashboardNavBar(
                mealLogButton, goalButton, recButton, reportsButton, profileButton, logoutButton);
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
        ProfileFrame profileFrame = new ProfileFrame(user, apiClient);
        UITheme.transition(this, profileFrame);
    }

    /**
     * Disposes the dashboard and opens ReportFrame for the logged-in User.
     */
    public void openReportFrame() {
        ReportFrame reportFrame = new ReportFrame(user, apiClient);
        UITheme.transition(this, reportFrame);
    }

    /**
     * Disposes the dashboard and opens RecommendationFrame for the logged-in User.
     */
    public void openRecommendationFrame() {
        RecommendationFrame recFrame = new RecommendationFrame(user, apiClient);
        UITheme.transition(this, recFrame);
    }

    /**
     * Disposes the dashboard and opens NutritionGoalFrame for the logged-in User.
     */
    public void openNutritionGoalFrame() {
        NutritionGoalFrame goalFrame = new NutritionGoalFrame(user, apiClient);
        UITheme.transition(this, goalFrame);
    }

    /**
     * Disposes the dashboard and opens MealLogFrame for the logged-in User.
     */
    public void openMealLogFrame() {
        MealLogFrame mealLogFrame = new MealLogFrame(user, apiClient);
        UITheme.transition(this, mealLogFrame);
    }

    /**
     * Handles logout by disposing the dashboard and opening LoginFrame.
     */
    public void handleLogout() {
        if (apiClient != null) {
            apiClient.logout();
        }
        LoginFrame loginFrame = new LoginFrame(apiClient);
        UITheme.transition(this, loginFrame);
    }

    /**
     * Refreshes the dashboard data and UI displays.
     */
    public void refreshDashboardData() {
        if (apiClient != null) {
            loadDashboardDataAsync();
        } else {
            loadDashboardData();
            updateAllDisplays();
        }
    }

    /**
     * Updates all UI displays and cards with current model data on the EDT.
     */
    public void updateAllDisplays() {
        updateProfileAndBmiDisplay();
        updateNutritionDisplay();
        updateGoalDisplay();
        updateRecommendationsDisplay();
        updateStatusNoticeDisplay();
    }

    private void updateStatusNoticeDisplay() {
        if (statusNoticeLabel == null) {
            return;
        }
        if (partialStatusMessage != null && !partialStatusMessage.trim().isEmpty()) {
            statusNoticeLabel.setText(partialStatusMessage);
            statusNoticeLabel.setVisible(true);
        } else {
            statusNoticeLabel.setText("");
            statusNoticeLabel.setVisible(false);
        }
    }

    private void updateProfileAndBmiDisplay() {
        if (welcomeLabel != null && user != null) {
            String userName = user.getName() != null ? user.getName() : "User";
            welcomeLabel.setText("Welcome, " + userName + "!");
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
            if (isLoading) {
                bmiLabel.setText("BMI: Loading...");
                bmiLabel.setForeground(UITheme.PRIMARY_COLOR);
            } else if (bmiLoadFailed) {
                bmiLabel.setText("BMI: Request failed");
                bmiLabel.setForeground(UITheme.DANGER_COLOR);
            } else {
                bmiLabel.setText("BMI: " + (bmi != null ? bmi.toPlainString() : "N/A"));
                bmiLabel.setForeground(UITheme.PRIMARY_COLOR);
            }
        }
    }

    private void updateNutritionDisplay() {
        if (caloriesLabel == null || proteinLabel == null || carbsLabel == null || fatLabel == null) {
            return;
        }
        if (isLoading) {
            caloriesLabel.setText("Calories: Loading...");
            proteinLabel.setText("Protein: Loading...");
            carbsLabel.setText("Carbs: Loading...");
            fatLabel.setText("Fat: Loading...");
            caloriesLabel.setForeground(UITheme.PRIMARY_COLOR);
            proteinLabel.setForeground(UITheme.TEXT_MAIN);
            carbsLabel.setForeground(UITheme.TEXT_MAIN);
            fatLabel.setForeground(UITheme.TEXT_MAIN);
        } else if (summaryLoadFailed) {
            caloriesLabel.setText("Calories: Request failed");
            proteinLabel.setText("Protein: Request failed");
            carbsLabel.setText("Carbs: Request failed");
            fatLabel.setText("Fat: Request failed");
            caloriesLabel.setForeground(UITheme.DANGER_COLOR);
            proteinLabel.setForeground(UITheme.DANGER_COLOR);
            carbsLabel.setForeground(UITheme.DANGER_COLOR);
            fatLabel.setForeground(UITheme.DANGER_COLOR);
        } else {
            BigDecimal cal = (dailySummary != null && dailySummary.getDailyCalories() != null)
                    ? dailySummary.getDailyCalories() : BigDecimal.ZERO;
            BigDecimal prot = (dailySummary != null && dailySummary.getDailyProtein() != null)
                    ? dailySummary.getDailyProtein() : BigDecimal.ZERO;
            BigDecimal carb = (dailySummary != null && dailySummary.getDailyCarbs() != null)
                    ? dailySummary.getDailyCarbs() : BigDecimal.ZERO;
            BigDecimal fat = (dailySummary != null && dailySummary.getDailyFat() != null)
                    ? dailySummary.getDailyFat() : BigDecimal.ZERO;

            caloriesLabel.setText("Calories: " + cal.toPlainString() + " kcal");
            proteinLabel.setText("Protein: " + prot.toPlainString() + " g");
            carbsLabel.setText("Carbs: " + carb.toPlainString() + " g");
            fatLabel.setText("Fat: " + fat.toPlainString() + " g");
            caloriesLabel.setForeground(UITheme.PRIMARY_COLOR);
            proteinLabel.setForeground(UITheme.TEXT_MAIN);
            carbsLabel.setForeground(UITheme.TEXT_MAIN);
            fatLabel.setForeground(UITheme.TEXT_MAIN);
        }
    }

    private void updateGoalDisplay() {
        if (goalContainer == null) {
            return;
        }
        goalContainer.removeAll();
        if (activeGoal != null && !summaryLoadFailed) {
            JPanel goalGrid = new JPanel(new GridLayout(2, 2, 10, 8));
            goalGrid.setBackground(UITheme.CARD_BG);
            goalGrid.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

            targetCaloriesLabel.setText("Target Cal: " + activeGoal.getTargetCalories() + " kcal");
            targetProteinLabel.setText("Target Protein: " + activeGoal.getTargetProtein() + " g");
            targetCarbsLabel.setText("Target Carbs: " + activeGoal.getTargetCarbs() + " g");
            targetFatLabel.setText("Target Fat: " + activeGoal.getTargetFats() + " g");

            goalGrid.add(targetCaloriesLabel);
            goalGrid.add(targetProteinLabel);
            goalGrid.add(targetCarbsLabel);
            goalGrid.add(targetFatLabel);
            goalContainer.add(goalGrid, BorderLayout.CENTER);
        } else {
            JPanel emptyGoalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
            emptyGoalPanel.setBackground(UITheme.CARD_BG);
            if (isLoading) {
                noGoalLabel.setText("Loading daily targets...");
                noGoalLabel.setForeground(UITheme.TEXT_MUTED);
            } else if (summaryLoadFailed) {
                noGoalLabel.setText("Failed to load daily goal from server.");
                noGoalLabel.setForeground(UITheme.DANGER_COLOR);
            } else {
                noGoalLabel.setText("No active goal set. Use 'Nutrition Goal' below to define your daily targets.");
                noGoalLabel.setForeground(UITheme.TEXT_MUTED);
            }
            emptyGoalPanel.add(noGoalLabel);
            goalContainer.add(emptyGoalPanel, BorderLayout.CENTER);
        }
        goalContainer.revalidate();
        goalContainer.repaint();
    }

    private void updateRecommendationsDisplay() {
        if (recContainer == null) {
            return;
        }
        recContainer.removeAll();
        JPanel recListPanel = new JPanel(new GridLayout(3, 1, 4, 6));
        recListPanel.setBackground(UITheme.CARD_BG);
        recListPanel.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        if (isLoading) {
            JLabel loadingRecLabel = new JLabel("- Loading recommendations...");
            loadingRecLabel.setFont(UITheme.FONT_REGULAR);
            loadingRecLabel.setForeground(UITheme.TEXT_MUTED);
            recListPanel.add(loadingRecLabel);
            recListPanel.add(new JLabel(""));
            recListPanel.add(new JLabel(""));
        } else if (recsLoadFailed) {
            JLabel failedRecLabel = new JLabel("- Failed to load recommendations from server.");
            failedRecLabel.setFont(UITheme.FONT_REGULAR);
            failedRecLabel.setForeground(UITheme.DANGER_COLOR);
            recListPanel.add(failedRecLabel);
            recListPanel.add(new JLabel(""));
            recListPanel.add(new JLabel(""));
        } else if (recommendations != null && !recommendations.isEmpty()) {
            int count = Math.min(3, recommendations.size());
            for (int i = 0; i < count; i++) {
                Recommendation rec = recommendations.get(i);
                JLabel recLabel = new JLabel("- [" + rec.getRecType() + "] " + rec.getMessage());
                recLabel.setFont(UITheme.FONT_REGULAR);
                recLabel.setForeground(UITheme.TEXT_MAIN);
                recListPanel.add(recLabel);
            }
            for (int i = count; i < 3; i++) {
                recListPanel.add(new JLabel(""));
            }
        } else {
            JLabel noRecLabel = new JLabel("- No dietary recommendations generated for today.");
            noRecLabel.setFont(UITheme.FONT_REGULAR);
            noRecLabel.setForeground(UITheme.TEXT_MUTED);
            recListPanel.add(noRecLabel);
            recListPanel.add(new JLabel(""));
            recListPanel.add(new JLabel(""));
        }
        recContainer.add(recListPanel, BorderLayout.CENTER);
        recContainer.revalidate();
        recContainer.repaint();
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

    public boolean isBmiLoadFailed() {
        return bmiLoadFailed;
    }

    public boolean isSummaryLoadFailed() {
        return summaryLoadFailed;
    }

    public boolean isRecsLoadFailed() {
        return recsLoadFailed;
    }

    public String getPartialStatusMessage() {
        return partialStatusMessage;
    }

    public JLabel getStatusNoticeLabel() {
        return statusNoticeLabel;
    }
}
