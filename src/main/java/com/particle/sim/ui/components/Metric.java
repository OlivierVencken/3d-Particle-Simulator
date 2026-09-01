package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.Colors;
import com.particle.sim.ui.theme.Fonts;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

/** Compact diagnostic card and aligned label/value row. */
public final class Metric {
    private Metric() {}

    public static void card(String id, String label, String value, float width) {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, Colors.SURFACE_DEFAULT.withAlpha(0.72f).vec4());
        int flags = ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse;
        try {
            boolean visible =
                    ImGui.beginChild(
                            "###metric-" + id,
                            width,
                            Theme.tokens().metricCardHeight(),
                            true,
                            flags);
            try {
                if (visible) {
                    ImGui.textDisabled(label);
                    ImGui.pushFont(Fonts.section());
                    try {
                        ImGui.textUnformatted(value);
                    } finally {
                        ImGui.popFont();
                    }
                }
            } finally {
                ImGui.endChild();
            }
        } finally {
            ImGui.popStyleColor();
        }
    }

    public static void row(String label, String value) {
        ImGui.textDisabled(label);
        float valueWidth = ImGui.calcTextSize(value).x;
        float right = ImGui.getWindowContentRegionMaxX();
        ImGui.sameLine(
                Math.max(ImGui.getCursorPosX() + Theme.tokens().spaceMd(), right - valueWidth));
        ImGui.textUnformatted(value);
    }

    static float secondaryWidth(
            float availableWidth, float primaryWidth, float spacing, float minimum) {
        return Math.max(minimum, availableWidth - primaryWidth - spacing);
    }
}
