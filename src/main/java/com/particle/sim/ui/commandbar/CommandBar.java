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
    private static final String COMPACT_MENU = "##compact-menu";
    private final HotkeyPopup hotkeyPopup = new HotkeyPopup();
    private final AboutPopup aboutPopup = new AboutPopup();
    private final ResetSettingsPopup resetSettingsPopup = new ResetSettingsPopup();
    private final ErrorPopup errorPopup = new ErrorPopup();
    private final SvgIconTexture sidebarToggleIcon = SvgIconTexture.forUiIcon(
            "/assets/icons/sidebar-toggle.svg");
    private float simulationMenuX;
    private float simulationMenuY;
    private float viewMenuX;
    private float viewMenuY;
    private float infoMenuX;
    private float infoMenuY;
    private float compactMenuX;
    private float compactMenuY;

    public void render(UILayout layout, UIState state, SimulationUiModel model, SimulationUiActions actions,
            float fps, ImBoolean showDebug) {
        UILayout.Panel panel = layout.commandBar();
        UIDesignTokens tokens = UITheme.tokens();
        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, tokens.spaceXs(), tokens.spaceXs());
        if (ImGui.begin("##command-bar", WINDOW_FLAGS)) {
            ImGui.pushFont(UIFonts.commandBar());
            renderMenuButtons(layout.mode(), panel.width(), state,
                    model.application(), actions.simulation(), tokens);
            renderStatistics(panel.width(), panel.height(), model.particles(), fps, tokens);
            ImGui.popFont();
            renderSimulationMenu(model.application(), actions.simulation(), actions.application());
            renderViewMenu(showDebug, actions.application());
            renderInfoMenu();
            renderCompactMenu(showDebug, model, actions);
        }
        ImGui.end();
        ImGui.popStyleVar();

        resetSettingsPopup.render(actions.application());
        hotkeyPopup.render();
        aboutPopup.render();
        errorPopup.render();
    }

    private void renderMenuButtons(UILayout.Mode mode, float width, UIState state,
            SimulationUiModel.Application application, SimulationUiActions.Simulation actions,
            UIDesignTokens tokens) {
        if (sidebarToggleButton(state)) {
            state.toggleSidebar();
        }

        ImGui.sameLine(0.0f, tokens.spaceXs());
        if (usesUnifiedMenu(mode, width, tokens)) {
            boolean compactClicked = dropdownButton("Menu", "compact", tokens, COMPACT_MENU);
            compactMenuX = ImGui.getItemRectMinX();
            compactMenuY = ImGui.getItemRectMaxY();
            if (compactClicked) {
                ImGui.openPopup(COMPACT_MENU);
            }
            return;
        }

        boolean compactLabels = mode == UILayout.Mode.COMPACT || mode == UILayout.Mode.FOCUS;
        boolean simulationClicked = dropdownButton(compactLabels ? "Sim" : "Simulation",
                "simulation", tokens, SIMULATION_MENU);
        simulationMenuX = ImGui.getItemRectMinX();
        simulationMenuY = ImGui.getItemRectMaxY();
        if (simulationClicked) {
            ImGui.openPopup(SIMULATION_MENU);
        }
        ImGui.sameLine(0.0f, tokens.spaceXs());
        boolean viewClicked = dropdownButton("View", "view", tokens, VIEW_MENU);
        viewMenuX = ImGui.getItemRectMinX();
        viewMenuY = ImGui.getItemRectMaxY();
        if (viewClicked) {
            ImGui.openPopup(VIEW_MENU);
        }
        ImGui.sameLine(0.0f, tokens.spaceXs());
        boolean infoClicked = dropdownButton("Info", "info", tokens, INFO_MENU);
        infoMenuX = ImGui.getItemRectMinX();
        infoMenuY = ImGui.getItemRectMaxY();
        if (infoClicked) {
            ImGui.openPopup(INFO_MENU);
        }

        ImGui.sameLine(0.0f, tokens.spaceXs());
        boolean paused = application.paused();
        if (UIButton.text(paused ? "Resume" : "Pause", "command-pause",
                paused ? UIComponentVariant.SELECTED : UIComponentVariant.GHOST,
                0.0f, tokens.compactControlHeight())) {
            actions.togglePause();
        }
    }

    private boolean sidebarToggleButton(UIState state) {
        return UIButton.icon(state.sidebarVisible() ? "Minimize settings sidebar" : "Show settings sidebar",
                "toggle-sidebar", sidebarToggleIcon.textureId(), false, true);
    }

    private boolean dropdownButton(String label, String id, UIDesignTokens tokens, String popupId) {
        UIComponentVariant variant = ImGui.isPopupOpen(popupId)
                ? UIComponentVariant.SELECTED : UIComponentVariant.GHOST;
        return UIButton.text(label, "command-" + id, variant,
                0.0f, tokens.compactControlHeight());
    }

    static boolean usesUnifiedMenu(UILayout.Mode mode, float width, UIDesignTokens tokens) {
        return mode == UILayout.Mode.FOCUS && width < tokens.compactCommandMenuBreakpoint();
    }

    public void dispose() {
        sidebarToggleIcon.dispose();
    }

    public boolean hasOpenModal() {
        return resetSettingsPopup.isOpen();
    }

    public boolean hasOpenWindow() {
        return hasOpenModal() || hotkeyPopup.isOpen() || aboutPopup.isOpen() || errorPopup.isOpen();
    }

    public void showError(String summary, String details) {
        errorPopup.open(summary, details);
    }

    private void renderStatistics(float width, float height, SimulationUiModel.Particles particles, float fps,
            UIDesignTokens tokens) {
        if (width < tokens.mediumBreakpoint()) {
            return;
        }
        String statistics = "%,d particles  |  %.0f FPS".formatted(particles.particleCount(), fps);
        float statisticsWidth = ImGui.calcTextSize(statistics).x;
        float statisticsX = width - statisticsWidth - tokens.spaceXl();
        float statisticsY = Math.max(0.0f, (height - ImGui.getTextLineHeight()) * 0.5f);
        ImGui.getWindowDrawList().addText(
                ImGui.getWindowPosX() + statisticsX,
                ImGui.getWindowPosY() + statisticsY,
                ImGui.getColorU32(ImGuiCol.TextDisabled),
                statistics);
    }

    private void renderSimulationMenu(SimulationUiModel.Application application,
            SimulationUiActions.Simulation simulationActions,
            SimulationUiActions.Application applicationActions) {
        if (!UIMenu.beginAnchored(SIMULATION_MENU, simulationMenuX, simulationMenuY)) {
            return;
        }

        if (UIMenu.item(application.paused() ? "Resume simulation" : "Pause simulation",
                "toggle-pause", application.paused(), true)) {
            simulationActions.togglePause();
        }
        if (UIMenu.item("Step simulation", "step", false, application.paused())) {
            simulationActions.step();
        }
        if (UIMenu.item("Reset particle state", "reset-particles")) {
            simulationActions.resetParticles();
        }
        UIMenu.separator();
        if (UIMenu.item("Load...", "load-preset")) {
            applicationActions.loadPreset();
        }
        if (UIMenu.item("Save...", "save-preset")) {
            applicationActions.savePreset();
        }
        UIMenu.separator();
        if (UIMenu.item("Reset settings...", "reset-settings")) {
            resetSettingsPopup.open();
        }
        UIMenu.separator();
        if (UIMenu.item("Exit", "exit")) {
            applicationActions.exit();
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

    private void renderCompactMenu(ImBoolean showDebug, SimulationUiModel model, SimulationUiActions actions) {
        if (!UIMenu.beginAnchored(COMPACT_MENU, compactMenuX, compactMenuY)) {
            return;
        }
        if (UIMenu.item(model.application().paused() ? "Resume simulation" : "Pause simulation",
                "compact-toggle-pause", model.application().paused(), true)) {
            actions.simulation().togglePause();
        }
        if (UIMenu.item("Step simulation", "compact-step", false, model.application().paused())) {
            actions.simulation().step();
        }
        if (UIMenu.item("Reset particle state", "compact-reset-particles")) {
            actions.simulation().resetParticles();
        }
        UIMenu.separator();
        if (UIMenu.item("Load preset...", "compact-load-preset")) {
            actions.application().loadPreset();
        }
        if (UIMenu.item("Save preset...", "compact-save-preset")) {
            actions.application().savePreset();
        }
        if (UIMenu.item("Reset settings...", "compact-reset-settings")) {
            resetSettingsPopup.open();
        }
        UIMenu.separator();
        if (UIMenu.item("Hide UI", "compact-hide-ui")) {
            actions.application().hideUi();
        }
        if (UIMenu.item(showDebug.get() ? "Hide debug panel" : "Show debug panel", "compact-toggle-debug",
                showDebug.get(), true)) {
            showDebug.set(!showDebug.get());
        }
        UIMenu.separator();
        if (UIMenu.item("Hotkeys", "compact-hotkeys")) {
            hotkeyPopup.open();
        }
        if (UIMenu.item("About", "compact-about")) {
            aboutPopup.open();
        }
        UIMenu.separator();
        if (UIMenu.item("Exit", "compact-exit")) {
            actions.application().exit();
        }
        ImGui.endPopup();
    }

}
