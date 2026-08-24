package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIColors;
import com.particle.sim.ui.theme.UIFonts;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiComboFlags;
import imgui.type.ImInt;

public final class UICombo {
    private UICombo() {
    }

    public static boolean render(String label, String id, ImInt valueRef, String[] values) {
        ImGui.pushFont(UIFonts.medium());
        ImGui.textUnformatted(label);

        String preview = isValidIndex(valueRef.get(), values) ? values[valueRef.get()] : "Select...";
        float width = ImGui.getContentRegionAvailX();
        float height = ImGui.getFrameHeight();
        ImVec2 origin = ImGui.getCursorScreenPos();
        ImDrawList drawList = ImGui.getWindowDrawList();

        ImGui.setNextItemWidth(width);
        hideNativeCombo();
        boolean open = ImGui.beginCombo("##combo-" + id, "", ImGuiComboFlags.NoArrowButton);
        restoreNativeCombo();

        boolean hovered = ImGui.isMouseHoveringRect(origin.x, origin.y, origin.x + width, origin.y + height);
        boolean focused = ImGui.isItemFocused();
        drawPreview(drawList, origin, width, height, preview, hovered, focused, open);

        boolean changed = false;
        if (open) {
            for (int index = 0; index < values.length; index++) {
                boolean selected = index == valueRef.get();
                if (ImGui.selectable(values[index] + "##combo-option-" + id + "-" + index, selected)) {
                    valueRef.set(index);
                    changed = true;
                }
                if (selected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }

        ImGui.popFont();
        return changed;
    }

    private static void hideNativeCombo() {
        ImGui.pushStyleColor(ImGuiCol.FrameBg, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.Border, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.NavHighlight, UIColors.TRANSPARENT.vec4());
    }

    private static void restoreNativeCombo() {
        ImGui.popStyleColor(5);
    }

    private static void drawPreview(ImDrawList drawList, ImVec2 origin, float width, float height, String preview,
            boolean hovered, boolean focused, boolean open) {
        UIDesignTokens tokens = UITheme.tokens();
        int fillColor = ImGui.getColorU32(open ? ImGuiCol.FrameBgActive
                : hovered ? ImGuiCol.FrameBgHovered : ImGuiCol.FrameBg);
        int borderColor = ImGui.getColorU32(focused ? ImGuiCol.NavHighlight
                : hovered ? ImGuiCol.SeparatorHovered : ImGuiCol.Border);
        float borderThickness = focused ? tokens.emphasizedBorderWidth() : tokens.borderWidth();

        float maxX = origin.x + width;
        float maxY = origin.y + height;
        drawList.addRectFilled(origin.x, origin.y, maxX, maxY, fillColor, tokens.radiusMd());
        drawList.addRect(origin.x, origin.y, maxX, maxY, borderColor, tokens.radiusMd(), 0, borderThickness);

        float textY = origin.y + (height - ImGui.getTextLineHeight()) * 0.5f;
        drawList.addText(origin.x + tokens.spaceLg(), textY, ImGui.getColorU32(ImGuiCol.Text), preview);

        float chevronWidth = tokens.chevronWidth();
        float centerX = maxX - tokens.spaceLg() - chevronWidth * 0.5f;
        float centerY = origin.y + height * 0.5f;
        float direction = open ? -1.0f : 1.0f;
        int chevronColor = ImGui.getColorU32(ImGuiCol.TextDisabled);
        drawList.addLine(centerX - chevronWidth * 0.5f, centerY - direction * tokens.spaceXxs(),
                centerX, centerY + direction * tokens.spaceXxs(), chevronColor, tokens.emphasizedBorderWidth());
        drawList.addLine(centerX, centerY + direction * tokens.spaceXxs(),
                centerX + chevronWidth * 0.5f, centerY - direction * tokens.spaceXxs(),
                chevronColor, tokens.emphasizedBorderWidth());
    }

    private static boolean isValidIndex(int value, String[] values) {
        return value >= 0 && value < values.length;
    }
}
