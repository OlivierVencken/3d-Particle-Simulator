package com.particle.sim.ui.components;

import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;
import com.particle.sim.ui.theme.Color;
import com.particle.sim.ui.theme.Colors;
import com.particle.sim.ui.theme.ComponentVariant;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Theme;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiButtonFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

/** Focusable attraction matrix and its shared action controls. */
public final class AttractionMatrixControl {
    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int RIGHT_MOUSE_BUTTON = 1;
    private static final float HEADER_CIRCLE_RADIUS_SCALE = 0.34f;

    private AttractionMatrixControl() {}

    public static void render(
            SimulationViewModel.Particles particles, SimulationViewActions.Particles actions) {
        renderActions(particles, actions);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, Colors.TRANSPARENT.vec4());
        try {
            renderGrid(particles, actions);
        } finally {
            ImGui.popStyleColor();
        }
        renderLegend();
    }

    private static void renderActions(
            SimulationViewModel.Particles particles, SimulationViewActions.Particles actions) {
        DesignTokens tokens = Theme.tokens();
        if (Button.text(
                "Mutate 5%",
                "matrix-mutate-5", ComponentVariant.SECONDARY, 0.0f, tokens.controlHeight())) {
            actions.mutateAttractionMatrix(0.05f);
        }
        continueActionRow("10%", tokens);
        if (Button.text(
                "10%",
                "matrix-mutate-10", ComponentVariant.SECONDARY, 0.0f, tokens.controlHeight())) {
            actions.mutateAttractionMatrix(0.10f);
        }
        continueActionRow("25%", tokens);
        if (Button.text(
                "25%",
                "matrix-mutate-25", ComponentVariant.SECONDARY, 0.0f, tokens.controlHeight())) {
            actions.mutateAttractionMatrix(0.25f);
        }

        if (Button.text(
                "Zero", "matrix-zero", ComponentVariant.SECONDARY, 0.0f, tokens.controlHeight())) {
            actions.zeroAttractionMatrix();
        }
        continueActionRow("Symmetrize", tokens);
        if (Button.text(
                "Symmetrize",
                "matrix-symmetrize",
                ComponentVariant.SECONDARY,
                0.0f,
                tokens.controlHeight())) {
            actions.symmetrizeAttractionMatrix();
        }
        continueActionRow("Invert", tokens);
        if (Button.text(
                "Invert",
                "matrix-invert",
                ComponentVariant.SECONDARY,
                0.0f,
                tokens.controlHeight())) {
            actions.invertAttractionMatrix();
        }
        continueActionRow("Normalize", tokens);
        if (Button.text(
                "Normalize",
                "matrix-normalize",
                ComponentVariant.SECONDARY,
                0.0f,
                tokens.controlHeight())) {
            actions.normalizeAttractionMatrix();
        }

        if (Button.text(
                "Undo",
                "matrix-undo",
                ComponentVariant.SECONDARY,
                0.0f,
                tokens.controlHeight(),
                particles.canUndoAttractionMatrix())) {
            actions.undoAttractionMatrix();
        }
        continueActionRow("Redo", tokens);
        if (Button.text(
                "Redo",
                "matrix-redo",
                ComponentVariant.SECONDARY,
                0.0f,
                tokens.controlHeight(),
                particles.canRedoAttractionMatrix())) {
            actions.redoAttractionMatrix();
        }

        ImBoolean animated = new ImBoolean(particles.attractionMutationAnimated());
        if (Checkbox.renderToggle("Gradual mutation", "matrix-animate-mutation", animated, true)) {
            actions.setAttractionMutationAnimated(animated.get());
        }
    }

    private static void continueActionRow(String nextLabel, DesignTokens tokens) {
        float nextWidth = ImGui.calcTextSize(nextLabel).x + tokens.frameInsetHorizontal() * 2.0f;
        float rightEdge = ImGui.getWindowPosX() + ImGui.getWindowContentRegionMaxX();
        if (fitsOnLine(ImGui.getItemRectMaxX(), nextWidth, tokens.spaceMd(), rightEdge)) {
            ImGui.sameLine();
        }
    }

    static boolean fitsOnLine(float currentRight, float nextWidth, float spacing, float rightEdge) {
        return currentRight + Math.max(0.0f, spacing) + Math.max(0.0f, nextWidth) <= rightEdge;
    }

    private static void renderGrid(
            SimulationViewModel.Particles particles, SimulationViewActions.Particles actions) {
        DesignTokens tokens = Theme.tokens();
        ImGui.spacing();
        int groupCount = particles.groupCount();
        if (groupCount <= 0) {
            Text.emptyState("No groups to display.");
            return;
        }

        float availableWidth = Math.max(0.0f, ImGui.getContentRegionAvailX());
        float gap = tokens.matrixGap();
        float fittedSize = fittedCellSize(availableWidth, groupCount, gap);
        boolean needsScroll = fittedSize < tokens.minimumHitTarget();
        float cellSize =
                resolvedCellSize(
                        availableWidth,
                        groupCount,
                        gap,
                        tokens.minimumHitTarget(),
                        tokens.matrixCellMaximumSize());
        if (cellSize <= tokens.matrixCellInset()) {
            Text.emptyState("Increase the panel width to display the matrix.");
            return;
        }
        float totalSize = (groupCount + 1) * cellSize + groupCount * gap;
        if (needsScroll) {
            float viewportHeight =
                    Math.min(totalSize + tokens.spaceMd(), tokens.matrixViewportMaximumHeight());
            int flags = ImGuiWindowFlags.HorizontalScrollbar;
            boolean visible =
                    ImGui.beginChild(
                            "###attraction-matrix-scroll", 0.0f, viewportHeight, false, flags);
            try {
                if (visible) {
                    drawGrid(particles, actions, groupCount, gap, cellSize, totalSize, tokens);
                }
            } finally {
                ImGui.endChild();
            }
            return;
        }
        ImGui.setCursorPosX(
                ImGui.getCursorPosX() + Math.max(0.0f, (availableWidth - totalSize) * 0.5f));
        drawGrid(particles, actions, groupCount, gap, cellSize, totalSize, tokens);
    }

    private static void drawGrid(
            SimulationViewModel.Particles particles,
            SimulationViewActions.Particles actions,
            int groupCount,
            float gap,
            float cellSize,
            float totalSize,
            DesignTokens tokens) {
        ImVec2 origin = ImGui.getCursorScreenPos();
        ImDrawList drawList = ImGui.getWindowDrawList();

        drawList.addRectFilled(
                origin.x,
                origin.y,
                origin.x + totalSize,
                origin.y + totalSize,
                ImGui.getColorU32(Colors.MATRIX_BACKGROUND.vec4()),
                tokens.radiusLg());
        drawList.addRect(
                origin.x,
                origin.y,
                origin.x + totalSize,
                origin.y + totalSize,
                ImGui.getColorU32(Colors.MATRIX_PANEL_BORDER.vec4()),
                tokens.radiusLg());

        for (int column = 0; column < groupCount; column++) {
            drawHeader(
                    particles,
                    column,
                    true,
                    origin.x + (column + 1) * (cellSize + gap),
                    origin.y,
                    cellSize,
                    drawList,
                    tokens);
        }
        for (int row = 0; row < groupCount; row++) {
            float y = origin.y + (row + 1) * (cellSize + gap);
            drawHeader(particles, row, false, origin.x, y, cellSize, drawList, tokens);
            for (int column = 0; column < groupCount; column++) {
                drawCell(
                        particles,
                        actions,
                        row,
                        column,
                        origin.x + (column + 1) * (cellSize + gap),
                        y,
                        cellSize,
                        drawList,
                        tokens);
            }
        }
        ImGui.setCursorScreenPos(origin.x, origin.y);
        ImGui.dummy(totalSize, totalSize);
    }

    private static void drawHeader(
            SimulationViewModel.Particles particles,
            int group,
            boolean column,
            float x,
            float y,
            float size,
            ImDrawList drawList,
            DesignTokens tokens) {
        ImVec4 groupColor = particles.groupColor(group);
        float centerX = x + size * 0.5f;
        float centerY = y + size * 0.5f;
        float radius = size * HEADER_CIRCLE_RADIUS_SCALE;

        ImGui.pushID((column ? "column-" : "row-") + group);
        try {
            ImGui.setCursorScreenPos(x, y);
            ImGui.invisibleButton("###matrix-header", size, size);
            boolean hovered = ImGui.isItemHovered();
            boolean focused = ImGui.isItemFocused();
            drawList.addCircleFilled(
                    centerX,
                    centerY,
                    radius,
                    ImGui.getColorU32(groupColor.x, groupColor.y, groupColor.z, groupColor.w),
                    24);
            drawList.addCircle(
                    centerX,
                    centerY,
                    radius,
                    ImGui.getColorU32(Colors.MATRIX_HEADER_BORDER.vec4()),
                    24,
                    tokens.emphasizedBorderWidth());
            if (focused || hovered) {
                drawList.addRect(
                        x,
                        y,
                        x + size,
                        y + size,
                        ImGui.getColorU32(
                                focused ? ImGuiCol.NavHighlight : ImGuiCol.SeparatorHovered),
                        tokens.radiusSm(),
                        0,
                        focused ? tokens.emphasizedBorderWidth() : tokens.borderWidth());
                Tooltip.forLastItem("Group %d %s".formatted(group + 1, column ? "column" : "row"));
            }
        } finally {
            ImGui.popID();
        }
    }

    private static void drawCell(
            SimulationViewModel.Particles particles,
            SimulationViewActions.Particles actions,
            int row,
            int column,
            float x,
            float y,
            float size,
            ImDrawList drawList,
            DesignTokens tokens) {
        float value = particles.attraction(row, column);
        ImVec4 color = attractionColor(value).vec4();
        float renderedSize = Math.max(0.0f, size - tokens.matrixCellInset());

        ImGui.pushID(row * 10_000 + column);
        try {
            ImGui.setCursorScreenPos(x, y);
            ImGui.invisibleButton(
                    "###matrix-cell",
                    renderedSize,
                    renderedSize,
                    ImGuiButtonFlags.MouseButtonLeft | ImGuiButtonFlags.MouseButtonRight);
            boolean hovered = ImGui.isItemHovered();
            boolean focused = ImGui.isItemFocused();
            boolean leftClick = ImGui.isItemClicked(LEFT_MOUSE_BUTTON);
            boolean rightClick = ImGui.isItemClicked(RIGHT_MOUSE_BUTTON);
            boolean keyboardActivation = ImGui.isItemActivated() && !leftClick && !rightClick;

            drawList.addRectFilled(
                    x,
                    y,
                    x + renderedSize,
                    y + renderedSize,
                    ImGui.getColorU32(color.x, color.y, color.z, color.w),
                    tokens.radiusSm());
            int borderColor =
                    focused
                            ? ImGui.getColorU32(ImGuiCol.NavHighlight)
                            : ImGui.getColorU32(
                                    hovered
                                            ? Colors.MATRIX_CELL_HOVER.vec4()
                                            : Colors.MATRIX_CELL_BORDER.vec4());
            drawList.addRect(
                    x,
                    y,
                    x + renderedSize,
                    y + renderedSize,
                    borderColor,
                    tokens.radiusSm(),
                    0,
                    hovered || focused ? tokens.emphasizedBorderWidth() : tokens.borderWidth());

            if (leftClick || keyboardActivation) {
                actions.adjustAttraction(row, column, particles.matrixEditStep());
            } else if (rightClick) {
                actions.adjustAttraction(row, column, -particles.matrixEditStep());
            }
            if (hovered || focused) {
                Tooltip.forLastItem(
                        "Group %d toward group %d: %+.2f".formatted(row + 1, column + 1, value)
                                + '\n'
                                + "Activate to increase; right-click to decrease.");
            }
        } finally {
            ImGui.popID();
        }
    }

    private static void renderLegend() {
        ImGui.spacing();
        String attraction = "+ attraction";
        String neutral = "0 neutral";
        String repulsion = "- repulsion";
        float spacing = ImGui.getStyle().getItemSpacingX();
        float width =
                ImGui.calcTextSize(attraction).x
                        + ImGui.calcTextSize(neutral).x
                        + ImGui.calcTextSize(repulsion).x
                        + spacing * 2.0f;
        ImGui.setCursorPosX(
                ImGui.getCursorPosX()
                        + Math.max(0.0f, (ImGui.getContentRegionAvailX() - width) * 0.5f));
        coloredText(Colors.INTERACTION_ATTRACTION, attraction);
        ImGui.sameLine();
        coloredText(Colors.INTERACTION_NEUTRAL, neutral);
        ImGui.sameLine();
        coloredText(Colors.INTERACTION_REPULSION, repulsion);
    }

    private static void coloredText(Color color, String text) {
        ImGui.textColored(color.red(), color.green(), color.blue(), color.alpha(), text);
    }

    static float fittedCellSize(float availableWidth, int groupCount, float gap) {
        if (availableWidth <= 0.0f || groupCount <= 0) {
            return 0.0f;
        }
        return Math.max(
                0.0f, (availableWidth - groupCount * Math.max(0.0f, gap)) / (groupCount + 1));
    }

    static float resolvedCellSize(
            float availableWidth,
            int groupCount,
            float gap,
            float minimumCellSize,
            float maximumCellSize) {
        if (availableWidth <= 0.0f || groupCount <= 0 || maximumCellSize <= 0.0f) {
            return 0.0f;
        }
        float minimum = Math.max(0.0f, Math.min(minimumCellSize, maximumCellSize));
        return Math.max(
                minimum,
                Math.min(maximumCellSize, fittedCellSize(availableWidth, groupCount, gap)));
    }

    static Color attractionColor(float value) {
        float strength = Math.min(1.0f, Math.abs(value));
        return value >= 0.0f
                ? Colors.INTERACTION_NEUTRAL_SURFACE.blend(Colors.INTERACTION_ATTRACTION, strength)
                : Colors.INTERACTION_NEUTRAL_SURFACE.blend(Colors.INTERACTION_REPULSION, strength);
    }
}
