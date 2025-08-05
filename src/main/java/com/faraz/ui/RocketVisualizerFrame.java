package com.faraz.ui;

import com.faraz.communication.SerialReader;
import com.faraz.model.TelemetryData;
import com.faraz.ui.components.TelemetryPanel;
import com.faraz.ui.components.VisualizationPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Enhanced main application frame for TVC Rocket Control Center
 * Features improved 3D visualization and TVC monitoring
 */
public class RocketVisualizerFrame extends JFrame implements KeyListener {
    private final TelemetryData telemetryData;
    private final SerialReader serialReader;
    private Timer renderTimer;
    private Timer statusTimer;

    // UI Components
    private JLabel connectionLabel;
    private JLabel tvcStatusLabel;
    private JLabel frameRateLabel;

    // Performance monitoring
    private long lastFrameTime = System.currentTimeMillis();
    private int frameCount = 0;
    private double currentFPS = 0;

    // UI Colors - Enhanced for 3D theme
    private static final Color BG_DARK = new Color(12, 15, 25);
    private static final Color BG_PANEL = new Color(22, 28, 42);
    private static final Color BG_PANEL_LIGHT = new Color(32, 40, 58);
    private static final Color ACCENT_BLUE = new Color(64, 150, 255);
    private static final Color ACCENT_CYAN = new Color(64, 255, 200);
    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(180, 190, 200);
    private static final Color STATUS_OK = new Color(50, 255, 120);
    private static final Color STATUS_WARNING = new Color(255, 180, 50);
    private static final Color STATUS_ERROR = new Color(255, 80, 80);
    private static final Color TVC_ACTIVE = new Color(255, 165, 0);

    // Configuration
    private static final String SERIAL_PORT = "/dev/ttyACM0";
    private static final int BAUD_RATE = 500000;
    private static final int TARGET_FPS = 60;

    public RocketVisualizerFrame() {
        this.telemetryData = new TelemetryData();
        this.serialReader = new SerialReader(telemetryData, SERIAL_PORT, BAUD_RATE);

        setupFrame();
        createComponents();
        startSystems();

        // Add keyboard listener for controls
        addKeyListener(this);
        setFocusable(true);
    }

    /**
     * Enhanced frame setup with better styling
     */
    private void setupFrame() {
        setTitle("TVC 3D Rocket Control Center v2.1 - Enhanced Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 1000);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1300, 800));

        // Enhanced background
        getContentPane().setBackground(BG_DARK);

        // Try to set system look and feel for better integration
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            // Use default if system L&F not available
        }
    }

    /**
     * Create enhanced UI components with 3D theme
     */
    private void createComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create enhanced header panel
        JPanel headerPanel = createEnhancedHeaderPanel();

        // Create main content panels
        VisualizationPanel visualizationPanel = new VisualizationPanel(telemetryData);
        TelemetryPanel telemetryPanel = new TelemetryPanel(telemetryData);

        // Create footer panel for additional info
        JPanel footerPanel = createFooterPanel();

        // Layout components
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(visualizationPanel, BorderLayout.CENTER);
        mainPanel.add(telemetryPanel, BorderLayout.EAST);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Create enhanced header panel with comprehensive status monitoring
     */
    private JPanel createEnhancedHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_PANEL);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 2),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        // Left section - Title and version
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(BG_PANEL);

        JLabel titleLabel = new JLabel(" TVC 3D ROCKET STABILIZATION MONITOR");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel versionLabel = new JLabel("v2.1 Enhanced");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        versionLabel.setForeground(ACCENT_CYAN);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 0));

        titlePanel.add(titleLabel);
        titlePanel.add(versionLabel);

        // Center section - TVC Status
        JPanel tvcPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        tvcPanel.setBackground(BG_PANEL);

        JLabel tvcLabel = new JLabel("TVC STATUS:");
        tvcLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        tvcLabel.setForeground(TEXT_SECONDARY);

        tvcStatusLabel = new JLabel("● INITIALIZING");
        tvcStatusLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        tvcStatusLabel.setForeground(STATUS_WARNING);

        tvcPanel.add(tvcLabel);
        tvcPanel.add(tvcStatusLabel);

        // Right section - Connection and performance
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statusPanel.setBackground(BG_PANEL);

        frameRateLabel = new JLabel("FPS: --");
        frameRateLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        frameRateLabel.setForeground(TEXT_SECONDARY);

        JLabel portLabel = new JLabel("Port: " + SERIAL_PORT);
        portLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        portLabel.setForeground(TEXT_SECONDARY);

        connectionLabel = new JLabel("● INITIALIZING");
        connectionLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        connectionLabel.setForeground(STATUS_WARNING);

        statusPanel.add(frameRateLabel);
        statusPanel.add(new JLabel("|") {{ setForeground(TEXT_SECONDARY); }});
        statusPanel.add(portLabel);
        statusPanel.add(connectionLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(tvcPanel, BorderLayout.CENTER);
        headerPanel.add(statusPanel, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Create footer panel with controls and system info
     */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(BG_PANEL_LIGHT);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Left section - Controls info
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        controlsPanel.setBackground(BG_PANEL_LIGHT);

        JLabel controlsLabel = new JLabel("CONTROLS:");
        controlsLabel.setFont(new Font("Consolas", Font.BOLD, 11));
        controlsLabel.setForeground(TEXT_SECONDARY);

        JLabel spaceLabel = new JLabel("[SPACE] Pause | [R] Reset | [ESC] Exit");
        spaceLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        spaceLabel.setForeground(TEXT_SECONDARY);

        controlsPanel.add(controlsLabel);
        controlsPanel.add(spaceLabel);

        // Right section - System info
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        infoPanel.setBackground(BG_PANEL_LIGHT);

        JLabel renderLabel = new JLabel("3D RENDERER: ACTIVE");
        renderLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        renderLabel.setForeground(ACCENT_CYAN);

        JLabel baudLabel = new JLabel("BAUD: " + BAUD_RATE);
        baudLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        baudLabel.setForeground(TEXT_SECONDARY);

        infoPanel.add(renderLabel);
        infoPanel.add(new JLabel("|") {{ setForeground(TEXT_SECONDARY); }});
        infoPanel.add(baudLabel);

        footerPanel.add(controlsPanel, BorderLayout.WEST);
        footerPanel.add(infoPanel, BorderLayout.EAST);

        return footerPanel;
    }

    /**
     * Start all system components with enhanced monitoring
     */
    private void startSystems() {
        // Start serial communication
        boolean serialStarted = serialReader.start();
        if (!serialStarted) {
            showConnectionError();
        }

        // Start render timer with precise timing
        renderTimer = new Timer(1000 / TARGET_FPS, e -> {
            repaint();
            updateFrameRate();
        });
        renderTimer.start();

        // Start status update timer
        statusTimer = new Timer(500, e -> updateAllStatus());
        statusTimer.start();

        System.out.println("Enhanced TVC 3D Control Center started successfully");
        System.out.println("Target FPS: " + TARGET_FPS);
        System.out.println("Serial Port: " + SERIAL_PORT + " @ " + BAUD_RATE + " baud");
        System.out.println("3D Renderer: ACTIVE");
        System.out.println("TVC Physics: STABILIZATION MODE");
    }

    /**
     * Update frame rate calculation
     */
    private void updateFrameRate() {
        frameCount++;
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFrameTime >= 1000) {
            currentFPS = frameCount * 1000.0 / (currentTime - lastFrameTime);
            frameCount = 0;
            lastFrameTime = currentTime;

            // Update FPS display
            frameRateLabel.setText(String.format("FPS: %.1f", currentFPS));

            // Color code FPS based on performance
            if (currentFPS >= TARGET_FPS * 0.9) {
                frameRateLabel.setForeground(STATUS_OK);
            } else if (currentFPS >= TARGET_FPS * 0.7) {
                frameRateLabel.setForeground(STATUS_WARNING);
            } else {
                frameRateLabel.setForeground(STATUS_ERROR);
            }
        }
    }

    /**
     * Update all status indicators
     */
    private void updateAllStatus() {
        updateConnectionStatus();
        updateTVCStatus();
    }

    /**
     * Enhanced connection status display
     */
    private void updateConnectionStatus() {
        if (telemetryData.isConnected() && telemetryData.isDataFresh()) {
            connectionLabel.setText("● CONNECTED");
            connectionLabel.setForeground(STATUS_OK);
        } else if (telemetryData.isConnected()) {
            connectionLabel.setText("● CONNECTED (STALE)");
            connectionLabel.setForeground(STATUS_WARNING);
        } else {
            connectionLabel.setText("● DISCONNECTED");
            connectionLabel.setForeground(STATUS_ERROR);
        }
    }

    /**
     * Update TVC system status
     */
    private void updateTVCStatus() {
        if (!telemetryData.isConnected()) {
            tvcStatusLabel.setText("● OFFLINE");
            tvcStatusLabel.setForeground(STATUS_ERROR);
            return;
        }

        // Calculate TVC activity level
        double pidActivity = Math.sqrt(
                Math.pow(telemetryData.getPidPitch(), 2) +
                        Math.pow(telemetryData.getPidRoll(), 2)
        );

        // Determine TVC status based on activity
        if (pidActivity > 20) {
            tvcStatusLabel.setText("● ACTIVE CORRECTION");
            tvcStatusLabel.setForeground(TVC_ACTIVE);
        } else if (pidActivity > 5) {
            tvcStatusLabel.setText("● MINOR CORRECTION");
            tvcStatusLabel.setForeground(STATUS_WARNING);
        } else {
            tvcStatusLabel.setText("● STABLE");
            tvcStatusLabel.setForeground(STATUS_OK);
        }
    }

    /**
     * Enhanced connection error dialog
     */
    private void showConnectionError() {
        SwingUtilities.invokeLater(() -> {
            String message = String.format(
                    "Failed to establish connection with TVC system on %s\n\n" +
                            "TROUBLESHOOTING CHECKLIST:\n" +
                            "✓ ESP32 connected and powered on\n" +
                            "✓ USB cable functional (try different cable)\n" +
                            "✓ Serial port permissions (sudo usermod -a -G dialout $USER)\n" +
                            "✓ Correct port identified (%s)\n" +
                            "✓ TVC firmware uploaded and running\n" +
                            "✓ No other applications using the port\n\n" +
                            "The 3D visualizer will continue running and attempt to reconnect.\n" +
                            "Check the connection status indicator in the header.",
                    SERIAL_PORT, SERIAL_PORT
            );

            JOptionPane.showMessageDialog(this, message,
                    "TVC System Connection Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Keyboard controls implementation
     */
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                // Toggle pause/resume
                if (renderTimer.isRunning()) {
                    renderTimer.stop();
                    setTitle(getTitle() + " [PAUSED]");
                } else {
                    renderTimer.start();
                    setTitle(getTitle().replace(" [PAUSED]", ""));
                }
                break;

            case KeyEvent.VK_R:
                // Reset telemetry data
                if (telemetryData != null) {
                    // Add reset method to TelemetryData if available
                    System.out.println("Telemetry data reset requested");
                }
                break;

            case KeyEvent.VK_ESCAPE:
                // Graceful exit
                shutdown();
                System.exit(0);
                break;

            case KeyEvent.VK_F1:
                // Show help dialog
                showHelpDialog();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * Show help dialog with controls and information
     */
    private void showHelpDialog() {
        String helpText =
                "TVC 3D ROCKET CONTROL CENTER - HELP\n\n" +
                        "KEYBOARD CONTROLS:\n" +
                        "SPACE - Pause/Resume visualization\n" +
                        "R - Reset telemetry data\n" +
                        "ESC - Exit application\n" +
                        "F1 - Show this help dialog\n\n" +
                        "TVC SYSTEM OPERATION:\n" +
                        "• Engine gimbal actively counters rocket rotation\n" +
                        "• PID controller calculates correction signals\n" +
                        "• 3D visualization shows real-time TVC movement\n" +
                        "• Status indicators show system health\n\n" +
                        "TROUBLESHOOTING:\n" +
                        "• Check serial connection if data appears stale\n" +
                        "• Verify ESP32 power and firmware\n" +
                        "• Monitor FPS for performance issues\n\n" +
                        "For more information, check the documentation.";

        JOptionPane.showMessageDialog(this, helpText,
                "TVC Control Center Help", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Enhanced graceful shutdown
     */
    public void shutdown() {
        System.out.println("Shutting down Enhanced TVC 3D Control Center...");

        // Stop all timers
        if (renderTimer != null) {
            renderTimer.stop();
            System.out.println("Render timer stopped");
        }

        if (statusTimer != null) {
            statusTimer.stop();
            System.out.println("Status timer stopped");
        }

        // Stop serial communication
        if (serialReader != null) {
            serialReader.stop();
            System.out.println("Serial communication stopped");
        }

        System.out.println("Enhanced shutdown complete - all systems offline");
    }

    /**
     * Enhanced window closing event handler
     */
    @Override
    protected void processWindowEvent(java.awt.event.WindowEvent e) {
        if (e.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING) {
            shutdown();
        }
        super.processWindowEvent(e);
    }

    /**
     * Get current system performance metrics
     */
    public double getCurrentFPS() {
        return currentFPS;
    }

    /**
     * Get TVC system status
     */
    public boolean isTVCActive() {
        if (!telemetryData.isConnected()) return false;

        double pidActivity = Math.sqrt(
                Math.pow(telemetryData.getPidPitch(), 2) +
                        Math.pow(telemetryData.getPidRoll(), 2)
        );

        return pidActivity > 5; // Threshold for "active" TVC
    }
}