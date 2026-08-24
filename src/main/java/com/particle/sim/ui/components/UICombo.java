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
        return render(label, id, valueRef, values, true);
    }

    public static boolean render(String label, String id, ImInt valueRef, String[] values, boolean enabled) {
        ImGui.pushFont(UIFonts.medium());
        try {
            ImGui.textUnformatted(label);

            String preview = isValidIndex(valueRef.get(), values) ? values[valueRef.get()] : "Select...";
            float width = ImGui.getContentRegionAvailX();
            float height = ImGui.getFrameHeight();
            ImVec2 origin = ImGui.getCursorScreenPos();
            ImDrawList drawList = ImGui.getWindowDrawList();

            ImGui.setNextItemWidth(width);
            hideNativeCombo();
            ImGui.beginDisabled(!enabled);
            try {
                boolean open;
                try {
                    open = ImGui.beginCombo("###combo-" + id, "", ImGuiComboFlags.NoArrowButton);
                } finally {
                    restoreNativeCombo();
                }

                boolean hovered = ImGui.isMouseHoveringRect(origin.x, origin.y, origin.x + width, origin.y + height);
                boolean focused = ImGui.isItemFocused();
                drawPreview(drawList, origin, width, height, preview, hovered, focused, open, enabled);
                boolean changed = false;
                if (open) {
                    try {
                        for (int index = 0; index < values.length; index++) {
                            boolean selected = index == valueRef.get();
                            if (ImGui.selectable(UIButton.itemLabel(values[index],
                                    "combo-option-" + id + "-" + index), selected)) {
                                valueRef.set(index);
                                changed = true;
                            }
                            if (selected) {
                                ImGui.setItemDefaultFocus();
                            }
                        }
                    } finally {
                        ImGui.endCombo();
                    }
                }
                return enabled && changed;
            } finally {
                ImGui.endDisabled();
            }
        } finally {
            ImGui.popFont();
        }
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
            boolean hovered, boolean focused, boolean open, boolean enabled) {
        UIDesignTokens tokens = UITheme.tokens();
        int fillColor = ImGui.getColorU32(!enabled ? ImGuiCol.FrameBg : open ? ImGuiCol.FrameBgActive
                : hovered ? ImGuiCol.FrameBgHovered : ImGuiCol.FrameBg);
        int borderColor = ImGui.getColorU32(focused ? ImGuiCol.NavHighlight
                : hovered ? ImGuiCol.SeparatorHovered : ImGuiCol.Border);
        float borderThickness = focused ? tokens.emphasizedBorderWidth() : tokens.borderWidth();

        float maxX = origin.x + width;
        float maxY = origin.y + height;
        drawList.addRectFilled(origin.x, origin.y, maxX, maxY, fillColor, tokens.radiusMd());
        drawList.addRect(origin.x, origin.y, maxX, maxY, borderColor, tokens.radiusMd(), 0, borderThickness);

        float textY = origin.y + (height - ImGui.getTextLineHeight()) * 0.5f;
        float textStart = origin.x + tokens.spaceLg();
        float textEnd = maxX - tokens.spaceLg() * 2.0f - tokens.chevronWidth();
        drawList.pushClipRect(textStart, origin.y, Math.max(textStart, textEnd), maxY, true);
        drawList.addText(textStart, textY, ImGui.getColorU32(enabled ? ImGuiCol.Text : ImGuiCol.TextDisabled), preview);
        drawList.popClipRect();

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

    static boolean isValidIndex(int value, String[] values) {
        return value >= 0 && value < values.length;
    }

    static float previewTextWidth(float width, UIDesignTokens tokens) {
        return Math.max(0.0f, width - tokens.spaceLg() * 3.0f - tokens.chevronWidth());
    }
}
