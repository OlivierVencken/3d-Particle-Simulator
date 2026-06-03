package com.particle.sim.ui;

import imgui.ImGui;

final class UiControls {
    private UiControls() {
    }

    static void settingSlider(String label, float value, float min, float max, int decimals, FloatSetter setter,
            Runnable settingsChanged) {
        if (slider(label, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
    }

    static void settingIntSlider(String label, int value, int min, int max, int decimals, IntSetter setter, Runnable settingsChanged) {
        if (slider(label, value, min, max, decimals, setter)) {
            settingsChanged.run();
        }
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
