package com.nutriguide.gui;

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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
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
 * - Java Lab Exp 6: Anonymous Inner Classes for ActionListeners
 * - Java Lab Exp 17-18: Standard Swing UI Components (JFrame, JPanel, JLabel, JTextField,
 *   JButton, JTable, DefaultTableModel, JOptionPane)
 * - Java Lab Exp 24: GUI-to-Service Integration (Zero SQL/JDBC in GUI layer)
 */
public class RecommendationFrame extends JFrame {

    private final User user;
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

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public RecommendationFrame(User user) {
        this(user, new RecommendationService(), new NutritionService());
    }

    public RecommendationFrame(User user, RecommendationService recommendationService, NutritionService nutritionService) {
        this.user = user;
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
     * Retrieves BMI via NutritionService using the MySQL stored function.
     */
    private void loadUserProfileAndBmi() {
        if (user != null && user.getWeightKg() != null && user.getHeightCm() != null) {
            try {
                this.bmi = nutritionService.calculateBmi(user.getWeightKg(), user.getHeightCm());
            } catch (ValidationException e) {
                this.bmi = null;
            } catch (DatabaseOperationException e) {
                this.bmi = null;
            }
        }
    }

    private void initializeUI() {
        setTitle("NutriGuide - Recommendations");
        setSize(780, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ====================================================================
        // Section 1: Top Profile Summary & Date Controls (North)
        // ====================================================================
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        // Profile Box
        JPanel profileBox = new JPanel(new GridLayout(2, 2, 10, 5));
        profileBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("User Health Profile Summary"),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        String userName = (user != null && user.getName() != null) ? user.getName() : "N/A";
        nameLabel = new JLabel("User: " + userName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        String ageStr = (user != null && user.getAge() != null) ? user.getAge() + " yrs" : "N/A";
        String genderStr = (user != null && user.getGender() != null) ? user.getGender() : "N/A";
        ageGenderLabel = new JLabel("Age / Gender: " + ageStr + " / " + genderStr);

        String htStr = (user != null && user.getHeightCm() != null) ? user.getHeightCm() + " cm" : "N/A";
        String wtStr = (user != null && user.getWeightKg() != null) ? user.getWeightKg() + " kg" : "N/A";
        heightWeightLabel = new JLabel("Height / Weight: " + htStr + " / " + wtStr);

        String bmiStr = (bmi != null) ? bmi.setScale(2, RoundingMode.HALF_UP).toPlainString() : "N/A";
        bmiLabel = new JLabel("Calculated BMI: " + bmiStr + " (via MySQL function)");
        bmiLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        profileBox.add(nameLabel);
        profileBox.add(ageGenderLabel);
        profileBox.add(heightWeightLabel);
        profileBox.add(bmiLabel);
        northPanel.add(profileBox);

        // Date Controls Panel
        JPanel dateControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        dateControlPanel.setBorder(BorderFactory.createTitledBorder("Date Selection & Recommendation Generation"));

        dateControlPanel.add(new JLabel("Evaluation Date (yyyy-MM-dd):"));
        dateField = new JTextField(LocalDate.now().toString(), 10);
        dateControlPanel.add(dateField);

        loadButton = new JButton("Load Recommendations");
        generateButton = new JButton("Generate Recommendations");
        dateControlPanel.add(loadButton);
        dateControlPanel.add(generateButton);

        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        dateControlPanel.add(statusLabel);

        northPanel.add(dateControlPanel);
        add(northPanel, BorderLayout.NORTH);

        // ====================================================================
        // Section 2: Recommendations Tables (Center)
        // ====================================================================
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 15));

        // Upper: Current Date Recommendations Table
        JPanel currentRecPanel = new JPanel(new BorderLayout(5, 5));
        currentRecPanel.setBorder(BorderFactory.createTitledBorder("Active Rule-Based Recommendations (For Selected Date)"));

        String[] currentCols = new String[]{"Type", "Recommendation", "Generated At"};
        recommendationTableModel = new DefaultTableModel(currentCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recommendationTable = new JTable(recommendationTableModel);
        recommendationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recommendationTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        recommendationTable.getColumnModel().getColumn(1).setPreferredWidth(450);
        recommendationTable.getColumnModel().getColumn(2).setPreferredWidth(130);

        JScrollPane currentScrollPane = new JScrollPane(recommendationTable);
        currentRecPanel.add(currentScrollPane, BorderLayout.CENTER);
        centerPanel.add(currentRecPanel);

        // Lower: Saved Recommendation History Table
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setBorder(BorderFactory.createTitledBorder("Saved Recommendation History"));

        String[] historyCols = new String[]{"Recommendation ID", "Type", "Message", "Generated At"};
        historyTableModel = new DefaultTableModel(historyCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(350);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(130);

        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);
        centerPanel.add(historyPanel);

        add(centerPanel, BorderLayout.CENTER);

        // ====================================================================
        // Section 3: Navigation Buttons (South)
        // ====================================================================
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        backButton = new JButton("Back to Dashboard");
        southPanel.add(backButton);
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
     * Loads date-specific rule-based recommendations from RecommendationService.
     */
    public void handleLoadRecommendations() {
        LocalDate date = validateAndParseDate();
        if (date == null) {
            return;
        }

        if (user == null || user.getUserId() == null) {
            statusLabel.setText("No user logged in.");
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
            JOptionPane.showMessageDialog(this,
                    "User profile not found in database: " + e.getMessage(),
                    "User Not Found",
                    JOptionPane.ERROR_MESSAGE);
        } catch (DatabaseOperationException e) {
            statusLabel.setText("Database error: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Database error while generating recommendations: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Generates rule-based recommendations and persists them to the database.
     */
    public void handleGenerateRecommendations() {
        LocalDate date = validateAndParseDate();
        if (date == null) {
            return;
        }

        if (user == null || user.getUserId() == null) {
            statusLabel.setText("No user logged in.");
            return;
        }

        try {
            ArrayList<Recommendation> saved = recommendationService.generateAndSaveRecommendations(user.getUserId(), date);
            this.currentRecommendations = saved;
            displayCurrentRecommendations();
            refreshHistory();

            int count = (saved != null) ? saved.size() : 0;
            statusLabel.setText(count + " recommendations generated.");
            JOptionPane.showMessageDialog(this,
                    count + " recommendations generated.",
                    "Recommendations Generated",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (ResourceNotFoundException e) {
            statusLabel.setText("User not found: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "User profile not found: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (DatabaseOperationException e) {
            statusLabel.setText("Database error: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Failed to save recommendations: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Refreshes the saved recommendation history table from RecommendationService.
     */
    public void refreshHistory() {
        historyTableModel.setRowCount(0);
        if (user == null || user.getUserId() == null) {
            this.recommendationHistory = new ArrayList<Recommendation>();
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
        } catch (DatabaseOperationException e) {
            this.recommendationHistory = new ArrayList<Recommendation>();
            JOptionPane.showMessageDialog(this,
                    "Failed to load recommendation history: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this,
                        "Please enter a date in yyyy-MM-dd format.",
                        "Date Required",
                        JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this,
                        "Invalid date format: '" + text + "'. Please enter date in yyyy-MM-dd format.",
                        "Invalid Date",
                        JOptionPane.WARNING_MESSAGE);
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
        this.dispose();
        DashboardFrame dashboard = new DashboardFrame(user);
        dashboard.setVisible(true);
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
