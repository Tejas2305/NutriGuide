package com.nutriguide.gui;

import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.model.DailyNutritionSummary;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.User;
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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * ReportFrame: Swing GUI for viewing daily nutrition summaries, target comparisons,
 * generated text reports, and nutrition history across date ranges.
 *
 * Demonstrates:
 * - Exp 6: Anonymous Inner Classes for ActionListeners
 * - Exp 17-18: Swing UI Components (JFrame, JPanel, JLabel, JTextField, JButton, JTable, JTextArea, JTabbedPane)
 * - Exp 7 (DBMS): Calling stored procedure 'get_daily_summary' via ReportService
 * - Exp 4 (DBMS): Calling database view 'daily_nutrition_summary' via ReportService for history
 * - Layered Architecture: GUI delegates directly to ReportService and NutritionService
 *   (Strictly ZERO java.sql imports and ZERO SQL strings in GUI)
 */
public class ReportFrame extends JFrame {

    private final User user;
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

    // Report Text Area
    private JTextArea reportTextArea;

    // History Section
    private JTextField fromDateField;
    private JTextField toDateField;
    private JButton loadHistoryButton;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;

    public ReportFrame(User user) {
        this(user, new ReportService(), new NutritionService());
    }

    public ReportFrame(User user, ReportService reportService, NutritionService nutritionService) {
        this.user = user;
        this.reportService = reportService;
        this.nutritionService = nutritionService;
        this.selectedDate = LocalDate.now();

        initializeUI();
        loadDailyReport(selectedDate);
        loadNutritionHistory(selectedDate.minusDays(30), selectedDate);
    }

    private void initializeUI() {
        setTitle("NutriGuide - Nutrition Reports");
        setSize(850, 650);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        JLabel titleLabel = new JLabel("Nutrition Reports & History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(24, 43, 73));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        String userDesc = (user != null) ? "User: " + user.getName() + " (" + user.getEmail() + ")" : "User: Guest";
        JLabel userLabel = new JLabel(userDesc);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(Color.DARK_GRAY);
        headerPanel.add(userLabel, BorderLayout.EAST);

        // 2. Date Selection Toolbar
        JPanel topToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topToolBar.setBorder(BorderFactory.createTitledBorder("Daily Report Date"));

        topToolBar.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField(selectedDate.toString(), 10);
        topToolBar.add(dateField);

        viewDailyReportButton = new JButton("View Daily Report");
        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back to Dashboard");

        topToolBar.add(viewDailyReportButton);
        topToolBar.add(refreshButton);
        topToolBar.add(backButton);

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.add(headerPanel);
        northContainer.add(topToolBar);
        add(northContainer, BorderLayout.NORTH);

        // 3. Tabbed Center Pane: Tab 1 = Daily Report & Targets, Tab 2 = History
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel dailyTab = createDailyReportPanel();
        tabbedPane.addTab("Daily Nutrition Report & Target Comparison", dailyTab);

        JPanel historyTab = createHistoryPanel();
        tabbedPane.addTab("Nutrition History (View)", historyTab);

        add(tabbedPane, BorderLayout.CENTER);

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
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left Panel: Daily Intake Cards & Target Comparison Grid
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(380, 480));

        // Daily Intake Section (DBMS Exp 7 - Stored Procedure get_daily_summary)
        JPanel intakePanel = new JPanel(new GridLayout(5, 2, 6, 6));
        intakePanel.setBorder(BorderFactory.createTitledBorder("Daily Intake (Stored Procedure)"));

        selectedDateLabel = new JLabel(selectedDate.toString());
        caloriesLabel = new JLabel("0.00 kcal");
        proteinLabel = new JLabel("0.00 g");
        carbsLabel = new JLabel("0.00 g");
        fatLabel = new JLabel("0.00 g");

        Font valFont = new Font("Arial", Font.BOLD, 12);
        caloriesLabel.setFont(valFont);
        proteinLabel.setFont(valFont);
        carbsLabel.setFont(valFont);
        fatLabel.setFont(valFont);

        intakePanel.add(new JLabel("Summary Date:"));
        intakePanel.add(selectedDateLabel);
        intakePanel.add(new JLabel("Calories:"));
        intakePanel.add(caloriesLabel);
        intakePanel.add(new JLabel("Protein:"));
        intakePanel.add(proteinLabel);
        intakePanel.add(new JLabel("Carbohydrates:"));
        intakePanel.add(carbsLabel);
        intakePanel.add(new JLabel("Fat:"));
        intakePanel.add(fatLabel);
        leftPanel.add(intakePanel);

        // Nutrition Target Comparison Section
        JPanel targetPanel = new JPanel(new GridLayout(5, 3, 4, 4));
        targetPanel.setBorder(BorderFactory.createTitledBorder("Target Goal Comparison"));

        goalStatusLabel = new JLabel("Checking active goal...");
        goalStatusLabel.setFont(new Font("Arial", Font.ITALIC, 11));

        targetCaloriesLabel = new JLabel("0.00 kcal");
        targetProteinLabel = new JLabel("0.00 g");
        targetCarbsLabel = new JLabel("0.00 g");
        targetFatLabel = new JLabel("0.00 g");

        diffCaloriesLabel = new JLabel("0.00 kcal");
        diffProteinLabel = new JLabel("0.00 g");
        diffCarbsLabel = new JLabel("0.00 g");
        diffFatLabel = new JLabel("0.00 g");

        targetPanel.add(new JLabel("Nutrient"));
        targetPanel.add(new JLabel("Daily Target"));
        targetPanel.add(new JLabel("Difference"));

        targetPanel.add(new JLabel("Calories:"));
        targetPanel.add(targetCaloriesLabel);
        targetPanel.add(diffCaloriesLabel);

        targetPanel.add(new JLabel("Protein:"));
        targetPanel.add(targetProteinLabel);
        targetPanel.add(diffProteinLabel);

        targetPanel.add(new JLabel("Carbs:"));
        targetPanel.add(targetCarbsLabel);
        targetPanel.add(diffCarbsLabel);

        targetPanel.add(new JLabel("Fat:"));
        targetPanel.add(targetFatLabel);
        targetPanel.add(diffFatLabel);

        JPanel goalStatusContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        goalStatusContainer.add(goalStatusLabel);

        leftPanel.add(goalStatusContainer);
        leftPanel.add(targetPanel);

        panel.add(leftPanel, BorderLayout.WEST);

        // Right Panel: Formatted Text Report (Java Lab Exp 5 - StringBuffer)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Daily Nutrition Report (Text Format)"));

        reportTextArea = new JTextArea();
        reportTextArea.setEditable(false);
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportTextArea.setLineWrap(false);
        JScrollPane textScrollPane = new JScrollPane(reportTextArea);
        rightPanel.add(textScrollPane, BorderLayout.CENTER);

        panel.add(rightPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Date Range Toolbar
        JPanel rangeToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        rangeToolbar.setBorder(BorderFactory.createTitledBorder("Filter Nutrition History (MySQL View)"));

        rangeToolbar.add(new JLabel("From Date:"));
        fromDateField = new JTextField(selectedDate.minusDays(30).toString(), 10);
        rangeToolbar.add(fromDateField);

        rangeToolbar.add(new JLabel("To Date:"));
        toDateField = new JTextField(selectedDate.toString(), 10);
        rangeToolbar.add(toDateField);

        loadHistoryButton = new JButton("Load History");
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
        historyTable.setRowHeight(22);
        historyTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane tableScrollPane = new JScrollPane(historyTable);

        panel.add(tableScrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Loads the daily nutrition summary and formatted report for the given date.
     * Delegates to ReportService (calling stored procedure get_daily_summary).
     */
    public void loadDailyReport(LocalDate date) {
        if (user == null || user.getUserId() == null) {
            return;
        }

        this.selectedDate = date;
        selectedDateLabel.setText(date.toString());

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
     * Loads nutrition history from the MySQL view 'daily_nutrition_summary' (DBMS Exp 4).
     */
    public void loadNutritionHistory(LocalDate fromDate, LocalDate toDate) {
        if (user == null || user.getUserId() == null) {
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
            JOptionPane.showMessageDialog(this,
                    "Failed to load nutrition history: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Validates and parses a date string in YYYY-MM-DD format.
     * Non-blocking overload for programmatic test suites.
     */
    public LocalDate validateAndParseDate(String dateStr, boolean showDialog) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a date in YYYY-MM-DD format.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
            }
            throw new IllegalArgumentException("Date string cannot be null or empty.");
        }

        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException ex) {
            if (showDialog) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid date in YYYY-MM-DD format.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this,
                        "From Date cannot be after To Date.",
                        "Date Range Error",
                        JOptionPane.WARNING_MESSAGE);
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
        this.dispose();
        DashboardFrame dashboardFrame = new DashboardFrame(user);
        dashboardFrame.setVisible(true);
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
