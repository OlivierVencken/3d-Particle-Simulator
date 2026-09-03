package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.Fonts;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiColorEditFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;

public final class Controls {
    private final ImBoolean booleanRef = new ImBoolean();
    private final ImInt integerRef = new ImInt();
    private final float[] floatRef = new float[1];
    private final int[] intRef = new int[1];
    private final float[] colorRef = new float[3];

    public void settingSlider(
            String label, float value, float min, float max, int decimals, FloatSetter setter) {
        slider(label, label, value, min, max, decimals, setter);
    }

    public void settingSlider(
            String label,
            String id,
            float value,
            float min,
            float max,
            int decimals,
            FloatSetter setter) {
        slider(label, id, value, min, max, decimals, setter);
    }

    public void settingIntSlider(String label, int value, int min, int max, IntSetter setter) {
        slider(label, label, value, min, max, setter);
    }

    public void settingIntSlider(
            String label, String id, int value, int min, int max, IntSetter setter) {
        slider(label, id, value, min, max, setter);
    }

    public void settingCheckbox(String label, String id, boolean value, BooleanSetter setter) {
        booleanRef.set(value);
        if (checkbox(label, id, booleanRef)) {
            setter.set(booleanRef.get());
        }
    }

    public static boolean checkbox(String label, String id, ImBoolean valueRef) {
        return Checkbox.render(label, id, valueRef);
    }

    public void settingCombo(
            String label, String id, int value, String[] values, IntSetter setter) {
        integerRef.set(value);
        if (Combo.render(label, id, integerRef, values)) {
            setter.set(integerRef.get());
        }
    }

    public void settingColor(String label, String id, ImVec4 value, ColorSetter setter) {
        colorRef[0] = value.x;
        colorRef[1] = value.y;
        colorRef[2] = value.z;

        ImGui.pushFont(Fonts.medium());
        try {
            ImGui.textUnformatted(label);
            ImGui.setNextItemWidth(-1.0f);
            int flags =
                    ImGuiColorEditFlags.NoAlpha
                            | ImGuiColorEditFlags.DisplayHex
                            | ImGuiColorEditFlags.Uint8
                            | ImGuiColorEditFlags.InputRGB;
            if (ImGui.colorEdit3("###color-" + id, colorRef, flags)) {
                setter.set(new ImVec4(colorRef[0], colorRef[1], colorRef[2], 1.0f));
            }
        } finally {
            ImGui.popFont();
        }
    }

    public static void sectionHeading(String label) {
        Text.sectionHeading(label);
    }

    public static String[] enumLabels(Enum<?>[] values) {
        String[] labels = new String[values.length];
        for (int index = 0; index < values.length; index++) {
            String raw = values[index].name().toLowerCase().replace('_', ' ');
            labels[index] = Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
        }
        return labels;
    }

    private boolean slider(
            String label,
            String id,
            float value,
            float min,
            float max,
            int decimals,
            FloatSetter setter) {
        floatRef[0] = value;
        if (Slider.render(label, id, floatRef, min, max, decimals)) {
            setter.set(floatRef[0]);
            return true;
        }
        return false;
    }

    private boolean slider(String label, String id, int value, int min, int max, IntSetter setter) {
        intRef[0] = value;
        if (Slider.render(label, id, intRef, min, max)) {
            setter.set(intRef[0]);
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

    @FunctionalInterface
    public interface ColorSetter {
        void set(ImVec4 value);
    }
}
