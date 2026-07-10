package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

final class WorkspaceInspector {
    private static final String[] SECTION_LABELS = java.util.Arrays.stream(UiSection.values())
            .map(UiSection::label).toArray(String[]::new);
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
                state.select(UiSection.values()[selected.get()]);
            }
        } else {
            ImGui.textUnformatted(state.activeSection().label());
        }
    }

    int customSpawnAmount() { return sections.customSpawnAmount(); }
    void setCustomSpawnAmount(int amount) { sections.setCustomSpawnAmount(amount); }
    float matrixEditStep() { return sections.matrixEditStep(); }
    void setMatrixEditStep(float step) { sections.setMatrixEditStep(step); }
}
