package com.particle.sim.ui.commandbar;

import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.UILayout;
import com.particle.sim.ui.UIState;
import com.particle.sim.ui.components.SvgIconTexture;
import com.particle.sim.ui.theme.UIColors;
import com.particle.sim.ui.theme.UIFonts;
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
    private static final float SIDEBAR_ICON_SIZE = 18.0f;
    private static final float BUTTON_HEIGHT = 28.0f;

    private final HotkeyPopup hotkeyPopup = new HotkeyPopup();
    private final AboutPopup aboutPopup = new AboutPopup();
    private final ResetSettingsPopup resetSettingsPopup = new ResetSettingsPopup();
    private final SvgIconTexture sidebarToggleIcon = new SvgIconTexture(
            "/assets/icons/sidebar-toggle.svg", 64);
    private float simulationMenuX;
    private float simulationMenuY;
    private float viewMenuX;
    private float viewMenuY;
    private float infoMenuX;
    private float infoMenuY;

    public void render(UILayout layout, UIState state, SimulationUiModel model, SimulationUiActions actions,
            float fps, ImBoolean showDebug) {
        UILayout.Panel panel = layout.commandBar();
        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4.0f, 4.0f);
        if (ImGui.begin("##command-bar", WINDOW_FLAGS)) {
            ImGui.pushFont(UIFonts.commandBar());
            renderMenuButtons(state);
            renderStatistics(panel.width(), panel.height(), model.particles(), fps);
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

    private void renderMenuButtons(UIState state) {
        if (sidebarToggleButton()) {
            state.toggleSidebar();
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip(state.sidebarVisible() ? "Minimize settings sidebar" : "Show settings sidebar");
        }

        ImGui.sameLine(0.0f, 4.0f);
        boolean simulationClicked = dropdownButton("Simulation", "simulation");
        simulationMenuX = ImGui.getItemRectMinX();
        simulationMenuY = ImGui.getItemRectMaxY();
        if (simulationClicked) {
            ImGui.openPopup(SIMULATION_MENU);
        }
        ImGui.sameLine(0.0f, 4.0f);
        boolean viewClicked = dropdownButton("View", "view");
        viewMenuX = ImGui.getItemRectMinX();
        viewMenuY = ImGui.getItemRectMaxY();
        if (viewClicked) {
            ImGui.openPopup(VIEW_MENU);
        }
        ImGui.sameLine(0.0f, 4.0f);
        boolean infoClicked = dropdownButton("Info", "info");
        infoMenuX = ImGui.getItemRectMinX();
        infoMenuY = ImGui.getItemRectMaxY();
        if (infoClicked) {
            ImGui.openPopup(INFO_MENU);
        }
    }

    private boolean sidebarToggleButton() {
        pushTopBarButtonStyle();
        boolean clicked = ImGui.button("##toggle-sidebar", BUTTON_HEIGHT, BUTTON_HEIGHT);
        popTopBarButtonStyle();

        float centerX = (ImGui.getItemRectMinX() + ImGui.getItemRectMaxX()) * 0.5f;
        float centerY = (ImGui.getItemRectMinY() + ImGui.getItemRectMaxY()) * 0.5f;
        float iconMinX = centerX - SIDEBAR_ICON_SIZE * 0.5f;
        float iconMinY = centerY - SIDEBAR_ICON_SIZE * 0.5f;
        int color = ImGui.getColorU32(ImGuiCol.Text);
        ImGui.getWindowDrawList().addImage(
                sidebarToggleIcon.textureId(),
                iconMinX, iconMinY,
                iconMinX + SIDEBAR_ICON_SIZE, iconMinY + SIDEBAR_ICON_SIZE,
                0.0f, 0.0f, 1.0f, 1.0f,
                color);
        return clicked;
    }

    private boolean dropdownButton(String label, String id) {
        pushTopBarButtonStyle();
        boolean clicked = ImGui.button(label + "##command-" + id, 0.0f, BUTTON_HEIGHT);
        popTopBarButtonStyle();
        return clicked;
    }

    public void dispose() {
        sidebarToggleIcon.dispose();
    }

    private void pushTopBarButtonStyle() {
        ImGui.pushStyleColor(ImGuiCol.Button, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 0.0f);
    }

    private void popTopBarButtonStyle() {
        ImGui.popStyleVar();
        ImGui.popStyleColor();
    }

    private void renderStatistics(float width, float height, SimulationUiModel.Particles particles, float fps) {
        if (width < 720.0f) {
            return;
        }
        String statistics = "%,d particles  |  %.0f FPS".formatted(particles.particleCount(), fps);
        float statisticsWidth = ImGui.calcTextSize(statistics).x;
        float statisticsX = width - statisticsWidth - 12.0f;
        float statisticsY = Math.max(0.0f,
                (height - ImGui.getTextLineHeight()) * 0.5f);
        ImGui.getWindowDrawList().addText(
                ImGui.getWindowPosX() + statisticsX,
                ImGui.getWindowPosY() + statisticsY,
                ImGui.getColorU32(ImGuiCol.TextDisabled),
                statistics);
    }

    private void renderSimulationMenu(SimulationUiActions.Application actions) {
        if (!beginAnchoredPopup(SIMULATION_MENU, simulationMenuX, simulationMenuY)) {
            return;
        }

        if (ImGui.menuItem("Load...")) {
            actions.loadPreset();
        }
        if (ImGui.menuItem("Save...")) {
            actions.savePreset();
        }
        ImGui.separator();
        if (ImGui.menuItem("Reset settings...")) {
            resetSettingsPopup.open();
        }
        ImGui.separator();
        if (ImGui.menuItem("Exit")) {
            actions.exit();
        }
        ImGui.endPopup();
    }

    private void renderViewMenu(ImBoolean showDebug, SimulationUiActions.Application actions) {
        if (!beginAnchoredPopup(VIEW_MENU, viewMenuX, viewMenuY)) {
            return;
        }

        if (ImGui.menuItem("Hide UI")) {
            actions.hideUi();
        }
        if (ImGui.menuItem(showDebug.get() ? "Hide debug menu" : "Show debug menu")) {
            showDebug.set(!showDebug.get());
        }
        ImGui.endPopup();
    }

    private void renderInfoMenu() {
        if (!beginAnchoredPopup(INFO_MENU, infoMenuX, infoMenuY)) {
            return;
        }

        if (ImGui.menuItem("Hotkeys")) {
            hotkeyPopup.open();
        }
        if (ImGui.menuItem("About")) {
            aboutPopup.open();
        }
        ImGui.endPopup();
    }

    private boolean beginAnchoredPopup(String id, float x, float y) {
        if (ImGui.isPopupOpen(id)) {
            ImGui.setNextWindowPos(x, y);
        }
        return ImGui.beginPopup(id);
    }

}
