package com.particle.sim.ui.workspace;

import com.particle.sim.particles.GpuParticleSystem;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

final class WorkspaceCommandBar {
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
    private static final String SIMULATION_MENU = "##simulation-menu";
    private static final String VIEW_MENU = "##view-menu";
    private static final String INFO_MENU = "##info-menu";
    private static final String RESET_POPUP = "Reset simulation settings?";

    private final ImBoolean showHotkeys = new ImBoolean(false);
    private final ImBoolean showAbout = new ImBoolean(false);
    private final HotkeyPopup hotkeyPopup = new HotkeyPopup();
    private final AboutPopup aboutPopup = new AboutPopup();

    void render(WorkspaceLayout layout, WorkspaceState state, GpuParticleSystem particles, float fps,
            Runnable savePreset, Runnable loadPreset, Runnable resetSettings, ImBoolean showDebug,
            Runnable hideUi, Runnable exitApplication) {
        WorkspaceLayout.Panel panel = layout.commandBar();
        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8.0f, 6.0f);
        if (ImGui.begin("##workspace-command-bar", WINDOW_FLAGS)) {
            renderMenuButtons(state);
            renderStatistics(panel.width(), particles, fps);
            renderSimulationMenu(state, loadPreset, savePreset, exitApplication);
            renderViewMenu(showDebug, hideUi);
            renderInfoMenu();
        }
        ImGui.end();
        ImGui.popStyleVar();

        renderResetConfirmation(state, resetSettings);
        hotkeyPopup.render(showHotkeys);
        aboutPopup.render(showAbout);
    }

    private void renderMenuButtons(WorkspaceState state) {
        if (ImGui.arrowButton("##toggle-sidebar", state.sidebarVisible() ? ImGuiDir.Left : ImGuiDir.Right)) {
            state.toggleSidebar();
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(state.sidebarVisible() ? "Minimize settings sidebar" : "Show settings sidebar");
        }

        ImGui.sameLine(0.0f, 6.0f);
        if (dropdownButton("Simulation", "simulation", 102.0f)) {
            ImGui.openPopup(SIMULATION_MENU);
        }
        ImGui.sameLine(0.0f, 6.0f);
        if (dropdownButton("View", "view", 70.0f)) {
            ImGui.openPopup(VIEW_MENU);
        }
        ImGui.sameLine(0.0f, 6.0f);
        if (dropdownButton("Info", "info", 64.0f)) {
            ImGui.openPopup(INFO_MENU);
        }
    }

    private boolean dropdownButton(String label, String id, float width) {
        boolean clicked = ImGui.button(label + "##command-" + id, width, 32.0f);
        float right = ImGui.getItemRectMaxX();
        float centerY = (ImGui.getItemRectMinY() + ImGui.getItemRectMaxY()) * 0.5f + 1.0f;
        int color = ImGui.getColorU32(ImGuiCol.Text);
        ImGui.getWindowDrawList().addTriangleFilled(
                right - 15.0f, centerY - 2.5f,
                right - 9.0f, centerY - 2.5f,
                right - 12.0f, centerY + 2.0f,
                color);
        return clicked;
    }

    private void renderStatistics(float width, GpuParticleSystem particles, float fps) {
        if (width < 720.0f) {
            return;
        }
        String statistics = "%,d particles  |  %.0f FPS".formatted(particles.particleCount(), fps);
        float statisticsWidth = ImGui.calcTextSize(statistics).x;
        float statisticsX = width - statisticsWidth - 12.0f;
        float statisticsY = Math.max(0.0f,
                (WorkspaceLayoutCalculator.COMMAND_BAR_HEIGHT - ImGui.getTextLineHeight()) * 0.5f);
        ImGui.getWindowDrawList().addText(
                ImGui.getWindowPosX() + statisticsX,
                ImGui.getWindowPosY() + statisticsY,
                ImGui.getColorU32(ImGuiCol.TextDisabled),
                statistics);
    }

    private void renderSimulationMenu(WorkspaceState state, Runnable loadPreset, Runnable savePreset,
            Runnable exitApplication) {
        if (!ImGui.beginPopup(SIMULATION_MENU)) {
            return;
        }

        if (ImGui.menuItem("Load...")) {
            loadPreset.run();
        }
        if (ImGui.menuItem("Save...")) {
            savePreset.run();
        }
        ImGui.separator();
        if (ImGui.menuItem("Reset settings...")) {
            state.requestResetConfirmation();
        }
        ImGui.separator();
        if (ImGui.menuItem("Exit")) {
            exitApplication.run();
        }
        ImGui.endPopup();
    }

    private void renderViewMenu(ImBoolean showDebug, Runnable hideUi) {
        if (!ImGui.beginPopup(VIEW_MENU)) {
            return;
        }

        if (ImGui.menuItem("Hide UI")) {
            hideUi.run();
        }
        if (ImGui.menuItem(showDebug.get() ? "Hide debug menu" : "Show debug menu")) {
            showDebug.set(!showDebug.get());
        }
        ImGui.endPopup();
    }

    private void renderInfoMenu() {
        if (!ImGui.beginPopup(INFO_MENU)) {
            return;
        }

        if (ImGui.menuItem("Hotkeys")) {
            showHotkeys.set(true);
        }
        if (ImGui.menuItem("About")) {
            showAbout.set(true);
        }
        ImGui.endPopup();
    }

    private void renderResetConfirmation(WorkspaceState state, Runnable resetSettings) {
        if (state.resetConfirmationOpen()) {
            ImGui.openPopup(RESET_POPUP);
            state.closeResetConfirmation();
        }
        if (ImGui.beginPopupModal(RESET_POPUP, ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.textUnformatted("Restore every simulation setting to its default value?");
            ImGui.textDisabled("This also regenerates the default particle population.");
            ImGui.spacing();
            if (ImGui.button("Reset settings", 128.0f, 32.0f)) {
                resetSettings.run();
                ImGui.closeCurrentPopup();
            }
            ImGui.sameLine();
            if (ImGui.button("Cancel", 88.0f, 32.0f)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }
}
