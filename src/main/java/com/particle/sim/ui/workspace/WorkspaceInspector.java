package com.particle.sim.ui.workspace;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.ui.theme.UIFonts;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

final class WorkspaceInspector {
    private static final String[] SECTION_LABELS = java.util.Arrays.stream(UISection.values())
            .map(UISection::label).toArray(String[]::new);
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings;

    private final InspectorSections sections = new InspectorSections();

    void render(WorkspaceLayout layout, WorkspaceState state, GpuParticleSystem particles, CameraController camera,
            Runnable settingsChanged) {
        WorkspaceLayout.Panel panel = layout.inspector();
        if (!panel.visible()) return;
        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        if (ImGui.begin("##workspace-inspector", WINDOW_FLAGS)) {
            renderHeader(layout, state);
            ImGui.separator();
            sections.render(state.activeSection(), particles, camera, settingsChanged);
        }
        ImGui.end();
    }

    private void renderHeader(WorkspaceLayout layout, WorkspaceState state) {
        if (layout.mode() == WorkspaceLayout.Mode.FOCUS) {
            if (ImGui.button("Back to simulation##inspector-back")) {
                state.setInspectorVisible(false);
            }
            ImGui.spacing();
        }
        if (!layout.navigation().visible()) {
            ImInt selected = new ImInt(state.activeSection().ordinal());
            ImGui.setNextItemWidth(-1.0f);
            if (ImGui.combo("##section-selector", selected, SECTION_LABELS)) {
                state.select(UISection.values()[selected.get()]);
            }
        } else {
            ImGui.pushFont(UIFonts.section());
            ImGui.textUnformatted(state.activeSection().label());
            ImGui.popFont();
            ImGui.textDisabled(sectionDescription(state.activeSection()));
        }
    }

    private String sectionDescription(UISection section) {
        return switch (section) {
            case SIMULATION -> "World boundaries and motion";
            case PARTICLES -> "Population and spawning";
            case APPEARANCE -> "Rendering, glow, and trails";
            case CAMERA -> "Navigation and movement";
            case INTERACTIONS -> "Attraction matrix and group forces";
        };
    }

    int customSpawnAmount() { return sections.customSpawnAmount(); }
    void setCustomSpawnAmount(int amount) { sections.setCustomSpawnAmount(amount); }
    float matrixEditStep() { return sections.matrixEditStep(); }
    void setMatrixEditStep(float step) { sections.setMatrixEditStep(step); }
}
