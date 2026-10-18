package com.nutriguide.gui;

import com.nutriguide.client.NutriGuideApiClient;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.Recommendation;
import com.nutriguide.model.User;
import com.nutriguide.service.NutritionService;
import com.nutriguide.service.RecommendationService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * RecommendationFrame: Swing GUI for personalized, rule-based nutrition recommendations.
 *
 * Demonstrates:
 * - Decoupled client communicating with localhost REST API (with direct service fallback)
 * - SwingWorker for background recommendation loading and generation
 * - General educational guidance disclaimer (non-medical / non-clinical)
 * - Java Lab Exp 6: Anonymous Inner Classes for ActionListeners
 * - Java Lab Exp 17-18: Standard Swing UI Components (JFrame, JPanel, JLabel, JTextField,
 *   JButton, JTable, DefaultTableModel, JOptionPane)
 * - Java Lab Exp 24: GUI-to-Service Integration (Zero SQL/JDBC in GUI layer)
 */
public class RecommendationFrame extends JFrame {

    private final User user;
    private final NutriGuideApiClient apiClient;
    private final RecommendationService recommendationService;
    private final NutritionService nutritionService;

    private BigDecimal bmi;
    private ArrayList<Recommendation> currentRecommendations;
    private ArrayList<Recommendation> recommendationHistory;

    // Profile Summary Labels
    private JLabel nameLabel;
    private JLabel ageGenderLabel;
    private JLabel heightWeightLabel;
    private JLabel bmiLabel;

    // Date & Control Fields
    private JTextField dateField;
    private JButton loadButton;
    private JButton generateButton;
    private JButton backButton;
    private JLabel statusLabel;

    // Recommendation Tables
    private DefaultTableModel recommendationTableModel;
    private JTable recommendationTable;

    private DefaultTableModel historyTableModel;
    private JTable historyTable;
    private JLabel historyStatusLabel;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public RecommendationFrame(User user) {
        this(user, NutriGuideApiClient.getInstance(), null, null);
    }

    public RecommendationFrame(User user, NutriGuideApiClient apiClient) {
        this(user, apiClient, null, null);
    }

    /**
     * For test and demo harness execution only: allows direct service injection
     * without requiring a running localhost HTTP backend server.
     *
     * @param user Active user model
     * @param recommendationService Directly injected RecommendationService for offline/integration testing
     * @param nutritionService Directly injected NutritionService for offline/integration testing
     */
    public RecommendationFrame(User user, RecommendationService recommendationService, NutritionService nutritionService) {
        this(user, null, recommendationService, nutritionService);
    }

    /**
     * For test and demo harness execution only: allows explicit injection of both
     * client and service implementations.
     *
     * @param user Active user model
     * @param apiClient API client implementation or null if operating in test harness mode
     * @param recommendationService Directly injected RecommendationService or null for standard client mode
     * @param nutritionService Directly injected NutritionService or null for standard client mode
     */
    public RecommendationFrame(User user, NutriGuideApiClient apiClient, RecommendationService recommendationService, NutritionService nutritionService) {
        this.user = user;
        this.apiClient = apiClient;
        this.recommendationService = recommendationService;
        this.nutritionService = nutritionService;
        this.currentRecommendations = new ArrayList<Recommendation>();
        this.recommendationHistory = new ArrayList<Recommendation>();

        loadUserProfileAndBmi();
        initializeUI();
        handleLoadRecommendations();
        refreshHistory();
    }

    /**
     * Retrieves BMI via API or NutritionService using the MySQL stored function.
     */
    private void loadUserProfileAndBmi() {
        if (apiClient != null) {
            SwingWorker<BigDecimal, Void> worker = new SwingWorker<BigDecimal, Void>() {
                @Override
                protected BigDecimal doInBackground() throws Exception {
                    return apiClient.calculateBmi();
                }

                @Override
                protected void done() {
                    try {
                        bmi = get();
                        if (bmiLabel != null && bmi != null) {
                            String bmiStr = bmi.setScale(2, RoundingMode.HALF_UP).toPlainString();
                            bmiLabel.setText("Calculated BMI: " + bmiStr + " (via backend API)");
                        }
                    } catch (Exception e) {
                        bmi = null;
                    }
                }
            };
            worker.execute();
            return;
        }

        if (user != null && user.getWeightKg() != null && user.getHeightCm() != null && nutritionService != null) {
            try {
                this.bmi = nutritionService.calculateBmi(user.getWeightKg(), user.getHeightCm());
            } catch (Exception e) {
                this.bmi = null;
            }
        }
    }

    private void initializeUI() {
        setTitle("NutriGuide - Recommendations");
        UITheme.configureFrame(this, 1024, 750, 850, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 10));

        // ====================================================================
        // Section 1: Top Profile Summary & Date Controls (North)
        // ====================================================================
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBackground(UITheme.BG_COLOR);
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 5, 16));

        // Profile Box
        JPanel profileBox = new JPanel(new GridLayout(2, 2, 14, 6));
        profileBox.setBackground(UITheme.CARD_BG);
        profileBox.setBorder(UITheme.createTitledCardBorder("User Health Profile Summary"));

        String userName = (user != null && user.getName() != null) ? user.getName() : "N/A";
        nameLabel = new JLabel("User: " + userName);
        nameLabel.setFont(UITheme.FONT_BOLD);
        nameLabel.setForeground(UITheme.TEXT_MAIN);

        String ageStr = (user != null && user.getAge() != null) ? user.getAge() + " yrs" : "N/A";
        String genderStr = (user != null && user.getGender() != null) ? user.getGender() : "N/A";
        ageGenderLabel = new JLabel("Age / Gender: " + ageStr + " / " + genderStr);
        ageGenderLabel.setFont(UITheme.FONT_REGULAR);
        ageGenderLabel.setForeground(UITheme.TEXT_MAIN);

        String htStr = (user != null && user.getHeightCm() != null) ? user.getHeightCm() + " cm" : "N/A";
        String wtStr = (user != null && user.getWeightKg() != null) ? user.getWeightKg() + " kg" : "N/A";
        heightWeightLabel = new JLabel("Height / Weight: " + htStr + " / " + wtStr);
        heightWeightLabel.setFont(UITheme.FONT_REGULAR);
        heightWeightLabel.setForeground(UITheme.TEXT_MAIN);

        String bmiStr = (bmi != null) ? bmi.setScale(2, RoundingMode.HALF_UP).toPlainString() : "N/A";
        bmiLabel = new JLabel("Calculated BMI: " + bmiStr + " (via MySQL function)");
        bmiLabel.setFont(UITheme.FONT_BOLD);
        bmiLabel.setForeground(UITheme.PRIMARY_COLOR);

        profileBox.add(nameLabel);
        profileBox.add(ageGenderLabel);
        profileBox.add(heightWeightLabel);
        profileBox.add(bmiLabel);
        northPanel.add(profileBox);

        // Date Controls Panel
        JPanel dateControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        dateControlPanel.setBackground(UITheme.CARD_BG);
        dateControlPanel.setBorder(UITheme.createTitledCardBorder("Date Selection & Recommendation Generation"));

        JLabel lEval = new JLabel("Evaluation Date (yyyy-MM-dd):");
        lEval.setFont(UITheme.FONT_BOLD);
        lEval.setForeground(UITheme.TEXT_MAIN);
        dateControlPanel.add(lEval);

        dateField = new JTextField(LocalDate.now().toString(), 10);
        UITheme.styleTextField(dateField);
        dateControlPanel.add(dateField);

        loadButton = new JButton("Load Recommendations");
        generateButton = new JButton("Generate Recommendations");
        UITheme.styleSecondaryButton(loadButton);
        UITheme.stylePrimaryButton(generateButton);

        dateControlPanel.add(loadButton);
        dateControlPanel.add(generateButton);

        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        dateControlPanel.add(statusLabel);

        northPanel.add(dateControlPanel);

        // Educational Guidance Disclaimer Banner
        JPanel disclaimerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        disclaimerPanel.setBackground(UITheme.BG_COLOR);
        JLabel disclaimerLabel = new JLabel("Educational Guidance Only: Recommendations are generated based on nutritional rules for educational demonstration and do not constitute medical or clinical advice.");
        disclaimerLabel.setFont(UITheme.FONT_SMALL);
        disclaimerLabel.setForeground(UITheme.TEXT_MUTED);
        disclaimerPanel.add(disclaimerLabel);
        northPanel.add(disclaimerPanel);

        add(northPanel, BorderLayout.NORTH);

        // ====================================================================
        // Section 2: Recommendations Tables (Center)
        // ====================================================================
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBackground(UITheme.BG_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 5, 16));

        // Upper: Current Date Recommendations Table
        JPanel currentRecPanel = new JPanel(new BorderLayout(5, 5));
        currentRecPanel.setBackground(UITheme.CARD_BG);
        currentRecPanel.setBorder(UITheme.createTitledCardBorder("Active Rule-Based Recommendations (For Selected Date)"));

        String[] currentCols = new String[]{"Type", "Recommendation", "Generated At"};
        recommendationTableModel = new DefaultTableModel(currentCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recommendationTable = new JTable(recommendationTableModel);
        UITheme.styleTable(recommendationTable);
        recommendationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recommendationTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        recommendationTable.getColumnModel().getColumn(1).setPreferredWidth(450);
        recommendationTable.getColumnModel().getColumn(2).setPreferredWidth(130);

        JScrollPane currentScrollPane = new JScrollPane(recommendationTable);
        currentScrollPane.getViewport().setBackground(Color.WHITE);
        currentRecPanel.add(currentScrollPane, BorderLayout.CENTER);
        centerPanel.add(currentRecPanel);

        // Lower: Saved Recommendation History Table
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setBackground(UITheme.CARD_BG);
        historyPanel.setBorder(UITheme.createTitledCardBorder("Saved Recommendation History"));

        String[] historyCols = new String[]{"Recommendation ID", "Type", "Message", "Generated At"};
        historyTableModel = new DefaultTableModel(historyCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);
        UITheme.styleTable(historyTable);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(350);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(130);

        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.getViewport().setBackground(Color.WHITE);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);

        historyStatusLabel = new JLabel("Loading saved recommendation history...");
        historyStatusLabel.setFont(UITheme.FONT_SMALL);
        historyStatusLabel.setForeground(UITheme.TEXT_MUTED);
        historyPanel.add(historyStatusLabel, BorderLayout.SOUTH);
        centerPanel.add(historyPanel);

        add(centerPanel, BorderLayout.CENTER);

        // ====================================================================
        // Section 3: Navigation Buttons (South)
        // ====================================================================
        backButton = new JButton("Back to Dashboard");
        JPanel southPanel = UITheme.createSecondaryNavBar(backButton);
        add(southPanel, BorderLayout.SOUTH);

        // ====================================================================
        // Section 4: Action Listeners (Anonymous Inner Classes - Java Lab Exp 6)
        // ====================================================================
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLoadRecommendations();
            }
        });

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleGenerateRecommendations();
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
     * Loads date-specific rule-based recommendations from API or RecommendationService.
     */
    public void handleLoadRecommendations() {
        final LocalDate date = validateAndParseDate();
        if (date == null) {
            return;
        }

        if (user == null || user.getUserId() == null) {
            statusLabel.setText("No user logged in.");
            return;
        }

        if (apiClient != null) {
            statusLabel.setText("Loading recommendations from server...");
            loadButton.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<ArrayList<Recommendation>, Void> worker = new SwingWorker<ArrayList<Recommendation>, Void>() {
                @Override
                protected ArrayList<Recommendation> doInBackground() throws Exception {
                    return apiClient.getRecommendations(date);
                }

                @Override
                protected void done() {
                    loadButton.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        currentRecommendations = get();
                        displayCurrentRecommendations();
                        int count = (currentRecommendations != null) ? currentRecommendations.size() : 0;
                        if (count > 0) {
                            statusLabel.setText(count + " recommendation" + (count > 1 ? "s" : "") + " loaded.");
                        } else {
                            statusLabel.setText("No recommendations available for this date.");
                        }
                    } catch (Exception e) {
                        String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        statusLabel.setText("Error loading recommendations.");
                        UITheme.showError(RecommendationFrame.this, "Failed to load recommendations: " + msg, "Error");
                    }
                }
            };
            worker.execute();
            return;
        }

        if (recommendationService == null) {
            statusLabel.setText("API client unavailable.");
            return;
        }

        try {
            this.currentRecommendations = recommendationService.generateRecommendations(user.getUserId(), date);
            displayCurrentRecommendations();
            int count = (currentRecommendations != null) ? currentRecommendations.size() : 0;
            if (count > 0) {
                statusLabel.setText(count + " recommendation" + (count > 1 ? "s" : "") + " loaded.");
            } else {
                statusLabel.setText("No recommendations available for this date.");
            }
        } catch (ResourceNotFoundException e) {
            statusLabel.setText("User data not found: " + e.getMessage());
            UITheme.showError(this, "User profile not found in database: " + e.getMessage(), "User Not Found");
        } catch (DatabaseOperationException e) {
            statusLabel.setText("Database error: " + e.getMessage());
            UITheme.showError(this, "Database error while generating recommendations: " + e.getMessage(), "Database Error");
        }
    }

    /**
     * Generates rule-based recommendations and persists them to the database.
     */
    public void handleGenerateRecommendations() {
        final LocalDate date = validateAndParseDate();
        if (date == null) {
            return;
        }

        if (user == null || user.getUserId() == null) {
            statusLabel.setText("No user logged in.");
            return;
        }

        if (apiClient != null) {
            statusLabel.setText("Generating recommendations on server...");
            generateButton.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<ArrayList<Recommendation>, Void> worker = new SwingWorker<ArrayList<Recommendation>, Void>() {
                @Override
                protected ArrayList<Recommendation> doInBackground() throws Exception {
                    return apiClient.generateAndSaveRecommendations(date);
                }

                @Override
                protected void done() {
                    generateButton.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        ArrayList<Recommendation> saved = get();
                        currentRecommendations = saved;
                        displayCurrentRecommendations();
                        refreshHistory();
                        int count = (saved != null) ? saved.size() : 0;
                        statusLabel.setText(count + " recommendations generated.");
                        UITheme.showInfo(RecommendationFrame.this, count + " recommendations generated.", "Recommendations Generated");
                    } catch (Exception e) {
                        String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        statusLabel.setText("Generation error.");
                        UITheme.showError(RecommendationFrame.this, "Failed to generate recommendations: " + msg, "Error");
                    }
                }
            };
            worker.execute();
            return;
        }

        if (recommendationService == null) {
            UITheme.showError(this,
                    "NutriGuide Localhost API Client is not connected, and direct database service fallback is disabled.",
                    "Connection Unavailable");
            return;
        }

        try {
            ArrayList<Recommendation> saved = recommendationService.generateAndSaveRecommendations(user.getUserId(), date);
            this.currentRecommendations = saved;
            displayCurrentRecommendations();
            refreshHistory();

            int count = (saved != null) ? saved.size() : 0;
            statusLabel.setText(count + " recommendations generated.");
            UITheme.showInfo(this, count + " recommendations generated.", "Recommendations Generated");

        } catch (ResourceNotFoundException e) {
            statusLabel.setText("User not found: " + e.getMessage());
            UITheme.showError(this, "User profile not found: " + e.getMessage(), "Error");
        } catch (DatabaseOperationException e) {
            statusLabel.setText("Database error: " + e.getMessage());
            UITheme.showError(this, "Failed to save recommendations: " + e.getMessage(), "Database Error");
        }
    }

    /**
     * Refreshes the saved recommendation history table from API or RecommendationService.
     */
    public void refreshHistory() {
        historyTableModel.setRowCount(0);
        if (historyStatusLabel != null) {
            historyStatusLabel.setForeground(UITheme.TEXT_MUTED);
            historyStatusLabel.setText("Loading saved recommendation history...");
        }
        if (user == null || user.getUserId() == null) {
            this.recommendationHistory = new ArrayList<Recommendation>();
            if (historyStatusLabel != null) {
                historyStatusLabel.setForeground(UITheme.DANGER_COLOR);
                historyStatusLabel.setText("No signed-in user; saved history cannot be loaded.");
            }
            return;
        }

        if (apiClient != null) {
            SwingWorker<ArrayList<Recommendation>, Void> worker = new SwingWorker<ArrayList<Recommendation>, Void>() {
                @Override
                protected ArrayList<Recommendation> doInBackground() throws Exception {
                    return apiClient.getRecommendationHistory();
                }

                @Override
                protected void done() {
                    try {
                        recommendationHistory = get();
                        if (recommendationHistory == null) {
                            recommendationHistory = new ArrayList<Recommendation>();
                        }
                        historyTableModel.setRowCount(0);
                        for (int i = 0; i < recommendationHistory.size(); i++) {
                            Recommendation r = recommendationHistory.get(i);
                            String genAtStr = (r.getGeneratedAt() != null)
                                    ? r.getGeneratedAt().format(DATE_TIME_FORMATTER)
                                    : "N/A";

                            historyTableModel.addRow(new Object[]{
                                    r.getRecId() != null ? r.getRecId() : "N/A",
                                    r.getRecType() != null ? r.getRecType() : "GENERAL",
                                    r.getMessage() != null ? r.getMessage() : "",
                                    genAtStr
                            });
                        }
                        if (historyStatusLabel != null) {
                            historyStatusLabel.setForeground(UITheme.TEXT_MUTED);
                            historyStatusLabel.setText(recommendationHistory.isEmpty()
                                    ? "No saved recommendations yet."
                                    : recommendationHistory.size() + " saved recommendation(s).");
                        }
                    } catch (Exception e) {
                        recommendationHistory = new ArrayList<Recommendation>();
                        if (historyStatusLabel != null) {
                            historyStatusLabel.setForeground(UITheme.DANGER_COLOR);
                            historyStatusLabel.setText("History could not be loaded. Check the backend and database, then reopen this screen.");
                        }
                    }
                }
            };
            worker.execute();
            return;
        }

        if (recommendationService == null) {
            return;
        }

        try {
            this.recommendationHistory = recommendationService.getRecommendationHistory(user.getUserId());
            if (this.recommendationHistory == null) {
                this.recommendationHistory = new ArrayList<Recommendation>();
            }

            for (int i = 0; i < recommendationHistory.size(); i++) {
                Recommendation r = recommendationHistory.get(i);
                String genAtStr = (r.getGeneratedAt() != null)
                        ? r.getGeneratedAt().format(DATE_TIME_FORMATTER)
                        : "N/A";

                historyTableModel.addRow(new Object[]{
                        r.getRecId() != null ? r.getRecId() : "N/A",
                        r.getRecType() != null ? r.getRecType() : "GENERAL",
                        r.getMessage() != null ? r.getMessage() : "",
                        genAtStr
                });
            }
            if (historyStatusLabel != null) {
                historyStatusLabel.setForeground(UITheme.TEXT_MUTED);
                historyStatusLabel.setText(recommendationHistory.isEmpty()
                        ? "No saved recommendations yet."
                        : recommendationHistory.size() + " saved recommendation(s).");
            }
        } catch (DatabaseOperationException e) {
            this.recommendationHistory = new ArrayList<Recommendation>();
            if (historyStatusLabel != null) {
                historyStatusLabel.setForeground(UITheme.DANGER_COLOR);
                historyStatusLabel.setText("History could not be loaded: " + e.getMessage());
            }
        }
    }

    /**
     * Displays current recommendations in the upper JTable.
     */
    private void displayCurrentRecommendations() {
        recommendationTableModel.setRowCount(0);
        if (currentRecommendations == null || currentRecommendations.isEmpty()) {
            return;
        }

        for (int i = 0; i < currentRecommendations.size(); i++) {
            Recommendation r = currentRecommendations.get(i);
            String genAtStr = (r.getGeneratedAt() != null)
                    ? r.getGeneratedAt().format(DATE_TIME_FORMATTER)
                    : "N/A";

            recommendationTableModel.addRow(new Object[]{
                    r.getRecType() != null ? r.getRecType() : "GENERAL",
                    r.getMessage() != null ? r.getMessage() : "",
                    genAtStr
            });
        }
    }

    /**
     * Validates date string in dateField and parses into LocalDate.
     */
    public LocalDate validateAndParseDate() {
        return validateAndParseDate(true);
    }

    /**
     * Validates date string in dateField and parses into LocalDate, with optional JOptionPane display.
     */
    public LocalDate validateAndParseDate(boolean showDialog) {
        String text = (dateField != null && dateField.getText() != null) ? dateField.getText().trim() : "";
        if (text.isEmpty()) {
            if (showDialog) {
                UITheme.showWarning(this, "Please enter a date in yyyy-MM-dd format.", "Date Required");
            }
            if (dateField != null) {
                dateField.requestFocus();
            }
            return null;
        }

        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            if (showDialog) {
                UITheme.showWarning(this, "Invalid date format: '" + text + "'. Please enter date in yyyy-MM-dd format.", "Invalid Date");
            }
            if (dateField != null) {
                dateField.requestFocus();
            }
            return null;
        }
    }

    /**
     * Disposes this frame and returns to DashboardFrame.
     */
    public void handleBackToDashboard() {
        DashboardFrame dashboard = apiClient != null ? new DashboardFrame(user, apiClient) : new DashboardFrame(user);
        UITheme.transition(this, dashboard);
    }

    // ========================================================================
    // Programmatic Accessors for Automated Verification & Testing
    // ========================================================================

    public User getUser() {
        return user;
    }

    public BigDecimal getBmi() {
        return bmi;
    }

    public RecommendationService getRecommendationService() {
        return recommendationService;
    }

    public NutritionService getNutritionService() {
        return nutritionService;
    }

    public ArrayList<Recommendation> getCurrentRecommendations() {
        return currentRecommendations;
    }

    public ArrayList<Recommendation> getRecommendationHistory() {
        return recommendationHistory;
    }

    public JTextField getDateField() {
        return dateField;
    }

    public JButton getLoadButton() {
        return loadButton;
    }

    public JButton getGenerateButton() {
        return generateButton;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public JLabel getBmiLabel() {
        return bmiLabel;
    }

    public JLabel getNameLabel() {
        return nameLabel;
    }

    public JTable getRecommendationTable() {
        return recommendationTable;
    }

    public DefaultTableModel getRecommendationTableModel() {
        return recommendationTableModel;
    }

    public JTable getHistoryTable() {
        return historyTable;
    }

    public DefaultTableModel getHistoryTableModel() {
        return historyTableModel;
    }
}
