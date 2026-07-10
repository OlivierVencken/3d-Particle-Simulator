package com.particle.sim.ui;

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
    private static final float MIN_CELL_SIZE = 28.0f;
    private static final float MAX_CELL_SIZE = 44.0f;

    private float matrixEditStep = SimulationDefaults.MATRIX_EDIT_STEP;



    void renderSettings(GpuParticleSystem particles, Runnable settingsChanged) {
        UiControls.settingSlider(
                "Edit step",
                matrixEditStep, 0.01f,
                0.5f,
                2,
                value -> matrixEditStep = value,
                settingsChanged);

        renderMatrixActions(particles, settingsChanged);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, UiPalette.CLEAR.vec4());
        ImGui.beginChild("matrix-scroll", 0.0f, Math.max(180.0f, ImGui.getContentRegionAvailY() - 34.0f), false,
                imgui.flag.ImGuiWindowFlags.HorizontalScrollbar);
        renderMatrix(particles, settingsChanged);
        ImGui.endChild();
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

        float availableWidth = ImGui.getContentRegionAvailX();

        float fittedCellSize = (availableWidth - (groupCount * MATRIX_GAP)) / (groupCount + 1);
        float cellSize = Math.max(MIN_CELL_SIZE, Math.min(MAX_CELL_SIZE, fittedCellSize));

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
            ImGui.setTooltip("Group %d".formatted(group));
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
            ImGui.setTooltip("G%d → G%d: %.2f".formatted(row, column, value));
        }

        String display = "%.1f".formatted(value);
        float textWidth = ImGui.calcTextSize(display).x;
        int textColor = ImGui.getColorU32(UiPalette.TEXT.vec4());
        drawList.addText(x + Math.max(2.0f, (size - textWidth) * 0.5f),
                y + Math.max(2.0f, (size - ImGui.getTextLineHeight()) * 0.5f), textColor, display);

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

        textColored(UiPalette.MATRIX_ATTRACTION, attraction);
        ImGui.sameLine();
        textColored(UiPalette.MATRIX_NEUTRAL, neutral);
        ImGui.sameLine();
        textColored(UiPalette.MATRIX_REPULSION, repulsion);
    }

    private ImVec4 groupColor(GpuParticleSystem particles, int group) {
        ImVec4[] groupColors = particles.groupColors();
        return groupColors[Math.floorMod(group, groupColors.length)];
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
