package com.nutriguide.gui;

import com.nutriguide.client.NutriGuideApiClient;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.Meal;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.User;
import com.nutriguide.server.MealHandler;
import com.nutriguide.service.NutritionService;
import com.nutriguide.service.ReportService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Map;

/**
 * ReportFrame: Swing GUI for viewing daily nutrition summaries, target comparisons,
 * generated text reports, and nutrition history across date ranges.
 *
 * Demonstrates:
 * - Decoupled client communicating with localhost REST API (with direct service fallback)
 * - SwingWorker for background report loading and history aggregation
 * - Exp 6: Anonymous Inner Classes for ActionListeners
 * - Exp 17-18: Swing UI Components (JFrame, JPanel, JLabel, JTextField, JButton, JTable, JTextArea, JTabbedPane)
 * - Exp 7 (DBMS): Calling stored procedure 'get_daily_summary' via ReportService
 * - Exp 4 (DBMS): Calling database view 'daily_nutrition_summary' via ReportService for history
 * - Layered Architecture: GUI delegates directly to ReportService and NutritionService
 *   (Strictly ZERO java.sql imports and ZERO SQL strings in GUI)
 */
public class ReportFrame extends JFrame {

    private final User user;
    private final NutriGuideApiClient apiClient;
    private final ReportService reportService;
    private final NutritionService nutritionService;

    // Current State
    private LocalDate selectedDate;
    private DailyNutritionSummary dailySummary;
    private NutritionGoal activeGoal;

    // Top Controls
    private JTextField dateField;
    private JButton viewDailyReportButton;
    private JButton refreshButton;
    private JButton backButton;

    // Daily Report Intake Labels
    private JLabel selectedDateLabel;
    private JLabel caloriesLabel;
    private JLabel proteinLabel;
    private JLabel carbsLabel;
    private JLabel fatLabel;

    // Target Comparison Labels
    private JLabel goalStatusLabel;
    private JLabel targetCaloriesLabel;
    private JLabel targetProteinLabel;
    private JLabel targetCarbsLabel;
    private JLabel targetFatLabel;

    // Intake vs Target Difference Labels
    private JLabel diffCaloriesLabel;
    private JLabel diffProteinLabel;
    private JLabel diffCarbsLabel;
    private JLabel diffFatLabel;

    // Report Text Area (Tab 2 Plain-Text View)
    private JTextArea reportTextArea;

    // Structured Report Presentation Components (Tab 1 Executive View)
    private JTabbedPane rightTabbedPane;
    private JLabel structHeaderDateLabel;
    private JLabel structNameLabel;
    private JLabel structEmailLabel;
    private JLabel structAgeGenderLabel;
    private JLabel structHeightWeightLabel;
    private JLabel structActivityLabel;
    private JLabel structBmiLabel;
    private MetricTile structCalTile;
    private MetricTile structProtTile;
    private MetricTile structCarbTile;
    private MetricTile structFatTile;
    private JLabel structGoalBannerLabel;
    private JTable structCompTable;
    private DefaultTableModel structCompTableModel;
    private JLabel structMealsHeaderLabel;
    private JTable structMealsTable;
    private DefaultTableModel structMealsTableModel;

    // History Section
    private JTextField fromDateField;
    private JTextField toDateField;
    private JButton loadHistoryButton;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;

    private static class MetricTile {
        JPanel panel;
        JLabel valueLabel;
        JLabel unitLabel;
    }

    public ReportFrame(User user) {
        this(user, NutriGuideApiClient.getInstance(), null, null);
    }

    public ReportFrame(User user, NutriGuideApiClient apiClient) {
        this(user, apiClient, null, null);
    }

    /**
     * For test and demo harness execution only: allows direct service injection
     * without requiring a running localhost HTTP backend server.
     *
     * @param user Active user model
     * @param reportService Directly injected ReportService for offline/integration testing
     * @param nutritionService Directly injected NutritionService for offline/integration testing
     */
    public ReportFrame(User user, ReportService reportService, NutritionService nutritionService) {
        this(user, null, reportService, nutritionService);
    }

    /**
     * For test and demo harness execution only: allows explicit injection of both
     * client and service implementations.
     *
     * @param user Active user model
     * @param apiClient API client implementation or null if operating in test harness mode
     * @param reportService Directly injected ReportService or null for standard client mode
     * @param nutritionService Directly injected NutritionService or null for standard client mode
     */
    public ReportFrame(User user, NutriGuideApiClient apiClient, ReportService reportService, NutritionService nutritionService) {
        this.user = user;
        this.apiClient = apiClient;
        this.reportService = reportService;
        this.nutritionService = nutritionService;
        this.selectedDate = LocalDate.now();

        initializeUI();
        loadDailyReport(selectedDate);
        loadNutritionHistory(selectedDate.minusDays(30), selectedDate);
    }

    private void initializeUI() {
        setTitle("NutriGuide - Nutrition Reports");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        UITheme.configureFrame(this, 1024, 720, 850, 600);
        getContentPane().setBackground(UITheme.COLOR_BG);
        setLayout(new BorderLayout(12, 12));

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_CARD);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLabel = new JLabel("Nutrition Reports & History");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.COLOR_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        String userDesc = (user != null) ? "User: " + user.getName() + " (" + user.getEmail() + ")" : "User: Guest";
        JLabel userLabel = new JLabel(userDesc);
        userLabel.setFont(UITheme.FONT_REGULAR);
        userLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        headerPanel.add(userLabel, BorderLayout.EAST);

        // 2. Date Selection Toolbar
        JPanel topToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        topToolBar.setBackground(UITheme.COLOR_BG);
        topToolBar.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        JLabel datePromptLabel = new JLabel("Date (YYYY-MM-DD):");
        datePromptLabel.setFont(UITheme.FONT_BOLD);
        datePromptLabel.setForeground(UITheme.COLOR_TEXT_MAIN);
        topToolBar.add(datePromptLabel);

        dateField = new JTextField(selectedDate.toString(), 10);
        UITheme.styleTextField(dateField);
        topToolBar.add(dateField);

        viewDailyReportButton = new JButton("View Daily Report");
        UITheme.stylePrimaryButton(viewDailyReportButton);

        refreshButton = new JButton("Refresh");
        UITheme.styleSecondaryButton(refreshButton);

        topToolBar.add(viewDailyReportButton);
        topToolBar.add(refreshButton);

        JPanel northContainer = new JPanel();
        northContainer.setBackground(UITheme.COLOR_BG);
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.add(headerPanel);
        northContainer.add(topToolBar);
        add(northContainer, BorderLayout.NORTH);

        // 3. Tabbed Center Pane: Tab 1 = Daily Report & Targets, Tab 2 = History
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(UITheme.COLOR_BG);

        JPanel dailyTab = createDailyReportPanel();
        tabbedPane.addTab("Daily Nutrition Report & Target Comparison", dailyTab);

        JPanel historyTab = createHistoryPanel();
        tabbedPane.addTab("Nutrition History (View)", historyTab);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(UITheme.COLOR_BG);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));
        centerWrapper.add(tabbedPane, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);

        // 4. Standard Bottom Navigation Bar
        backButton = new JButton("Back to Dashboard");
        JPanel footerNav = UITheme.createSecondaryNavBar(backButton);
        add(footerNav, BorderLayout.SOUTH);

        // Action Listeners (Anonymous Inner Classes - Java Lab Exp 6)
        viewDailyReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    LocalDate parsed = validateAndParseDate(dateField.getText(), true);
                    loadDailyReport(parsed);
                } catch (IllegalArgumentException ignored) {
                    // Handled inside validateAndParseDate
                }
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    LocalDate parsed = validateAndParseDate(dateField.getText(), true);
                    loadDailyReport(parsed);
                } catch (IllegalArgumentException ignored) {
                    // Handled inside validateAndParseDate
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleBackToDashboard();
            }
        });

        loadHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    boolean valid = validateDateRange(fromDateField.getText(), toDateField.getText(), true);
                    if (valid) {
                        LocalDate from = LocalDate.parse(fromDateField.getText().trim());
                        LocalDate to = LocalDate.parse(toDateField.getText().trim());
                        loadNutritionHistory(from, to);
                    }
                } catch (IllegalArgumentException ignored) {
                    // Handled inside validateDateRange
                }
            }
        });
    }

    private JPanel createDailyReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Left Panel: Daily Intake Cards & Target Comparison Grid (Compact & Balanced)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(UITheme.COLOR_BG);

        JPanel leftStack = new JPanel();
        leftStack.setLayout(new BoxLayout(leftStack, BoxLayout.Y_AXIS));
        leftStack.setBackground(UITheme.COLOR_BG);

        // Daily Intake Section (DBMS Exp 7 - Stored Procedure get_daily_summary)
        JPanel intakePanel = new JPanel(new GridLayout(5, 2, 8, 4));
        intakePanel.setBackground(UITheme.COLOR_CARD);
        intakePanel.setBorder(BorderFactory.createCompoundBorder(
                UITheme.createTitledCardBorder("Daily Intake (Stored Procedure)"),
                BorderFactory.createEmptyBorder(4, 10, 8, 10)
        ));

        selectedDateLabel = new JLabel(selectedDate.toString());
        caloriesLabel = new JLabel("0.00 kcal");
        proteinLabel = new JLabel("0.00 g");
        carbsLabel = new JLabel("0.00 g");
        fatLabel = new JLabel("0.00 g");

        selectedDateLabel.setFont(UITheme.FONT_BOLD);
        caloriesLabel.setFont(UITheme.FONT_BOLD);
        caloriesLabel.setForeground(UITheme.COLOR_PRIMARY);
        proteinLabel.setFont(UITheme.FONT_BOLD);
        carbsLabel.setFont(UITheme.FONT_BOLD);
        fatLabel.setFont(UITheme.FONT_BOLD);

        JLabel l1 = new JLabel("Summary Date:");
        l1.setFont(UITheme.FONT_REGULAR);
        intakePanel.add(l1);
        intakePanel.add(selectedDateLabel);

        JLabel l2 = new JLabel("Calories:");
        l2.setFont(UITheme.FONT_REGULAR);
        intakePanel.add(l2);
        intakePanel.add(caloriesLabel);

        JLabel l3 = new JLabel("Protein:");
        l3.setFont(UITheme.FONT_REGULAR);
        intakePanel.add(l3);
        intakePanel.add(proteinLabel);

        JLabel l4 = new JLabel("Carbohydrates:");
        l4.setFont(UITheme.FONT_REGULAR);
        intakePanel.add(l4);
        intakePanel.add(carbsLabel);

        JLabel l5 = new JLabel("Fat:");
        l5.setFont(UITheme.FONT_REGULAR);
        intakePanel.add(l5);
        intakePanel.add(fatLabel);
        leftStack.add(intakePanel);

        // Spacer
        leftStack.add(javax.swing.Box.createVerticalStrut(10));

        // Nutrition Target Comparison Section
        JPanel targetPanel = new JPanel(new GridLayout(5, 3, 6, 4));
        targetPanel.setBackground(UITheme.COLOR_CARD);

        goalStatusLabel = new JLabel("Checking active goal...");
        goalStatusLabel.setFont(UITheme.FONT_REGULAR);
        goalStatusLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        targetCaloriesLabel = new JLabel("0.00 kcal");
        targetProteinLabel = new JLabel("0.00 g");
        targetCarbsLabel = new JLabel("0.00 g");
        targetFatLabel = new JLabel("0.00 g");

        diffCaloriesLabel = new JLabel("0.00 kcal");
        diffProteinLabel = new JLabel("0.00 g");
        diffCarbsLabel = new JLabel("0.00 g");
        diffFatLabel = new JLabel("0.00 g");

        JLabel th1 = new JLabel("Nutrient");
        th1.setFont(UITheme.FONT_BOLD);
        JLabel th2 = new JLabel("Daily Target");
        th2.setFont(UITheme.FONT_BOLD);
        JLabel th3 = new JLabel("Difference");
        th3.setFont(UITheme.FONT_BOLD);

        targetPanel.add(th1);
        targetPanel.add(th2);
        targetPanel.add(th3);

        JLabel nl1 = new JLabel("Calories:");
        nl1.setFont(UITheme.FONT_REGULAR);
        targetPanel.add(nl1);
        targetPanel.add(targetCaloriesLabel);
        targetPanel.add(diffCaloriesLabel);

        JLabel nl2 = new JLabel("Protein:");
        nl2.setFont(UITheme.FONT_REGULAR);
        targetPanel.add(nl2);
        targetPanel.add(targetProteinLabel);
        targetPanel.add(diffProteinLabel);

        JLabel nl3 = new JLabel("Carbs:");
        nl3.setFont(UITheme.FONT_REGULAR);
        targetPanel.add(nl3);
        targetPanel.add(targetCarbsLabel);
        targetPanel.add(diffCarbsLabel);

        JLabel nl4 = new JLabel("Fat:");
        nl4.setFont(UITheme.FONT_REGULAR);
        targetPanel.add(nl4);
        targetPanel.add(targetFatLabel);
        targetPanel.add(diffFatLabel);

        JPanel goalStatusContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        goalStatusContainer.setBackground(UITheme.COLOR_CARD);
        goalStatusContainer.add(goalStatusLabel);

        JPanel targetWrapper = new JPanel(new BorderLayout());
        targetWrapper.setBackground(UITheme.COLOR_CARD);
        targetWrapper.setBorder(BorderFactory.createCompoundBorder(
                UITheme.createTitledCardBorder("Target Goal Comparison"),
                BorderFactory.createEmptyBorder(4, 10, 8, 10)
        ));
        targetWrapper.add(goalStatusContainer, BorderLayout.NORTH);
        targetWrapper.add(targetPanel, BorderLayout.CENTER);

        leftStack.add(targetWrapper);
        leftPanel.add(leftStack, BorderLayout.NORTH);

        JScrollPane leftScrollPane = new JScrollPane(leftPanel);
        leftScrollPane.setBorder(BorderFactory.createEmptyBorder());
        leftScrollPane.getViewport().setBackground(UITheme.COLOR_BG);
        leftScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        leftScrollPane.getVerticalScrollBar().setUnitIncrement(14);
        leftScrollPane.setPreferredSize(new Dimension(385, 480));
        leftScrollPane.setMinimumSize(new Dimension(360, 400));

        panel.add(leftScrollPane, BorderLayout.WEST);

        // Right Panel: Structured Executive Report + Monospace Export Tabs
        rightTabbedPane = new JTabbedPane();
        rightTabbedPane.setFont(UITheme.FONT_BOLD);
        rightTabbedPane.setBackground(UITheme.COLOR_BG);

        // 1. Structured Report Container
        JPanel structuredContainer = new JPanel();
        structuredContainer.setLayout(new BoxLayout(structuredContainer, BoxLayout.Y_AXIS));
        structuredContainer.setBackground(UITheme.COLOR_BG);
        structuredContainer.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));

        // Header Banner Card
        JPanel headerBanner = new JPanel(new BorderLayout(12, 6));
        headerBanner.setBackground(UITheme.COLOR_CARD);
        headerBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setBackground(UITheme.COLOR_CARD);

        JLabel docTitle = new JLabel("Daily Nutrition Executive Report");
        docTitle.setFont(UITheme.FONT_TITLE);
        docTitle.setForeground(UITheme.COLOR_PRIMARY);

        structHeaderDateLabel = new JLabel("Summary Date: " + selectedDate.toString());
        structHeaderDateLabel.setFont(UITheme.FONT_REGULAR);
        structHeaderDateLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        headerLeft.add(docTitle);
        headerLeft.add(javax.swing.Box.createVerticalStrut(2));
        headerLeft.add(structHeaderDateLabel);

        JButton viewExportBtn = new JButton("View Monospace Text");
        UITheme.styleSecondaryButton(viewExportBtn);
        viewExportBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rightTabbedPane.setSelectedIndex(1);
            }
        });

        headerBanner.add(headerLeft, BorderLayout.CENTER);
        headerBanner.add(viewExportBtn, BorderLayout.EAST);
        structuredContainer.add(headerBanner);
        structuredContainer.add(javax.swing.Box.createVerticalStrut(8));

        // 1. User Profile & Health Metrics Card
        JPanel userCard = new JPanel(new BorderLayout(8, 8));
        userCard.setBackground(UITheme.COLOR_CARD);
        userCard.setBorder(BorderFactory.createCompoundBorder(
                UITheme.createTitledCardBorder("1. User Profile & Health Metrics"),
                BorderFactory.createEmptyBorder(4, 10, 8, 10)
        ));

        JPanel userGrid = new JPanel(new GridLayout(3, 4, 12, 6));
        userGrid.setBackground(UITheme.COLOR_CARD);

        userGrid.add(createBoldLabel("User Name:"));
        structNameLabel = new JLabel(user != null ? user.getName() : "Guest");
        structNameLabel.setFont(UITheme.FONT_BOLD);
        userGrid.add(structNameLabel);

        userGrid.add(createBoldLabel("Email:"));
        structEmailLabel = new JLabel(user != null ? user.getEmail() : "N/A");
        structEmailLabel.setFont(UITheme.FONT_REGULAR);
        userGrid.add(structEmailLabel);

        userGrid.add(createBoldLabel("Age / Gender:"));
        structAgeGenderLabel = new JLabel();
        structAgeGenderLabel.setFont(UITheme.FONT_REGULAR);
        userGrid.add(structAgeGenderLabel);

        userGrid.add(createBoldLabel("Height / Weight:"));
        structHeightWeightLabel = new JLabel();
        structHeightWeightLabel.setFont(UITheme.FONT_REGULAR);
        userGrid.add(structHeightWeightLabel);

        userGrid.add(createBoldLabel("Activity Level:"));
        structActivityLabel = new JLabel();
        structActivityLabel.setFont(UITheme.FONT_REGULAR);
        userGrid.add(structActivityLabel);

        userGrid.add(createBoldLabel("Calculated BMI:"));
        structBmiLabel = new JLabel("0.00 (via MySQL calculate_bmi)");
        structBmiLabel.setFont(UITheme.FONT_BOLD);
        structBmiLabel.setForeground(UITheme.COLOR_PRIMARY);
        userGrid.add(structBmiLabel);

        userCard.add(userGrid, BorderLayout.CENTER);
        structuredContainer.add(userCard);
        structuredContainer.add(javax.swing.Box.createVerticalStrut(8));

        // 2. Daily Nutrient Intake Summary Card
        JPanel intakeCard = new JPanel(new BorderLayout(8, 8));
        intakeCard.setBackground(UITheme.COLOR_CARD);
        intakeCard.setBorder(BorderFactory.createCompoundBorder(
                UITheme.createTitledCardBorder("2. Daily Nutrient Intake (Stored Procedure get_daily_summary)"),
                BorderFactory.createEmptyBorder(4, 10, 8, 10)
        ));

        JPanel intakeGrid = new JPanel(new GridLayout(1, 4, 10, 6));
        intakeGrid.setBackground(UITheme.COLOR_CARD);

        structCalTile = createMetricTile("Calories", "0.00", "kcal", UITheme.COLOR_PRIMARY);
        structProtTile = createMetricTile("Protein", "0.00", "g", UITheme.COLOR_TEXT_MAIN);
        structCarbTile = createMetricTile("Carbohydrates", "0.00", "g", UITheme.COLOR_TEXT_MAIN);
        structFatTile = createMetricTile("Fats", "0.00", "g", UITheme.COLOR_TEXT_MAIN);

        intakeGrid.add(structCalTile.panel);
        intakeGrid.add(structProtTile.panel);
        intakeGrid.add(structCarbTile.panel);
        intakeGrid.add(structFatTile.panel);

        intakeCard.add(intakeGrid, BorderLayout.CENTER);
        structuredContainer.add(intakeCard);
        structuredContainer.add(javax.swing.Box.createVerticalStrut(8));

        // 3. Nutrition Target Goal Comparison Card
        JPanel comparisonCard = new JPanel(new BorderLayout(6, 6));
        comparisonCard.setBackground(UITheme.COLOR_CARD);
        comparisonCard.setBorder(BorderFactory.createCompoundBorder(
                UITheme.createTitledCardBorder("3. Target Goal Comparison"),
                BorderFactory.createEmptyBorder(4, 10, 8, 10)
        ));

        structGoalBannerLabel = new JLabel("Active Goal: Checking...");
        structGoalBannerLabel.setFont(UITheme.FONT_BOLD);
        structGoalBannerLabel.setForeground(UITheme.COLOR_TEXT_MAIN);
        JPanel bannerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        bannerPanel.setBackground(UITheme.COLOR_CARD);
        bannerPanel.add(structGoalBannerLabel);
        comparisonCard.add(bannerPanel, BorderLayout.NORTH);

        String[] compCols = {"Nutrient", "Actual Intake", "Daily Target", "Difference", "Goal Status"};
        structCompTableModel = new DefaultTableModel(compCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        structCompTable = new JTable(structCompTableModel);
        structCompTable.getTableHeader().setReorderingAllowed(false);
        UITheme.styleTable(structCompTable);
        structCompTable.setPreferredScrollableViewportSize(new Dimension(500, 115));
        JScrollPane compScrollPane = new JScrollPane(structCompTable);
        compScrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        comparisonCard.add(compScrollPane, BorderLayout.CENTER);

        structuredContainer.add(comparisonCard);
        structuredContainer.add(javax.swing.Box.createVerticalStrut(8));

        // 4. Meals Logged Today Card
        JPanel mealsCard = new JPanel(new BorderLayout(6, 6));
        mealsCard.setBackground(UITheme.COLOR_CARD);
        mealsCard.setBorder(BorderFactory.createCompoundBorder(
                UITheme.createTitledCardBorder("4. Meals Logged Today"),
                BorderFactory.createEmptyBorder(4, 10, 8, 10)
        ));

        structMealsHeaderLabel = new JLabel("Meals Logged for Selected Date:");
        structMealsHeaderLabel.setFont(UITheme.FONT_REGULAR);
        structMealsHeaderLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        JPanel mealsBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        mealsBanner.setBackground(UITheme.COLOR_CARD);
        mealsBanner.add(structMealsHeaderLabel);
        mealsCard.add(mealsBanner, BorderLayout.NORTH);

        String[] mealCols = {"Meal ID", "Meal Type", "Logged Date", "Status"};
        structMealsTableModel = new DefaultTableModel(mealCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        structMealsTable = new JTable(structMealsTableModel);
        structMealsTable.getTableHeader().setReorderingAllowed(false);
        UITheme.styleTable(structMealsTable);
        structMealsTable.setPreferredScrollableViewportSize(new Dimension(500, 115));
        JScrollPane mealsScrollPane = new JScrollPane(structMealsTable);
        mealsScrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        mealsCard.add(mealsScrollPane, BorderLayout.CENTER);

        structuredContainer.add(mealsCard);

        JScrollPane structuredScrollPane = new JScrollPane(structuredContainer);
        structuredScrollPane.setBorder(BorderFactory.createEmptyBorder());
        structuredScrollPane.getViewport().setBackground(UITheme.COLOR_BG);
        structuredScrollPane.getVerticalScrollBar().setUnitIncrement(14);

        rightTabbedPane.addTab("Structured Report", structuredScrollPane);

        // 2. Formatted Monospace Text / Export Tab
        JPanel textExportPanel = new JPanel(new BorderLayout(8, 8));
        textExportPanel.setBackground(UITheme.COLOR_CARD);
        textExportPanel.setBorder(BorderFactory.createCompoundBorder(
                UITheme.createTitledCardBorder("Plain-Text Nutrition Report (Lab Exp 5 - StringBuffer)"),
                BorderFactory.createEmptyBorder(6, 10, 8, 10)
        ));

        JPanel textToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        textToolbar.setBackground(UITheme.COLOR_CARD);

        JButton copyButton = new JButton("Copy Report to Clipboard");
        UITheme.stylePrimaryButton(copyButton);

        JButton backToStructuredBtn = new JButton("Back to Structured View");
        UITheme.styleSecondaryButton(backToStructuredBtn);

        textToolbar.add(copyButton);
        textToolbar.add(backToStructuredBtn);
        textExportPanel.add(textToolbar, BorderLayout.NORTH);

        reportTextArea = new JTextArea();
        reportTextArea.setEditable(false);
        Font reportFont = new Font("Consolas", Font.PLAIN, 14);
        if (!"Consolas".equalsIgnoreCase(reportFont.getFamily())) {
            reportFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        }
        reportTextArea.setFont(reportFont);
        reportTextArea.setForeground(UITheme.COLOR_TEXT_MAIN);
        reportTextArea.setBackground(new Color(250, 252, 254));
        reportTextArea.setLineWrap(false);
        reportTextArea.setTabSize(4);
        reportTextArea.setMargin(new Insets(16, 20, 16, 20));
        reportTextArea.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JScrollPane textScrollPane = new JScrollPane(reportTextArea);
        textScrollPane.getViewport().setBackground(new Color(250, 252, 254));
        textScrollPane.getVerticalScrollBar().setUnitIncrement(14);
        textScrollPane.getHorizontalScrollBar().setUnitIncrement(14);
        textScrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        textExportPanel.add(textScrollPane, BorderLayout.CENTER);

        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (reportTextArea.getText() != null && !reportTextArea.getText().isEmpty()) {
                    try {
                        StringSelection selection = new StringSelection(reportTextArea.getText());
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                        UITheme.showInfo(ReportFrame.this,
                                "Report text copied to clipboard successfully!",
                                "Copied");
                    } catch (Exception ex) {
                        UITheme.showError(ReportFrame.this,
                                "Failed to copy report to clipboard: " + ex.getMessage(),
                                "Clipboard Error");
                    }
                }
            }
        });

        backToStructuredBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rightTabbedPane.setSelectedIndex(0);
            }
        });

        rightTabbedPane.addTab("Plain-Text / Export", textExportPanel);

        panel.add(rightTabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Date Range Toolbar
        JPanel rangeToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        rangeToolbar.setBackground(UITheme.COLOR_CARD);
        rangeToolbar.setBorder(UITheme.createTitledCardBorder("Filter Nutrition History (MySQL View)"));

        JLabel fromLabel = new JLabel("From Date:");
        fromLabel.setFont(UITheme.FONT_BOLD);
        fromLabel.setForeground(UITheme.COLOR_TEXT_MAIN);
        rangeToolbar.add(fromLabel);

        fromDateField = new JTextField(selectedDate.minusDays(30).toString(), 10);
        UITheme.styleTextField(fromDateField);
        rangeToolbar.add(fromDateField);

        JLabel toLabel = new JLabel("To Date:");
        toLabel.setFont(UITheme.FONT_BOLD);
        toLabel.setForeground(UITheme.COLOR_TEXT_MAIN);
        rangeToolbar.add(toLabel);

        toDateField = new JTextField(selectedDate.toString(), 10);
        UITheme.styleTextField(toDateField);
        rangeToolbar.add(toDateField);

        loadHistoryButton = new JButton("Load History");
        UITheme.stylePrimaryButton(loadHistoryButton);
        rangeToolbar.add(loadHistoryButton);

        panel.add(rangeToolbar, BorderLayout.NORTH);

        // History Table (DBMS Exp 4 - daily_nutrition_summary View)
        String[] columnNames = {"Date", "Calories (kcal)", "Protein (g)", "Carbohydrates (g)", "Fat (g)"};
        historyTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.setFillsViewportHeight(true);
        UITheme.styleTable(historyTable);
        JScrollPane tableScrollPane = new JScrollPane(historyTable);
        tableScrollPane.getVerticalScrollBar().setUnitIncrement(14);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));

        panel.add(tableScrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Loads the daily nutrition summary and formatted report for the given date.
     * Delegates to API or ReportService (calling stored procedure get_daily_summary).
     */
    public void loadDailyReport(LocalDate date) {
        if (user == null || user.getUserId() == null) {
            return;
        }

        this.selectedDate = date;
        selectedDateLabel.setText(date.toString());

        if (apiClient != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            final LocalDate finalDate = date;
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                private DailyNutritionSummary summary;
                private NutritionGoal goal;
                private String reportText;
                private ArrayList<MealHandler.MealResponseDto> meals;

                @Override
                protected Void doInBackground() throws Exception {
                    Map<String, Object> daily = apiClient.getDailyReport(finalDate);
                    summary = (DailyNutritionSummary) daily.get("summary");
                    goal = (NutritionGoal) daily.get("activeGoal");
                    reportText = apiClient.getTextReport(finalDate);
                    try {
                        meals = apiClient.getMealsByDate(finalDate);
                    } catch (Exception ignored) {}
                    return null;
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        get();
                        dailySummary = summary != null ? summary : new DailyNutritionSummary(user.getUserId(), finalDate, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                        activeGoal = goal;
                        caloriesLabel.setText(formatNumber(dailySummary.getDailyCalories()) + " kcal");
                        proteinLabel.setText(formatNumber(dailySummary.getDailyProtein()) + " g");
                        carbsLabel.setText(formatNumber(dailySummary.getDailyCarbs()) + " g");
                        fatLabel.setText(formatNumber(dailySummary.getDailyFat()) + " g");
                        updateGoalComparison();
                        reportTextArea.setText(reportText != null ? reportText : "");
                        reportTextArea.setCaretPosition(0);
                        updateStructuredReportDisplay(finalDate);

                        // Populate meals table directly if available
                        if (structMealsTableModel != null && meals != null) {
                            structMealsTableModel.setRowCount(0);
                            if (meals.isEmpty()) {
                                structMealsTableModel.addRow(new Object[]{"-", "No meals logged on this date", finalDate.toString(), "-"});
                                if (structMealsHeaderLabel != null) {
                                    structMealsHeaderLabel.setText("Meals Logged: 0 meals recorded for " + finalDate);
                                }
                            } else {
                                for (MealHandler.MealResponseDto m : meals) {
                                    structMealsTableModel.addRow(new Object[]{
                                            "Meal #" + m.mealId,
                                            m.mealType,
                                            m.mealDate != null ? m.mealDate.toString() : finalDate.toString(),
                                            "Logged"
                                    });
                                }
                                if (structMealsHeaderLabel != null) {
                                    structMealsHeaderLabel.setText("Meals Logged: " + meals.size() + " meal(s) recorded for " + finalDate);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        reportTextArea.setText("Error loading report from server: " + msg);
                    }
                }
            };
            worker.execute();
            return;
        }

        if (reportService == null) {
            reportTextArea.setText("NutriGuide Localhost API Client is not connected, and direct database service fallback is disabled.");
            return;
        }

        // 1. Call stored procedure get_daily_summary via ReportService
        try {
            this.dailySummary = reportService.getDailySummaryViaProcedure(user.getUserId(), date);
            if (this.dailySummary == null) {
                this.dailySummary = new DailyNutritionSummary(user.getUserId(), date, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            }
        } catch (DatabaseOperationException ex) {
            this.dailySummary = new DailyNutritionSummary(user.getUserId(), date, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        caloriesLabel.setText(formatNumber(dailySummary.getDailyCalories()) + " kcal");
        proteinLabel.setText(formatNumber(dailySummary.getDailyProtein()) + " g");
        carbsLabel.setText(formatNumber(dailySummary.getDailyCarbs()) + " g");
        fatLabel.setText(formatNumber(dailySummary.getDailyFat()) + " g");

        // 2. Load active goal for comparison
        try {
            this.activeGoal = reportService.getGoalSummary(user.getUserId());
        } catch (DatabaseOperationException ex) {
            this.activeGoal = null;
        }

        updateGoalComparison();

        // 3. Generate plain-text report using ReportService (Exp 5 StringBuffer)
        try {
            String reportText = reportService.generateDailyUserReport(user.getUserId(), date);
            reportTextArea.setText(reportText);
            reportTextArea.setCaretPosition(0);
        } catch (ResourceNotFoundException ex) {
            reportTextArea.setText("Error: User record not found for ID " + user.getUserId());
        } catch (DatabaseOperationException ex) {
            reportTextArea.setText("Error generating report: " + ex.getMessage());
        }

        // 4. Update structured report presentation
        updateStructuredReportDisplay(date);
    }

    private MetricTile createMetricTile(String title, String initialVal, String unit, Color valColor) {
        MetricTile tile = new MetricTile();
        tile.panel = new JPanel();
        tile.panel.setLayout(new BoxLayout(tile.panel, BoxLayout.Y_AXIS));
        tile.panel.setBackground(new Color(246, 248, 251));
        tile.panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(UITheme.FONT_REGULAR);
        tLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        tile.valueLabel = new JLabel(initialVal);
        tile.valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        tile.valueLabel.setForeground(valColor);

        tile.unitLabel = new JLabel(unit);
        tile.unitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tile.unitLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        tile.panel.add(tLbl);
        tile.panel.add(javax.swing.Box.createVerticalStrut(3));
        tile.panel.add(tile.valueLabel);
        tile.panel.add(tile.unitLabel);

        return tile;
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.COLOR_TEXT_MAIN);
        return label;
    }

    private String getBmiCategory(BigDecimal bmi) {
        if (bmi == null) return "N/A";
        double val = bmi.doubleValue();
        if (val < 18.5) {
            return "Underweight";
        } else if (val < 25.0) {
            return "Normal weight";
        } else if (val < 30.0) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }

    private String getGoalStatus(BigDecimal diff, boolean isCalories) {
        if (diff == null) return "N/A";
        double val = diff.doubleValue();
        if (Math.abs(val) < 1.0) {
            return "Met Target";
        } else if (val > 0) {
            return isCalories ? "Surplus" : "Exceeded";
        } else {
            return "Deficit";
        }
    }

    private void updateStructuredReportDisplay(LocalDate date) {
        if (structHeaderDateLabel != null) {
            String uName = (user != null && user.getName() != null) ? user.getName() : "Guest";
            structHeaderDateLabel.setText("Report Date: " + date.toString() + "  •  User: " + uName);
        }

        if (structNameLabel != null) {
            structNameLabel.setText(user != null ? user.getName() : "Guest");
        }
        if (structEmailLabel != null) {
            structEmailLabel.setText(user != null ? user.getEmail() : "N/A");
        }
        if (structAgeGenderLabel != null) {
            String ageStr = (user != null && user.getAge() > 0) ? user.getAge() + " yrs" : "N/A";
            String genStr = (user != null && user.getGender() != null) ? user.getGender() : "N/A";
            structAgeGenderLabel.setText(ageStr + " / " + genStr);
        }
        if (structHeightWeightLabel != null) {
            String hStr = (user != null && user.getHeightCm() != null) ? formatNumber(user.getHeightCm()) + " cm" : "N/A";
            String wStr = (user != null && user.getWeightKg() != null) ? formatNumber(user.getWeightKg()) + " kg" : "N/A";
            structHeightWeightLabel.setText(hStr + " / " + wStr);
        }
        if (structActivityLabel != null) {
            structActivityLabel.setText(user != null && user.getActivityLevel() != null ? user.getActivityLevel() : "N/A");
        }

        // Calculated BMI (MySQL calculate_bmi function via nutritionService)
        if (structBmiLabel != null) {
            if (user != null && user.getHeightCm() != null && user.getWeightKg() != null
                    && user.getHeightCm().compareTo(BigDecimal.ZERO) > 0) {
                try {
                    BigDecimal bmi = null;
                    if (nutritionService != null) {
                        bmi = nutritionService.calculateBmi(user.getWeightKg(), user.getHeightCm());
                    } else {
                        double hM = user.getHeightCm().doubleValue() / 100.0;
                        if (hM > 0) {
                            double bmiVal = user.getWeightKg().doubleValue() / (hM * hM);
                            bmi = BigDecimal.valueOf(bmiVal).setScale(2, RoundingMode.HALF_UP);
                        }
                    }
                    if (bmi != null) {
                        String cat = getBmiCategory(bmi);
                        structBmiLabel.setText(formatNumber(bmi) + " (" + cat + ")");
                    } else {
                        structBmiLabel.setText("N/A");
                    }
                } catch (Exception ex) {
                    structBmiLabel.setText("N/A");
                }
            } else {
                structBmiLabel.setText("N/A");
            }
        }

        // Daily Nutrient Intake Tiles
        if (dailySummary != null) {
            if (structCalTile != null) structCalTile.valueLabel.setText(formatNumber(dailySummary.getDailyCalories()));
            if (structProtTile != null) structProtTile.valueLabel.setText(formatNumber(dailySummary.getDailyProtein()));
            if (structCarbTile != null) structCarbTile.valueLabel.setText(formatNumber(dailySummary.getDailyCarbs()));
            if (structFatTile != null) structFatTile.valueLabel.setText(formatNumber(dailySummary.getDailyFat()));
        }

        // Target Goal Comparison Table
        if (structCompTableModel != null) {
            structCompTableModel.setRowCount(0);
            if (activeGoal != null) {
                String gDesc = activeGoal.getDescription() != null ? activeGoal.getDescription() : "Active Goal";
                if (structGoalBannerLabel != null) {
                    structGoalBannerLabel.setText("Active Goal: " + gDesc);
                    structGoalBannerLabel.setForeground(new Color(30, 110, 30));
                }

                BigDecimal actCal = dailySummary != null ? dailySummary.getDailyCalories() : BigDecimal.ZERO;
                BigDecimal actProt = dailySummary != null ? dailySummary.getDailyProtein() : BigDecimal.ZERO;
                BigDecimal actCarb = dailySummary != null ? dailySummary.getDailyCarbs() : BigDecimal.ZERO;
                BigDecimal actFat = dailySummary != null ? dailySummary.getDailyFat() : BigDecimal.ZERO;

                BigDecimal tgtCal = activeGoal.getTargetCalories();
                BigDecimal tgtProt = activeGoal.getTargetProtein();
                BigDecimal tgtCarb = activeGoal.getTargetCarbs();
                BigDecimal tgtFat = activeGoal.getTargetFats();

                BigDecimal diffCal = actCal.subtract(tgtCal != null ? tgtCal : BigDecimal.ZERO);
                BigDecimal diffProt = actProt.subtract(tgtProt != null ? tgtProt : BigDecimal.ZERO);
                BigDecimal diffCarb = actCarb.subtract(tgtCarb != null ? tgtCarb : BigDecimal.ZERO);
                BigDecimal diffFat = actFat.subtract(tgtFat != null ? tgtFat : BigDecimal.ZERO);

                structCompTableModel.addRow(new Object[]{"Calories", formatNumber(actCal) + " kcal", formatNumber(tgtCal) + " kcal", formatDifference(diffCal, "kcal"), getGoalStatus(diffCal, true)});
                structCompTableModel.addRow(new Object[]{"Protein", formatNumber(actProt) + " g", formatNumber(tgtProt) + " g", formatDifference(diffProt, "g"), getGoalStatus(diffProt, false)});
                structCompTableModel.addRow(new Object[]{"Carbohydrates", formatNumber(actCarb) + " g", formatNumber(tgtCarb) + " g", formatDifference(diffCarb, "g"), getGoalStatus(diffCarb, false)});
                structCompTableModel.addRow(new Object[]{"Fat", formatNumber(actFat) + " g", formatNumber(tgtFat) + " g", formatDifference(diffFat, "g"), getGoalStatus(diffFat, false)});
            } else {
                if (structGoalBannerLabel != null) {
                    structGoalBannerLabel.setText("Active Goal: No active nutrition goal configured.");
                    structGoalBannerLabel.setForeground(Color.GRAY);
                }
                structCompTableModel.addRow(new Object[]{"Calories", "N/A", "N/A", "N/A", "No Goal Set"});
                structCompTableModel.addRow(new Object[]{"Protein", "N/A", "N/A", "N/A", "No Goal Set"});
                structCompTableModel.addRow(new Object[]{"Carbohydrates", "N/A", "N/A", "N/A", "No Goal Set"});
                structCompTableModel.addRow(new Object[]{"Fat", "N/A", "N/A", "N/A", "No Goal Set"});
            }
        }

        // Meals Logged Today Table
        if (structMealsTableModel != null && reportService != null) {
            structMealsTableModel.setRowCount(0);
            int count = 0;
            boolean dbError = false;
            String dbErrorMessage = null;
            if (user != null && user.getUserId() != null) {
                try {
                    ArrayList<Meal> allMeals = reportService.getMealHistory(user.getUserId());
                    if (allMeals != null) {
                        for (Meal m : allMeals) {
                            if (m.getMealDate() != null && m.getMealDate().equals(date)) {
                                count++;
                                structMealsTableModel.addRow(new Object[]{
                                        "Meal #" + m.getMealId(),
                                        m.getMealType(),
                                        m.getMealDate().toString(),
                                        "Logged"
                                });
                            }
                        }
                    }
                } catch (DatabaseOperationException ex) {
                    dbError = true;
                    dbErrorMessage = ex.getMessage();
                }
            }
            if (dbError) {
                structMealsTableModel.addRow(new Object[]{
                        "Error",
                        "Database error loading meals: " + (dbErrorMessage != null ? dbErrorMessage : "Database failure"),
                        date.toString(),
                        "Error"
                });
                if (structMealsHeaderLabel != null) {
                    structMealsHeaderLabel.setText("Meals Logged: Database error loading meals for " + date);
                }
            } else if (count == 0) {
                structMealsTableModel.addRow(new Object[]{"-", "No meals logged on this date", date.toString(), "-"});
                if (structMealsHeaderLabel != null) {
                    structMealsHeaderLabel.setText("Meals Logged: 0 meals recorded for " + date);
                }
            } else {
                if (structMealsHeaderLabel != null) {
                    structMealsHeaderLabel.setText("Meals Logged: " + count + " meal(s) recorded for " + date);
                }
            }
        }
    }

    private void updateGoalComparison() {
        if (activeGoal != null) {
            String desc = activeGoal.getDescription() != null ? activeGoal.getDescription() : "Active Goal";
            goalStatusLabel.setText("Active Goal: " + desc);
            goalStatusLabel.setForeground(new Color(30, 110, 30));

            BigDecimal targetCal = activeGoal.getTargetCalories();
            BigDecimal targetProt = activeGoal.getTargetProtein();
            BigDecimal targetCarb = activeGoal.getTargetCarbs();
            BigDecimal targetFatVal = activeGoal.getTargetFats();

            targetCaloriesLabel.setText(formatNumber(targetCal) + " kcal");
            targetProteinLabel.setText(formatNumber(targetProt) + " g");
            targetCarbsLabel.setText(formatNumber(targetCarb) + " g");
            targetFatLabel.setText(formatNumber(targetFatVal) + " g");

            BigDecimal actualCal = (dailySummary != null && dailySummary.getDailyCalories() != null) ? dailySummary.getDailyCalories() : BigDecimal.ZERO;
            BigDecimal actualProt = (dailySummary != null && dailySummary.getDailyProtein() != null) ? dailySummary.getDailyProtein() : BigDecimal.ZERO;
            BigDecimal actualCarb = (dailySummary != null && dailySummary.getDailyCarbs() != null) ? dailySummary.getDailyCarbs() : BigDecimal.ZERO;
            BigDecimal actualFat = (dailySummary != null && dailySummary.getDailyFat() != null) ? dailySummary.getDailyFat() : BigDecimal.ZERO;

            BigDecimal diffCal = actualCal.subtract(targetCal != null ? targetCal : BigDecimal.ZERO);
            BigDecimal diffProt = actualProt.subtract(targetProt != null ? targetProt : BigDecimal.ZERO);
            BigDecimal diffCarb = actualCarb.subtract(targetCarb != null ? targetCarb : BigDecimal.ZERO);
            BigDecimal diffFat = actualFat.subtract(targetFatVal != null ? targetFatVal : BigDecimal.ZERO);

            diffCaloriesLabel.setText(formatDifference(diffCal, "kcal"));
            diffProteinLabel.setText(formatDifference(diffProt, "g"));
            diffCarbsLabel.setText(formatDifference(diffCarb, "g"));
            diffFatLabel.setText(formatDifference(diffFat, "g"));
        } else {
            goalStatusLabel.setText("No active nutrition goal configured.");
            goalStatusLabel.setForeground(Color.GRAY);

            targetCaloriesLabel.setText("N/A");
            targetProteinLabel.setText("N/A");
            targetCarbsLabel.setText("N/A");
            targetFatLabel.setText("N/A");

            diffCaloriesLabel.setText("N/A");
            diffProteinLabel.setText("N/A");
            diffCarbsLabel.setText("N/A");
            diffFatLabel.setText("N/A");
        }
    }

    /**
     * Loads nutrition history from API or MySQL view 'daily_nutrition_summary' (DBMS Exp 4).
     */
    public void loadNutritionHistory(final LocalDate fromDate, final LocalDate toDate) {
        if (user == null || user.getUserId() == null) {
            return;
        }

        if (apiClient != null) {
            SwingWorker<ArrayList<DailyNutritionSummary>, Void> worker = new SwingWorker<ArrayList<DailyNutritionSummary>, Void>() {
                @Override
                protected ArrayList<DailyNutritionSummary> doInBackground() throws Exception {
                    Map<String, Object> resp = apiClient.getRangeReport(fromDate, toDate);
                    return (ArrayList<DailyNutritionSummary>) resp.get("history");
                }

                @Override
                protected void done() {
                    try {
                        ArrayList<DailyNutritionSummary> list = get();
                        historyTableModel.setRowCount(0);
                        if (list != null) {
                            for (DailyNutritionSummary item : list) {
                                historyTableModel.addRow(new Object[]{
                                        item.getMealDate() != null ? item.getMealDate().toString() : "N/A",
                                        formatNumber(item.getDailyCalories()),
                                        formatNumber(item.getDailyProtein()),
                                        formatNumber(item.getDailyCarbs()),
                                        formatNumber(item.getDailyFat())
                                });
                            }
                        }
                    } catch (Exception ex) {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        UITheme.showError(ReportFrame.this, "Failed to load nutrition history: " + msg, "Error");
                    }
                }
            };
            worker.execute();
            return;
        }

        if (reportService == null) {
            return;
        }

        try {
            ArrayList<DailyNutritionSummary> list = reportService.getNutritionHistory(user.getUserId(), fromDate, toDate);
            historyTableModel.setRowCount(0);
            if (list != null) {
                for (DailyNutritionSummary item : list) {
                    historyTableModel.addRow(new Object[]{
                            item.getMealDate() != null ? item.getMealDate().toString() : "N/A",
                            formatNumber(item.getDailyCalories()),
                            formatNumber(item.getDailyProtein()),
                            formatNumber(item.getDailyCarbs()),
                            formatNumber(item.getDailyFat())
                    });
                }
            }
        } catch (DatabaseOperationException ex) {
            UITheme.showError(this,
                    "Failed to load nutrition history: " + ex.getMessage(),
                    "Database Error");
        }
    }

    /**
     * Validates and parses a date string in YYYY-MM-DD format.
     * Non-blocking overload for programmatic test suites.
     */
    public LocalDate validateAndParseDate(String dateStr, boolean showDialog) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            if (showDialog) {
                UITheme.showWarning(this,
                        "Please enter a date in YYYY-MM-DD format.",
                        "Validation Error");
            }
            throw new IllegalArgumentException("Date string cannot be null or empty.");
        }

        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException ex) {
            if (showDialog) {
                UITheme.showWarning(this,
                        "Please enter a valid date in YYYY-MM-DD format.",
                        "Validation Error");
            }
            throw new IllegalArgumentException("Invalid date format: " + dateStr, ex);
        }
    }

    public LocalDate validateAndParseDate(String dateStr) {
        return validateAndParseDate(dateStr, true);
    }

    /**
     * Validates date range constraints (valid formats, fromDate <= toDate).
     */
    public boolean validateDateRange(String fromStr, String toStr, boolean showDialog) {
        LocalDate fromDate = validateAndParseDate(fromStr, showDialog);
        LocalDate toDate = validateAndParseDate(toStr, showDialog);

        if (fromDate.isAfter(toDate)) {
            if (showDialog) {
                UITheme.showWarning(this,
                        "From Date cannot be after To Date.",
                        "Date Range Error");
            }
            throw new IllegalArgumentException("From date (" + fromDate + ") cannot be after to date (" + toDate + ").");
        }
        return true;
    }

    public boolean validateDateRange(String fromStr, String toStr) {
        return validateDateRange(fromStr, toStr, true);
    }

    /**
     * Disposes this frame and returns to DashboardFrame.
     */
    public void handleBackToDashboard() {
        DashboardFrame dashboardFrame = apiClient != null ? new DashboardFrame(user, apiClient) : new DashboardFrame(user);
        UITheme.transition(this, dashboardFrame);
    }

    private String formatNumber(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private String formatDifference(BigDecimal diff, String unit) {
        if (diff == null) {
            return "0.00 " + unit;
        }
        BigDecimal rounded = diff.setScale(2, RoundingMode.HALF_UP);
        if (rounded.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + rounded.toString() + " " + unit;
        }
        return rounded.toString() + " " + unit;
    }

    // ========================================================================
    // Programmatic Accessors for Testing & Verification
    // ========================================================================
    public User getUser() {
        return user;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public NutritionService getNutritionService() {
        return nutritionService;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public DailyNutritionSummary getDailySummary() {
        return dailySummary;
    }

    public NutritionGoal getActiveGoal() {
        return activeGoal;
    }

    public JTextField getDateField() {
        return dateField;
    }

    public JTextField getFromDateField() {
        return fromDateField;
    }

    public JTextField getToDateField() {
        return toDateField;
    }

    public JButton getViewDailyReportButton() {
        return viewDailyReportButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JButton getLoadHistoryButton() {
        return loadHistoryButton;
    }

    public JLabel getSelectedDateLabel() {
        return selectedDateLabel;
    }

    public JLabel getCaloriesLabel() {
        return caloriesLabel;
    }

    public JLabel getProteinLabel() {
        return proteinLabel;
    }

    public JLabel getCarbsLabel() {
        return carbsLabel;
    }

    public JLabel getFatLabel() {
        return fatLabel;
    }

    public JLabel getGoalStatusLabel() {
        return goalStatusLabel;
    }

    public JLabel getTargetCaloriesLabel() {
        return targetCaloriesLabel;
    }

    public JLabel getTargetProteinLabel() {
        return targetProteinLabel;
    }

    public JLabel getTargetCarbsLabel() {
        return targetCarbsLabel;
    }

    public JLabel getTargetFatLabel() {
        return targetFatLabel;
    }

    public JLabel getDiffCaloriesLabel() {
        return diffCaloriesLabel;
    }

    public JLabel getDiffProteinLabel() {
        return diffProteinLabel;
    }

    public JLabel getDiffCarbsLabel() {
        return diffCarbsLabel;
    }

    public JLabel getDiffFatLabel() {
        return diffFatLabel;
    }

    public JTextArea getReportTextArea() {
        return reportTextArea;
    }

    public JTable getHistoryTable() {
        return historyTable;
    }

    public DefaultTableModel getHistoryTableModel() {
        return historyTableModel;
    }
}
