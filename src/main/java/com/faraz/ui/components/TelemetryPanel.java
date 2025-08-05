package com.faraz.ui.components;

import com.faraz.model.TelemetryData;
import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * Professional telemetry display panel
 * Shows real-time rocket data with color-coded status indicators
 */
public class TelemetryPanel extends JPanel {
    private final TelemetryData telemetryData;
    private TelemetryDisplay[] displays;

    // UI Colors
    private static final Color BG_PANEL = new Color(25, 35, 50);
    private static final Color ACCENT_BLUE = new Color(64, 150, 255);
    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(180, 180, 180);
    private static final Color STATUS_OK = new Color(50, 200, 100);
    private static final Color STATUS_WARN = new Color(255, 165, 0);
    private static final Color STATUS_ERROR = new Color(255, 80, 80);

    public TelemetryPanel(TelemetryData telemetryData) {
        this.telemetryData = telemetryData;

        setupLayout();
        createTelemetryDisplays();
        startUpdateTimer();
    }

    private void setupLayout() {
        setLayout(new GridLayout(9, 1, 0, 8));
        setBackground(BG_PANEL);
        setPreferredSize(new Dimension(300, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
    }

    private void createTelemetryDisplays() {
        displays = new TelemetryDisplay[] {
                new TelemetryDisplay("PITCH", "°", () -> telemetryData.getPitch()),
                new TelemetryDisplay("ROLL", "°", () -> telemetryData.getRoll()),
                new TelemetryDisplay("PID PITCH", "", () -> telemetryData.getPidPitch()),
                new TelemetryDisplay("PID ROLL", "", () -> telemetryData.getPidRoll()),
                new TelemetryDisplay("SERVO X", "°", () -> (double)telemetryData.getServoX()),
                new TelemetryDisplay("SERVO Y", "°", () -> (double)telemetryData.getServoY()),
                new TelemetryDisplay("BUFFER", "", () -> (double)telemetryData.getBufferCount()),
                new TelemetryDisplay("ALTITUDE", "m", () -> telemetryData.getAltitude()),
                new TelemetryDisplay("TVC ACTIVITY", "%", () -> telemetryData.getTvcActivity())
        };

        for (TelemetryDisplay display : displays) {
            add(display);
        }
    }

    private void startUpdateTimer() {
        Timer updateTimer = new Timer(50, e -> {
            for (TelemetryDisplay display : displays) {
                display.updateValue();
            }
        });
        updateTimer.start();
    }

    /**
     * Individual telemetry display component
     */
    private class TelemetryDisplay extends JPanel {
        private final String label;
        private final String unit;
        private final Supplier<Double> valueProvider;
        private final JLabel valueLabel;

        public TelemetryDisplay(String label, String unit, Supplier<Double> valueProvider) {
            this.label = label;
            this.unit = unit;
            this.valueProvider = valueProvider;

            setupLayout();
            this.valueLabel = createValueLabel();
            addComponents();
        }

        private void setupLayout() {
            setLayout(new BorderLayout());
            setBackground(new Color(15, 20, 30));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 60, 80), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        }

        private JLabel createValueLabel() {
            JLabel label = new JLabel("0.00" + unit);
            label.setFont(new Font("Consolas", Font.BOLD, 16));
            label.setForeground(TEXT_PRIMARY);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            return label;
        }

        private void addComponents() {
            JLabel nameLabel = new JLabel(label);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 11));
            nameLabel.setForeground(TEXT_SECONDARY);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

            add(nameLabel, BorderLayout.NORTH);
            add(valueLabel, BorderLayout.CENTER);
        }

        public void updateValue() {
            double value = valueProvider.get();
            String formatted;

            // Format based on value type
            if (label.contains("PID")) {
                formatted = String.format("%.3f%s", value, unit);
            } else if (label.equals("TVC ACTIVITY")) {
                formatted = String.format("%.1f%s", value, unit);
            } else {
                formatted = String.format("%.2f%s", value, unit);
            }

            valueLabel.setText(formatted);

            // Apply color coding based on parameter type and value
            Color textColor = getStatusColor(value);
            valueLabel.setForeground(textColor);
        }

        private Color getStatusColor(double value) {
            switch (label) {
                case "PITCH":
                case "ROLL":
                    return Math.abs(value) > 15 ? STATUS_ERROR :
                            Math.abs(value) > 8 ? STATUS_WARN : TEXT_PRIMARY;

                case "BUFFER":
                    return value > 800 ? STATUS_ERROR :
                            value > 500 ? STATUS_WARN : STATUS_OK;

                case "TVC ACTIVITY":
                    return value > 70 ? STATUS_ERROR :
                            value > 40 ? STATUS_WARN :
                                    value > 10 ? STATUS_OK : TEXT_SECONDARY;

                case "SERVO X":
                case "SERVO Y":
                    double deflection = Math.abs(value - 90);
                    return deflection > 25 ? STATUS_ERROR :
                            deflection > 15 ? STATUS_WARN : TEXT_PRIMARY;

                default:
                    return TEXT_PRIMARY;
            }
        }
    }
}