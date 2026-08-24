package com.particle.sim.ui.components;

import com.particle.sim.AppInfo;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.system.SystemLoadMonitor;
import com.particle.sim.system.SystemLoadSnapshot;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiDiagnostics;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import org.lwjgl.Version;

import static org.lwjgl.opengl.GL43C.GL_RENDERER;
import static org.lwjgl.opengl.GL43C.GL_SHADING_LANGUAGE_VERSION;
import static org.lwjgl.opengl.GL43C.GL_VENDOR;
import static org.lwjgl.opengl.GL43C.GL_VERSION;
import static org.lwjgl.opengl.GL43C.glGetString;

public final class DebugPanel {
    private final SystemLoadMonitor systemLoadMonitor = new SystemLoadMonitor();
    private final ImBoolean unlimitedFps = new ImBoolean();
    private final ImInt fpsCapRef = new ImInt();

    private String glVendor;
    private String glRenderer;
    private String glVersion;
    private String glslVersion;

    public void render(float deltaTime, float currentFps, SimulationUiModel model,
            SimulationUiActions actions, ImBoolean open) {
        cacheOpenGlInfo();

        if (ImGui.begin("Debug", open)) {
            renderPerformance(deltaTime, currentFps, model.application().fpsCap(), actions.application());
            renderSimulationInternals(model.performance().diagnostics());
            renderRuntime();
            renderGraphics();
        }
        ImGui.end();
    }

    private void renderPerformance(float deltaTime, float currentFps, int fpsCap,
            SimulationUiActions.Application actions) {
        ImGui.separatorText("Performance");
        ImGui.text("FPS: %.0f".formatted(currentFps));
        ImGui.text("Frame time: %.2f ms".formatted(deltaTime * 1000.0f));
        renderSystemLoad();

        unlimitedFps.set(fpsCap <= 0);
        if (UIControls.checkbox("Unlimited FPS", "debug-unlimited-fps", unlimitedFps)) {
            actions.setFpsCap(unlimitedFps.get() ? 0 : SimulationDefaults.FPS_CAP);
        }

        if (!unlimitedFps.get()) {
            fpsCapRef.set(fpsCap);
            if (UIIntegerInput.render("FPS cap", "debug-fps-cap", fpsCapRef, 5, 15,
                    SimulationDefaults.MIN_FPS_CAP, SimulationDefaults.MAX_FPS_CAP,
                    UITheme.tokens().debugInputWidth())) {
                actions.setFpsCap(fpsCapRef.get());
            }
        }
    }

    private void renderSystemLoad() {
        SystemLoadSnapshot load = systemLoadMonitor.snapshot();

        ImGui.textUnformatted("CPU load: %s".formatted(formatLoad(load.cpuLoad())));
        ImGui.textUnformatted("GPU load: %s".formatted(formatLoad(load.gpuLoad())));
        ImGui.textUnformatted("RAM usage: %s".formatted(formatMemoryUsage(load.usedMemoryBytes(), load.totalMemoryBytes())));
    }

    private void renderSimulationInternals(SimulationUiDiagnostics diagnostics) {
        ImGui.separatorText("Simulation Internals");
        ImGui.text("Particles: %,d / %,d".formatted(
                diagnostics.particleCount(), diagnostics.maximumParticleCount()));
        ImGui.text("Grid: %d x %d x %d".formatted(
                diagnostics.gridSize(), diagnostics.gridSize(), diagnostics.gridSize()));
        ImGui.text("Grid cells: %,d".formatted(diagnostics.gridCellCount()));
        ImGui.textUnformatted("Cell storage: exact compact ranges");
        ImGui.text("GPU simulation: %s".formatted(formatMilliseconds(diagnostics.simulationMilliseconds())));
        ImGui.text("  Count / scan / scatter: %s / %s / %s".formatted(
                formatMilliseconds(diagnostics.gridCountMilliseconds()),
                formatMilliseconds(diagnostics.gridScanMilliseconds()),
                formatMilliseconds(diagnostics.gridScatterMilliseconds())));
        ImGui.text("  Force integration: %s".formatted(formatMilliseconds(diagnostics.integrationMilliseconds())));
        ImGui.text("GPU particles / trails / bloom: %s / %s / %s".formatted(
                formatMilliseconds(diagnostics.particleRenderMilliseconds()),
                formatMilliseconds(diagnostics.trailRenderMilliseconds()),
                formatMilliseconds(diagnostics.bloomMilliseconds())));
        ImGui.text("Estimated GPU buffers: %s".formatted(formatBytes(diagnostics.allocatedGpuBytes())));
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

    private String formatMilliseconds(double milliseconds) {
        return milliseconds < 0.0 ? "pending" : "%.3f ms".formatted(milliseconds);
    }
}
