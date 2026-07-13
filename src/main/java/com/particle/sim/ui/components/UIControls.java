package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIFonts;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

public final class UIControls {
    private UIControls() {
    }

    public static void settingSlider(String label, float value, float min, float max, int decimals, FloatSetter setter,
            Runnable settingsChanged) {
        if (slider(label, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
    }

    public static void settingSlider(String label, String id, float value, float min, float max, int decimals,
            FloatSetter setter, Runnable settingsChanged) {
        settingLabel(label, ("%." + decimals + "f").formatted(value));
        ImGui.setNextItemWidth(-1.0f);
        if (slider("##" + id, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
    }

    public static void settingIntSlider(String label, int value, int min, int max, int decimals, IntSetter setter,
            Runnable settingsChanged) {
        if (slider(label, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
    }

    public static void settingIntSlider(String label, String id, int value, int min, int max, int decimals,
            IntSetter setter, Runnable settingsChanged) {
        settingLabel(label, Integer.toString(value));
        ImGui.setNextItemWidth(-1.0f);
        if (slider("##" + id, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
    }

    public static void settingCheckbox(String label, String id, boolean value, BooleanSetter setter,
            Runnable settingsChanged) {
        ImBoolean valueRef = new ImBoolean(value);
        if (ImGui.checkbox(label + "##" + id, valueRef)) {
            setter.set(valueRef.get());
            settingsChanged.run();
        }
    }

    public static void settingCombo(String label, String id, int value, String[] values, IntSetter setter,
            Runnable settingsChanged) {
        ImGui.textDisabled(label);
        ImInt valueRef = new ImInt(value);
        ImGui.setNextItemWidth(-1.0f);
        if (ImGui.combo("##" + id, valueRef, values)) {
            setter.set(valueRef.get());
            settingsChanged.run();
        }
    }

    public static void sectionHeading(String label) {
        ImGui.pushFont(UIFonts.section());
        ImGui.text(label);
        ImGui.popFont();
    }

    public static String[] enumLabels(Enum<?>[] values) {
        String[] labels = new String[values.length];
        for (int index = 0; index < values.length; index++) {
            String raw = values[index].name().toLowerCase().replace('_', ' ');
            labels[index] = Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
        }
        return labels;
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
    public interface FloatSetter {
        void set(float value);
    }

    @FunctionalInterface
    public interface IntSetter {
        void set(int value);
    }

    @FunctionalInterface
    public interface BooleanSetter {
        void set(boolean value);
    }
}
