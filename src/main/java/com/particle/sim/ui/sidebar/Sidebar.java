package com.particle.sim.ui.sidebar;

import com.particle.sim.ui.UILayout;
import com.particle.sim.ui.UIState;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.sidebar.sections.SidebarContent;
import com.particle.sim.ui.components.UINavigation;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UIScrollRegion;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;
import com.particle.sim.ui.theme.UIComponentVariant;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

public final class Sidebar {
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
    private final SidebarContent content = new SidebarContent();

    public void render(UILayout.Panel panel, UIState state, SimulationUiModel model, SimulationUiActions actions) {
        if (!panel.visible()) {
            return;
        }

        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        if (ImGui.begin("##sidebar", WINDOW_FLAGS)) {
            renderOverlayHeader(state);
            renderSectionButtons(state);
            UIScrollRegion.render("sidebar-content",
                    () -> renderSectionContent(state.activeSection(), model, actions));
        }
        ImGui.end();
        content.renderPopups(actions);
    }

    private void renderOverlayHeader(UIState state) {
        if (state.layoutMode() != UILayout.Mode.COMPACT && state.layoutMode() != UILayout.Mode.FOCUS) {
            return;
        }
        UIDesignTokens tokens = UITheme.tokens();
        ImGui.alignTextToFramePadding();
        ImGui.textDisabled("SETTINGS");
        float closeWidth = ImGui.calcTextSize("Close").x + tokens.frameInsetHorizontal() * 2.0f;
        ImGui.sameLine(Math.max(ImGui.getCursorPosX() + tokens.spaceMd(),
                ImGui.getWindowContentRegionMaxX() - closeWidth));
        if (UIButton.text("Close", "close-overlay-sidebar", UIComponentVariant.GHOST,
                closeWidth, tokens.compactControlHeight())) {
            state.setSidebarVisible(false);
        }
        ImGui.spacing();
    }

    private void renderSectionButtons(UIState state) {
        UIDesignTokens tokens = UITheme.tokens();
        SidebarSection[] availableSections = SidebarSection.values();
        float totalTextWidth = 0.0f;
        for (SidebarSection section : availableSections) {
            totalTextWidth += ImGui.calcTextSize(section.label()).x;
        }

        float contentWidth = ImGui.getContentRegionAvailX();
        float spacingWidth = tokens.spaceXs() * (availableSections.length - 1);
        float availablePadding = (contentWidth - totalTextWidth - spacingWidth)
                / (availableSections.length * 2.0f);
        boolean fitsSingleRow = availablePadding >= 1.0f;
        float horizontalPadding = fitsSingleRow
                ? Math.min(tokens.frameInsetHorizontal(), availablePadding)
                : tokens.frameInsetHorizontal();

        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, horizontalPadding, tokens.frameInsetVertical());
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, tokens.spaceXs(), tokens.spaceLg());
        float rowWidth = 0.0f;
        for (int index = 0; index < availableSections.length; index++) {
            SidebarSection section = availableSections[index];
            float buttonWidth = ImGui.calcTextSize(section.label()).x + horizontalPadding * 2.0f;
            if (rowWidth > 0.0f && (fitsSingleRow
                    || rowWidth + tokens.spaceXs() + buttonWidth <= contentWidth)) {
                ImGui.sameLine();
                rowWidth += tokens.spaceXs();
            } else if (rowWidth > 0.0f) {
                rowWidth = 0.0f;
            }
            if (sectionButton(section, state.activeSection() == section, tokens)) {
                state.select(section);
            }
            rowWidth += buttonWidth;
        }
        ImGui.popStyleVar(2);
        ImGui.spacing();
    }

    private boolean sectionButton(SidebarSection section, boolean active, UIDesignTokens tokens) {
        return UINavigation.item(section.label(), section.name(), active, tokens.navigationControlHeight());
    }

    private void renderSectionContent(SidebarSection section, SimulationUiModel model, SimulationUiActions actions) {
        content.render(section, model, actions);
    }

    public int customSpawnAmount() {
        return content.customSpawnAmount();
    }

    public void setCustomSpawnAmount(int amount) {
        content.setCustomSpawnAmount(amount);
    }

    public float matrixEditStep() {
        return content.matrixEditStep();
    }

    public void setMatrixEditStep(float step) {
        content.setMatrixEditStep(step);
    }

    public boolean hasOpenModal() {
        return content.hasOpenModal();
    }
}
