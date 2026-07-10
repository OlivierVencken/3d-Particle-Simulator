package com.particle.sim.ui;

import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SimulationDefaults;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

final class WorkspaceStatusBar {
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;

    void render(WorkspaceLayout.Panel panel, boolean paused, GpuParticleSystem particles) {
        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        if (ImGui.begin("##workspace-status", WINDOW_FLAGS)) {
            ImGui.textUnformatted(paused ? "Paused" : "Running");
            ImGui.sameLine(0.0f, 16.0f);
            ImGui.textDisabled("Fixed %.0f Hz".formatted(1.0 / SimulationDefaults.SIMULATION_STEP_SECONDS));
            String qualityMessage = qualityMessage(particles);
            if (!qualityMessage.isEmpty() && panel.width() >= 720.0f) {
                float messageWidth = ImGui.calcTextSize(qualityMessage).x;
                ImGui.sameLine(Math.max(ImGui.getCursorPosX() + 8.0f, panel.width() - messageWidth - 12.0f));
                ImGui.textDisabled(qualityMessage);
            }
        }
        ImGui.end();
    }

    static String qualityMessage(GpuParticleSystem particles) {
        if (particles.effectiveTrailParticleStride() > 1) {
            return "Adaptive quality: trails sample 1/%d particles"
                    .formatted(particles.effectiveTrailParticleStride());
        }
        if (particles.effectiveTrailLength() > 0 && particles.effectiveTrailLength() < particles.trailLength()) {
            return "Adaptive quality: trail length reduced to %d".formatted(particles.effectiveTrailLength());
        }
        if (particles.effectiveBloomDivisor() > 1) {
            return "Adaptive quality: bloom rendered at 1/%d resolution".formatted(particles.effectiveBloomDivisor());
        }
        return "";
    }
}
