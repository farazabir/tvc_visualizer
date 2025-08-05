package com.faraz.communication;

import com.faraz.model.TelemetryData;
import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serial communication handler for ESP32 TVC system
 * Parses telemetry data and updates the data model
 */
public class SerialReader {
    private final TelemetryData telemetryData;
    private final String portName;
    private final int baudRate;
    private volatile boolean isRunning = false;
    private Thread readerThread;

    // Regex pattern for parsing ESP32 output
    private static final Pattern TELEMETRY_PATTERN = Pattern.compile(
            "P:([-\\d.]+) R:([-\\d.]+) \\| PID P:([-\\d.]+) R:([-\\d.]+) \\| SRV X:(\\d+) Y:(\\d+) \\| BUF:(\\d+)(?:: \\| ALT:([-\\d.]+)m)?"
    );

    public SerialReader(TelemetryData telemetryData, String portName, int baudRate) {
        this.telemetryData = telemetryData;
        this.portName = portName;
        this.baudRate = baudRate;
    }

    /**
     * Start reading serial data in background thread
     */
    public boolean start() {
        if (isRunning) {
            System.out.println("Serial reader already running");
            return true;
        }

        readerThread = new Thread(this::serialReaderLoop, "SerialReader");
        readerThread.setDaemon(true);
        isRunning = true;
        readerThread.start();

        return true;
    }

    /**
     * Stop serial reader
     */
    public void stop() {
        isRunning = false;
        if (readerThread != null) {
            readerThread.interrupt();
        }
        telemetryData.setConnected(false);
    }

    /**
     * Main serial reading loop
     */
    private void serialReaderLoop() {
        SerialPort port = SerialPort.getCommPort(portName);
        port.setBaudRate(baudRate);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

        System.out.println("Attempting to connect to: " + portName + " @ " + baudRate + " baud");

        if (!port.openPort()) {
            System.err.println("Failed to open serial port: " + portName);
            telemetryData.setConnected(false);
            return;
        }

        System.out.println("✅ Connected to TVC system: " + portName);
        telemetryData.setConnected(true);

        try (InputStream input = port.getInputStream()) {
            StringBuilder buffer = new StringBuilder();

            while (isRunning && !Thread.currentThread().isInterrupted()) {
                int byteRead = input.read();
                if (byteRead == -1) continue;

                char c = (char) byteRead;
                if (c == '\n') {
                    String line = buffer.toString().trim();
                    buffer.setLength(0);

                    if (!line.isEmpty()) {
                        parseTelemetryLine(line);
                    }
                } else {
                    buffer.append(c);
                }
            }

        } catch (Exception e) {
            if (isRunning) { // Only log if not intentionally stopped
                System.err.println("Serial communication error: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            port.closePort();
            telemetryData.setConnected(false);
            System.out.println("Serial port closed: " + portName);
        }
    }

    /**
     * Parse telemetry line and update data model
     */
    private void parseTelemetryLine(String line) {
        try {
            Matcher matcher = TELEMETRY_PATTERN.matcher(line);
            if (matcher.find()) {
                // Parse orientation data
                double pitch = Double.parseDouble(matcher.group(1));
                double roll = Double.parseDouble(matcher.group(2));

                // Parse PID outputs
                double pidPitch = Double.parseDouble(matcher.group(3));
                double pidRoll = Double.parseDouble(matcher.group(4));

                // Parse servo positions
                int servoX = Integer.parseInt(matcher.group(5));
                int servoY = Integer.parseInt(matcher.group(6));

                // Parse system status
                int bufferCount = Integer.parseInt(matcher.group(7));

                // Parse altitude (optional)
                double altitude = 0.0;
                if (matcher.group(8) != null) {
                    altitude = Double.parseDouble(matcher.group(8));
                }

                // Update telemetry data atomically
                updateTelemetryData(pitch, roll, pidPitch, pidRoll, servoX, servoY, bufferCount, altitude);

            } else {
                // Log unmatched lines for debugging
                if (line.contains("P:") && line.contains("R:")) {
                    System.out.println("Failed to parse: " + line);
                }
            }

        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in line: " + line);
        } catch (Exception e) {
            System.err.println("Error parsing telemetry: " + e.getMessage());
        }
    }

    /**
     * Update telemetry data with new values
     */
    private void updateTelemetryData(double pitch, double roll, double pidPitch, double pidRoll,
                                     int servoX, int servoY, int bufferCount, double altitude) {
        telemetryData.setPitch(pitch);
        telemetryData.setRoll(roll);
        telemetryData.setPidPitch(pidPitch);
        telemetryData.setPidRoll(pidRoll);
        telemetryData.setServoX(servoX);
        telemetryData.setServoY(servoY);
        telemetryData.setBufferCount(bufferCount);
        telemetryData.setAltitude(altitude);
    }

    /**
     * Get current connection status
     */
    public boolean isConnected() {
        return isRunning && telemetryData.isConnected();
    }
}