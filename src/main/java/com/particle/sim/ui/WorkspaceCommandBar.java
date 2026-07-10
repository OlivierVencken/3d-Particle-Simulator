package com.particle.sim.ui;

import com.particle.sim.particles.GpuParticleSystem;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

final class WorkspaceCommandBar {
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
    private static final String RESET_POPUP = "Reset simulation settings?";

    private final ImBoolean showHotkeys = new ImBoolean(false);
    private final HotkeyPopup hotkeyPopup = new HotkeyPopup();

    void render(WorkspaceLayout layout, WorkspaceState state, GpuParticleSystem particles, float fps,
            boolean paused, Runnable togglePause, Runnable savePreset, Runnable loadPreset, Runnable resetSettings,
            Runnable showDebug, Runnable hideUi, Runnable exitApplication) {
        WorkspaceLayout.Panel panel = layout.commandBar();
        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12.0f, 6.0f);
        if (ImGui.begin("##workspace-command-bar", WINDOW_FLAGS)) {
            renderLeft(panel.width(), loadPreset, savePreset);
            renderCenter(panel.width(), layout.simulation(), particles, paused, togglePause);
            renderRight(panel.width(), state, particles, fps, paused, loadPreset, savePreset, showDebug, hideUi,
                    exitApplication);
        }
        ImGui.end();
        ImGui.popStyleVar();
        renderResetConfirmation(state, resetSettings);
        hotkeyPopup.render(showHotkeys);
    }

    private void renderLeft(float width, Runnable loadPreset, Runnable savePreset) {
        ImGui.pushFont(UiFonts.title());
        ImGui.textUnformatted(width >= 900.0f ? "3D Particle Simulator" : "3DPS");
        ImGui.popFont();
        if (width >= 1100.0f) {
            ImGui.sameLine(0.0f, 20.0f);
            if (ImGui.button("Load##command-load", 72.0f, 32.0f)) {
                loadPreset.run();
            }
            ImGui.sameLine();
            if (ImGui.button("Save##command-save", 72.0f, 32.0f)) {
                savePreset.run();
            }
        }
    }

    private void renderCenter(float width, WorkspaceLayout.Panel simulation, GpuParticleSystem particles,
            boolean paused, Runnable togglePause) {
        float controlsWidth = width >= 900.0f ? 246.0f : 92.0f;
        ImGui.sameLine(Math.max(ImGui.getCursorPosX() + 12.0f, centeredControlsX(simulation, controlsWidth)));
        ImGui.pushStyleColor(ImGuiCol.Button, UiPalette.ACCENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, UiPalette.ACCENT_BRIGHT.vec4());
        if (ImGui.button(paused ? "Resume##command-pause" : "Pause##command-pause", 92.0f, 32.0f)) {
            togglePause.run();
        }
        ImGui.popStyleColor(2);
        if (width >= 900.0f) {
            ImGui.sameLine();
            ImGui.beginDisabled(!paused);
            if (ImGui.button("Step##command-step", 68.0f, 32.0f)) {
                particles.step();
            }
            ImGui.endDisabled();
            ImGui.sameLine();
            if (ImGui.button("Reset##command-reset-particles", 78.0f, 32.0f)) {
                particles.reset();
            }
        }
    }

    private void renderRight(float width, WorkspaceState state, GpuParticleSystem particles, float fps, boolean paused,
            Runnable loadPreset, Runnable savePreset, Runnable showDebug, Runnable hideUi, Runnable exitApplication) {
        float rightWidth = width >= 1100.0f ? 214.0f : 44.0f;
        ImGui.sameLine(Math.max(ImGui.getCursorPosX() + 8.0f, width - rightWidth - 12.0f));
        if (width >= 1100.0f) {
            ImGui.textDisabled("%,d particles  ·  %.0f FPS".formatted(particles.particleCount(), fps));
            ImGui.sameLine();
        }
        if (ImGui.button("...##command-overflow", 38.0f, 32.0f)) {
            ImGui.openPopup("command-overflow-menu");
        }
        if (ImGui.beginPopup("command-overflow-menu")) {
            if (width < 1100.0f && ImGui.menuItem("Load...")) loadPreset.run();
            if (width < 1100.0f && ImGui.menuItem("Save...")) savePreset.run();
            if (width < 900.0f) {
                ImGui.beginDisabled(!paused);
                if (ImGui.menuItem("Step")) particles.step();
                ImGui.endDisabled();
                if (ImGui.menuItem("Reset particles")) particles.reset();
            }
            if (!state.inspectorVisible() && ImGui.menuItem("Open inspector")) {
                state.setInspectorVisible(true);
            }
            if (width < 1100.0f) {
                ImGui.textDisabled("%,d particles | %.0f FPS".formatted(particles.particleCount(), fps));
                ImGui.separator();
            }
            if (ImGui.menuItem("Debug")) showDebug.run();
            if (ImGui.menuItem("Hotkeys")) showHotkeys.set(true);
            if (ImGui.menuItem("Reset settings...")) {
                state.requestResetConfirmation();
            }
            if (ImGui.menuItem("Hide UI")) hideUi.run();
            ImGui.separator();
            if (ImGui.menuItem("Exit")) exitApplication.run();
            ImGui.endPopup();
        }
    }

    static float centeredControlsX(WorkspaceLayout.Panel simulation, float controlsWidth) {
        return simulation.x() + Math.max(0.0f, (simulation.width() - controlsWidth) * 0.5f);
    }

    private void renderResetConfirmation(WorkspaceState state, Runnable resetSettings) {
        if (state.resetConfirmationOpen()) {
            ImGui.openPopup(RESET_POPUP);
            state.closeResetConfirmation();
        }
        if (ImGui.beginPopupModal(RESET_POPUP, ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.textUnformatted("Restore every simulation setting to its default value?");
            ImGui.textDisabled("This does not reset the current particle positions.");
            ImGui.spacing();
            ImGui.pushStyleColor(ImGuiCol.Button, UiPalette.DESTRUCTIVE.vec4());
            if (ImGui.button("Reset settings", 128.0f, 32.0f)) {
                resetSettings.run();
                ImGui.closeCurrentPopup();
            }
            ImGui.popStyleColor();
            ImGui.sameLine();
            if (ImGui.button("Cancel", 88.0f, 32.0f)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }
}
