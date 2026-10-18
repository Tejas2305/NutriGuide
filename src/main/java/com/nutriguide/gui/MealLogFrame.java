package com.nutriguide.gui;

import com.nutriguide.client.ApiClientException;
import com.nutriguide.client.NutriGuideApiClient;
import com.nutriguide.exception.DatabaseOperationException;
import com.nutriguide.exception.ValidationException;
import com.nutriguide.model.Food;
import com.nutriguide.model.Meal;
import com.nutriguide.model.MealItem;
import com.nutriguide.model.User;
import com.nutriguide.server.MealHandler;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * MealLogFrame: Provides Swing GUI for meal creation, food selection, temporary
 * item calculation, transactional meal persistence, and existing meal management.
 *
 * Demonstrates:
 * - Decoupled client communicating with localhost REST API (with direct service fallback)
 * - SwingWorker for background meal loading and persistence
 * - Java Lab Exp 6: Anonymous Inner Classes for ActionListeners
 * - Java Lab Exp 17-18: Swing UI Components
 */
public class MealLogFrame extends JFrame {

    private final User user;
    private final NutriGuideApiClient apiClient;
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
        this(user, NutriGuideApiClient.getInstance(), null, null);
    }

    public MealLogFrame(User user, NutriGuideApiClient apiClient) {
        this(user, apiClient, null, null);
    }

    /**
     * For test and demo harness execution only: allows direct service injection
     * without requiring a running localhost HTTP backend server.
     *
     * @param user Active user model
     * @param mealService Directly injected MealService for offline/integration testing
     * @param foodService Directly injected FoodService for offline/integration testing
     */
    public MealLogFrame(User user, MealService mealService, FoodService foodService) {
        this(user, null, mealService, foodService);
    }

    /**
     * For test and demo harness execution only: allows explicit injection of both
     * client and service implementations.
     *
     * @param user Active user model
     * @param apiClient API client implementation or null if operating in test harness mode
     * @param mealService Directly injected MealService or null for standard client mode
     * @param foodService Directly injected FoodService or null for standard client mode
     */
    public MealLogFrame(User user, NutriGuideApiClient apiClient, MealService mealService, FoodService foodService) {
        this.user = user;
        this.apiClient = apiClient;
        this.mealService = mealService;
        this.foodService = foodService;
        this.currentMealItems = new ArrayList<MealItem>();
        this.currentItemFoods = new ArrayList<Food>();

        this.availableFoods = new ArrayList<Food>();
        initializeUI();
        loadFoodCatalog();
        refreshExistingMeals();
    }

    /**
     * Loads available foods from the API or service layer.
     */
    private void loadFoodCatalog() {
        if (apiClient != null) {
            foodComboBox.removeAllItems();
            foodComboBox.addItem("Loading food catalog...");
            foodComboBox.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<ArrayList<Food>, Void> worker = new SwingWorker<ArrayList<Food>, Void>() {
                @Override
                protected ArrayList<Food> doInBackground() throws Exception {
                    return apiClient.getAllFoods();
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        ArrayList<Food> foods = get();
                        availableFoods = foods != null ? foods : new ArrayList<Food>();
                        foodComboBox.removeAllItems();
                        if (availableFoods.isEmpty()) {
                            foodComboBox.addItem("No foods available");
                            foodComboBox.setEnabled(false);
                            foodNutrientsLabel.setText("The food catalog is empty. Add foods to the database before logging meals.");
                        } else {
                            for (Food food : availableFoods) {
                                foodComboBox.addItem(food.getFoodName());
                            }
                            foodComboBox.setEnabled(true);
                        }
                        updateFoodPreview();
                    } catch (Exception ex) {
                        availableFoods = new ArrayList<Food>();
                        foodComboBox.removeAllItems();
                        foodComboBox.addItem("Food catalog unavailable");
                        foodComboBox.setEnabled(false);
                        foodNutrientsLabel.setText("Could not load the food catalog. Check that the backend and database are available.");
                        String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        UITheme.showError(MealLogFrame.this,
                                "Could not load foods: " + message,
                                "Food Catalog Unavailable");
                    }
                }
            };
            worker.execute();
            return;
        }

        try {
            if (foodService != null) {
                this.availableFoods = foodService.getAllFoods();
            } else {
                this.availableFoods = new ArrayList<Food>();
            }
            populateFoodComboBox();
        } catch (Exception e) {
            this.availableFoods = new ArrayList<Food>();
            populateFoodComboBox();
            UITheme.showError(this, "Failed to load foods: " + e.getMessage(), "Food Catalog Error");
        }
    }

    private void populateFoodComboBox() {
        foodComboBox.removeAllItems();
        if (availableFoods == null || availableFoods.isEmpty()) {
            foodComboBox.addItem("No foods available");
            foodComboBox.setEnabled(false);
            foodNutrientsLabel.setText("The food catalog is empty. Add foods to the database before logging meals.");
            return;
        }

        for (Food food : availableFoods) {
            foodComboBox.addItem(food.getFoodName());
        }
        foodComboBox.setEnabled(true);
        updateFoodPreview();
    }

    private void initializeUI() {
        setTitle("NutriGuide - Meal Log");
        UITheme.configureFrame(this, 1024, 750, 850, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 10));

        // ====================================================================
        // Top Section: Meal Form & Food Selection (North)
        // ====================================================================
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBackground(UITheme.BG_COLOR);
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 5, 16));

        // Row 1: Header Label
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(UITheme.BG_COLOR);
        JLabel titleLabel = new JLabel("Log Meal & Food Items for " + (user != null ? user.getName() : "User"));
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.PRIMARY_DARK);
        titlePanel.add(titleLabel);
        northPanel.add(titlePanel);

        // Row 2: Form Inputs Panel (Date, Meal Type, Food, Quantity)
        JPanel formGrid = new JPanel(new GridLayout(2, 4, 12, 8));
        formGrid.setBackground(UITheme.CARD_BG);
        formGrid.setBorder(UITheme.createTitledCardBorder("Meal Details & Food Item Selection"));

        JLabel lDate = new JLabel("Meal Date (yyyy-MM-dd):"); lDate.setFont(UITheme.FONT_BOLD); lDate.setForeground(UITheme.TEXT_MAIN);
        formGrid.add(lDate);
        dateField = new JTextField(LocalDate.now().toString());
        UITheme.styleTextField(dateField);
        formGrid.add(dateField);

        JLabel lType = new JLabel("Meal Type:"); lType.setFont(UITheme.FONT_BOLD); lType.setForeground(UITheme.TEXT_MAIN);
        formGrid.add(lType);
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
        mealTypeComboBox = new JComboBox<String>(mealTypes);
        UITheme.styleComboBox(mealTypeComboBox);
        formGrid.add(mealTypeComboBox);

        JLabel lFood = new JLabel("Select Food:"); lFood.setFont(UITheme.FONT_BOLD); lFood.setForeground(UITheme.TEXT_MAIN);
        formGrid.add(lFood);
        foodComboBox = new JComboBox<String>();
        UITheme.styleComboBox(foodComboBox);
        foodComboBox.addItem("Loading food catalog...");
        foodComboBox.setEnabled(false);
        formGrid.add(foodComboBox);

        JLabel lQty = new JLabel("Quantity (servings):"); lQty.setFont(UITheme.FONT_BOLD); lQty.setForeground(UITheme.TEXT_MAIN);
        formGrid.add(lQty);
        quantityField = new JTextField("1.0");
        UITheme.styleTextField(quantityField);
        formGrid.add(quantityField);

        northPanel.add(formGrid);

        // Row 3: Food Nutritional Info Preview
        JPanel foodInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        foodInfoPanel.setBackground(UITheme.BG_COLOR);
        foodNutrientsLabel = new JLabel("Nutrients: Select a food to preview serving info");
        foodNutrientsLabel.setFont(UITheme.FONT_SMALL);
        foodNutrientsLabel.setForeground(UITheme.TEXT_MUTED);
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

        // Row 4: Top Action Buttons (Add Item, Clear)
        JPanel formActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        formActionPanel.setBackground(UITheme.BG_COLOR);
        addFoodButton = new JButton("Add Food Item");
        clearButton = new JButton("Clear Form");

        UITheme.stylePrimaryButton(addFoodButton);
        UITheme.styleSecondaryButton(clearButton);

        formActionPanel.add(addFoodButton);
        formActionPanel.add(clearButton);
        northPanel.add(formActionPanel);

        add(northPanel, BorderLayout.NORTH);

        // ====================================================================
        // Center Section: Current Unsaved Meal Items Table
        // ====================================================================
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(UITheme.CARD_BG);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 16, 5, 16),
                UITheme.createTitledCardBorder("Current Meal Items (Unsaved)")
        ));

        String[] itemCols = {"Food Name", "Quantity", "Calories (kcal)", "Protein (g)", "Carbs (g)", "Fat (g)"};
        currentItemsTableModel = new DefaultTableModel(itemCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        currentItemsTable = new JTable(currentItemsTableModel);
        UITheme.styleTable(currentItemsTable);
        JScrollPane currentItemsScrollPane = new JScrollPane(currentItemsTable);
        currentItemsScrollPane.getViewport().setBackground(Color.WHITE);
        centerPanel.add(currentItemsScrollPane, BorderLayout.CENTER);

        // Sub-panel below current items: Item Actions & Totals
        JPanel itemFooterPanel = new JPanel(new BorderLayout());
        itemFooterPanel.setBackground(UITheme.CARD_BG);

        // Left side: Remove Item button & Save Meal button
        JPanel itemButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        itemButtonsPanel.setBackground(UITheme.CARD_BG);
        removeItemButton = new JButton("Remove Selected Item");
        saveMealButton = new JButton("Save Meal");
        UITheme.styleDangerButton(removeItemButton);
        UITheme.stylePrimaryButton(saveMealButton);
        itemButtonsPanel.add(removeItemButton);
        itemButtonsPanel.add(saveMealButton);
        itemFooterPanel.add(itemButtonsPanel, BorderLayout.WEST);

        // Right side: Totals Display
        JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 6));
        totalsPanel.setBackground(UITheme.CARD_BG);
        totalCaloriesLabel = new JLabel("Total Calories: 0.00 kcal");
        totalProteinLabel = new JLabel("Protein: 0.00 g");
        totalCarbsLabel = new JLabel("Carbs: 0.00 g");
        totalFatLabel = new JLabel("Fat: 0.00 g");

        totalCaloriesLabel.setFont(UITheme.FONT_BOLD);
        totalCaloriesLabel.setForeground(UITheme.PRIMARY_DARK);
        totalProteinLabel.setFont(UITheme.FONT_BOLD);
        totalProteinLabel.setForeground(UITheme.TEXT_MAIN);
        totalCarbsLabel.setFont(UITheme.FONT_BOLD);
        totalCarbsLabel.setForeground(UITheme.TEXT_MAIN);
        totalFatLabel.setFont(UITheme.FONT_BOLD);
        totalFatLabel.setForeground(UITheme.TEXT_MAIN);

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
        southPanel.setBackground(UITheme.CARD_BG);
        southPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 16, 10, 16),
                UITheme.createTitledCardBorder("Existing Meals for Selected Date")
        ));

        String[] existingCols = {"Meal ID", "Date", "Meal Type", "Number of Items"};
        existingMealsTableModel = new DefaultTableModel(existingCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        existingMealsTable = new JTable(existingMealsTableModel);
        UITheme.styleTable(existingMealsTable);
        JScrollPane existingScrollPane = new JScrollPane(existingMealsTable);
        existingScrollPane.setPreferredSize(new java.awt.Dimension(800, 130));
        existingScrollPane.getViewport().setBackground(Color.WHITE);
        southPanel.add(existingScrollPane, BorderLayout.CENTER);

        // Existing Meals Actions: Load & Delete
        JPanel existingActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        existingActionPanel.setBackground(UITheme.CARD_BG);
        loadMealsButton = new JButton("Refresh Meals for Date");
        deleteMealButton = new JButton("Delete Selected Meal");

        UITheme.styleSecondaryButton(loadMealsButton);
        UITheme.styleDangerButton(deleteMealButton);

        existingActionPanel.add(loadMealsButton);
        existingActionPanel.add(deleteMealButton);
        southPanel.add(existingActionPanel, BorderLayout.SOUTH);

        backButton = new JButton("Back to Dashboard");
        JPanel navFooter = UITheme.createSecondaryNavBar(backButton);

        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.setBackground(UITheme.BG_COLOR);
        southContainer.add(southPanel, BorderLayout.CENTER);
        southContainer.add(navFooter, BorderLayout.SOUTH);

        add(southContainer, BorderLayout.SOUTH);

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
            UITheme.showWarning(this, "Please select a food item.", "Validation Error");
            return;
        }

        String qtyStr = quantityField.getText().trim();
        if (qtyStr.isEmpty()) {
            UITheme.showWarning(this, "Quantity cannot be empty.", "Validation Error");
            return;
        }

        BigDecimal quantity;
        try {
            quantity = new BigDecimal(qtyStr);
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                UITheme.showWarning(this, "Quantity must be strictly greater than 0.", "Validation Error");
                return;
            }
        } catch (NumberFormatException ex) {
            UITheme.showError(this, "Quantity must be a valid numeric value.", "Format Error");
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
            UITheme.showWarning(this, "Please select an item from the table to remove.", "No Selection");
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
     * Persists the meal and all its items transactionally via API or MealService.
     */
    public void handleSaveMeal() {
        final String dateStr = dateField.getText().trim();
        LocalDate mealDate;
        try {
            mealDate = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
            UITheme.showWarning(this, "Invalid date format. Please use yyyy-MM-dd (e.g. 2026-10-03).", "Date Error");
            return;
        }

        final String mealType = (String) mealTypeComboBox.getSelectedItem();
        if (mealType == null || mealType.trim().isEmpty()) {
            UITheme.showWarning(this, "Please select a meal type.", "Validation Error");
            return;
        }

        if (currentMealItems.isEmpty()) {
            UITheme.showWarning(this, "Cannot save an empty meal. Please add at least one food item.", "Validation Error");
            return;
        }

        if (apiClient != null) {
            final ArrayList<MealItem> itemsToSave = new ArrayList<MealItem>(currentMealItems);
            setMealEditorEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            final LocalDate finalMealDate = mealDate;
            SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return apiClient.createMeal(finalMealDate, mealType, itemsToSave);
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        int mealId = get();
                        UITheme.showInfo(MealLogFrame.this,
                                "Meal saved successfully!\nMeal ID: #" + mealId + " (" + mealType + ")",
                                "Meal Saved");
                        handleClear();
                        refreshExistingMeals();
                    } catch (Exception ex) {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        UITheme.showError(MealLogFrame.this, "Failed to save meal: " + msg, "Error");
                    } finally {
                        setMealEditorEnabled(true);
                    }
                }
            };
            worker.execute();
            return;
        }

        if (mealService == null) {
            UITheme.showError(this,
                    "NutriGuide Localhost API Client is not connected, and direct database service fallback is disabled.",
                    "Connection Unavailable");
            return;
        }

        Meal meal = new Meal(user.getUserId(), mealType, mealDate);

        try {
            int mealId = mealService.addMealWithItems(meal, currentMealItems);

            UITheme.showInfo(this,
                    "Meal saved successfully!\nMeal ID: #" + mealId + " (" + mealType + ")",
                    "Meal Saved");

            // Clear current meal form
            handleClear();

            // Refresh existing meals for the saved date
            refreshExistingMeals();

        } catch (ValidationException ex) {
            UITheme.showWarning(this, ex.getMessage(), "Validation Error");
        } catch (DatabaseOperationException ex) {
            UITheme.showError(this, "Failed to save meal: " + ex.getMessage(), "Database Error");
        }
    }

    private void setMealEditorEnabled(boolean enabled) {
        dateField.setEnabled(enabled);
        mealTypeComboBox.setEnabled(enabled);
        foodComboBox.setEnabled(enabled && availableFoods != null && !availableFoods.isEmpty());
        quantityField.setEnabled(enabled);
        addFoodButton.setEnabled(enabled && availableFoods != null && !availableFoods.isEmpty());
        removeItemButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        saveMealButton.setEnabled(enabled);
        backButton.setEnabled(enabled);
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
        final LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
            return;
        }

        if (user == null || user.getUserId() == null) {
            return;
        }

        if (apiClient != null) {
            SwingWorker<ArrayList<MealHandler.MealResponseDto>, Void> worker = new SwingWorker<ArrayList<MealHandler.MealResponseDto>, Void>() {
                @Override
                protected ArrayList<MealHandler.MealResponseDto> doInBackground() throws Exception {
                    return apiClient.getMealsByDate(date);
                }

                @Override
                protected void done() {
                    try {
                        ArrayList<MealHandler.MealResponseDto> meals = get();
                        existingMealsTableModel.setRowCount(0);
                        if (meals != null) {
                            for (MealHandler.MealResponseDto m : meals) {
                                Object[] row = {
                                        m.mealId,
                                        m.mealDate != null ? m.mealDate.toString() : date.toString(),
                                        m.mealType,
                                        m.itemCount
                                };
                                existingMealsTableModel.addRow(row);
                            }
                        }
                    } catch (Exception ex) {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        UITheme.showWarning(MealLogFrame.this, "Could not load existing meals: " + msg, "Warning");
                    }
                }
            };
            worker.execute();
            return;
        }

        if (mealService == null) {
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
            UITheme.showWarning(this, "Could not load existing meals: " + ex.getMessage(), "Warning");
        }
    }

    /**
     * Handles deletion of selected meal using API or MealService.
     */
    public void handleDeleteSelectedMeal() {
        int selectedRow = existingMealsTable.getSelectedRow();
        if (selectedRow < 0) {
            UITheme.showWarning(this, "Please select an existing meal to delete.", "No Selection");
            return;
        }

        final Integer mealId = (Integer) existingMealsTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Meal #" + mealId + "?\nAll associated items will be removed.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (apiClient != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        apiClient.deleteMeal(mealId);
                        return null;
                    }

                    @Override
                    protected void done() {
                        setCursor(Cursor.getDefaultCursor());
                        try {
                            get();
                            UITheme.showInfo(MealLogFrame.this, "Meal #" + mealId + " deleted successfully.", "Meal Deleted");
                            refreshExistingMeals();
                        } catch (Exception ex) {
                            String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                            UITheme.showError(MealLogFrame.this, "Failed to delete meal: " + msg, "Error");
                        }
                    }
                };
                worker.execute();
                return;
            }

            if (mealService == null) {
                UITheme.showError(this,
                        "NutriGuide Localhost API Client is not connected, and direct database service fallback is disabled.",
                        "Connection Unavailable");
                return;
            }

            try {
                mealService.deleteMeal(mealId, user.getUserId());
                UITheme.showInfo(this, "Meal #" + mealId + " deleted successfully.", "Meal Deleted");
                refreshExistingMeals();
            } catch (Exception ex) {
                UITheme.showError(this, "Failed to delete meal: " + ex.getMessage(), "Error");
            }
        }
    }

    /**
     * Disposes this frame and returns to DashboardFrame for the current User.
     */
    public void handleBackToDashboard() {
        DashboardFrame dashboard = apiClient != null ? new DashboardFrame(user, apiClient) : new DashboardFrame(user);
        UITheme.transition(this, dashboard);
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
