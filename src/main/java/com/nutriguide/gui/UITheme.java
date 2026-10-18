package com.nutriguide.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

/**
 * UITheme: Centralized UI styling and design constants for NutriGuide Swing GUI.
 *
 * Provides a clean, modern, professional light theme with an academic teal accent.
 * Compatible with Java 8 syntax (no lambdas, streams, or external UI libraries).
 */
public final class UITheme {

    // Color Palette
    public static final Color BG_COLOR = new Color(245, 247, 250);          // Soft background #F5F7FA
    public static final Color CARD_BG = new Color(255, 255, 255);           // Card container white #FFFFFF
    public static final Color CARD_ALT_BG = new Color(250, 252, 254);       // Light card tint
    public static final Color PRIMARY_COLOR = new Color(0, 121, 107);       // Teal 700 accent
    public static final Color PRIMARY_DARK = new Color(0, 77, 64);          // Deep teal
    public static final Color PRIMARY_LIGHT = new Color(224, 242, 241);     // Soft teal highlight
    public static final Color TEXT_MAIN = new Color(33, 37, 41);            // Dark readable charcoal #212529
    public static final Color TEXT_MUTED = new Color(108, 117, 125);        // Slate gray #6C757D
    public static final Color BORDER_COLOR = new Color(218, 224, 233);      // Subtle borders
    public static final Color BORDER_DARK = new Color(180, 190, 205);       // Focus border
    public static final Color BTN_SECONDARY_BG = new Color(238, 242, 246);  // Light secondary button
    public static final Color DANGER_COLOR = new Color(198, 40, 40);        // Crimson red for alerts/logout

    // Aliases for convenience across frames
    public static final Color COLOR_BG = BG_COLOR;
    public static final Color COLOR_CARD = CARD_BG;
    public static final Color COLOR_PRIMARY = PRIMARY_COLOR;
    public static final Color COLOR_BORDER = BORDER_COLOR;
    public static final Color COLOR_TEXT_MAIN = TEXT_MAIN;
    public static final Color COLOR_TEXT_MUTED = TEXT_MUTED;

    // Typography
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_SECTION = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_METRIC = new Font("SansSerif", Font.BOLD, 15);

    private UITheme() {
        // Private constructor prevents instantiation
    }

    /**
     * Applies standard sizing, minimum sizing, and background, then centers the frame.
     */
    public static void configureFrame(JFrame frame, int defWidth, int defHeight, int minWidth, int minHeight) {
        frame.setSize(defWidth, defHeight);
        frame.setMinimumSize(new Dimension(minWidth, minHeight));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG_COLOR);
    }

    /**
     * Convenient overload using default dimensions as minimum dimensions.
     */
    public static void configureFrame(JFrame frame, int minWidth, int minHeight) {
        configureFrame(frame, minWidth, minHeight, minWidth, minHeight);
    }

    /**
     * Styles a primary action button (Teal background, White text).
     */
    public static void stylePrimaryButton(JButton button) {
        button.setFont(FONT_BOLD);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(6, 14, 6, 14));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_DARK, 1),
                new EmptyBorder(5, 12, 5, 12)
        ));
    }

    /**
     * Styles a secondary button (Light gray background, Charcoal text).
     */
    public static void styleSecondaryButton(JButton button) {
        button.setFont(FONT_REGULAR);
        button.setBackground(BTN_SECONDARY_BG);
        button.setForeground(TEXT_MAIN);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(6, 12, 6, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    /**
     * Styles a danger/destructive button (Muted red outline/text).
     */
    public static void styleDangerButton(JButton button) {
        button.setFont(FONT_BOLD);
        button.setBackground(new Color(253, 237, 237));
        button.setForeground(DANGER_COLOR);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(6, 12, 6, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(239, 154, 154), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    /**
     * Styles a standard text input field with subtle border, internal padding,
     * consistent height, and a visible active focus state.
     */
    public static void styleTextField(final JTextField field) {
        field.setFont(FONT_REGULAR);
        field.setForeground(TEXT_MAIN);
        field.setBackground(Color.WHITE);
        field.setCaretColor(PRIMARY_DARK);

        final Border normalBorder = BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 8, 5, 8)
        );
        final Border focusBorder = BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_COLOR, 1),
                new EmptyBorder(5, 8, 5, 8)
        );

        field.setBorder(normalBorder);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 32));

        field.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(focusBorder);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(normalBorder);
            }
        });
    }

    /**
     * Styles a combobox with consistent height and font.
     */
    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(FONT_REGULAR);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_MAIN);
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 32));
    }

    /**
     * Styles a JTable for clean, comfortable readability.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(28);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY_DARK);
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT_MAIN);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SECTION);
        header.setBackground(BTN_SECONDARY_BG);
        header.setForeground(TEXT_MAIN);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 30));
    }

    /**
     * Creates a card panel with a clean border and white background.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return panel;
    }

    /**
     * Creates a titled card border with consistent font and teal color.
     */
    public static Border createTitledCardBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                new LineBorder(BORDER_COLOR, 1),
                " " + title + " ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                FONT_SECTION,
                PRIMARY_COLOR
        );
        return new CompoundBorder(new EmptyBorder(4, 4, 4, 4), tb);
    }

    /**
     * Creates a standardized bottom navigation bar for the Dashboard screen.
     */
    public static JPanel createDashboardNavBar(JButton mealLogBtn, JButton goalBtn,
                                               JButton recBtn, JButton repBtn,
                                               JButton profBtn, JButton logoutBtn) {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        navBar.setBackground(CARD_BG);
        navBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(4, 16, 6, 16)
        ));

        stylePrimaryButton(mealLogBtn);
        stylePrimaryButton(goalBtn);
        stylePrimaryButton(recBtn);
        stylePrimaryButton(repBtn);
        stylePrimaryButton(profBtn);
        styleDangerButton(logoutBtn);

        navBar.add(mealLogBtn);
        navBar.add(goalBtn);
        navBar.add(recBtn);
        navBar.add(repBtn);
        navBar.add(profBtn);
        navBar.add(logoutBtn);

        return navBar;
    }

    /**
     * Creates a standardized bottom footer navigation bar for secondary screens.
     * Places the 'Back to Dashboard' button consistently on the left, with optional
     * screen action buttons placed on the right.
     */
    public static JPanel createSecondaryNavBar(JButton backBtn, Component... rightComponents) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(CARD_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(8, 16, 8, 16)
        ));

        styleSecondaryButton(backBtn);
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(CARD_BG);
        leftPanel.add(backBtn);
        footer.add(leftPanel, BorderLayout.WEST);

        if (rightComponents != null && rightComponents.length > 0) {
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightPanel.setBackground(CARD_BG);
            for (Component comp : rightComponents) {
                if (comp != null) {
                    rightPanel.add(comp);
                }
            }
            footer.add(rightPanel, BorderLayout.EAST);
        }

        return footer;
    }

    /**
     * Seamlessly transitions from currentFrame to nextFrame without screen flicker.
     * Ensures the destination frame is fully initialized and visible before disposing
     * the current frame, keeping the maximized window state consistent.
     */
    public static void transition(JFrame currentFrame, JFrame nextFrame) {
        if (nextFrame != null) {
            if (currentFrame != null) {
                nextFrame.setExtendedState(currentFrame.getExtendedState());
            }
            nextFrame.setVisible(true);
        }
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }

    /**
     * Displays a standardized error dialog (JOptionPane.ERROR_MESSAGE).
     *
     * @param parent The parent Swing component (frame or dialog)
     * @param message The error message to display
     * @param title The dialog title
     */
    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays an error dialog with default "Error" title.
     *
     * @param parent The parent Swing component
     * @param message The error message to display
     */
    public static void showError(Component parent, String message) {
        showError(parent, message, "Error");
    }

    /**
     * Displays a standardized warning dialog (JOptionPane.WARNING_MESSAGE).
     *
     * @param parent The parent Swing component
     * @param message The warning message to display
     * @param title The dialog title
     */
    public static void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Displays a standardized informational dialog (JOptionPane.INFORMATION_MESSAGE).
     *
     * @param parent The parent Swing component
     * @param message The informational message to display
     * @param title The dialog title
     */
    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
