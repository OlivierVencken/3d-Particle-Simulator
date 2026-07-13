package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIColors;
import com.particle.sim.ui.theme.UIFonts;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiSliderFlags;
import imgui.flag.ImGuiStyleVar;

public final class UISlider {
    private static final float TRACK_HEIGHT = 6.0f;
    private static final float THUMB_RADIUS = 7.0f;
    private static final float NATIVE_GRAB_PADDING = 2.0f;

    private UISlider() {
    }

    public static boolean render(String label, String id, float[] valueRef, float min, float max, int decimals) {
        ImGui.pushFont(UIFonts.medium());
        drawLabel(label, ("%." + decimals + "f").formatted(valueRef[0]));

        ImGui.setNextItemWidth(-1.0f);
        hideNativeSlider();
        boolean changed = ImGui.sliderFloat("##slider-" + id, valueRef, min, max, "", ImGuiSliderFlags.NoInput);
        restoreNativeSlider();

        drawTrack(normalize(valueRef[0], min, max));
        ImGui.popFont();
        return changed;
    }

    public static boolean render(String label, String id, int[] valueRef, int min, int max) {
        ImGui.pushFont(UIFonts.medium());
        drawLabel(label, Integer.toString(valueRef[0]));

        ImGui.setNextItemWidth(-1.0f);
        hideNativeSlider();
        boolean changed = ImGui.sliderInt("##slider-" + id, valueRef, min, max, "", ImGuiSliderFlags.NoInput);
        restoreNativeSlider();

        drawTrack(normalize(valueRef[0], min, max));
        ImGui.popFont();
        return changed;
    }

    private static void drawLabel(String label, String value) {
        ImGui.textUnformatted(label);
        float right = ImGui.getWindowContentRegionMaxX();
        float valueWidth = ImGui.calcTextSize(value).x;
        ImGui.sameLine(Math.max(ImGui.getCursorPosX() + 8.0f, right - valueWidth));
        ImGui.textDisabled(value);
    }

    private static void hideNativeSlider() {
        ImGui.pushStyleColor(ImGuiCol.FrameBg, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.SliderGrab, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.SliderGrabActive, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.Border, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.NavHighlight, UIColors.TRANSPARENT.vec4());
        ImGui.pushStyleVar(ImGuiStyleVar.GrabMinSize, THUMB_RADIUS * 2.0f);
    }

    private static void restoreNativeSlider() {
        ImGui.popStyleVar();
        ImGui.popStyleColor(7);
    }

    private static void drawTrack(float normalizedValue) {
        boolean hovered = ImGui.isItemHovered();
        boolean active = ImGui.isItemActive();
        boolean focused = ImGui.isItemFocused();
        ImVec2 min = ImGui.getItemRectMin();
        ImVec2 max = ImGui.getItemRectMax();

        float trackStart = min.x + THUMB_RADIUS + NATIVE_GRAB_PADDING;
        float trackEnd = max.x - THUMB_RADIUS - NATIVE_GRAB_PADDING;
        float centerY = (min.y + max.y) * 0.5f;
        float thumbX = trackStart + (trackEnd - trackStart) * normalizedValue;
        float trackTop = centerY - TRACK_HEIGHT * 0.5f;
        float trackBottom = centerY + TRACK_HEIGHT * 0.5f;

        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(trackStart, trackTop, trackEnd, trackBottom,
                ImGui.getColorU32(hovered ? ImGuiCol.FrameBgHovered : ImGuiCol.FrameBg), TRACK_HEIGHT * 0.5f);
        if (thumbX > trackStart) {
            drawList.addRectFilled(trackStart, trackTop, thumbX, trackBottom,
                    ImGui.getColorU32(active ? ImGuiCol.SliderGrabActive : ImGuiCol.SliderGrab),
                    TRACK_HEIGHT * 0.5f);
        }

        int thumbColor = ImGui.getColorU32(active || hovered ? ImGuiCol.SliderGrabActive : ImGuiCol.SliderGrab);
        int thumbBorder = ImGui.getColorU32(focused ? ImGuiCol.NavHighlight : ImGuiCol.Border);
        drawList.addCircleFilled(thumbX, centerY, THUMB_RADIUS, thumbColor, 20);
        drawList.addCircle(thumbX, centerY, THUMB_RADIUS, thumbBorder, 20, focused ? 2.0f : 1.0f);
    }

    static float normalize(float value, float min, float max) {
        if (max <= min) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, (value - min) / (max - min)));
    }
}
