package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;
import imgui.flag.ImGuiHoveredFlags;

/** Delayed, wrapped tooltips that also appear for keyboard-focused items. */
public final class Tooltip {
    private Tooltip() {}

    public static void forLastItem(String text) {
        if (!hasContent(text)) {
            return;
        }
        boolean hoveredLongEnough =
                ImGui.isItemHovered(ImGuiHoveredFlags.ForTooltip | ImGuiHoveredFlags.DelayNormal);
        if (!hoveredLongEnough && !ImGui.isItemFocused()) {
            return;
        }

        ImGui.beginTooltip();
        ImGui.pushTextWrapPos(ImGui.getCursorPosX() + Theme.tokens().tooltipWrapWidth());
        try {
            ImGui.textUnformatted(text);
        } finally {
            ImGui.popTextWrapPos();
            ImGui.endTooltip();
        }
    }

    static boolean hasContent(String text) {
        return text != null && !text.isBlank();
    }
}
