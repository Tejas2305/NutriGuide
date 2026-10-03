package com.nutriguide.gui;

import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.Food;
import com.nutriguide.model.Meal;
import com.nutriguide.model.MealItem;
import com.nutriguide.model.User;
import com.nutriguide.service.FoodService;
import com.nutriguide.service.MealService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * MealLogFrame: Provides Swing GUI for meal creation, food selection, temporary
 * item calculation, transactional meal persistence, and existing meal management.
 *
 * Demonstrates:
 * - Java Lab Exp 6: Anonymous Inner Classes for ActionListeners
 * - Java Lab Exp 17-18: Swing UI Components (JFrame, JPanel, JLabel, JTextField, JComboBox,
 *   JButton, JTable, DefaultTableModel, JOptionPane)
 * - Java Lab Exp 24: GUI-to-Service JDBC Transaction Integration
 *   (Strictly NO SQL, Connection, PreparedStatement, or ResultSet inside the GUI)
 */
public class MealLogFrame extends JFrame {

    private final User user;
    private final MealService mealService;
    private final FoodService foodService;

    // Available foods catalog
    private ArrayList<Food> availableFoods;

    // Temporary meal items currently being prepared
    private final ArrayList<MealItem> currentMealItems;
    private final ArrayList<Food> currentItemFoods;

    // UI Input Components
    private JTextField dateField;
    private JComboBox<String> mealTypeComboBox;
    private JComboBox<String> foodComboBox;
    private JTextField quantityField;
    private JLabel foodNutrientsLabel;

    // Current Meal Items Table
    private DefaultTableModel currentItemsTableModel;
    private JTable currentItemsTable;
    private JLabel totalCaloriesLabel;
    private JLabel totalProteinLabel;
    private JLabel totalCarbsLabel;
    private JLabel totalFatLabel;

    // Buttons
    private JButton addFoodButton;
    private JButton removeItemButton;
    private JButton saveMealButton;
    private JButton clearButton;
    private JButton backButton;

    // Existing Meals Table
    private DefaultTableModel existingMealsTableModel;
    private JTable existingMealsTable;
    private JButton loadMealsButton;
    private JButton deleteMealButton;

    public MealLogFrame(User user) {
        this(user, new MealService(), new FoodService());
    }

    public MealLogFrame(User user, MealService mealService, FoodService foodService) {
        this.user = user;
        this.mealService = mealService;
        this.foodService = foodService;
        this.currentMealItems = new ArrayList<MealItem>();
        this.currentItemFoods = new ArrayList<Food>();

        loadFoodCatalog();
        initializeUI();
        refreshExistingMeals();
    }

    /**
     * Loads available foods from the service layer.
     */
    private void loadFoodCatalog() {
        try {
            this.availableFoods = foodService.getAllFoods();
        } catch (DatabaseOperationException e) {
            this.availableFoods = new ArrayList<Food>();
            JOptionPane.showMessageDialog(this,
                    "Failed to load foods from database: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeUI() {
        setTitle("NutriGuide - Meal Log");
        setSize(880, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ====================================================================
        // Top Section: Meal Form & Food Selection (North)
        // ====================================================================
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        // Row 1: Header Label
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Log Meal & Food Items for " + (user != null ? user.getName() : "User"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titlePanel.add(titleLabel);
        northPanel.add(titlePanel);

        // Row 2: Form Inputs Panel (Date, Meal Type, Food, Quantity)
        JPanel formGrid = new JPanel(new GridLayout(2, 4, 10, 8));
        formGrid.setBorder(BorderFactory.createTitledBorder("Meal Details & Food Item Selection"));

        formGrid.add(new JLabel("Meal Date (yyyy-MM-dd):"));
        dateField = new JTextField(LocalDate.now().toString());
        formGrid.add(dateField);

        formGrid.add(new JLabel("Meal Type:"));
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
        mealTypeComboBox = new JComboBox<String>(mealTypes);
        formGrid.add(mealTypeComboBox);

        formGrid.add(new JLabel("Select Food:"));
        foodComboBox = new JComboBox<String>();
        if (availableFoods != null) {
            for (Food f : availableFoods) {
                foodComboBox.addItem(f.getFoodName());
            }
        }
        formGrid.add(foodComboBox);

        formGrid.add(new JLabel("Quantity (servings):"));
        quantityField = new JTextField("1.0");
        formGrid.add(quantityField);

        northPanel.add(formGrid);

        // Row 3: Food Nutritional Info Preview
        JPanel foodInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        foodNutrientsLabel = new JLabel("Nutrients: Select a food to preview serving info");
        foodNutrientsLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        foodInfoPanel.add(foodNutrientsLabel);
        northPanel.add(foodInfoPanel);

        // Food Combo Box Change Listener (Updates nutrient preview)
        foodComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFoodPreview();
            }
        });
        updateFoodPreview();

        // Row 4: Top Action Buttons (Add Item, Clear, Back)
        JPanel formActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addFoodButton = new JButton("Add Food Item");
        clearButton = new JButton("Clear Form");
        backButton = new JButton("Back to Dashboard");

        formActionPanel.add(addFoodButton);
        formActionPanel.add(clearButton);
        formActionPanel.add(backButton);
        northPanel.add(formActionPanel);

        add(northPanel, BorderLayout.NORTH);

        // ====================================================================
        // Center Section: Current Unsaved Meal Items Table
        // ====================================================================
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 15, 5, 15),
                BorderFactory.createTitledBorder("Current Meal Items (Unsaved)")
        ));

        String[] itemCols = {"Food Name", "Quantity", "Calories (kcal)", "Protein (g)", "Carbs (g)", "Fat (g)"};
        currentItemsTableModel = new DefaultTableModel(itemCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        currentItemsTable = new JTable(currentItemsTableModel);
        JScrollPane currentItemsScrollPane = new JScrollPane(currentItemsTable);
        centerPanel.add(currentItemsScrollPane, BorderLayout.CENTER);

        // Sub-panel below current items: Item Actions & Totals
        JPanel itemFooterPanel = new JPanel(new BorderLayout());

        // Left side: Remove Item button & Save Meal button
        JPanel itemButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        removeItemButton = new JButton("Remove Selected Item");
        saveMealButton = new JButton("Save Meal");
        itemButtonsPanel.add(removeItemButton);
        itemButtonsPanel.add(saveMealButton);
        itemFooterPanel.add(itemButtonsPanel, BorderLayout.WEST);

        // Right side: Totals Display
        JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        totalCaloriesLabel = new JLabel("Total Calories: 0.00 kcal");
        totalProteinLabel = new JLabel("Protein: 0.00 g");
        totalCarbsLabel = new JLabel("Carbs: 0.00 g");
        totalFatLabel = new JLabel("Fat: 0.00 g");

        totalsPanel.add(totalCaloriesLabel);
        totalsPanel.add(totalProteinLabel);
        totalsPanel.add(totalCarbsLabel);
        totalsPanel.add(totalFatLabel);
        itemFooterPanel.add(totalsPanel, BorderLayout.EAST);

        centerPanel.add(itemFooterPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // ====================================================================
        // South Section: Existing Meals for Selected Date Table
        // ====================================================================
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 15, 10, 15),
                BorderFactory.createTitledBorder("Existing Meals for Selected Date")
        ));

        String[] existingCols = {"Meal ID", "Date", "Meal Type", "Number of Items"};
        existingMealsTableModel = new DefaultTableModel(existingCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        existingMealsTable = new JTable(existingMealsTableModel);
        existingMealsTable.setRowHeight(20);
        JScrollPane existingScrollPane = new JScrollPane(existingMealsTable);
        existingScrollPane.setPreferredSize(new java.awt.Dimension(800, 130));
        southPanel.add(existingScrollPane, BorderLayout.CENTER);

        // Existing Meals Actions: Load & Delete
        JPanel existingActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        loadMealsButton = new JButton("Refresh Meals for Date");
        deleteMealButton = new JButton("Delete Selected Meal");

        existingActionPanel.add(loadMealsButton);
        existingActionPanel.add(deleteMealButton);
        southPanel.add(existingActionPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        // ====================================================================
        // Event Listeners (Anonymous Inner Classes - Java Lab Exp 6)
        // ====================================================================
        addFoodButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddFoodItem();
            }
        });

        removeItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRemoveSelectedItem();
            }
        });

        saveMealButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSaveMeal();
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

        loadMealsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshExistingMeals();
            }
        });

        deleteMealButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeleteSelectedMeal();
            }
        });
    }

    /**
     * Updates preview label with the selected food's baseline nutritional data.
     */
    private void updateFoodPreview() {
        int index = foodComboBox.getSelectedIndex();
        if (index >= 0 && availableFoods != null && index < availableFoods.size()) {
            Food food = availableFoods.get(index);
            foodNutrientsLabel.setText(String.format(
                    "Serving: %sg  |  Calories: %s kcal  |  Protein: %sg  |  Carbs: %sg  |  Fat: %sg",
                    food.getServingSize(), food.getCalories(), food.getProtein(),
                    food.getCarbohydrates(), food.getFat()
            ));
        } else {
            foodNutrientsLabel.setText("Nutrients: None selected");
        }
    }

    /**
     * Adds selected food and quantity to the temporary current meal items list.
     */
    public void handleAddFoodItem() {
        int selectedIndex = foodComboBox.getSelectedIndex();
        if (selectedIndex < 0 || availableFoods == null || selectedIndex >= availableFoods.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a food item.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyStr = quantityField.getText().trim();
        if (qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Quantity cannot be empty.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal quantity;
        try {
            quantity = new BigDecimal(qtyStr);
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Quantity must be strictly greater than 0.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Quantity must be a valid numeric value.",
                    "Format Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Food food = availableFoods.get(selectedIndex);

        // Calculate nutrient contributions: quantity * food nutritional values
        BigDecimal calories = food.getCalories().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        BigDecimal protein = food.getProtein().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        BigDecimal carbs = food.getCarbohydrates().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fat = food.getFat().multiply(quantity).setScale(2, RoundingMode.HALF_UP);

        // Add to in-memory collections
        MealItem item = new MealItem(null, null, food.getFoodId(), quantity);
        currentMealItems.add(item);
        currentItemFoods.add(food);

        // Add row to table model
        Object[] rowData = {
                food.getFoodName(),
                quantity.toPlainString(),
                calories.toPlainString(),
                protein.toPlainString(),
                carbs.toPlainString(),
                fat.toPlainString()
        };
        currentItemsTableModel.addRow(rowData);

        // Update totals
        updateTotals();
    }

    /**
     * Removes the currently selected row from the temporary items table.
     */
    public void handleRemoveSelectedItem() {
        int selectedRow = currentItemsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item from the table to remove.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentItemsTableModel.removeRow(selectedRow);
        currentMealItems.remove(selectedRow);
        currentItemFoods.remove(selectedRow);

        updateTotals();
    }

    /**
     * Calculates and displays totals for all temporary meal items.
     */
    public void updateTotals() {
        BigDecimal totCal = BigDecimal.ZERO;
        BigDecimal totProt = BigDecimal.ZERO;
        BigDecimal totCarbs = BigDecimal.ZERO;
        BigDecimal totFat = BigDecimal.ZERO;

        for (int i = 0; i < currentMealItems.size(); i++) {
            MealItem item = currentMealItems.get(i);
            Food food = currentItemFoods.get(i);
            BigDecimal qty = item.getQuantity();

            totCal = totCal.add(food.getCalories().multiply(qty));
            totProt = totProt.add(food.getProtein().multiply(qty));
            totCarbs = totCarbs.add(food.getCarbohydrates().multiply(qty));
            totFat = totFat.add(food.getFat().multiply(qty));
        }

        totalCaloriesLabel.setText("Total Calories: " + totCal.setScale(2, RoundingMode.HALF_UP).toPlainString() + " kcal");
        totalProteinLabel.setText("Protein: " + totProt.setScale(2, RoundingMode.HALF_UP).toPlainString() + " g");
        totalCarbsLabel.setText("Carbs: " + totCarbs.setScale(2, RoundingMode.HALF_UP).toPlainString() + " g");
        totalFatLabel.setText("Fat: " + totFat.setScale(2, RoundingMode.HALF_UP).toPlainString() + " g");
    }

    /**
     * Persists the meal and all its items transactionally via MealService.
     */
    public void handleSaveMeal() {
        String dateStr = dateField.getText().trim();
        LocalDate mealDate;
        try {
            mealDate = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use yyyy-MM-dd (e.g. 2026-10-03).",
                    "Date Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String mealType = (String) mealTypeComboBox.getSelectedItem();
        if (mealType == null || mealType.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a meal type.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentMealItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot save an empty meal. Please add at least one food item.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Meal meal = new Meal(user.getUserId(), mealType, mealDate);

        try {
            int mealId = mealService.addMealWithItems(meal, currentMealItems);

            JOptionPane.showMessageDialog(this,
                    "Meal saved successfully!\nMeal ID: #" + mealId + " (" + mealType + ")",
                    "Meal Saved",
                    JOptionPane.INFORMATION_MESSAGE);

            // Clear current meal form
            handleClear();

            // Refresh existing meals for the saved date
            refreshExistingMeals();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save meal: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Clears current temporary meal inputs, items, and resets totals.
     */
    public void handleClear() {
        currentMealItems.clear();
        currentItemFoods.clear();
        currentItemsTableModel.setRowCount(0);
        quantityField.setText("1.0");
        updateTotals();
    }

    /**
     * Refreshes the existing meals table for the date specified in dateField.
     */
    public void refreshExistingMeals() {
        existingMealsTableModel.setRowCount(0);

        String dateStr = dateField.getText().trim();
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
            return;
        }

        if (user == null || user.getUserId() == null) {
            return;
        }

        try {
            ArrayList<Meal> meals = mealService.getMealsByUserIdAndDate(user.getUserId(), date);
            for (Meal m : meals) {
                ArrayList<MealItem> items = mealService.getItemsByMealId(m.getMealId());
                Object[] row = {
                        m.getMealId(),
                        m.getMealDate().toString(),
                        m.getMealType(),
                        items != null ? items.size() : 0
                };
                existingMealsTableModel.addRow(row);
            }
        } catch (DatabaseOperationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load existing meals: " + ex.getMessage(),
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Handles deletion of selected meal using MealService.
     */
    public void handleDeleteSelectedMeal() {
        int selectedRow = existingMealsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an existing meal to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer mealId = (Integer) existingMealsTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Meal #" + mealId + "?\nAll associated items will be removed.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                mealService.deleteMeal(mealId, user.getUserId());
                JOptionPane.showMessageDialog(this,
                        "Meal #" + mealId + " deleted successfully.",
                        "Meal Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshExistingMeals();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete meal: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Disposes this frame and returns to DashboardFrame for the current User.
     */
    public void handleBackToDashboard() {
        this.dispose();
        DashboardFrame dashboard = new DashboardFrame(user);
        dashboard.setVisible(true);
    }

    // ========================================================================
    // Testing and Verification Accessors
    // ========================================================================
    public User getUser() {
        return user;
    }

    public MealService getMealService() {
        return mealService;
    }

    public FoodService getFoodService() {
        return foodService;
    }

    public JTextField getDateField() {
        return dateField;
    }

    public JComboBox<String> getMealTypeComboBox() {
        return mealTypeComboBox;
    }

    public JComboBox<String> getFoodComboBox() {
        return foodComboBox;
    }

    public JTextField getQuantityField() {
        return quantityField;
    }

    public DefaultTableModel getCurrentItemsTableModel() {
        return currentItemsTableModel;
    }

    public JTable getCurrentItemsTable() {
        return currentItemsTable;
    }

    public ArrayList<MealItem> getCurrentMealItems() {
        return currentMealItems;
    }

    public ArrayList<Food> getCurrentItemFoods() {
        return currentItemFoods;
    }

    public JLabel getTotalCaloriesLabel() {
        return totalCaloriesLabel;
    }

    public JLabel getTotalProteinLabel() {
        return totalProteinLabel;
    }

    public JLabel getTotalCarbsLabel() {
        return totalCarbsLabel;
    }

    public JLabel getTotalFatLabel() {
        return totalFatLabel;
    }

    public DefaultTableModel getExistingMealsTableModel() {
        return existingMealsTableModel;
    }

    public JTable getExistingMealsTable() {
        return existingMealsTable;
    }

    public JButton getAddFoodButton() {
        return addFoodButton;
    }

    public JButton getRemoveItemButton() {
        return removeItemButton;
    }

    public JButton getSaveMealButton() {
        return saveMealButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JButton getLoadMealsButton() {
        return loadMealsButton;
    }

    public JButton getDeleteMealButton() {
        return deleteMealButton;
    }
}
