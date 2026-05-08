# 3D Particle Simulator

Java + LWJGL + OpenGL compute shaders + ImGui.

## Requirements

- Java 21 or newer
- OpenGL 4.3 capable GPU/driver

## Run

```powershell
mvn compile exec:java
```

The first run downloads LWJGL and imgui-java dependencies.

## Camera Controls

- Left mouse drag: rotate
- Middle mouse drag, or Shift + left mouse drag: pan
- Mouse wheel: zoom
- WASD: move through the scene
- Space / Ctrl: move up and down
- Home: reset camera

## Simulation Model

Particles are assigned to color-coded groups. Each compute step first populates a 3D spatial grid, then evaluates nearby particles from neighboring grid cells using a group attraction matrix, short-range repulsion, velocity damping, and bounded-space collisions.

The attraction matrix window edits group-to-group forces live. Left-click a tile to increase attraction, right-click to increase repulsion. Green cells attract, grey cells are neutral, and red cells repel.

Wrap-around mode can be enabled in the Physics section. When enabled, particles use 3D toroidal distance and wrap through the simulation bounds instead of bouncing.

The Spawn section can add/remove particles with presets, clear the scene, or add a custom amount. Existing particles keep their current simulated state; newly spawned particles are randomly distributed through the simulation volume and across groups.

## Project Layout

- `src/main/java/com/particle/sim/ParticleSimulatorApp.java` - GLFW window, OpenGL loop, frame orchestration
- `src/main/java/com/particle/sim/camera/CameraController.java` - camera movement, rotation, zoom, and pan
- `src/main/java/com/particle/sim/particles/GpuParticleSystem.java` - particle buffers, compute dispatch, and rendering
- `src/main/java/com/particle/sim/ui/SimulationUi.java` - ImGui controls
- `src/main/java/com/particle/sim/graphics/ShaderProgram.java` - shader loading, compilation, and linking
- `src/main/java/com/particle/sim/math/Math3d.java` - small matrix/vector helpers
- `src/main/resources/shaders/particle.comp` - GPU simulation compute shader
- `src/main/resources/shaders/particle.vert` - particle point vertex shader
- `src/main/resources/shaders/particle.frag` - particle fragment shader
