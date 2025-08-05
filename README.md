# Thrust Vector Control (TVC) Visualizer

![TVC Visualizer Demo](/gif/tvc.gif)

A real-time 3D visualization tool for rocket thrust vector control systems. Connects to a flight computer via serial to display orientation (pitch/roll), servo positions, and stability metrics.

## Features

- 🚀 Real-time 3D rocket visualization
- 📊 Telemetry data display (pitch, roll, servo positions)
- 🔄 Dynamic TVC nozzle movement
- 🔥 Animated engine exhaust effects
- 🎮 Interactive camera controls (rotate, zoom)
- 📡 Serial communication at 50,000 baud

## Hardware Requirements

- Flight computer with serial output
- USB serial connection
- TVC system with X/Y servo control

## Software Requirements

- Java 8 or higher
- JOGL (OpenGL bindings for Java) - included in project
- Serial port drivers for your flight computer

## Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/tvc-visualizer.git
   ```
2. Navigate to the project directory:
   ```bash
   cd tvc-visualizer
   ```

## Usage

1. Connect your flight computer via USB.
2. Run the visualizer:
   ```bash
   java -jar TVCVisualizer.jar
   ```
3. Configure serial port settings:
   - Select correct COM port
   - Set baud rate to 50,000
   - Click "Connect" to start visualization

## Controls

- **Mouse drag**: Rotate view
- **Mouse wheel**: Zoom in/out
- **R key**: Reset camera
- **D key**: Toggle debug mode (show axes)

## Data Format

The visualizer expects serial data in the following format:
```
PITCH:23.5,ROLL:12.1,SERVO_X:92,SERVO_Y:88,STABILITY:0.85\n
```

