package com.particle.sim.ui.components;

import static org.lwjgl.opengl.GL43C.GL_RENDERER;
import static org.lwjgl.opengl.GL43C.GL_SHADING_LANGUAGE_VERSION;
import static org.lwjgl.opengl.GL43C.GL_VENDOR;
import static org.lwjgl.opengl.GL43C.GL_VERSION;
import static org.lwjgl.opengl.GL43C.glGetString;

import com.particle.sim.app.AppInfo;
import com.particle.sim.diagnostics.SystemLoadMonitor;
import com.particle.sim.diagnostics.SystemLoadSnapshot;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewDiagnostics;
import com.particle.sim.ui.SimulationViewModel;
import com.particle.sim.ui.theme.Fonts;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import org.lwjgl.Version;

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

    public void render(
            float deltaTime,
            float currentFps,
            SimulationViewModel model,
            SimulationViewActions actions,
            ImBoolean open) {
        cacheOpenGlInfo();

        if (ImGui.begin("Debug", open)) {
            ImGui.pushFont(Fonts.medium());
            try {
                renderPerformance(
                        deltaTime, currentFps, model.application().fpsCap(), actions.application());
                renderSimulationInternals(deltaTime, model.performance().diagnostics());
                renderRuntime();
                renderGraphics();
            } finally {
                ImGui.popFont();
            }
        }
        ImGui.end();
    }

    private void renderPerformance(
            float deltaTime,
            float currentFps,
            int fpsCap,
            SimulationViewActions.Application actions) {
        Text.sectionHeading("Performance");
        Metric.row("FPS", "%.0f".formatted(currentFps));
        Metric.row("Frame time", "%.2f ms".formatted(deltaTime * 1000.0f));
        renderSystemLoad();

        unlimitedFps.set(fpsCap <= 0);
        if (Controls.checkbox("Unlimited FPS", "debug-unlimited-fps", unlimitedFps)) {
            actions.setFpsCap(unlimitedFps.get() ? 0 : SimulationDefaults.FPS_CAP);
        }

        if (!unlimitedFps.get()) {
            fpsCapRef.set(fpsCap);
            if (IntegerInput.render(
                    "FPS cap",
                    "debug-fps-cap",
                    fpsCapRef,
                    5,
                    15,
                    SimulationDefaults.MIN_FPS_CAP,
                    SimulationDefaults.MAX_FPS_CAP,
                    Theme.tokens().debugInputWidth())) {
                actions.setFpsCap(fpsCapRef.get());
            }
        }
    }

    private void renderSystemLoad() {
        SystemLoadSnapshot load = systemLoadMonitor.snapshot();

        Metric.row("CPU load", formatLoad(load.cpuLoad()));
        Metric.row("GPU load", formatLoad(load.gpuLoad()));
        Metric.row("RAM usage", formatMemoryUsage(load.usedMemoryBytes(), load.totalMemoryBytes()));
    }

    private void renderSimulationInternals(float deltaTime, SimulationViewDiagnostics diagnostics) {
        diagnosticRefreshAccumulator += Math.max(0.0f, deltaTime);
        if (diagnosticRefreshAccumulator >= DIAGNOSTIC_REFRESH_SECONDS) {
            updateDiagnosticStrings(diagnostics);
            diagnosticRefreshAccumulator = 0.0f;
        }

        Text.divider();
        Text.sectionHeading("Simulation internals");
        Metric.row("Particles", particleDiagnostics);
        Metric.row("Grid", gridDimensions);
        Metric.row("Grid cells", gridCells);
        Metric.row("Cell storage", "Exact compact ranges");
        Metric.row("GPU simulation", gpuSimulation);
        Metric.row("Count / scan / scatter", gpuStages);
        Metric.row("Force integration", gpuIntegration);
        Metric.row("Particles / trails / bloom", gpuRendering);
        Metric.row("Estimated GPU buffers", gpuBuffers);
        Metric.row(
                "Simulation step",
                "%.2f ms (%.0f Hz)"
                        .formatted(
                                SimulationDefaults.SIMULATION_STEP_SECONDS * 1000.0,
                                1.0 / SimulationDefaults.SIMULATION_STEP_SECONDS));
    }

    private void updateDiagnosticStrings(SimulationViewDiagnostics diagnostics) {
        particleDiagnostics =
                "%,d / %,d"
                        .formatted(diagnostics.particleCount(), diagnostics.maximumParticleCount());
        gridDimensions =
                "%d × %d × %d"
                        .formatted(
                                diagnostics.gridSize(),
                                diagnostics.gridSize(),
                                diagnostics.gridSize());
        gridCells = "%,d".formatted(diagnostics.gridCellCount());
        gpuSimulation = formatMilliseconds(diagnostics.simulationMilliseconds());
        gpuStages =
                "%s / %s / %s"
                        .formatted(
                                formatMilliseconds(diagnostics.gridCountMilliseconds()),
                                formatMilliseconds(diagnostics.gridScanMilliseconds()),
                                formatMilliseconds(diagnostics.gridScatterMilliseconds()));
        gpuIntegration = formatMilliseconds(diagnostics.integrationMilliseconds());
        gpuRendering =
                "%s / %s / %s"
                        .formatted(
                                formatMilliseconds(diagnostics.particleRenderMilliseconds()),
                                formatMilliseconds(diagnostics.trailRenderMilliseconds()),
                                formatMilliseconds(diagnostics.bloomMilliseconds()));
        gpuBuffers = formatBytes(diagnostics.allocatedGpuBytes());
    }

    private void renderRuntime() {
        Text.divider();
        Text.sectionHeading("Runtime");
        Metric.row("App version", AppInfo.version());
        Metric.row("Java version", System.getProperty("java.version", "unknown"));
        Metric.row("JVM", System.getProperty("java.vm.name", "unknown"));
        Metric.row(
                "OS",
                "%s %s"
                        .formatted(
                                System.getProperty("os.name", "unknown"),
                                System.getProperty("os.version", "unknown")));
    }

    private void renderGraphics() {
        Text.divider();
        Text.sectionHeading("Graphics");
        Metric.row("ImGui version", ImGui.getVersion());
        Metric.row("LWJGL version", Version.getVersion());
        Metric.row("OpenGL version", glVersion);
        Metric.row("GLSL version", glslVersion);
        Metric.row("OpenGL vendor", glVendor);
        Metric.row("OpenGL renderer", glRenderer);
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
