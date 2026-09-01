package com.particle.sim.ui.sidebar;

import com.particle.sim.ui.InterfaceState;
import com.particle.sim.ui.Layout;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;
import com.particle.sim.ui.components.Button;
import com.particle.sim.ui.components.Navigation;
import com.particle.sim.ui.components.ScrollRegion;
import com.particle.sim.ui.sidebar.sections.SidebarContent;
import com.particle.sim.ui.theme.ComponentVariant;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

public final class Sidebar {
    private static final int WINDOW_FLAGS =
            ImGuiWindowFlags.NoTitleBar
                    | ImGuiWindowFlags.NoMove
                    | ImGuiWindowFlags.NoResize
                    | ImGuiWindowFlags.NoCollapse
                    | ImGuiWindowFlags.NoSavedSettings
                    | ImGuiWindowFlags.NoScrollbar
                    | ImGuiWindowFlags.NoScrollWithMouse;
    private final SidebarContent content = new SidebarContent();

    public void render(
            Layout.Panel panel,
            InterfaceState state,
            SimulationViewModel model,
            SimulationViewActions actions) {
        if (panel.visible()) {
            ImGui.setNextWindowPos(panel.x(), panel.y());
            ImGui.setNextWindowSize(panel.width(), panel.height());
            if (ImGui.begin("##sidebar", WINDOW_FLAGS)) {
                renderOverlayHeader(state);
                renderSectionButtons(state);
                renderAnimatedSectionContent(state, model, actions);
            }
            ImGui.end();
        }
        content.renderPopups(actions);
    }

    private void renderOverlayHeader(InterfaceState state) {
        if (state.layoutMode() != Layout.Mode.COMPACT && state.layoutMode() != Layout.Mode.FOCUS) {
            return;
        }
        DesignTokens tokens = Theme.tokens();
        ImGui.alignTextToFramePadding();
        ImGui.textDisabled("SETTINGS");
        float closeWidth = ImGui.calcTextSize("Close").x + tokens.frameInsetHorizontal() * 2.0f;
        ImGui.sameLine(
                Math.max(
                        ImGui.getCursorPosX() + tokens.spaceMd(),
                        ImGui.getWindowContentRegionMaxX() - closeWidth));
        if (Button.text(
                "Close",
                "close-overlay-sidebar",
                ComponentVariant.GHOST,
                closeWidth,
                tokens.compactControlHeight())) {
            state.setSidebarVisible(false);
        }
        ImGui.spacing();
    }

    private void renderSectionButtons(InterfaceState state) {
        DesignTokens tokens = Theme.tokens();
        SidebarSection[] availableSections = SidebarSection.values();
        float totalTextWidth = 0.0f;
        for (SidebarSection section : availableSections) {
            totalTextWidth += ImGui.calcTextSize(section.label()).x;
        }

        float contentWidth = ImGui.getContentRegionAvailX();
        float spacingWidth = tokens.spaceXs() * (availableSections.length - 1);
        float availablePadding =
                (contentWidth - totalTextWidth - spacingWidth) / (availableSections.length * 2.0f);
        boolean fitsSingleRow = availablePadding >= 1.0f;
        float horizontalPadding =
                fitsSingleRow
                        ? Math.min(tokens.frameInsetHorizontal(), availablePadding)
                        : tokens.frameInsetHorizontal();

        ImGui.pushStyleVar(
                ImGuiStyleVar.FramePadding, horizontalPadding, tokens.frameInsetVertical());
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, tokens.spaceXs(), tokens.spaceLg());
        float rowWidth = 0.0f;
        for (int index = 0; index < availableSections.length; index++) {
            SidebarSection section = availableSections[index];
            float buttonWidth = ImGui.calcTextSize(section.label()).x + horizontalPadding * 2.0f;
            if (rowWidth > 0.0f
                    && (fitsSingleRow
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

    private boolean sectionButton(SidebarSection section, boolean active, DesignTokens tokens) {
        return Navigation.item(
                section.label(), section.name(), active, tokens.navigationControlHeight());
    }

    private void renderSectionContent(
            SidebarSection section, SimulationViewModel model, SimulationViewActions actions) {
        content.render(section, model, actions);
    }

    private void renderAnimatedSectionContent(
            InterfaceState state, SimulationViewModel model, SimulationViewActions actions) {
        float reveal = state.sectionReveal();
        float alpha = 0.4f + reveal * 0.6f;
        float offset = Theme.tokens().spaceSm() * (1.0f - reveal);
        ScrollRegion.render(
                "sidebar-content",
                () -> {
                    ImGui.pushStyleVar(ImGuiStyleVar.Alpha, alpha);
                    ImGui.setCursorPosY(ImGui.getCursorPosY() + offset);
                    try {
                        renderSectionContent(state.activeSection(), model, actions);
                    } finally {
                        ImGui.popStyleVar();
                    }
                });
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
