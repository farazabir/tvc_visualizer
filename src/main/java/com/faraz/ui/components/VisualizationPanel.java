package com.faraz.ui.components;

import com.faraz.graphics.RocketRenderer;
import com.faraz.model.TelemetryData;
import javax.swing.*;
import java.awt.*;

/**
 * Main rocket visualization panel with HUD overlay
 */
public class VisualizationPanel extends JPanel {
    private final TelemetryData telemetryData;
    private final RocketRenderer rocketRenderer;

    // UI Colors
    private static final Color BG_PANEL = new Color(25, 35, 50);
    private static final Color ACCENT_BLUE = new Color(64, 150, 255);
    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(180, 180, 180);
    private static final Color STATUS_OK = new Color(50, 200, 100);
    private static final Color STATUS_WARN = new Color(255, 165, 0);
    private static final Color STATUS_ERROR = new Color(255, 80, 80);
    private static final Color GRID_COLOR = new Color(40, 50, 70, 100);

    public VisualizationPanel(TelemetryData telemetryData) {
        this.telemetryData = telemetryData;
        this.rocketRenderer = new RocketRenderer(telemetryData);

        // FIXED: Use OverlayLayout instead of BorderLayout
        setLayout(new OverlayLayout(this));
        setBackground(BG_PANEL);
        setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 1));

        // Add the OpenGL renderer first (background)
        rocketRenderer.setAlignmentX(Component.CENTER_ALIGNMENT);
        rocketRenderer.setAlignmentY(Component.CENTER_ALIGNMENT);
        add(rocketRenderer);

        // Add HUD overlay on top
        HUDOverlay hudOverlay = new HUDOverlay();
        hudOverlay.setAlignmentX(Component.CENTER_ALIGNMENT);
        hudOverlay.setAlignmentY(Component.CENTER_ALIGNMENT);
        add(hudOverlay);

        System.out.println("VisualizationPanel created with RocketRenderer and HUD overlay");
    }

    /**
     * Inner class for HUD overlay that draws over the 3D renderer
     */
    private class HUDOverlay extends JPanel {
        public HUDOverlay() {
            setOpaque(false); // Make transparent so 3D renders through
            setBackground(new Color(0, 0, 0, 0)); // Fully transparent
        }

        @Override
        public Dimension getPreferredSize() {
            // Match the parent size
            Container parent = getParent();
            if (parent != null) {
                return parent.getSize();
            }
            return new Dimension(800, 600);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Enable anti-aliasing for smooth graphics
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Draw HUD overlay
            drawHUD(g2d);

            // Draw connection status if disconnected
            if (!telemetryData.isConnected()) {
                drawDisconnectedOverlay(g2d);
            }
        }
    }

    /**
     * Draw HUD overlay with flight information
     */
    private void drawHUD(Graphics2D g2d) {
        g2d.setFont(new Font("Consolas", Font.BOLD, 12));

        int x = 20;
        int y = 30;
        int lineHeight = 20;

        // TVC Status
        double tvcActivity = telemetryData.getTvcActivity();
        g2d.setColor(TEXT_SECONDARY);
        g2d.drawString("TVC SYSTEM STATUS", x, y);
        y += lineHeight;

        Color activityColor = tvcActivity > 70 ? STATUS_ERROR :
                tvcActivity > 40 ? STATUS_WARN : STATUS_OK;
        g2d.setColor(activityColor);
        g2d.drawString(String.format("Activity: %.1f%%", tvcActivity), x, y);
        y += lineHeight;

        // Gimbal angles
        g2d.setColor(TEXT_PRIMARY);
        g2d.drawString(String.format("Gimbal: X%+d° Y%+d°",
                telemetryData.getServoXDeflection(),
                telemetryData.getServoYDeflection()), x, y);
        y += lineHeight;

        // Stability indicator
        double stability = telemetryData.getStability();
        Color stabilityColor = stability > 80 ? STATUS_OK :
                stability > 50 ? STATUS_WARN : STATUS_ERROR;
        g2d.setColor(stabilityColor);
        g2d.drawString(String.format("Stability: %.0f%%", stability), x, y);
        y += lineHeight * 2;

        // Flight data
        g2d.setColor(TEXT_SECONDARY);
        g2d.drawString("FLIGHT DATA", x, y);
        y += lineHeight;

        g2d.setColor(TEXT_PRIMARY);
        g2d.drawString(String.format("Pitch: %+.2f°", telemetryData.getPitch()), x, y);
        y += lineHeight;
        g2d.drawString(String.format("Roll:  %+.2f°", telemetryData.getRoll()), x, y);
        y += lineHeight;
        g2d.drawString(String.format("Alt:   %.1fm", telemetryData.getAltitude()), x, y);

        // Performance indicator (top right)
        drawPerformanceIndicator(g2d);
    }

    /**
     * Draw real-time performance indicator
     */
    private void drawPerformanceIndicator(Graphics2D g2d) {
        int x = getWidth() - 150;
        int y = 30;

        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.setColor(TEXT_SECONDARY);
        g2d.drawString("SYSTEM PERFORMANCE", x, y);

        // Data freshness indicator
        boolean dataFresh = telemetryData.isDataFresh();
        g2d.setColor(dataFresh ? STATUS_OK : STATUS_ERROR);
        g2d.fillOval(x, y + 10, 8, 8);
        g2d.setColor(TEXT_PRIMARY);
        g2d.drawString(dataFresh ? "Data Fresh" : "Data Stale", x + 15, y + 18);

        // Buffer status
        int bufferCount = telemetryData.getBufferCount();
        Color bufferColor = bufferCount > 800 ? STATUS_ERROR :
                bufferCount > 500 ? STATUS_WARN : STATUS_OK;
        g2d.setColor(bufferColor);
        g2d.fillOval(x, y + 25, 8, 8);
        g2d.setColor(TEXT_PRIMARY);
        g2d.drawString(String.format("Buffer: %d", bufferCount), x + 15, y + 33);
    }

    /**
     * Draw disconnected overlay
     */
    private void drawDisconnectedOverlay(Graphics2D g2d) {
        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Disconnected message
        g2d.setColor(STATUS_ERROR);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        String message = "TVC SYSTEM DISCONNECTED";
        int messageWidth = fm.stringWidth(message);
        g2d.drawString(message, (getWidth() - messageWidth) / 2, getHeight() / 2);

        // Connection instructions
        g2d.setColor(TEXT_SECONDARY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        fm = g2d.getFontMetrics();
        String instruction = "Check serial connection and restart application";
        int instructionWidth = fm.stringWidth(instruction);
        g2d.drawString(instruction, (getWidth() - instructionWidth) / 2, getHeight() / 2 + 40);
    }

    /**
     * Cleanup method to properly dispose of OpenGL resources
     */
    public void cleanup() {
        if (rocketRenderer != null) {
            rocketRenderer.cleanup();
        }
    }
}