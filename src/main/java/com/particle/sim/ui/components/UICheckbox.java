package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIFonts;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.type.ImBoolean;

public final class UICheckbox {
    private static final float SIZE = 20.0f;
    private static final float LABEL_GAP = 10.0f;
    private static final float VERTICAL_PADDING = 2.0f;
    private static final float ROUNDING = 4.0f;

    private UICheckbox() {
    }

    public static boolean render(String label, String id, ImBoolean valueRef) {
        ImGui.pushFont(UIFonts.medium());

        float textHeight = ImGui.getTextLineHeight();
        float rowHeight = Math.max(SIZE, textHeight) + VERTICAL_PADDING * 2.0f;
        float minimumWidth = SIZE + LABEL_GAP + ImGui.calcTextSize(label).x;
        float rowWidth = Math.max(minimumWidth, ImGui.getContentRegionAvailX());
        ImVec2 origin = ImGui.getCursorScreenPos();

        boolean changed = ImGui.invisibleButton("##checkbox-" + id, rowWidth, rowHeight);
        if (changed) {
            valueRef.set(!valueRef.get());
        }

        draw(label, valueRef.get(), origin, rowHeight);
        ImGui.popFont();
        return changed;
    }

    private static void draw(String label, boolean checked, ImVec2 origin, float rowHeight) {
        boolean hovered = ImGui.isItemHovered();
        boolean active = ImGui.isItemActive();
        boolean focused = ImGui.isItemFocused();

        float boxX = origin.x;
        float boxY = origin.y + (rowHeight - SIZE) * 0.5f;
        float boxMaxX = boxX + SIZE;
        float boxMaxY = boxY + SIZE;

        int fillColor;
        if (checked) {
            fillColor = ImGui.getColorU32(hovered || active ? ImGuiCol.ButtonActive : ImGuiCol.FrameBgActive);
        } else if (active) {
            fillColor = ImGui.getColorU32(ImGuiCol.FrameBgActive);
        } else if (hovered) {
            fillColor = ImGui.getColorU32(ImGuiCol.FrameBgHovered);
        } else {
            fillColor = ImGui.getColorU32(ImGuiCol.FrameBg);
        }

        int borderColor = ImGui.getColorU32(focused ? ImGuiCol.NavHighlight
                : hovered ? ImGuiCol.SeparatorHovered : ImGuiCol.Border);
        float borderThickness = focused ? 2.0f : 1.0f;

        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(boxX, boxY, boxMaxX, boxMaxY, fillColor, ROUNDING);
        drawList.addRect(boxX, boxY, boxMaxX, boxMaxY, borderColor, ROUNDING, 0, borderThickness);

        if (checked) {
            int checkColor = ImGui.getColorU32(ImGuiCol.CheckMark);
            drawList.addLine(boxX + 4.5f, boxY + 10.5f, boxX + 8.6f, boxY + 14.5f, checkColor, 2.25f);
            drawList.addLine(boxX + 8.4f, boxY + 14.5f, boxX + 16.0f, boxY + 5.5f, checkColor, 2.25f);
        }

        float labelX = boxMaxX + LABEL_GAP;
        float labelY = origin.y + (rowHeight - ImGui.getTextLineHeight()) * 0.5f;
        drawList.addText(labelX, labelY, ImGui.getColorU32(ImGuiCol.Text), label);
    }
}
