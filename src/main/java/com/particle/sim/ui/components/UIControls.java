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
        if (slider(label, label, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
    }

    public static void settingSlider(String label, String id, float value, float min, float max, int decimals,
            FloatSetter setter, Runnable settingsChanged) {
        if (slider(label, id, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
    }

    public static void settingIntSlider(String label, int value, int min, int max, int decimals, IntSetter setter,
            Runnable settingsChanged) {
        if (slider(label, label, value, min, max, setter)) {
            settingsChanged.run();
        }
    }

    public static void settingIntSlider(String label, String id, int value, int min, int max, int decimals,
            IntSetter setter, Runnable settingsChanged) {
        if (slider(label, id, value, min, max, setter)) {
            settingsChanged.run();
        }
    }

    public static void settingCheckbox(String label, String id, boolean value, BooleanSetter setter,
            Runnable settingsChanged) {
        ImBoolean valueRef = new ImBoolean(value);
        if (checkbox(label, id, valueRef)) {
            setter.set(valueRef.get());
            settingsChanged.run();
        }
    }

    public static boolean checkbox(String label, String id, ImBoolean valueRef) {
        return UICheckbox.render(label, id, valueRef);
    }

    public static void settingCombo(String label, String id, int value, String[] values, IntSetter setter,
            Runnable settingsChanged) {
        ImInt valueRef = new ImInt(value);
        if (UICombo.render(label, id, valueRef, values)) {
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

    private static boolean slider(String label, String id, float value, float min, float max, int decimals,
            FloatSetter setter) {
        float[] valueRef = { value };
        if (UISlider.render(label, id, valueRef, min, max, decimals)) {
            setter.set(valueRef[0]);
            return true;
        }
        return false;
    }

    private static boolean slider(String label, String id, int value, int min, int max, IntSetter setter) {
        int[] valueRef = { value };
        if (UISlider.render(label, id, valueRef, min, max)) {
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
