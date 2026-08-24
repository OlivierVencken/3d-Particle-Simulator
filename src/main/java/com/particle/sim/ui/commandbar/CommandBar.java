package com.particle.sim.ui.commandbar;

import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.UILayout;
import com.particle.sim.ui.UIState;
import com.particle.sim.ui.components.SvgIconTexture;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UIMenu;
import com.particle.sim.ui.theme.UIComponentVariant;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UIFonts;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

public final class CommandBar {
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
    private static final String SIMULATION_MENU = "##simulation-menu";
    private static final String VIEW_MENU = "##view-menu";
    private static final String INFO_MENU = "##info-menu";
    private final HotkeyPopup hotkeyPopup = new HotkeyPopup();
    private final AboutPopup aboutPopup = new AboutPopup();
    private final ResetSettingsPopup resetSettingsPopup = new ResetSettingsPopup();
    private final SvgIconTexture sidebarToggleIcon = SvgIconTexture.forUiIcon(
            "/assets/icons/sidebar-toggle.svg");
    private float simulationMenuX;
    private float simulationMenuY;
    private float viewMenuX;
    private float viewMenuY;
    private float infoMenuX;
    private float infoMenuY;

    public void render(UILayout layout, UIState state, SimulationUiModel model, SimulationUiActions actions,
            float fps, ImBoolean showDebug) {
        UILayout.Panel panel = layout.commandBar();
        UIDesignTokens tokens = UITheme.tokens();
        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, tokens.spaceXs(), tokens.spaceXs());
        if (ImGui.begin("##command-bar", WINDOW_FLAGS)) {
            ImGui.pushFont(UIFonts.commandBar());
            renderMenuButtons(state, tokens);
            renderStatistics(panel.width(), panel.height(), model.particles(), fps, tokens);
            ImGui.popFont();
            renderSimulationMenu(actions.application());
            renderViewMenu(showDebug, actions.application());
            renderInfoMenu();
        }
        ImGui.end();
        ImGui.popStyleVar();

        resetSettingsPopup.render(actions.application());
        hotkeyPopup.render();
        aboutPopup.render();
    }

    private void renderMenuButtons(UIState state, UIDesignTokens tokens) {
        if (sidebarToggleButton(state)) {
            state.toggleSidebar();
        }

        ImGui.sameLine(0.0f, tokens.spaceXs());
        boolean simulationClicked = dropdownButton("Simulation", "simulation", tokens);
        simulationMenuX = ImGui.getItemRectMinX();
        simulationMenuY = ImGui.getItemRectMaxY();
        if (simulationClicked) {
            ImGui.openPopup(SIMULATION_MENU);
        }
        ImGui.sameLine(0.0f, tokens.spaceXs());
        boolean viewClicked = dropdownButton("View", "view", tokens);
        viewMenuX = ImGui.getItemRectMinX();
        viewMenuY = ImGui.getItemRectMaxY();
        if (viewClicked) {
            ImGui.openPopup(VIEW_MENU);
        }
        ImGui.sameLine(0.0f, tokens.spaceXs());
        boolean infoClicked = dropdownButton("Info", "info", tokens);
        infoMenuX = ImGui.getItemRectMinX();
        infoMenuY = ImGui.getItemRectMaxY();
        if (infoClicked) {
            ImGui.openPopup(INFO_MENU);
        }
    }

    private boolean sidebarToggleButton(UIState state) {
        return UIButton.icon(state.sidebarVisible() ? "Minimize settings sidebar" : "Show settings sidebar",
                "toggle-sidebar", sidebarToggleIcon.textureId(), false, true);
    }

    private boolean dropdownButton(String label, String id, UIDesignTokens tokens) {
        return UIButton.text(label, "command-" + id, UIComponentVariant.GHOST,
                0.0f, tokens.compactControlHeight());
    }

    public void dispose() {
        sidebarToggleIcon.dispose();
    }

    public boolean hasOpenModal() {
        return resetSettingsPopup.isOpen();
    }

    public boolean hasOpenWindow() {
        return hasOpenModal() || hotkeyPopup.isOpen() || aboutPopup.isOpen();
    }

    private void renderStatistics(float width, float height, SimulationUiModel.Particles particles, float fps,
            UIDesignTokens tokens) {
        if (width < tokens.compactBreakpoint()) {
            return;
        }
        String statistics = "%,d particles  |  %.0f FPS".formatted(particles.particleCount(), fps);
        float statisticsWidth = ImGui.calcTextSize(statistics).x;
        float statisticsX = width - statisticsWidth - tokens.spaceXl();
        float statisticsY = Math.max(0.0f,
                (height - ImGui.getTextLineHeight()) * 0.5f);
        ImGui.getWindowDrawList().addText(
                ImGui.getWindowPosX() + statisticsX,
                ImGui.getWindowPosY() + statisticsY,
                ImGui.getColorU32(ImGuiCol.TextDisabled),
                statistics);
    }

    private void renderSimulationMenu(SimulationUiActions.Application actions) {
        if (!UIMenu.beginAnchored(SIMULATION_MENU, simulationMenuX, simulationMenuY)) {
            return;
        }

        if (UIMenu.item("Load...", "load-preset")) {
            actions.loadPreset();
        }
        if (UIMenu.item("Save...", "save-preset")) {
            actions.savePreset();
        }
        UIMenu.separator();
        if (UIMenu.item("Reset settings...", "reset-settings")) {
            resetSettingsPopup.open();
        }
        UIMenu.separator();
        if (UIMenu.item("Exit", "exit")) {
            actions.exit();
        }
        ImGui.endPopup();
    }

    private void renderViewMenu(ImBoolean showDebug, SimulationUiActions.Application actions) {
        if (!UIMenu.beginAnchored(VIEW_MENU, viewMenuX, viewMenuY)) {
            return;
        }

        if (UIMenu.item("Hide UI", "hide-ui")) {
            actions.hideUi();
        }
        if (UIMenu.item(showDebug.get() ? "Hide debug menu" : "Show debug menu", "toggle-debug",
                showDebug.get(), true)) {
            showDebug.set(!showDebug.get());
        }
        ImGui.endPopup();
    }

    private void renderInfoMenu() {
        if (!UIMenu.beginAnchored(INFO_MENU, infoMenuX, infoMenuY)) {
            return;
        }

        if (UIMenu.item("Hotkeys", "hotkeys")) {
            hotkeyPopup.open();
        }
        if (UIMenu.item("About", "about")) {
            aboutPopup.open();
        }
        ImGui.endPopup();
    }

}
