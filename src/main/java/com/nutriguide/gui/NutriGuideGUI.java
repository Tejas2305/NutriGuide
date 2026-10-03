package com.nutriguide.gui;

import javax.swing.SwingUtilities;

/**
 * NutriGuideGUI: Main entry point for the NutriGuide Swing GUI application.
 *
 * Demonstrates:
 * - Basic Java Swing application initialization
 * - Launching LoginFrame on the Event Dispatch Thread (EDT) using an anonymous Runnable
 */
public class NutriGuideGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}
