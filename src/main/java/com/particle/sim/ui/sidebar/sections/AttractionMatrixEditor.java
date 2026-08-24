package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.ui.theme.UIColor;
import com.particle.sim.ui.theme.UIColors;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;
import com.particle.sim.ui.components.UIControls;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;

final class AttractionMatrixEditor {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int RIGHT_MOUSE_BUTTON = 1;

    private static final float HEADER_CIRCLE_RADIUS_SCALE = 0.34f;

    private float matrixEditStep = SimulationDefaults.MATRIX_EDIT_STEP;
    void renderSettings(SimulationUiModel.Particles particles, SimulationUiActions.Particles actions) {
        UIControls.settingSlider(
                "Edit step",
                particles.matrixEditStep(), 0.01f,
                0.5f,
                2,
                actions::setMatrixEditStep);

        renderMatrixActions(actions);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, UIColors.TRANSPARENT.vec4());
        renderMatrix(particles, actions);
        ImGui.popStyleColor();
        renderLegend();
    }

    private void renderMatrixActions(SimulationUiActions.Particles actions) {
        if (ImGui.button("Randomize")) {
            actions.randomizeAttractionMatrix();
        }
        ImGui.sameLine();
        if (ImGui.button("Zero")) {
            actions.zeroAttractionMatrix();
        }
        ImGui.sameLine();
        if (ImGui.button("Symmetrize")) {
            actions.symmetrizeAttractionMatrix();
        }
        ImGui.sameLine();
        if (ImGui.button("Invert")) {
            actions.invertAttractionMatrix();
        }
    }

    private void renderMatrix(SimulationUiModel.Particles particles, SimulationUiActions.Particles actions) {
        UIDesignTokens tokens = UITheme.tokens();
        ImGui.spacing();

        int groupCount = particles.groupCount();
        if (groupCount <= 0) {
            ImGui.textUnformatted("No groups.");
            return;
        }

        float availableWidth = Math.max(0.0f, ImGui.getContentRegionAvailX());
        float matrixGap = tokens.matrixGap();
        float cellSize = fittedCellSize(availableWidth, groupCount, matrixGap);

        float totalSize = (groupCount + 1) * cellSize + groupCount * matrixGap;

        ImVec2 origin = ImGui.getCursorScreenPos();
        ImDrawList drawList = ImGui.getWindowDrawList();

        int panelBg = ImGui.getColorU32(UIColors.MATRIX_BACKGROUND.vec4());
        int panelBorder = ImGui.getColorU32(UIColors.MATRIX_PANEL_BORDER.vec4());
        drawList.addRectFilled(origin.x, origin.y, origin.x + totalSize, origin.y + totalSize,
                panelBg, tokens.radiusLg());
        drawList.addRect(origin.x, origin.y, origin.x + totalSize, origin.y + totalSize,
                panelBorder, tokens.radiusLg());

        drawEmptyCorner(origin.x, origin.y, cellSize, drawList, tokens);

        for (int column = 0; column < groupCount; column++) {
            float x = origin.x + (column + 1) * (cellSize + matrixGap);
            float y = origin.y;

            drawGroupHeaderCircle(particles, column, x, y, cellSize, true, drawList, tokens);
        }

        for (int row = 0; row < groupCount; row++) {
            float y = origin.y + (row + 1) * (cellSize + matrixGap);

            drawGroupHeaderCircle(particles, row, origin.x, y, cellSize, false, drawList, tokens);

            for (int column = 0; column < groupCount; column++) {
                float x = origin.x + (column + 1) * (cellSize + matrixGap);
                drawMatrixTile(particles, actions, row, column, x, y, cellSize, drawList, tokens);
            }
        }
        ImGui.setCursorScreenPos(origin.x, origin.y);
        ImGui.dummy(totalSize, totalSize);
    }

    static float fittedCellSize(float availableWidth, int groupCount) {
        return fittedCellSize(availableWidth, groupCount, UIDesignTokens.unscaled().matrixGap());
    }

    static float fittedCellSize(float availableWidth, int groupCount, float matrixGap) {
        if (availableWidth <= 0.0f || groupCount <= 0) {
            return 0.0f;
        }
        return Math.max(0.0f,
                (availableWidth - groupCount * Math.max(0.0f, matrixGap)) / (groupCount + 1));
    }

    private void drawEmptyCorner(float x, float y, float cellSize, ImDrawList drawList,
            UIDesignTokens tokens) {
        int border = ImGui.getColorU32(UIColors.TRANSPARENT.vec4());
        drawList.addRect(x, y, x + cellSize, y + cellSize, border, tokens.radiusSm());
    }

    private void drawGroupHeaderCircle(SimulationUiModel.Particles particles,
            int group,
            float x, float y,
            float cellSize,
            boolean columnHeader,
            ImDrawList drawList,
            UIDesignTokens tokens) {
        ImVec4 groupColor = groupColor(particles, group);

        float cx = x + cellSize * 0.5f;
        float cy = y + cellSize * 0.5f;
        float radius = cellSize * HEADER_CIRCLE_RADIUS_SCALE;

        int fill = ImGui.getColorU32(groupColor.x, groupColor.y, groupColor.z, groupColor.w);
        int border = ImGui.getColorU32(UIColors.MATRIX_HEADER_BORDER.vec4());

        ImGui.pushID((columnHeader ? "col-" : "row-") + group);
        ImGui.setCursorScreenPos(new ImVec2(x, y));
        ImGui.invisibleButton("##group-header", new ImVec2(cellSize, cellSize));

        drawList.addCircleFilled(cx, cy, radius, fill, 24);
        drawList.addCircle(cx, cy, radius, border, 24, tokens.emphasizedBorderWidth());

        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Group %d".formatted(group + 1));
        }

        ImGui.popID();
    }

    private void drawMatrixTile(SimulationUiModel.Particles particles,
            SimulationUiActions.Particles actions,
            int row, int column,
            float x, float y, float size,
            ImDrawList drawList,
            UIDesignTokens tokens) {
        float value = particles.attraction(row, column);
        ImVec4 color = attractionColor(value);

        int fill = ImGui.getColorU32(color.x, color.y, color.z, color.w);
        int border = ImGui.getColorU32(UIColors.MATRIX_CELL_BORDER.vec4());
        int hoverBorder = ImGui.getColorU32(UIColors.MATRIX_CELL_HOVER.vec4());
        float inset = tokens.matrixCellInset();

        ImGui.pushID(row * 10_000 + column);
        ImGui.setCursorScreenPos(new ImVec2(x, y));
        ImGui.invisibleButton("##tile", new ImVec2(size - inset, size - inset));

        boolean hovered = ImGui.isItemHovered();
        boolean leftClick = ImGui.isItemClicked(LEFT_MOUSE_BUTTON);
        boolean rightClick = ImGui.isItemClicked(RIGHT_MOUSE_BUTTON);

        drawList.addRectFilled(x, y, x + size - inset, y + size - inset, fill, tokens.radiusSm());
        drawList.addRect(
                x, y, x + size - inset, y + size - inset,
                hovered ? hoverBorder : border,
                tokens.radiusSm(),
                0,
                hovered ? tokens.emphasizedBorderWidth() : tokens.borderWidth());

        if (leftClick) {
            actions.adjustAttraction(row, column, particles.matrixEditStep());
        }
        if (rightClick) {
            actions.adjustAttraction(row, column, -particles.matrixEditStep());
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

    private ImVec4 groupColor(SimulationUiModel.Particles particles, int group) {
        return particles.groupColor(group);
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
