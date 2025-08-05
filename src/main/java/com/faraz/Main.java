package com.faraz;

import com.faraz.model.TelemetryData;
import com.faraz.ui.RocketVisualizerFrame;
import javax.swing.*;

/**
 * Main application entry point
 * Professional TVC Rocket Control Center
 */
public class Main {
    public static void main(String[] args) {
        
        TelemetryData telemetryData = new TelemetryData();
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        // Enable hardware acceleration
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.uiScale", "1.0");

        // Launch application on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                RocketVisualizerFrame frame = new RocketVisualizerFrame( );
                frame.setVisible(true);

                // Graceful shutdown
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("Shutting down TVC Control Center...");
                    frame.shutdown();
                }));

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start TVC Control Center: " + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}