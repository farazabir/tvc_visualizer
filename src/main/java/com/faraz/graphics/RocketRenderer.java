package com.faraz.graphics;

import com.faraz.model.TelemetryData;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import javax.swing.*;
import java.awt.event.*;

public class RocketRenderer extends GLJPanel implements GLEventListener, MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {

    private final TelemetryData telemetryData;
    private final GLU glu = new GLU();
    private final FPSAnimator animator;

    // Camera controls
    private float cameraDistance = 15.0f;
    private float cameraRotationX = 20.0f;
    private float cameraRotationY = 0.0f;
    private int lastMouseX, lastMouseY;
    private boolean mousePressed = false;

    // Animation
    private float engineGlow = 0.0f;
    private long lastTime = System.currentTimeMillis();

    // TVC neutral positions (center positions for servos)
    private double neutralServoX = 90.0; // Adjust this to your servo's center position
    private double neutralServoY = 90.0; // Adjust this to your servo's center position

    // Debug
    private boolean debugMode = false;

    public RocketRenderer(TelemetryData telemetryData) {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        this.telemetryData = telemetryData;

        addGLEventListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setFocusable(true);

        animator = new FPSAnimator(this, 60);
        animator.start();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // Basic setup
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glClearColor(0.05f, 0.05f, 0.15f, 1.0f);

        // Simple lighting
        float[] lightPos = {10.0f, 10.0f, 10.0f, 1.0f};
        float[] lightColor = {1.0f, 1.0f, 1.0f, 1.0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightColor, 0);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        updateAnimation();
        setupCamera(gl);

        drawStars(gl);
        drawRocket(gl);

        if (debugMode) drawAxes(gl);

        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();

        if (height <= 0) height = 1;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, (double) width / height, 0.1, 100.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTime) / 1000.0f;
        lastTime = currentTime;

        if (telemetryData != null && telemetryData.isConnected()) {
            // Calculate TVC activity based on deflection from neutral, not absolute values
            double servoXDeflection = telemetryData.getServoX() - neutralServoX;
            double servoYDeflection = telemetryData.getServoY() - neutralServoY;

            double tvcActivity = Math.sqrt(
                    Math.pow(servoXDeflection, 2) +
                            Math.pow(servoYDeflection, 2)) / 50.0;
            float targetGlow = (float) Math.min(1.0, 0.3 + tvcActivity * 0.7);
            engineGlow += (targetGlow - engineGlow) * deltaTime * 5.0f;
        } else {
            engineGlow *= 0.95f;
        }
    }

    private void setupCamera(GL2 gl) {
        gl.glTranslatef(0.0f, 0.0f, -cameraDistance);
        gl.glRotatef(cameraRotationX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(cameraRotationY, 0.0f, 1.0f, 0.0f);
    }

    private void drawStars(GL2 gl) {
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glPointSize(1.5f);

        gl.glBegin(GL.GL_POINTS);
        for (int i = 0; i < 100; i++) {
            float x = (float) (Math.random() - 0.5) * 100;
            float y = (float) (Math.random() - 0.5) * 100;
            float z = (float) (Math.random() - 0.5) * 100;
            gl.glVertex3f(x, y, z);
        }
        gl.glEnd();

        gl.glEnable(GL2.GL_LIGHTING);
    }

    private void drawRocket(GL2 gl) {
        gl.glPushMatrix();

        // Apply rocket orientation from telemetry
        if (telemetryData != null && telemetryData.isConnected() && !debugMode) {
            gl.glRotatef((float) telemetryData.getRoll(), 0.0f, 0.0f, 1.0f);
            gl.glRotatef((float) telemetryData.getPitch(), 1.0f, 0.0f, 0.0f);
        }

        // Main body - pure white and slimmer
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        drawSimpleCylinder(gl, 0.35f, 4.0f);

        // Nose tube - pure white and slimmer
        gl.glPushMatrix();
        gl.glTranslatef(0.0f, 2.0f, 0.0f);
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        drawSimpleCylinder(gl, 0.35f, 1.0f); // Changed from drawSimpleCone to drawSimpleCylinder
        gl.glPopMatrix();

        // Fins - pure white
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        drawFins(gl);

        // TVC nozzle and exhaust - MOVED INSIDE rocket matrix
        drawTVCSystem(gl);

        gl.glPopMatrix();
    }

    private void drawTVCSystem(GL2 gl) {
        // Move to rocket bottom and apply TVC gimbal rotations
        gl.glPushMatrix();
        gl.glTranslatef(0.0f, -2.0f, 0.0f);

        // Apply servo rotations (TVC gimbal) - relative to neutral position
        if (telemetryData != null && telemetryData.isConnected()) {
            // Calculate deflection angles relative to neutral positions
            double servoXDeflection = telemetryData.getServoX() - neutralServoX;
            double servoYDeflection = telemetryData.getServoY() - neutralServoY;

            // Apply deflection rotations (small angles around neutral)
            // ServoX controls pitch (rotation around X axis)
            gl.glRotatef((float) servoXDeflection, 1.0f, 0.0f, 0.0f);
            // ServoY controls yaw (rotation around Z axis)
            gl.glRotatef((float) servoYDeflection, 0.0f, 0.0f, 1.0f);
        }

        // Draw TVC nozzle - pure white
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        drawSimpleCone(gl, 0.25f, 0.6f);

        // Draw exhaust flame in the same coordinate system
        drawExhaust(gl);

        gl.glPopMatrix();
    }

    private void drawExhaust(GL2 gl) {
        if (engineGlow < 0.1f) return;

        gl.glPushMatrix();
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // Move flame down from nozzle exit
        gl.glTranslatef(0.0f, -0.6f, 0.0f);

        float flameLength = 1.5f + engineGlow;

        // Multi-layer flame effect
        gl.glColor4f(1.0f, 0.8f, 0.2f, engineGlow * 0.8f);
        drawSimpleCone(gl, 0.25f, flameLength);

        gl.glColor4f(1.0f, 0.4f, 0.0f, engineGlow * 0.6f);
        drawSimpleCone(gl, 0.35f, flameLength * 0.7f);

        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glPopMatrix();
    }

    private void drawFins(GL2 gl) {
        for (int i = 0; i < 4; i++) {
            gl.glPushMatrix();
            gl.glRotatef(i * 90.0f, 0.0f, 1.0f, 0.0f);
            gl.glTranslatef(0.35f, -1.0f, 0.0f);

            // Simple triangular fin
            gl.glBegin(GL2.GL_TRIANGLES);
            gl.glNormal3f(0.0f, 0.0f, 1.0f);
            gl.glVertex3f(0.0f, 0.0f, 0.0f);
            gl.glVertex3f(0.6f, -0.3f, 0.0f);
            gl.glVertex3f(0.0f, -0.8f, 0.0f);
            gl.glEnd();

            gl.glPopMatrix();
        }
    }

    private void drawSimpleCylinder(GL2 gl, float radius, float height) {
        int slices = 16;

        // Side faces
        for (int i = 0; i < slices; i++) {
            float angle1 = (float) (2.0 * Math.PI * i / slices);
            float angle2 = (float) (2.0 * Math.PI * (i + 1) / slices);

            float x1 = radius * (float) Math.cos(angle1);
            float z1 = radius * (float) Math.sin(angle1);
            float x2 = radius * (float) Math.cos(angle2);
            float z2 = radius * (float) Math.sin(angle2);

            gl.glBegin(GL2.GL_QUADS);
            gl.glNormal3f(x1, 0.0f, z1);
            gl.glVertex3f(x1, -height/2, z1);
            gl.glVertex3f(x1, height/2, z1);
            gl.glNormal3f(x2, 0.0f, z2);
            gl.glVertex3f(x2, height/2, z2);
            gl.glVertex3f(x2, -height/2, z2);
            gl.glEnd();
        }
    }

    private void drawSimpleCone(GL2 gl, float radius, float height) {
        int slices = 12;

        for (int i = 0; i < slices; i++) {
            float angle1 = (float) (2.0 * Math.PI * i / slices);
            float angle2 = (float) (2.0 * Math.PI * (i + 1) / slices);

            float x1 = radius * (float) Math.cos(angle1);
            float z1 = radius * (float) Math.sin(angle1);
            float x2 = radius * (float) Math.cos(angle2);
            float z2 = radius * (float) Math.sin(angle2);

            gl.glBegin(GL2.GL_TRIANGLES);
            gl.glNormal3f(x1, 0.5f, z1);
            gl.glVertex3f(x1, 0.0f, z1);
            gl.glVertex3f(0.0f, -height, 0.0f);
            gl.glVertex3f(x2, 0.0f, z2);
            gl.glEnd();
        }
    }

    private void drawAxes(GL2 gl) {
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glLineWidth(3.0f);

        gl.glBegin(GL.GL_LINES);
        gl.glColor3f(1.0f, 0.0f, 0.0f); // X - Red
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(3.0f, 0.0f, 0.0f);

        gl.glColor3f(0.0f, 1.0f, 0.0f); // Y - Green
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, 3.0f, 0.0f);

        gl.glColor3f(0.0f, 0.0f, 1.0f); // Z - Blue
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, 3.0f);
        gl.glEnd();

        gl.glEnable(GL2.GL_LIGHTING);
    }

    // Mouse controls
    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mousePressed) {
            int deltaX = e.getX() - lastMouseX;
            int deltaY = e.getY() - lastMouseY;

            cameraRotationY += deltaX * 0.5f;
            cameraRotationX += deltaY * 0.5f;
            cameraRotationX = Math.max(-90.0f, Math.min(90.0f, cameraRotationX));

            lastMouseX = e.getX();
            lastMouseY = e.getY();
            repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        cameraDistance += e.getWheelRotation() * 1.5f;
        cameraDistance = Math.max(5.0f, Math.min(50.0f, cameraDistance));
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_R: // Reset camera
                cameraDistance = 15.0f;
                cameraRotationX = 20.0f;
                cameraRotationY = 0.0f;
                break;
            case KeyEvent.VK_D: // Toggle debug
                debugMode = !debugMode;
                break;
        }
        repaint();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        if (animator != null) {
            animator.stop();
        }
    }

    public void cleanup() {
        if (animator != null) {
            animator.stop();
        }
    }

    // Method to set neutral servo positions
    public void setNeutralServoPositions(double neutralX, double neutralY) {
        this.neutralServoX = neutralX;
        this.neutralServoY = neutralY;
    }

    // Method to auto-calibrate neutral positions from current telemetry
    public void calibrateNeutralPositions() {
        if (telemetryData != null && telemetryData.isConnected()) {
            this.neutralServoX = telemetryData.getServoX();
            this.neutralServoY = telemetryData.getServoY();
            System.out.println("Neutral positions set to: X=" + neutralServoX + ", Y=" + neutralServoY);
        }
    }

    // Unused interface methods
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}