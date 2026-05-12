package com.particle.sim.ui;

import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SimulationDefaults;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;

final class AttractionMatrixPanel {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int RIGHT_MOUSE_BUTTON = 1;

    private float matrixEditStep = SimulationDefaults.MATRIX_EDIT_STEP;

    void render(GpuParticleSystem particles, Runnable settingsChanged) {
        ImGui.begin("Attraction Matrix");

        UiControls.settingSlider("Edit step", matrixEditStep, 0.01f, 0.5f, value -> matrixEditStep = value,
                settingsChanged);

        renderMatrixActions(particles, settingsChanged);
        renderMatrixTable(particles, settingsChanged);
        renderLegend();

        ImGui.end();
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
        if (ImGui.beginTable("attraction-matrix-table", groupCount + 1)) {
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
        ImGui.textColored(0.2f, 1.0f, 0.25f, 1.0f, "Green: attraction");
        ImGui.sameLine();
        ImGui.textColored(0.55f, 0.55f, 0.55f, 1.0f, "Grey: zero");
        ImGui.sameLine();
        ImGui.textColored(1.0f, 0.2f, 0.16f, 1.0f, "Red: repulsion");
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
        float neutral = 0.16f;
        if (value >= 0.0f) {
            return new ImVec4(
                    neutral * (1.0f - strength),
                    neutral * (1.0f - strength) + strength,
                    neutral * (1.0f - strength),
                    1.0f
            );
        }

        return new ImVec4(
                neutral * (1.0f - strength) + strength,
                neutral * (1.0f - strength),
                neutral * (1.0f - strength),
                1.0f
        );
    }

    float matrixEditStep() {
        return matrixEditStep;
    }

    void setMatrixEditStep(float matrixEditStep) {
        this.matrixEditStep = Math.max(0.01f, Math.min(0.5f, matrixEditStep));
    }
}
