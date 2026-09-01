package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.Color;
import com.particle.sim.ui.theme.Colors;
import com.particle.sim.ui.theme.Fonts;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;
import imgui.flag.ImGuiCol;

/** Shared non-interactive typography and separators. */
public final class Text {
    private Text() {}

    public static void sectionHeading(String label) {
        ImGui.pushFont(Fonts.section());
        try {
            ImGui.textUnformatted(label);
        } finally {
            ImGui.popFont();
        }
    }

    public static void divider() {
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
    }

    public static void helper(String text) {
        ImGui.textDisabled(text);
    }

    public static void warning(String text) {
        coloredWrapped(Colors.TEXT_WARNING, text);
    }

    public static void error(String text) {
        coloredWrapped(Colors.TEXT_ERROR, text);
    }

    public static void emptyState(String text) {
        float y = ImGui.getCursorPosY();
        float offset =
                Math.max(
                        0.0f,
                        (Theme.tokens().emptyStateMinimumHeight() - ImGui.getTextLineHeight())
                                * 0.5f);
        ImGui.setCursorPosY(y + offset);
        ImGui.textDisabled(text);
        ImGui.setCursorPosY(
                Math.max(ImGui.getCursorPosY(), y + Theme.tokens().emptyStateMinimumHeight()));
    }

    private static void coloredWrapped(Color color, String text) {
        ImGui.pushStyleColor(ImGuiCol.Text, color.vec4());
        try {
            ImGui.textWrapped(text);
        } finally {
            ImGui.popStyleColor();
        }
    }
}
