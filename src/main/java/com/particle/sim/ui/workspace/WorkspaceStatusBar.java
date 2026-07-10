package com.particle.sim.ui.workspace;

import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.theme.UIColors;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

final class WorkspaceStatusBar {
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;

    void render(WorkspaceLayout.Panel panel, boolean paused, GpuParticleSystem particles) {
        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12.0f, 0.0f);
        if (ImGui.begin("##workspace-status", WINDOW_FLAGS)) {
            String status = "%s  ·  %.0f Hz fixed".formatted(
                    paused ? "Paused" : "Running", 1.0 / SimulationDefaults.SIMULATION_STEP_SECONDS);
            ImGui.setCursorPosX(12.0f);
            ImGui.setCursorPosY(Math.max(0.0f, (panel.height() - ImGui.getTextLineHeight()) * 0.5f));
            ImGui.pushStyleColor(ImGuiCol.Text,
                    paused ? UIColors.TEXT_MUTED.vec4() : UIColors.TEXT_PRIMARY.vec4());
            ImGui.textUnformatted(status);
            ImGui.popStyleColor();
            String qualityMessage = qualityMessage(particles);
            if (!qualityMessage.isEmpty() && panel.width() >= 720.0f) {
                float messageWidth = ImGui.calcTextSize(qualityMessage).x;
                ImGui.sameLine(Math.max(ImGui.getCursorPosX() + 16.0f, panel.width() - messageWidth - 12.0f));
                ImGui.textDisabled(qualityMessage);
            }
        }
        ImGui.end();
        ImGui.popStyleVar();
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
