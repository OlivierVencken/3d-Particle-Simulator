package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIColors;
import com.particle.sim.ui.theme.UIFonts;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiSliderFlags;
import imgui.flag.ImGuiStyleVar;

public final class UISlider {
    public enum NumericEntryPolicy {
        KEYBOARD_ADJUSTMENT_ONLY(ImGuiSliderFlags.NoInput),
        DIRECT_ENTRY(ImGuiSliderFlags.None);

        private final int flags;

        NumericEntryPolicy(int flags) {
            this.flags = flags;
        }

        int flags() {
            return flags;
        }
    }

    private UISlider() {
    }

    public static boolean render(String label, String id, float[] valueRef, float min, float max, int decimals) {
        return render(label, id, valueRef, min, max, decimals, NumericEntryPolicy.KEYBOARD_ADJUSTMENT_ONLY, true);
    }

    public static boolean render(String label, String id, float[] valueRef, float min, float max, int decimals,
            NumericEntryPolicy entryPolicy, boolean enabled) {
        ImGui.pushFont(UIFonts.medium());
        drawLabel(label, ("%." + decimals + "f").formatted(valueRef[0]));

        ImGui.setNextItemWidth(-1.0f);
        hideNativeSlider();
        ImGui.beginDisabled(!enabled);
        boolean changed = ImGui.sliderFloat("###slider-" + id, valueRef, min, max, "", entryPolicy.flags());
        ImGui.endDisabled();
        restoreNativeSlider();

        drawTrack(normalize(valueRef[0], min, max), enabled);
        ImGui.popFont();
        return enabled && changed;
    }

    public static boolean render(String label, String id, int[] valueRef, int min, int max) {
        return render(label, id, valueRef, min, max, NumericEntryPolicy.KEYBOARD_ADJUSTMENT_ONLY, true);
    }

    public static boolean render(String label, String id, int[] valueRef, int min, int max,
            NumericEntryPolicy entryPolicy, boolean enabled) {
        ImGui.pushFont(UIFonts.medium());
        drawLabel(label, Integer.toString(valueRef[0]));

        ImGui.setNextItemWidth(-1.0f);
        hideNativeSlider();
        ImGui.beginDisabled(!enabled);
        boolean changed = ImGui.sliderInt("###slider-" + id, valueRef, min, max, "", entryPolicy.flags());
        ImGui.endDisabled();
        restoreNativeSlider();

        drawTrack(normalize(valueRef[0], min, max), enabled);
        ImGui.popFont();
        return enabled && changed;
    }

    private static void drawLabel(String label, String value) {
        UIDesignTokens tokens = UITheme.tokens();
        ImGui.textUnformatted(label);
        float right = ImGui.getWindowContentRegionMaxX();
        float valueWidth = ImGui.calcTextSize(value).x;
        ImGui.sameLine(Math.max(ImGui.getCursorPosX() + tokens.spaceMd(), right - valueWidth));
        ImGui.textDisabled(value);
    }

    private static void hideNativeSlider() {
        UIDesignTokens tokens = UITheme.tokens();
        ImGui.pushStyleColor(ImGuiCol.FrameBg, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.SliderGrab, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.Border, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.NavHighlight, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleVar(ImGuiStyleVar.GrabMinSize, tokens.sliderThumbRadius() * 2.0f);
    }

    private static void restoreNativeSlider() {
        ImGui.popStyleVar();
        ImGui.popStyleColor(7);
    }

    private static void drawTrack(float normalizedValue, boolean enabled) {
        UIDesignTokens tokens = UITheme.tokens();
        boolean hovered = ImGui.isItemHovered();
        boolean active = ImGui.isItemActive();
        boolean focused = ImGui.isItemFocused();
        ImVec2 min = ImGui.getItemRectMin();
        ImVec2 max = ImGui.getItemRectMax();

        float thumbRadius = tokens.sliderThumbRadius();
        float trackHeight = tokens.sliderTrackHeight();
        float trackStart = min.x + thumbRadius + tokens.sliderGrabPadding();
        float trackEnd = max.x - thumbRadius - tokens.sliderGrabPadding();
        float centerY = (min.y + max.y) * 0.5f;
        float thumbX = trackStart + (trackEnd - trackStart) * normalizedValue;
        float trackTop = centerY - trackHeight * 0.5f;
        float trackBottom = centerY + trackHeight * 0.5f;

        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(trackStart, trackTop, trackEnd, trackBottom,
                ImGui.getColorU32(hovered ? ImGuiCol.FrameBgHovered : ImGuiCol.FrameBg), trackHeight * 0.5f);
        if (thumbX > trackStart) {
            drawList.addRectFilled(trackStart, trackTop, thumbX, trackBottom,
                    ImGui.getColorU32(active ? ImGuiCol.SliderGrabActive : ImGuiCol.SliderGrab),
                    trackHeight * 0.5f);
        }

        int thumbColor = ImGui.getColorU32(!enabled ? ImGuiCol.TextDisabled
                : active || hovered ? ImGuiCol.SliderGrabActive : ImGuiCol.SliderGrab);
        int thumbBorder = ImGui.getColorU32(focused ? ImGuiCol.NavHighlight : ImGuiCol.Border);
        drawList.addCircleFilled(thumbX, centerY, thumbRadius, thumbColor, 20);
        drawList.addCircle(thumbX, centerY, thumbRadius, thumbBorder, 20,
                focused ? tokens.emphasizedBorderWidth() : tokens.borderWidth());
    }

    static float normalize(float value, float min, float max) {
        if (max <= min) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, (value - min) / (max - min)));
    }
}
