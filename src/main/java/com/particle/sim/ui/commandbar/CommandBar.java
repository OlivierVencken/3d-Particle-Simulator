package com.particle.sim.ui.commandbar;

import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;
import com.particle.sim.ui.Layout;
import com.particle.sim.ui.InterfaceState;
import com.particle.sim.ui.components.SvgIconTexture;
import com.particle.sim.ui.components.Button;
import com.particle.sim.ui.components.Menu;
import com.particle.sim.ui.theme.ComponentVariant;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Fonts;
import com.particle.sim.ui.theme.Theme;
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

    public void render(Layout layout, InterfaceState state, SimulationViewModel model, SimulationViewActions actions,
            float fps, ImBoolean showDebug) {
        Layout.Panel panel = layout.commandBar();
        DesignTokens tokens = Theme.tokens();
        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, tokens.spaceXs(), tokens.spaceXs());
        if (ImGui.begin("##command-bar", WINDOW_FLAGS)) {
            ImGui.pushFont(Fonts.commandBar());
            renderMenuButtons(layout.mode(), panel.width(), state, tokens);
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

    private void renderMenuButtons(Layout.Mode mode, float width, InterfaceState state,
            DesignTokens tokens) {
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

        boolean compactLabels = mode == Layout.Mode.COMPACT || mode == Layout.Mode.FOCUS;
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

    }

    private boolean sidebarToggleButton(InterfaceState state) {
        return Button.icon(state.sidebarVisible() ? "Minimize settings sidebar" : "Show settings sidebar",
                "toggle-sidebar", sidebarToggleIcon.textureId(), false, true);
    }

    private boolean dropdownButton(String label, String id, DesignTokens tokens, String popupId) {
        ComponentVariant variant = ImGui.isPopupOpen(popupId)
                ? ComponentVariant.SELECTED : ComponentVariant.GHOST;
        return Button.text(label, "command-" + id, variant,
                0.0f, tokens.compactControlHeight());
    }

    static boolean usesUnifiedMenu(Layout.Mode mode, float width, DesignTokens tokens) {
        return mode == Layout.Mode.FOCUS && width < tokens.compactCommandMenuBreakpoint();
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

    private void renderStatistics(float width, float height, SimulationViewModel.Particles particles, float fps,
            DesignTokens tokens) {
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

    private void renderSimulationMenu(SimulationViewModel.Application application,
            SimulationViewActions.Simulation simulationActions,
            SimulationViewActions.Application applicationActions) {
        if (!Menu.beginAnchored(SIMULATION_MENU, simulationMenuX, simulationMenuY)) {
            return;
        }

        if (Menu.item(application.paused() ? "Resume simulation" : "Pause simulation",
                "toggle-pause", application.paused(), true)) {
            simulationActions.togglePause();
        }
        if (Menu.item("Step simulation", "step", false, application.paused())) {
            simulationActions.step();
        }
        if (Menu.item("Reset particle state", "reset-particles")) {
            simulationActions.resetParticles();
        }
        Menu.separator();
        if (Menu.item("Load...", "load-preset")) {
            applicationActions.loadPreset();
        }
        if (Menu.item("Save...", "save-preset")) {
            applicationActions.savePreset();
        }
        Menu.separator();
        if (Menu.item("Reset settings...", "reset-settings")) {
            resetSettingsPopup.open();
        }
        Menu.separator();
        if (Menu.item("Exit", "exit")) {
            applicationActions.exit();
        }
        ImGui.endPopup();
    }

    private void renderViewMenu(ImBoolean showDebug, SimulationViewActions.Application actions) {
        if (!Menu.beginAnchored(VIEW_MENU, viewMenuX, viewMenuY)) {
            return;
        }

        if (Menu.item("Hide UI", "hide-ui")) {
            actions.hideUi();
        }
        if (Menu.item(showDebug.get() ? "Hide debug menu" : "Show debug menu", "toggle-debug",
                showDebug.get(), true)) {
            showDebug.set(!showDebug.get());
        }
        ImGui.endPopup();
    }

    private void renderInfoMenu() {
        if (!Menu.beginAnchored(INFO_MENU, infoMenuX, infoMenuY)) {
            return;
        }

        if (Menu.item("Hotkeys", "hotkeys")) {
            hotkeyPopup.open();
        }
        if (Menu.item("About", "about")) {
            aboutPopup.open();
        }
        ImGui.endPopup();
    }

    private void renderCompactMenu(ImBoolean showDebug, SimulationViewModel model, SimulationViewActions actions) {
        if (!Menu.beginAnchored(COMPACT_MENU, compactMenuX, compactMenuY)) {
            return;
        }
        if (Menu.item(model.application().paused() ? "Resume simulation" : "Pause simulation",
                "compact-toggle-pause", model.application().paused(), true)) {
            actions.simulation().togglePause();
        }
        if (Menu.item("Step simulation", "compact-step", false, model.application().paused())) {
            actions.simulation().step();
        }
        if (Menu.item("Reset particle state", "compact-reset-particles")) {
            actions.simulation().resetParticles();
        }
        Menu.separator();
        if (Menu.item("Load preset...", "compact-load-preset")) {
            actions.application().loadPreset();
        }
        if (Menu.item("Save preset...", "compact-save-preset")) {
            actions.application().savePreset();
        }
        if (Menu.item("Reset settings...", "compact-reset-settings")) {
            resetSettingsPopup.open();
        }
        Menu.separator();
        if (Menu.item("Hide UI", "compact-hide-ui")) {
            actions.application().hideUi();
        }
        if (Menu.item(showDebug.get() ? "Hide debug panel" : "Show debug panel", "compact-toggle-debug",
                showDebug.get(), true)) {
            showDebug.set(!showDebug.get());
        }
        Menu.separator();
        if (Menu.item("Hotkeys", "compact-hotkeys")) {
            hotkeyPopup.open();
        }
        if (Menu.item("About", "compact-about")) {
            aboutPopup.open();
        }
        Menu.separator();
        if (Menu.item("Exit", "compact-exit")) {
            actions.application().exit();
        }
        ImGui.endPopup();
    }

}
