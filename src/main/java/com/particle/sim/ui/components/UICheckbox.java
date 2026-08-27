package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIFonts;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.type.ImBoolean;

public final class UICheckbox {
    private UICheckbox() {
    }

    public static boolean render(String label, String id, ImBoolean valueRef) {
        return render(label, id, valueRef, true);
    }

    public static boolean render(String label, String id, ImBoolean valueRef, boolean enabled) {
        ImGui.pushFont(UIFonts.medium());
        UIDesignTokens tokens = UITheme.tokens();

        float textHeight = ImGui.getTextLineHeight();
        float rowHeight = Math.max(tokens.minimumHitTarget(),
                Math.max(tokens.checkboxSize(), textHeight) + tokens.spaceXxs() * 2.0f);
        float minimumWidth = tokens.checkboxSize() + tokens.spaceLg() + ImGui.calcTextSize(label).x;
        float rowWidth = Math.max(minimumWidth, ImGui.getContentRegionAvailX());
        ImVec2 origin = ImGui.getCursorScreenPos();

        ImGui.beginDisabled(!enabled);
        boolean changed = ImGui.invisibleButton("###checkbox-" + id, rowWidth, rowHeight);
        ImGui.endDisabled();
        if (changed) {
            valueRef.set(!valueRef.get());
        }

        draw(label, valueRef.get(), origin, rowHeight, tokens, enabled);
        ImGui.popFont();
        return enabled && changed;
    }

    private static void draw(String label, boolean checked, ImVec2 origin, float rowHeight,
            UIDesignTokens tokens, boolean enabled) {
        boolean hovered = ImGui.isItemHovered();
        boolean active = ImGui.isItemActive();
        boolean focused = ImGui.isItemFocused();

        float boxX = origin.x;
        float size = tokens.checkboxSize();
        float boxY = origin.y + (rowHeight - size) * 0.5f;
        float boxMaxX = boxX + size;
        float boxMaxY = boxY + size;

        int fillColor;
        if (!enabled) {
            fillColor = ImGui.getColorU32(ImGuiCol.FrameBg);
        } else if (checked) {
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
        float borderThickness = focused ? tokens.emphasizedBorderWidth() : tokens.borderWidth();

        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(boxX, boxY, boxMaxX, boxMaxY, fillColor, tokens.radiusMd());
        drawList.addRect(boxX, boxY, boxMaxX, boxMaxY, borderColor, tokens.radiusMd(), 0, borderThickness);

        if (checked) {
            int checkColor = ImGui.getColorU32(ImGuiCol.CheckMark);
            drawList.addLine(boxX + size * 0.225f, boxY + size * 0.525f,
                    boxX + size * 0.43f, boxY + size * 0.725f, checkColor, tokens.checkmarkWidth());
            drawList.addLine(boxX + size * 0.42f, boxY + size * 0.725f,
                    boxX + size * 0.8f, boxY + size * 0.275f, checkColor, tokens.checkmarkWidth());
        }

        float labelX = boxMaxX + tokens.spaceLg();
        float labelY = origin.y + (rowHeight - ImGui.getTextLineHeight()) * 0.5f;
        drawList.addText(labelX, labelY, ImGui.getColorU32(enabled ? ImGuiCol.Text : ImGuiCol.TextDisabled), label);
    }

    public static boolean renderToggle(String label, String id, ImBoolean valueRef, boolean enabled) {
        ImGui.pushFont(UIFonts.medium());
        UIDesignTokens tokens = UITheme.tokens();
        float height = Math.max(tokens.minimumHitTarget(), tokens.checkboxSize());
        float toggleWidth = tokens.checkboxSize() * 1.8f;
        float width = Math.max(toggleWidth + tokens.spaceLg() + ImGui.calcTextSize(label).x,
                ImGui.getContentRegionAvailX());
        ImVec2 origin = ImGui.getCursorScreenPos();
        ImGui.beginDisabled(!enabled);
        boolean changed = ImGui.invisibleButton("###toggle-" + id, width, height);
        ImGui.endDisabled();
        if (changed) {
            valueRef.set(!valueRef.get());
        }

        boolean hovered = ImGui.isItemHovered();
        boolean focused = ImGui.isItemFocused();
        float trackHeight = tokens.checkboxSize();
        float trackY = origin.y + (height - trackHeight) * 0.5f;
        ImDrawList drawList = ImGui.getWindowDrawList();
        int fill = ImGui.getColorU32(valueRef.get() ? ImGuiCol.FrameBgActive
                : hovered ? ImGuiCol.FrameBgHovered : ImGuiCol.FrameBg);
        drawList.addRectFilled(origin.x, trackY, origin.x + toggleWidth, trackY + trackHeight,
                fill, trackHeight * 0.5f);
        drawList.addRect(origin.x, trackY, origin.x + toggleWidth, trackY + trackHeight,
                ImGui.getColorU32(focused ? ImGuiCol.NavHighlight : ImGuiCol.Border), trackHeight * 0.5f,
                0, focused ? tokens.emphasizedBorderWidth() : tokens.borderWidth());
        float thumbRadius = trackHeight * 0.36f;
        float thumbX = valueRef.get() ? origin.x + toggleWidth - trackHeight * 0.5f
                : origin.x + trackHeight * 0.5f;
        drawList.addCircleFilled(thumbX, trackY + trackHeight * 0.5f, thumbRadius,
                ImGui.getColorU32(enabled ? ImGuiCol.CheckMark : ImGuiCol.TextDisabled), 20);
        drawList.addText(origin.x + toggleWidth + tokens.spaceLg(),
                origin.y + (height - ImGui.getTextLineHeight()) * 0.5f,
                ImGui.getColorU32(enabled ? ImGuiCol.Text : ImGuiCol.TextDisabled), label);
        ImGui.popFont();
        return enabled && changed;
    }
}
