package com.nutriguide.gui;

import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ResourceNotFoundException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.NutritionGoal;
import com.nutriguide.model.User;
import com.nutriguide.service.NutritionService;
import com.nutriguide.util.ValidationUtil;

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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * NutritionGoalFrame: Swing GUI for managing the logged-in user's nutrition goals.
 *
 * Demonstrates:
 * - Java Lab Exp 6: Anonymous Inner Classes for ActionListeners & Event Handlers
 * - Java Lab Exp 17-18: Swing UI Components (JFrame, JPanel, JLabel, JTextField,
 *   JButton, JTable, DefaultTableModel, JOptionPane)
 * - Java Lab Exp 24: GUI-to-Service Integration (Zero SQL/JDBC in GUI layer)
 */
public class NutritionGoalFrame extends JFrame {

    private final User user;
    private final NutritionService nutritionService;

    // Active Goal State
    private NutritionGoal activeGoal;
    private NutritionGoal selectedGoal;
    private ArrayList<NutritionGoal> goalHistory;

    // Active Goal Display Labels
    private JLabel activeStatusTitleLabel;
    private JLabel activeGoalDetailsLabel;

    // Goal Form Fields
    private JTextField descriptionField;
    private JTextField caloriesField;
    private JTextField proteinField;
    private JTextField carbsField;
    private JTextField fatField;

    // Form / Action Buttons
    private JButton saveGoalButton;
    private JButton updateGoalButton;
    private JButton deactivateGoalButton;
    private JButton refreshButton;
    private JButton clearButton;
    private JButton backButton;

    // History Table
    private DefaultTableModel historyTableModel;
    private JTable historyTable;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public NutritionGoalFrame(User user) {
        this(user, new NutritionService());
    }

    public NutritionGoalFrame(User user, NutritionService nutritionService) {
        this.user = user;
        this.nutritionService = nutritionService;
        this.goalHistory = new ArrayList<NutritionGoal>();

        initializeUI();
        refreshData();
    }

    private void initializeUI() {
        setTitle("NutriGuide - Nutrition Goal");
        setSize(720, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ====================================================================
        // Section 1: Active Goal Display (North)
        // ====================================================================
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 15, 5, 15),
                BorderFactory.createTitledBorder("Current Active Goal")
        ));

        activeStatusTitleLabel = new JLabel("Status: Loading...");
        activeStatusTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        activeGoalDetailsLabel = new JLabel("Checking for active goal...");
        activeGoalDetailsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        northPanel.add(activeStatusTitleLabel);
        northPanel.add(activeGoalDetailsLabel);
        add(northPanel, BorderLayout.NORTH);

        // ====================================================================
        // Section 2: Form & History Table (Center)
        // ====================================================================
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 15));

        // Form Panel
        JPanel formContainer = new JPanel(new BorderLayout(5, 5));
        formContainer.setBorder(BorderFactory.createTitledBorder("Goal Details"));

        JPanel formGrid = new JPanel(new GridLayout(5, 2, 8, 8));
        formGrid.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        formGrid.add(new JLabel("Goal Description / Name:"));
        descriptionField = new JTextField();
        formGrid.add(descriptionField);

        formGrid.add(new JLabel("Target Calories (kcal):"));
        caloriesField = new JTextField();
        formGrid.add(caloriesField);

        formGrid.add(new JLabel("Target Protein (g):"));
        proteinField = new JTextField();
        formGrid.add(proteinField);

        formGrid.add(new JLabel("Target Carbohydrates (g):"));
        carbsField = new JTextField();
        formGrid.add(carbsField);

        formGrid.add(new JLabel("Target Fat (g):"));
        fatField = new JTextField();
        formGrid.add(fatField);

        formContainer.add(formGrid, BorderLayout.CENTER);
        centerPanel.add(formContainer, BorderLayout.NORTH);

        // History Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout(5, 5));
        tableContainer.setBorder(BorderFactory.createTitledBorder("Goal History (Select row to view/update)"));

        String[] columnNames = new String[]{
                "Goal ID", "Description", "Calories", "Protein", "Carbs", "Fat", "Active", "Created At"
        };
        historyTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table view
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    handleTableSelection();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(tableContainer, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // ====================================================================
        // Section 3: Action Buttons (South)
        // ====================================================================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        saveGoalButton = new JButton("Save Goal");
        updateGoalButton = new JButton("Update Goal");
        deactivateGoalButton = new JButton("Deactivate Goal");
        refreshButton = new JButton("Refresh");
        clearButton = new JButton("Clear");
        backButton = new JButton("Back to Dashboard");

        buttonPanel.add(saveGoalButton);
        buttonPanel.add(updateGoalButton);
        buttonPanel.add(deactivateGoalButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // ====================================================================
        // Section 4: Action Listeners (Java Lab Exp 6 - Anonymous Inner Classes)
        // ====================================================================
        saveGoalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSaveGoal();
            }
        });

        updateGoalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUpdateGoal();
            }
        });

        deactivateGoalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeactivateGoal();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleClear();
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
     * Refreshes the active goal display, goal history table, and default form values.
     */
    public void refreshData() {
        loadActiveGoal();
        loadGoalHistory();
    }

    /**
     * Loads the active goal for the logged-in user.
     * Gracefully handles ResourceNotFoundException when no active goal exists.
     */
    public void loadActiveGoal() {
        if (user == null || user.getUserId() == null) {
            updateActiveGoalDisplay(null);
            return;
        }

        try {
            this.activeGoal = nutritionService.getActiveGoal(user.getUserId());
            updateActiveGoalDisplay(this.activeGoal);
            if (selectedGoal == null && activeGoal != null) {
                populateForm(activeGoal);
            }
        } catch (ResourceNotFoundException e) {
            // Not a crash - expected when no active goal is configured
            this.activeGoal = null;
            updateActiveGoalDisplay(null);
            if (selectedGoal == null) {
                handleClear();
            }
        } catch (DatabaseOperationException e) {
            this.activeGoal = null;
            updateActiveGoalDisplay(null);
            JOptionPane.showMessageDialog(this,
                    "Database error while loading active goal: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates the Active Goal Display panel at the top.
     */
    private void updateActiveGoalDisplay(NutritionGoal goal) {
        if (goal == null || goal.getIsActive() == null || !goal.getIsActive()) {
            activeStatusTitleLabel.setText("No active nutrition goal.");
            activeGoalDetailsLabel.setText("You can set a new active goal below.");
        } else {
            String createdStr = goal.getCreatedAt() != null
                    ? goal.getCreatedAt().format(DATE_TIME_FORMATTER)
                    : "N/A";
            activeStatusTitleLabel.setText("Goal #" + goal.getGoalId() + " (" + goal.getDescription() + ") - Active");
            activeGoalDetailsLabel.setText("Target: " + formatDecimal(goal.getTargetCalories()) + " kcal | " +
                    "Protein: " + formatDecimal(goal.getTargetProtein()) + "g | " +
                    "Carbs: " + formatDecimal(goal.getTargetCarbs()) + "g | " +
                    "Fat: " + formatDecimal(goal.getTargetFats()) + "g | " +
                    "Created: " + createdStr);
        }
    }

    /**
     * Loads all historical and active goals for the logged-in user into the history table.
     */
    public void loadGoalHistory() {
        historyTableModel.setRowCount(0);
        if (user == null || user.getUserId() == null) {
            this.goalHistory = new ArrayList<NutritionGoal>();
            return;
        }

        try {
            this.goalHistory = nutritionService.getGoalHistory(user.getUserId());
            if (this.goalHistory == null) {
                this.goalHistory = new ArrayList<NutritionGoal>();
            }

            for (int i = 0; i < goalHistory.size(); i++) {
                NutritionGoal g = goalHistory.get(i);
                String createdStr = g.getCreatedAt() != null
                        ? g.getCreatedAt().format(DATE_TIME_FORMATTER)
                        : "N/A";
                String activeStr = (g.getIsActive() != null && g.getIsActive()) ? "Yes" : "No";

                historyTableModel.addRow(new Object[]{
                        g.getGoalId(),
                        g.getDescription(),
                        formatDecimal(g.getTargetCalories()),
                        formatDecimal(g.getTargetProtein()),
                        formatDecimal(g.getTargetCarbs()),
                        formatDecimal(g.getTargetFats()),
                        activeStr,
                        createdStr
                });
            }
        } catch (DatabaseOperationException e) {
            this.goalHistory = new ArrayList<NutritionGoal>();
            JOptionPane.showMessageDialog(this,
                    "Database error while loading goal history: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles table row selection to populate form fields.
     */
    private void handleTableSelection() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < goalHistory.size()) {
            this.selectedGoal = goalHistory.get(selectedRow);
            populateForm(this.selectedGoal);
        }
    }

    /**
     * Populates form fields from a NutritionGoal object.
     */
    public void populateForm(NutritionGoal goal) {
        if (goal == null) {
            handleClear();
            return;
        }
        descriptionField.setText(goal.getDescription() != null ? goal.getDescription() : "");
        caloriesField.setText(goal.getTargetCalories() != null ? goal.getTargetCalories().toPlainString() : "");
        proteinField.setText(goal.getTargetProtein() != null ? goal.getTargetProtein().toPlainString() : "");
        carbsField.setText(goal.getTargetCarbs() != null ? goal.getTargetCarbs().toPlainString() : "");
        fatField.setText(goal.getTargetFats() != null ? goal.getTargetFats().toPlainString() : "");
    }

    /**
     * Clears the input form and resets selection state.
     */
    public void handleClear() {
        descriptionField.setText("");
        caloriesField.setText("");
        proteinField.setText("");
        carbsField.setText("");
        fatField.setText("");
        this.selectedGoal = null;
        historyTable.clearSelection();
    }

    /**
     * Validates and saves a new active goal through NutritionService.
     * Transactionally deactivates old active goals and preserves history.
     */
    public void handleSaveGoal() {
        NutritionGoal goalToSave = validateAndBuildGoalFromForm();
        if (goalToSave == null) {
            return;
        }

        try {
            NutritionGoal saved = nutritionService.saveNewActiveGoal(goalToSave);
            JOptionPane.showMessageDialog(this,
                    "New active nutrition goal #" + saved.getGoalId() + " saved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            this.selectedGoal = saved;
            refreshData();
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: " + e.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseOperationException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save nutrition goal: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Validates and updates the selected or active goal through NutritionService.
     */
    public void handleUpdateGoal() {
        NutritionGoal targetGoal = selectedGoal != null ? selectedGoal : activeGoal;
        if (targetGoal == null || targetGoal.getGoalId() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a goal from the history table or load an active goal to update.",
                    "No Goal Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        NutritionGoal formGoal = validateAndBuildGoalFromForm();
        if (formGoal == null) {
            return;
        }

        targetGoal.setDescription(formGoal.getDescription());
        targetGoal.setTargetCalories(formGoal.getTargetCalories());
        targetGoal.setTargetProtein(formGoal.getTargetProtein());
        targetGoal.setTargetCarbs(formGoal.getTargetCarbs());
        targetGoal.setTargetFats(formGoal.getTargetFats());

        try {
            boolean updated = nutritionService.updateGoalForUser(targetGoal, user.getUserId());
            if (updated) {
                JOptionPane.showMessageDialog(this,
                        "Nutrition goal #" + targetGoal.getGoalId() + " updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update nutrition goal #" + targetGoal.getGoalId(),
                        "Update Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this,
                    "Validation Error: " + e.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (ResourceNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Goal not found: " + e.getMessage(),
                    "Not Found",
                    JOptionPane.ERROR_MESSAGE);
        } catch (DatabaseOperationException e) {
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Deactivates the currently selected or active goal.
     * Preserves historical record in database.
     */
    public void handleDeactivateGoal() {
        NutritionGoal goalToDeactivate = selectedGoal != null ? selectedGoal : activeGoal;

        if (goalToDeactivate == null || goalToDeactivate.getGoalId() == null) {
            JOptionPane.showMessageDialog(this,
                    "No active or selected nutrition goal to deactivate.",
                    "Deactivate Goal",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (goalToDeactivate.getIsActive() != null && !goalToDeactivate.getIsActive()) {
            JOptionPane.showMessageDialog(this,
                    "Goal #" + goalToDeactivate.getGoalId() + " is already inactive.",
                    "Already Inactive",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to deactivate Goal #" + goalToDeactivate.getGoalId() + "?\n" +
                "The goal will be kept in history but will no longer be active.",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deactivated = nutritionService.deactivateGoalForUser(goalToDeactivate.getGoalId(), user.getUserId());
            if (deactivated) {
                JOptionPane.showMessageDialog(this,
                        "Goal #" + goalToDeactivate.getGoalId() + " deactivated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                this.selectedGoal = null;
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not deactivate goal #" + goalToDeactivate.getGoalId(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this,
                    "Unauthorized or Invalid: " + e.getMessage(),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (ResourceNotFoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Goal not found: " + e.getMessage(),
                    "Not Found",
                    JOptionPane.ERROR_MESSAGE);
        } catch (DatabaseOperationException e) {
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Validates inputs from form fields and constructs a NutritionGoal.
     * Returns null if validation fails, displaying a friendly warning dialog.
     */
    private NutritionGoal validateAndBuildGoalFromForm() {
        String desc = descriptionField.getText() != null ? descriptionField.getText().trim() : "";
        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Goal description / name cannot be empty.",
                    "Input Validation",
                    JOptionPane.WARNING_MESSAGE);
            descriptionField.requestFocus();
            return null;
        }

        BigDecimal calories;
        try {
            String calText = caloriesField.getText() != null ? caloriesField.getText().trim() : "";
            if (calText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Target calories cannot be empty.",
                        "Input Validation",
                        JOptionPane.WARNING_MESSAGE);
                caloriesField.requestFocus();
                return null;
            }
            calories = new BigDecimal(calText);
            if (!ValidationUtil.isNonNegativeBigDecimal(calories)) {
                JOptionPane.showMessageDialog(this,
                        "Target calories must be greater than or equal to 0.",
                        "Input Validation",
                        JOptionPane.WARNING_MESSAGE);
                caloriesField.requestFocus();
                return null;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid numeric value for target calories.",
                    "Input Validation",
                    JOptionPane.WARNING_MESSAGE);
            caloriesField.requestFocus();
            return null;
        }

        BigDecimal protein;
        try {
            String protText = proteinField.getText() != null ? proteinField.getText().trim() : "";
            if (protText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Target protein cannot be empty.",
                        "Input Validation",
                        JOptionPane.WARNING_MESSAGE);
                proteinField.requestFocus();
                return null;
            }
            protein = new BigDecimal(protText);
            if (!ValidationUtil.isNonNegativeBigDecimal(protein)) {
                JOptionPane.showMessageDialog(this,
                        "Target protein must be greater than or equal to 0.",
                        "Input Validation",
                        JOptionPane.WARNING_MESSAGE);
                proteinField.requestFocus();
                return null;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid numeric value for target protein.",
                    "Input Validation",
                    JOptionPane.WARNING_MESSAGE);
            proteinField.requestFocus();
            return null;
        }

        BigDecimal carbs;
        try {
            String carbsText = carbsField.getText() != null ? carbsField.getText().trim() : "";
            if (carbsText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Target carbohydrates cannot be empty.",
                        "Input Validation",
                        JOptionPane.WARNING_MESSAGE);
                carbsField.requestFocus();
                return null;
            }
            carbs = new BigDecimal(carbsText);
            if (!ValidationUtil.isNonNegativeBigDecimal(carbs)) {
                JOptionPane.showMessageDialog(this,
                        "Target carbohydrates must be greater than or equal to 0.",
                        "Input Validation",
                        JOptionPane.WARNING_MESSAGE);
                carbsField.requestFocus();
                return null;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid numeric value for target carbohydrates.",
                    "Input Validation",
                    JOptionPane.WARNING_MESSAGE);
            carbsField.requestFocus();
            return null;
        }

        BigDecimal fat;
        try {
            String fatText = fatField.getText() != null ? fatField.getText().trim() : "";
            if (fatText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Target fat cannot be empty.",
                        "Input Validation",
                        JOptionPane.WARNING_MESSAGE);
                fatField.requestFocus();
                return null;
            }
            fat = new BigDecimal(fatText);
            if (!ValidationUtil.isNonNegativeBigDecimal(fat)) {
                JOptionPane.showMessageDialog(this,
                        "Target fat must be greater than or equal to 0.",
                        "Input Validation",
                        JOptionPane.WARNING_MESSAGE);
                fatField.requestFocus();
                return null;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid numeric value for target fat.",
                    "Input Validation",
                    JOptionPane.WARNING_MESSAGE);
            fatField.requestFocus();
            return null;
        }

        Integer userId = user != null ? user.getUserId() : null;
        NutritionGoal goal = new NutritionGoal(userId, calories, protein, carbs, fat, true);
        goal.setDescription(desc);
        return goal;
    }

    /**
     * Disposes this frame and returns to DashboardFrame.
     */
    public void handleBackToDashboard() {
        this.dispose();
        DashboardFrame dashboardFrame = new DashboardFrame(user);
        dashboardFrame.setVisible(true);
    }

    private String formatDecimal(BigDecimal val) {
        return val != null ? val.setScale(1, java.math.RoundingMode.HALF_UP).toPlainString() : "0.0";
    }

    // ========================================================================
    // Programmatic Accessors for Automated Verification & Testing
    // ========================================================================

    public User getUser() {
        return user;
    }

    public NutritionService getNutritionService() {
        return nutritionService;
    }

    public NutritionGoal getActiveGoal() {
        return activeGoal;
    }

    public NutritionGoal getSelectedGoal() {
        return selectedGoal;
    }

    public void setSelectedGoal(NutritionGoal selectedGoal) {
        this.selectedGoal = selectedGoal;
    }

    public ArrayList<NutritionGoal> getGoalHistory() {
        return goalHistory;
    }

    public JLabel getActiveStatusTitleLabel() {
        return activeStatusTitleLabel;
    }

    public JLabel getActiveGoalDetailsLabel() {
        return activeGoalDetailsLabel;
    }

    public JTextField getDescriptionField() {
        return descriptionField;
    }

    public JTextField getCaloriesField() {
        return caloriesField;
    }

    public JTextField getProteinField() {
        return proteinField;
    }

    public JTextField getCarbsField() {
        return carbsField;
    }

    public JTextField getFatField() {
        return fatField;
    }

    public JButton getSaveGoalButton() {
        return saveGoalButton;
    }

    public JButton getUpdateGoalButton() {
        return updateGoalButton;
    }

    public JButton getDeactivateGoalButton() {
        return deactivateGoalButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JTable getHistoryTable() {
        return historyTable;
    }

    public DefaultTableModel getHistoryTableModel() {
        return historyTableModel;
    }
}
