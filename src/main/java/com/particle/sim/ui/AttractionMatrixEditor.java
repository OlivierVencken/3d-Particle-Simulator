package com.particle.sim.ui;

import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SimulationDefaults;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiTableFlags;

final class AttractionMatrixEditor {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int RIGHT_MOUSE_BUTTON = 1;

    private float matrixEditStep = SimulationDefaults.MATRIX_EDIT_STEP;

    void renderSettings(GpuParticleSystem particles, Runnable settingsChanged) {
        UiControls.settingSlider("Edit step", matrixEditStep, 0.01f, 0.5f, value -> matrixEditStep = value,
                settingsChanged);
        renderMatrixActions(particles, settingsChanged);
        renderMatrixTable(particles, settingsChanged);
        renderLegend();
    }

    private void renderMatrixActions(GpuParticleSystem particles, Runnable settingsChanged) {
        if (ImGui.button("Randomize")) {
            particles.randomizeAttractionMatrix();
            settingsChanged.run();
        }
        ImGui.sameLine();
        if (ImGui.button("Zero")) {
            particles.zeroAttractionMatrix();
            settingsChanged.run();
        }
        ImGui.sameLine();
        if (ImGui.button("Symmetrize")) {
            particles.symmetrizeAttractionMatrix();
            settingsChanged.run();
        }
        ImGui.sameLine();
        if (ImGui.button("Invert")) {
            particles.invertAttractionMatrix();
            settingsChanged.run();
        }
    }

    private void renderMatrixTable(GpuParticleSystem particles, Runnable settingsChanged) {
        ImGui.spacing();

        int groupCount = particles.groupCount();
        int tableFlags = ImGuiTableFlags.RowBg | ImGuiTableFlags.BordersInner | ImGuiTableFlags.SizingFixedFit;
        if (ImGui.beginTable("attraction-matrix-table", groupCount + 1, tableFlags)) {
            ImGui.tableNextRow();
            ImGui.tableNextColumn();
            ImGui.textUnformatted("A/B");
            for (int column = 0; column < groupCount; column++) {
                ImGui.tableNextColumn();
                ImGui.textUnformatted("G%d".formatted(column));
            }

            for (int row = 0; row < groupCount; row++) {
                ImGui.tableNextRow();
                ImGui.tableNextColumn();
                ImGui.textUnformatted("G%d".formatted(row));

                for (int column = 0; column < groupCount; column++) {
                    ImGui.tableNextColumn();
                    renderMatrixTile(particles, row, column, settingsChanged);
                }
            }

            ImGui.endTable();
        }
    }

    private void renderLegend() {
        ImGui.spacing();
        textColored(UiPalette.MATRIX_ATTRACTION, "Green: attraction");
        ImGui.sameLine();
        textColored(UiPalette.MATRIX_NEUTRAL, "Grey: zero");
        ImGui.sameLine();
        textColored(UiPalette.MATRIX_REPULSION, "Red: repulsion");
    }

    private void renderMatrixTile(GpuParticleSystem particles, int row, int column, Runnable settingsChanged) {
        float value = particles.attraction(row, column);
        ImVec4 color = attractionColor(value);
        ImVec2 size = new ImVec2(34.0f, 24.0f);

        ImGui.pushID("matrix-%d-%d".formatted(row, column));
        ImGui.colorButton("##tile", color, size);

        if (ImGui.isItemClicked(LEFT_MOUSE_BUTTON)) {
            particles.adjustAttraction(row, column, matrixEditStep);
            settingsChanged.run();
        }
        if (ImGui.isItemClicked(RIGHT_MOUSE_BUTTON)) {
            particles.adjustAttraction(row, column, -matrixEditStep);
            settingsChanged.run();
        }
        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("G%d -> G%d: %.2f".formatted(row, column, value));
        }

        ImGui.popID();
    }

    private ImVec4 attractionColor(float value) {
        float strength = Math.min(1.0f, Math.abs(value));
        if (value >= 0.0f) {
            return UiPalette.MATRIX_TILE_NEUTRAL.blend(UiPalette.MATRIX_ATTRACTION, strength).vec4();
        }

        return UiPalette.MATRIX_TILE_NEUTRAL.blend(UiPalette.MATRIX_REPULSION, strength).vec4();
    }

    private void textColored(UiColor color, String text) {
        ImGui.textColored(color.red(), color.green(), color.blue(), color.alpha(), text);
    }

    float matrixEditStep() {
        return matrixEditStep;
    }

    void setMatrixEditStep(float matrixEditStep) {
        this.matrixEditStep = Math.max(0.01f, Math.min(0.5f, matrixEditStep));
    }
}
