package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.Colors;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Fonts;
import com.particle.sim.ui.theme.Theme;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiSliderFlags;
import imgui.flag.ImGuiStyleVar;

public final class Slider {
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

    private Slider() {}

    public static boolean render(
            String label, String id, float[] valueRef, float min, float max, int decimals) {
        return render(
                label,
                id,
                valueRef,
                min,
                max,
                decimals,
                NumericEntryPolicy.KEYBOARD_ADJUSTMENT_ONLY,
                true);
    }

    public static boolean render(
            String label,
            String id,
            float[] valueRef,
            float min,
            float max,
            int decimals,
            NumericEntryPolicy entryPolicy,
            boolean enabled) {
        ImGui.pushFont(Fonts.medium());
        drawLabel(label, ("%." + decimals + "f").formatted(valueRef[0]));

        ImGui.setNextItemWidth(-1.0f);
        hideNativeSlider();
        ImGui.beginDisabled(!enabled);
        boolean changed =
                ImGui.sliderFloat("###slider-" + id, valueRef, min, max, "", entryPolicy.flags());
        ImGui.endDisabled();
        restoreNativeSlider();

        drawTrack(normalize(valueRef[0], min, max), enabled);
        ImGui.popFont();
        return enabled && changed;
    }

    public static boolean render(String label, String id, int[] valueRef, int min, int max) {
        return render(
                label, id, valueRef, min, max, NumericEntryPolicy.KEYBOARD_ADJUSTMENT_ONLY, true);
    }

    public static boolean render(
            String label,
            String id,
            int[] valueRef,
            int min,
            int max,
            NumericEntryPolicy entryPolicy,
            boolean enabled) {
        ImGui.pushFont(Fonts.medium());
        drawLabel(label, Integer.toString(valueRef[0]));

        ImGui.setNextItemWidth(-1.0f);
        hideNativeSlider();
        ImGui.beginDisabled(!enabled);
        boolean changed =
                ImGui.sliderInt("###slider-" + id, valueRef, min, max, "", entryPolicy.flags());
        ImGui.endDisabled();
        restoreNativeSlider();

        drawTrack(normalize(valueRef[0], min, max), enabled);
        ImGui.popFont();
        return enabled && changed;
    }

    private static void drawLabel(String label, String value) {
        DesignTokens tokens = Theme.tokens();
        ImGui.textUnformatted(label);
        float right = ImGui.getWindowContentRegionMaxX();
        float valueWidth = ImGui.calcTextSize(value).x;
        ImGui.sameLine(Math.max(ImGui.getCursorPosX() + tokens.spaceMd(), right - valueWidth));
        ImGui.textDisabled(value);
    }

    private static void hideNativeSlider() {
        DesignTokens tokens = Theme.tokens();
        ImGui.pushStyleColor(ImGuiCol.FrameBg, Colors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, Colors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive, Colors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.SliderGrab, Colors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, Colors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.Border, Colors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.NavHighlight, Colors.TRANSPARENT.vec4());
        ImGui.pushStyleVar(ImGuiStyleVar.GrabMinSize, tokens.sliderThumbRadius() * 2.0f);
    }

    private static void restoreNativeSlider() {
        ImGui.popStyleVar();
        ImGui.popStyleColor(7);
    }

    private static void drawTrack(float normalizedValue, boolean enabled) {
        DesignTokens tokens = Theme.tokens();
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
        drawList.addRectFilled(
                trackStart,
                trackTop,
                trackEnd,
                trackBottom,
                ImGui.getColorU32(hovered ? ImGuiCol.FrameBgHovered : ImGuiCol.FrameBg),
                trackHeight * 0.5f);
        if (thumbX > trackStart) {
            drawList.addRectFilled(
                    trackStart,
                    trackTop,
                    thumbX,
                    trackBottom,
                    ImGui.getColorU32(active ? ImGuiCol.SliderGrabActive : ImGuiCol.SliderGrab),
                    trackHeight * 0.5f);
        }

        int thumbColor =
                ImGui.getColorU32(
                        !enabled
                                ? ImGuiCol.TextDisabled
                                : active || hovered
                                        ? ImGuiCol.SliderGrabActive
                                        : ImGuiCol.SliderGrab);
        int thumbBorder = ImGui.getColorU32(focused ? ImGuiCol.NavHighlight : ImGuiCol.Border);
        drawList.addCircleFilled(thumbX, centerY, thumbRadius, thumbColor, 20);
        drawList.addCircle(
                thumbX,
                centerY,
                thumbRadius,
                thumbBorder,
                20,
                focused ? tokens.emphasizedBorderWidth() : tokens.borderWidth());
    }

    static float normalize(float value, float min, float max) {
        if (max <= min) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, (value - min) / (max - min)));
    }
}
