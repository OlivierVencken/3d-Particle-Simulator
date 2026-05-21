package com.particle.sim.ui;

import com.particle.sim.AppInfo;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.system.SystemLoadMonitor;
import com.particle.sim.system.SystemLoadSnapshot;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import org.lwjgl.Version;

import java.util.function.IntConsumer;

import static org.lwjgl.opengl.GL43C.GL_RENDERER;
import static org.lwjgl.opengl.GL43C.GL_SHADING_LANGUAGE_VERSION;
import static org.lwjgl.opengl.GL43C.GL_VENDOR;
import static org.lwjgl.opengl.GL43C.GL_VERSION;
import static org.lwjgl.opengl.GL43C.glGetString;

final class DebugPanel {
    private final SystemLoadMonitor systemLoadMonitor = new SystemLoadMonitor();

    private String glVendor;
    private String glRenderer;
    private String glVersion;
    private String glslVersion;

    void render(float deltaTime, float currentFps, int fpsCap, IntConsumer fpsCapChanged, Runnable settingsChanged,
            GpuParticleSystem particles) {
        cacheOpenGlInfo();

        ImGui.begin("Debug");
        renderPerformance(deltaTime, currentFps, fpsCap, fpsCapChanged, settingsChanged);
        renderSimulationInternals(particles);
        renderRuntime();
        renderGraphics();
        ImGui.end();
    }

    private void renderPerformance(float deltaTime, float currentFps, int fpsCap, IntConsumer fpsCapChanged,
            Runnable settingsChanged) {
        ImGui.separatorText("Performance");
        ImGui.text("FPS: %.0f".formatted(currentFps));
        ImGui.text("Frame time: %.2f ms".formatted(deltaTime * 1000.0f));
        renderSystemLoad();

        ImBoolean unlimitedFps = new ImBoolean(fpsCap <= 0);
        if (ImGui.checkbox("Unlimited FPS", unlimitedFps)) {
            fpsCapChanged.accept(unlimitedFps.get() ? 0 : SimulationDefaults.FPS_CAP);
            settingsChanged.run();
        }

        if (!unlimitedFps.get()) {
            ImInt fpsCapRef = new ImInt(fpsCap);
            ImGui.setNextItemWidth(120.0f);
            if (ImGui.inputInt("FPS cap", fpsCapRef, 5, 15)) {
                fpsCapChanged.accept(fpsCapRef.get());
                settingsChanged.run();
            }
        }
    }

    private void renderSystemLoad() {
        SystemLoadSnapshot load = systemLoadMonitor.snapshot();

        ImGui.textUnformatted("CPU load: %s".formatted(formatLoad(load.cpuLoad())));
        ImGui.textUnformatted("GPU load: %s".formatted(formatLoad(load.gpuLoad())));
        ImGui.textUnformatted("RAM usage: %s".formatted(formatMemoryUsage(load.usedMemoryBytes(), load.totalMemoryBytes())));
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
        ImGui.text("Simulation step: %.2f ms (%.0f Hz)".formatted(
                SimulationDefaults.SIMULATION_STEP_SECONDS * 1000.0,
                1.0 / SimulationDefaults.SIMULATION_STEP_SECONDS));
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

    private String formatLoad(double load) {
        return load < 0.0 ? "unknown" : "%.0f%%".formatted(load * 100.0);
    }

    private String formatMemoryUsage(long usedBytes, long totalBytes) {
        if (usedBytes < 0L || totalBytes <= 0L) {
            return "unknown";
        }

        return "%s / %s".formatted(formatBytes(usedBytes), formatBytes(totalBytes));
    }

    private String formatBytes(long bytes) {
        double gib = bytes / 1024.0 / 1024.0 / 1024.0;
        if (gib >= 1.0) {
            return "%.1f GB".formatted(gib);
        }

        double mib = bytes / 1024.0 / 1024.0;
        return "%.0f MB".formatted(mib);
    }
}
