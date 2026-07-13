package com.particle.sim.ui.workspace;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.ui.theme.UIColors;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

final class WorkspaceSidebar {
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
    private static final float SECTION_BUTTON_HEIGHT = 32.0f;
    private static final float SECTION_BUTTON_SPACING = 4.0f;
    private static final float MAX_SECTION_BUTTON_PADDING = 8.0f;

    private final InspectorSections sections = new InspectorSections();

    void render(WorkspaceLayout.Panel panel, WorkspaceState state, GpuParticleSystem particles,
            CameraController camera, Runnable settingsChanged) {
        if (!panel.visible()) {
            return;
        }

        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        if (ImGui.begin("##workspace-sidebar", WINDOW_FLAGS)) {
            renderSectionButtons(state);
            renderSectionContent(state.activeSection(), particles, camera, settingsChanged);
        }
        ImGui.end();
    }

    private void renderSectionButtons(WorkspaceState state) {
        UISection[] availableSections = UISection.values();
        float totalTextWidth = 0.0f;
        for (UISection section : availableSections) {
            totalTextWidth += ImGui.calcTextSize(section.label()).x;
        }

        float contentWidth = ImGui.getContentRegionAvailX();
        float spacingWidth = SECTION_BUTTON_SPACING * (availableSections.length - 1);
        float availablePadding = (contentWidth - totalTextWidth - spacingWidth)
                / (availableSections.length * 2.0f);
        boolean fitsSingleRow = availablePadding >= 1.0f;
        float horizontalPadding = fitsSingleRow
                ? Math.min(MAX_SECTION_BUTTON_PADDING, availablePadding)
                : MAX_SECTION_BUTTON_PADDING;

        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, horizontalPadding, 7.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, SECTION_BUTTON_SPACING, 10.0f);
        float rowWidth = 0.0f;
        for (int index = 0; index < availableSections.length; index++) {
            UISection section = availableSections[index];
            float buttonWidth = ImGui.calcTextSize(section.label()).x + horizontalPadding * 2.0f;
            if (rowWidth > 0.0f && (fitsSingleRow
                    || rowWidth + SECTION_BUTTON_SPACING + buttonWidth <= contentWidth)) {
                ImGui.sameLine();
                rowWidth += SECTION_BUTTON_SPACING;
            } else if (rowWidth > 0.0f) {
                rowWidth = 0.0f;
            }
            if (sectionButton(section, state.activeSection() == section)) {
                state.select(section);
            }
            rowWidth += buttonWidth;
        }
        ImGui.popStyleVar(2);
    }

    private boolean sectionButton(UISection section, boolean active) {
        ImGui.pushStyleColor(ImGuiCol.Button,
                (active ? UIColors.SURFACE_ACTIVE : UIColors.TRANSPARENT).vec4());
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered,
                (active ? UIColors.CONTROL_ACTIVE : UIColors.SURFACE_HOVER).vec4());
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, UIColors.CONTROL_ACTIVE.vec4());
        ImGui.pushStyleColor(ImGuiCol.Border,
                (active ? UIColors.BORDER_STRONG : UIColors.TRANSPARENT).vec4());
        ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, active ? 1.0f : 0.0f);
        boolean clicked = ImGui.button(section.label() + "##section-button-" + section.name(),
                0.0f, SECTION_BUTTON_HEIGHT);
        ImGui.popStyleVar();
        ImGui.popStyleColor(4);
        return clicked;
    }

    private void renderSectionContent(UISection section, GpuParticleSystem particles, CameraController camera,
            Runnable settingsChanged) {
        if (ImGui.beginChild("##settings-content-" + section.name(), 0.0f, 0.0f, false)) {
            sections.render(section, particles, camera, settingsChanged);
        }
        ImGui.endChild();
    }

    int customSpawnAmount() {
        return sections.customSpawnAmount();
    }

    void setCustomSpawnAmount(int amount) {
        sections.setCustomSpawnAmount(amount);
    }

    float matrixEditStep() {
        return sections.matrixEditStep();
    }

    void setMatrixEditStep(float step) {
        sections.setMatrixEditStep(step);
    }
}
