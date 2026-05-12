package com.particle.sim.ui;

import com.particle.sim.AppInfo;
import com.particle.sim.particles.GpuParticleSystem;
import imgui.ImGui;
import org.lwjgl.Version;

import static org.lwjgl.opengl.GL43C.GL_RENDERER;
import static org.lwjgl.opengl.GL43C.GL_SHADING_LANGUAGE_VERSION;
import static org.lwjgl.opengl.GL43C.GL_VENDOR;
import static org.lwjgl.opengl.GL43C.GL_VERSION;
import static org.lwjgl.opengl.GL43C.glGetString;

final class DebugPanel {
    private String glVendor;
    private String glRenderer;
    private String glVersion;
    private String glslVersion;

    void render(float deltaTime, float currentFps, GpuParticleSystem particles) {
        cacheOpenGlInfo();

        ImGui.begin("Debug");
        renderPerformance(deltaTime, currentFps);
        renderSimulationInternals(particles);
        renderRuntime();
        renderGraphics();
        ImGui.end();
    }

    private void renderPerformance(float deltaTime, float currentFps) {
        ImGui.separatorText("Performance");
        ImGui.text("FPS: %.0f".formatted(currentFps));
        ImGui.text("Frame time: %.2f ms".formatted(deltaTime * 1000.0f));
    }

    private void renderSimulationInternals(GpuParticleSystem particles) {
        int gridSize = particles.gridSize();

        ImGui.separatorText("Simulation Internals");
        ImGui.text("Particles: %,d / %,d".formatted(particles.particleCount(), particles.maxParticleCount()));
        ImGui.text("Grid: %d x %d x %d".formatted(gridSize, gridSize, gridSize));
        ImGui.text("Grid cells: %,d".formatted(gridSize * gridSize * gridSize));
        ImGui.text("Cell capacity: %,d".formatted(particles.maxParticlesPerCell()));
        ImGui.text("Groups: %d".formatted(particles.groupCount()));
        ImGui.text("Color mode: %s".formatted(particles.colorMode()));
        ImGui.text("Spawn mode: %s".formatted(particles.spawnMode()));
    }

    private void renderRuntime() {
        ImGui.separatorText("Runtime");
        ImGui.text("App version: %s".formatted(AppInfo.version()));
        ImGui.text("Java version: %s".formatted(System.getProperty("java.version", "unknown")));
        ImGui.text("JVM: %s".formatted(System.getProperty("java.vm.name", "unknown")));
        ImGui.text("OS: %s %s".formatted(
                System.getProperty("os.name", "unknown"),
                System.getProperty("os.version", "unknown")));
    }

    private void renderGraphics() {
        ImGui.separatorText("Graphics");
        ImGui.text("ImGui version: %s".formatted(ImGui.getVersion()));
        ImGui.text("LWJGL version: %s".formatted(Version.getVersion()));
        ImGui.text("OpenGL version: %s".formatted(glVersion));
        ImGui.text("GLSL version: %s".formatted(glslVersion));
        ImGui.text("OpenGL vendor: %s".formatted(glVendor));
        ImGui.text("OpenGL renderer: %s".formatted(glRenderer));
    }

    private void cacheOpenGlInfo() {
        if (glVersion != null) {
            return;
        }

        glVendor = stringOrUnknown(glGetString(GL_VENDOR));
        glRenderer = stringOrUnknown(glGetString(GL_RENDERER));
        glVersion = stringOrUnknown(glGetString(GL_VERSION));
        glslVersion = stringOrUnknown(glGetString(GL_SHADING_LANGUAGE_VERSION));
    }

    private String stringOrUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
