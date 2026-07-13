package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.ui.theme.UIColor;
import com.particle.sim.ui.theme.UIColors;
import com.particle.sim.ui.components.UIControls;

import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SimulationDefaults;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;

final class AttractionMatrixEditor {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int RIGHT_MOUSE_BUTTON = 1;

    private static final float MATRIX_GAP = 4.0f;
    private static final float HEADER_CIRCLE_RADIUS_SCALE = 0.34f;

    private float matrixEditStep = SimulationDefaults.MATRIX_EDIT_STEP;
    void renderSettings(GpuParticleSystem particles, Runnable settingsChanged) {
        UIControls.settingSlider(
                "Edit step",
                matrixEditStep, 0.01f,
                0.5f,
                2,
                value -> matrixEditStep = value,
                settingsChanged);

        renderMatrixActions(particles, settingsChanged);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, UIColors.TRANSPARENT.vec4());
        renderMatrix(particles, settingsChanged);
        ImGui.popStyleColor();
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

    private void renderMatrix(GpuParticleSystem particles, Runnable settingsChanged) {
        ImGui.spacing();

        int groupCount = particles.groupCount();
        if (groupCount <= 0) {
            ImGui.textUnformatted("No groups.");
            return;
        }

        float availableWidth = Math.max(0.0f, ImGui.getContentRegionAvailX());
        float cellSize = fittedCellSize(availableWidth, groupCount);

        float totalSize = (groupCount + 1) * cellSize + groupCount * MATRIX_GAP;

        ImVec2 origin = ImGui.getCursorScreenPos();
        ImDrawList drawList = ImGui.getWindowDrawList();

        int panelBg = ImGui.getColorU32(0.10f, 0.10f, 0.10f, 1.0f);
        int panelBorder = ImGui.getColorU32(0.20f, 0.20f, 0.20f, 1.0f);
        drawList.addRectFilled(origin.x, origin.y, origin.x + totalSize, origin.y + totalSize, panelBg, 6.0f);
        drawList.addRect(origin.x, origin.y, origin.x + totalSize, origin.y + totalSize, panelBorder, 6.0f);

        drawEmptyCorner(origin.x, origin.y, cellSize, drawList);

        for (int column = 0; column < groupCount; column++) {
            float x = origin.x + (column + 1) * (cellSize + MATRIX_GAP);
            float y = origin.y;

            drawGroupHeaderCircle(particles, column, x, y, cellSize, true, drawList);
        }

        for (int row = 0; row < groupCount; row++) {
            float y = origin.y + (row + 1) * (cellSize + MATRIX_GAP);

            drawGroupHeaderCircle(particles, row, origin.x, y, cellSize, false, drawList);

            for (int column = 0; column < groupCount; column++) {
                float x = origin.x + (column + 1) * (cellSize + MATRIX_GAP);
                drawMatrixTile(particles, row, column, x, y, cellSize, settingsChanged, drawList);
            }
        }
        ImGui.setCursorScreenPos(origin.x, origin.y);
        ImGui.dummy(totalSize, totalSize);
    }

    static float fittedCellSize(float availableWidth, int groupCount) {
        if (availableWidth <= 0.0f || groupCount <= 0) {
            return 0.0f;
        }
        return Math.max(0.0f,
                (availableWidth - groupCount * MATRIX_GAP) / (groupCount + 1));
    }

    private void drawEmptyCorner(float x, float y, float cellSize, ImDrawList drawList) {
        int border = ImGui.getColorU32(0.0f, 0.0f, 0.0f, 0.0f);
        drawList.addRect(x, y, x + cellSize, y + cellSize, border, 3.0f);
    }

    private void drawGroupHeaderCircle(GpuParticleSystem particles,
            int group,
            float x, float y,
            float cellSize,
            boolean columnHeader,
            ImDrawList drawList) {
        ImVec4 groupColor = groupColor(particles, group);

        float cx = x + cellSize * 0.5f;
        float cy = y + cellSize * 0.5f;
        float radius = cellSize * HEADER_CIRCLE_RADIUS_SCALE;

        int fill = ImGui.getColorU32(groupColor.x, groupColor.y, groupColor.z, groupColor.w);
        int border = ImGui.getColorU32(0.12f, 0.12f, 0.12f, 1.0f);

        ImGui.pushID((columnHeader ? "col-" : "row-") + group);
        ImGui.setCursorScreenPos(new ImVec2(x, y));
        ImGui.invisibleButton("##group-header", new ImVec2(cellSize, cellSize));

        drawList.addCircleFilled(cx, cy, radius, fill, 24);
        drawList.addCircle(cx, cy, radius, border, 24, 2.0f);

        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Group %d".formatted(group + 1));
        }

        ImGui.popID();
    }

    private void drawMatrixTile(GpuParticleSystem particles,
            int row, int column,
            float x, float y, float size,
            Runnable settingsChanged,
            ImDrawList drawList) {
        float value = particles.attraction(row, column);
        ImVec4 color = attractionColor(value);

        int fill = ImGui.getColorU32(color.x, color.y, color.z, color.w);
        int border = ImGui.getColorU32(0.16f, 0.16f, 0.16f, 1.0f);
        int hoverBorder = ImGui.getColorU32(1.0f, 1.0f, 1.0f, 1.0f);

        ImGui.pushID(row * 10_000 + column);
        ImGui.setCursorScreenPos(new ImVec2(x, y));
        ImGui.invisibleButton("##tile", new ImVec2(size - 1.0f, size - 1.0f));

        boolean hovered = ImGui.isItemHovered();
        boolean leftClick = ImGui.isItemClicked(LEFT_MOUSE_BUTTON);
        boolean rightClick = ImGui.isItemClicked(RIGHT_MOUSE_BUTTON);

        drawList.addRectFilled(x, y, x + size - 1.0f, y + size - 1.0f, fill, 3.0f);
        drawList.addRect(
                x, y, x + size - 1.0f, y + size - 1.0f,
                hovered ? hoverBorder : border,
                3.0f,
                0,
                hovered ? 2.0f : 1.0f);

        if (leftClick) {
            particles.adjustAttraction(row, column, matrixEditStep);
            settingsChanged.run();
        }
        if (rightClick) {
            particles.adjustAttraction(row, column, -matrixEditStep);
            settingsChanged.run();
        }
        if (hovered) {
            ImGui.setTooltip("Group %d to group %d: %.2f".formatted(row + 1, column + 1, value));
        }

        ImGui.popID();
    }

    private void renderLegend() {
        ImGui.spacing();

        String attraction = "+ attraction";
        String neutral = "0 neutral";
        String repulsion = "- repulsion";

        float spacing = ImGui.getStyle().getItemSpacingX();

        float legendWidth = ImGui.calcTextSize(attraction).x +
                spacing +
                ImGui.calcTextSize(neutral).x +
                spacing +
                ImGui.calcTextSize(repulsion).x;

        float availableWidth = ImGui.getContentRegionAvail().x;
        float offset = Math.max(0.0f, (availableWidth - legendWidth) * 0.5f);

        ImGui.setCursorPosX(ImGui.getCursorPosX() + offset);

        textColored(UIColors.INTERACTION_ATTRACTION, attraction);
        ImGui.sameLine();
        textColored(UIColors.INTERACTION_NEUTRAL, neutral);
        ImGui.sameLine();
        textColored(UIColors.INTERACTION_REPULSION, repulsion);
    }

    private ImVec4 groupColor(GpuParticleSystem particles, int group) {
        ImVec4[] groupColors = particles.groupColors();
        return groupColors[Math.floorMod(group, groupColors.length)];
    }

    private ImVec4 attractionColor(float value) {
        float strength = Math.min(1.0f, Math.abs(value));
        if (value >= 0.0f) {
            return UIColors.INTERACTION_NEUTRAL_SURFACE.blend(UIColors.INTERACTION_ATTRACTION, strength).vec4();
        }

        return UIColors.INTERACTION_NEUTRAL_SURFACE.blend(UIColors.INTERACTION_REPULSION, strength).vec4();
    }

    private void textColored(UIColor color, String text) {
        ImGui.textColored(color.red(), color.green(), color.blue(), color.alpha(), text);
    }

    float matrixEditStep() {
        return matrixEditStep;
    }

    void setMatrixEditStep(float matrixEditStep) {
        this.matrixEditStep = Math.max(0.01f, Math.min(0.5f, matrixEditStep));
    }
}
