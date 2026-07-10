package com.particle.sim.ui.workspace;

import com.particle.sim.ui.theme.UIFonts;

import imgui.ImGui;

final class UIControls {
    private UIControls() {
    }

    static void settingSlider(String label, float value, float min, float max, int decimals, FloatSetter setter,
            Runnable settingsChanged) {
        if (slider(label, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
    }

    static void settingSlider(String label, String id, float value, float min, float max, int decimals,
            FloatSetter setter, Runnable settingsChanged) {
        settingLabel(label, ("%." + decimals + "f").formatted(value));
        ImGui.setNextItemWidth(-1.0f);
        if (slider("##" + id, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
    }

    static void settingIntSlider(String label, int value, int min, int max, int decimals, IntSetter setter, Runnable settingsChanged) {
        if (slider(label, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
    }

    static void settingIntSlider(String label, String id, int value, int min, int max, int decimals,
            IntSetter setter, Runnable settingsChanged) {
        settingLabel(label, Integer.toString(value));
        ImGui.setNextItemWidth(-1.0f);
        if (slider("##" + id, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
    }

    private static void settingLabel(String label, String value) {
        ImGui.pushFont(UIFonts.medium());
        ImGui.textUnformatted(label);
        ImGui.popFont();
        float right = ImGui.getWindowContentRegionMaxX();
        float valueWidth = ImGui.calcTextSize(value).x;
        ImGui.sameLine(Math.max(ImGui.getCursorPosX() + 8.0f, right - valueWidth));
        ImGui.textDisabled(value);
    }

    private static boolean slider(String label, float value, float min, float max, int decimals, FloatSetter setter) {
        float[] valueRef = { value };
        String format = "%." + decimals + "f";
        if (ImGui.sliderFloat(label, valueRef, min, max, format)) {
            setter.set(valueRef[0]);
            return true;
        }
        return false;
    }

    private static boolean slider(String label, int value, int min, int max, int decimals, IntSetter setter) {
        int[] valueRef = { value };
        String format = "%." + decimals + "d";
        if (ImGui.sliderInt(label, valueRef, min, max, format)) {
            setter.set(valueRef[0]);
            return true;
        }
        return false;
    }

    @FunctionalInterface
    interface FloatSetter {
        void set(float value);
    }

    @FunctionalInterface
    interface IntSetter {
        void set(int value);
    }
}
