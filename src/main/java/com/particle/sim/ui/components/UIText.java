package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIColor;
import com.particle.sim.ui.theme.UIColors;
import com.particle.sim.ui.theme.UIFonts;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;
import imgui.flag.ImGuiCol;

/** Shared non-interactive typography and separators. */
public final class UIText {
    private UIText() {
    }

    public static void sectionHeading(String label) {
        ImGui.pushFont(UIFonts.section());
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
        coloredWrapped(UIColors.TEXT_WARNING, text);
    }

    public static void error(String text) {
        coloredWrapped(UIColors.TEXT_ERROR, text);
    }

    public static void emptyState(String text) {
        float y = ImGui.getCursorPosY();
        float offset = Math.max(0.0f,
                (UITheme.tokens().emptyStateMinimumHeight() - ImGui.getTextLineHeight()) * 0.5f);
        ImGui.setCursorPosY(y + offset);
        ImGui.textDisabled(text);
        ImGui.setCursorPosY(Math.max(ImGui.getCursorPosY(), y + UITheme.tokens().emptyStateMinimumHeight()));
    }

    private static void coloredWrapped(UIColor color, String text) {
        ImGui.pushStyleColor(ImGuiCol.Text, color.vec4());
        try {
            ImGui.textWrapped(text);
        } finally {
            ImGui.popStyleColor();
        }
    }
}
