package com.particle.sim.ui.components;

import com.particle.sim.AppInfo;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.system.SystemLoadMonitor;
import com.particle.sim.system.SystemLoadSnapshot;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiDiagnostics;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.theme.UITheme;
import com.particle.sim.ui.theme.UIFonts;
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
    private static final float DIAGNOSTIC_REFRESH_SECONDS = 0.25f;
    private final SystemLoadMonitor systemLoadMonitor = new SystemLoadMonitor();
    private final ImBoolean unlimitedFps = new ImBoolean();
    private final ImInt fpsCapRef = new ImInt();

    private String glVendor;
    private String glRenderer;
    private String glVersion;
    private String glslVersion;
    private float diagnosticRefreshAccumulator = DIAGNOSTIC_REFRESH_SECONDS;
    private String particleDiagnostics = "";
    private String gridDimensions = "";
    private String gridCells = "";
    private String gpuSimulation = "";
    private String gpuStages = "";
    private String gpuIntegration = "";
    private String gpuRendering = "";
    private String gpuBuffers = "";

    public void render(float deltaTime, float currentFps, SimulationUiModel model,
            SimulationUiActions actions, ImBoolean open) {
        cacheOpenGlInfo();

        if (ImGui.begin("Debug", open)) {
            ImGui.pushFont(UIFonts.medium());
            try {
                renderPerformance(deltaTime, currentFps, model.application().fpsCap(), actions.application());
                renderSimulationInternals(deltaTime, model.performance().diagnostics());
                renderRuntime();
                renderGraphics();
            } finally {
                ImGui.popFont();
            }
        }
        ImGui.end();
    }

    private void renderPerformance(float deltaTime, float currentFps, int fpsCap,
            SimulationUiActions.Application actions) {
        UIText.sectionHeading("Performance");
        UIMetric.row("FPS", "%.0f".formatted(currentFps));
        UIMetric.row("Frame time", "%.2f ms".formatted(deltaTime * 1000.0f));
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

        UIMetric.row("CPU load", formatLoad(load.cpuLoad()));
        UIMetric.row("GPU load", formatLoad(load.gpuLoad()));
        UIMetric.row("RAM usage", formatMemoryUsage(load.usedMemoryBytes(), load.totalMemoryBytes()));
    }

    private void renderSimulationInternals(float deltaTime, SimulationUiDiagnostics diagnostics) {
        diagnosticRefreshAccumulator += Math.max(0.0f, deltaTime);
        if (diagnosticRefreshAccumulator >= DIAGNOSTIC_REFRESH_SECONDS) {
            updateDiagnosticStrings(diagnostics);
            diagnosticRefreshAccumulator = 0.0f;
        }

        UIText.divider();
        UIText.sectionHeading("Simulation internals");
        UIMetric.row("Particles", particleDiagnostics);
        UIMetric.row("Grid", gridDimensions);
        UIMetric.row("Grid cells", gridCells);
        UIMetric.row("Cell storage", "Exact compact ranges");
        UIMetric.row("GPU simulation", gpuSimulation);
        UIMetric.row("Count / scan / scatter", gpuStages);
        UIMetric.row("Force integration", gpuIntegration);
        UIMetric.row("Particles / trails / bloom", gpuRendering);
        UIMetric.row("Estimated GPU buffers", gpuBuffers);
        UIMetric.row("Simulation step", "%.2f ms (%.0f Hz)".formatted(
                SimulationDefaults.SIMULATION_STEP_SECONDS * 1000.0,
                1.0 / SimulationDefaults.SIMULATION_STEP_SECONDS));
    }

    private void updateDiagnosticStrings(SimulationUiDiagnostics diagnostics) {
        particleDiagnostics = "%,d / %,d".formatted(
                diagnostics.particleCount(), diagnostics.maximumParticleCount());
        gridDimensions = "%d × %d × %d".formatted(
                diagnostics.gridSize(), diagnostics.gridSize(), diagnostics.gridSize());
        gridCells = "%,d".formatted(diagnostics.gridCellCount());
        gpuSimulation = formatMilliseconds(diagnostics.simulationMilliseconds());
        gpuStages = "%s / %s / %s".formatted(
                formatMilliseconds(diagnostics.gridCountMilliseconds()),
                formatMilliseconds(diagnostics.gridScanMilliseconds()),
                formatMilliseconds(diagnostics.gridScatterMilliseconds()));
        gpuIntegration = formatMilliseconds(diagnostics.integrationMilliseconds());
        gpuRendering = "%s / %s / %s".formatted(
                formatMilliseconds(diagnostics.particleRenderMilliseconds()),
                formatMilliseconds(diagnostics.trailRenderMilliseconds()),
                formatMilliseconds(diagnostics.bloomMilliseconds()));
        gpuBuffers = formatBytes(diagnostics.allocatedGpuBytes());
    }

    private void renderRuntime() {
        UIText.divider();
        UIText.sectionHeading("Runtime");
        UIMetric.row("App version", AppInfo.version());
        UIMetric.row("Java version", System.getProperty("java.version", "unknown"));
        UIMetric.row("JVM", System.getProperty("java.vm.name", "unknown"));
        UIMetric.row("OS", "%s %s".formatted(
                System.getProperty("os.name", "unknown"),
                System.getProperty("os.version", "unknown")));
    }

    private void renderGraphics() {
        UIText.divider();
        UIText.sectionHeading("Graphics");
        UIMetric.row("ImGui version", ImGui.getVersion());
        UIMetric.row("LWJGL version", Version.getVersion());
        UIMetric.row("OpenGL version", glVersion);
        UIMetric.row("GLSL version", glslVersion);
        UIMetric.row("OpenGL vendor", glVendor);
        UIMetric.row("OpenGL renderer", glRenderer);
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
