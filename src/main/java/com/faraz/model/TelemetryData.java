package com.faraz.model;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe telemetry data container
 * Holds all rocket sensor and control data
 */
public class TelemetryData {
    // Orientation data (degrees)
    private final AtomicReference<Double> pitch = new AtomicReference<>(0.0);
    private final AtomicReference<Double> roll = new AtomicReference<>(0.0);

    // PID controller outputs
    private final AtomicReference<Double> pidPitch = new AtomicReference<>(0.0);
    private final AtomicReference<Double> pidRoll = new AtomicReference<>(0.0);

    // Servo positions (degrees, 90 = center)
    private final AtomicReference<Integer> servoX = new AtomicReference<>(90);
    private final AtomicReference<Integer> servoY = new AtomicReference<>(90);

    // System status
    private final AtomicReference<Integer> bufferCount = new AtomicReference<>(0);
    private final AtomicReference<Double> altitude = new AtomicReference<>(0.0);
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicReference<Long> lastUpdateTime = new AtomicReference<>(0L);

    // Getters
    public double getPitch() { return pitch.get(); }
    public double getRoll() { return roll.get(); }
    public double getPidPitch() { return pidPitch.get(); }
    public double getPidRoll() { return pidRoll.get(); }
    public int getServoX() { return servoX.get(); }
    public int getServoY() { return servoY.get(); }
    public int getBufferCount() { return bufferCount.get(); }
    public double getAltitude() { return altitude.get(); }
    public boolean isConnected() { return isConnected.get(); }
    public long getLastUpdateTime() { return lastUpdateTime.get(); }

    // Setters
    public void setPitch(double value) {
        pitch.set(value);
        updateTimestamp();
    }

    public void setRoll(double value) {
        roll.set(value);
        updateTimestamp();
    }

    public void setPidPitch(double value) { pidPitch.set(value); }
    public void setPidRoll(double value) { pidRoll.set(value); }

    public void setServoX(int value) {
        servoX.set(Math.max(0, Math.min(180, value))); // Clamp 0-180
    }

    public void setServoY(int value) {
        servoY.set(Math.max(0, Math.min(180, value))); // Clamp 0-180
    }

    public void setBufferCount(int value) { bufferCount.set(value); }
    public void setAltitude(double value) { altitude.set(value); }
    public void setConnected(boolean value) { isConnected.set(value); }

    private void updateTimestamp() {
        lastUpdateTime.set(System.currentTimeMillis());
    }

    /**
     * Calculate TVC activity percentage (0-100%)
     */
    public double getTvcActivity() {
        double deflectionX = Math.abs(getServoX() - 90) / 90.0;
        double deflectionY = Math.abs(getServoY() - 90) / 90.0;
        return (deflectionX + deflectionY) / 2.0 * 100.0;
    }

    /**
     * Calculate rocket stability percentage (0-100%)
     */
    public double getStability() {
        double pitchError = Math.abs(getPitch());
        double rollError = Math.abs(getRoll());
        double totalError = pitchError + rollError;
        return Math.max(0, 100 - totalError * 2); // 2 deg error = 4% stability loss
    }

    /**
     * Get servo deflection angles from center
     */
    public int getServoXDeflection() { return getServoX() - 90; }
    public int getServoYDeflection() { return getServoY() - 90; }

    /**
     * Check if data is recent (within last 2 seconds)
     */
    public boolean isDataFresh() {
        return (System.currentTimeMillis() - getLastUpdateTime()) < 2000;
    }
}