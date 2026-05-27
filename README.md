# 3D Particle Simulator

An interactive 3D particle-life sandbox built in Java. It simulates thousands of small particles that attract, repel, cluster, drift apart, and sometimes form surprisingly organic patterns.

This project is inspired by **Particle Life** by **Tom Mohr**, extended here into 3D.

## What It Is

Particle life is a simple idea with complex results: each particle belongs to a group, and each group has its own relationship with every other group. One color might be attracted to another, pushed away by a third, or ignore itself entirely.

When many particles follow these rules at the same time, larger patterns start to appear. Groups can form swarms, membranes, trails, pulsing clouds, or unstable life-like structures. The motion emerges from the attraction and repulsion rules.

This application takes that idea into 3D. This allows for more complex and interesting structure to form.

## Features

- Real-time 3D particle simulation
- Color-coded particle groups
- Free-look camera
- Live UI controls
- Wrap-around bounds
- Density regulation
- Visual effects

## Built With

- **Java 21** as the main programming language
- **Maven** for building and running the project
- **OpenGL** for computing and rendering the particles
- **imgui-java** for the settings UI
- **JUnit 5** for automated tests

## Requirements

- Java 21 or newer
- Maven
- A GPU and driver with OpenGL 4.3 support

## How To Run

From the project folder, run:

```powershell
mvn compile exec:java
```

The first launch may take a little longer while Maven downloads the required libraries.

## Controls

- **Left-click**: enter free-look camera mode
- **Right-click** or **Esc**: leave simulation focus
- **W / A / S / D**: move through the scene
- **Shift**: move up
- **Ctrl**: move down
- **Space**: pause or resume the simulation
- **R**: reset the particles
- **Home**: reset the camera
- **Esc**: exit application

## Experimenting

Use the UI panels to change how the simulation behaves while it is running. The attraction matrix works as follows:

- Green values pull groups together
- Red values push groups apart
- Neutral values have little or no effect

The simulator also includes spawn controls, physics settings, and visual options so you can quickly try different particle counts and behaviors.

## Particle life
This project is inspired and based on particle life of Tom Mohr. For more information, please watch his video about it

[![Watch the video](https://img.youtube.com/vi/p4YirERTVF0/maxresdefault.jpg)](https://youtu.be/p4YirERTVF0)
