package com.particle.sim.ui;

import imgui.ImGui;

final class UiControls {
    private UiControls() {
    }

    static void settingSlider(String label, float value, float min, float max, FloatSetter setter,
            Runnable settingsChanged) {
        if (slider(label, value, min, max, setter)) {
            settingsChanged.run();
        }
    }

    static void settingIntSlider(String label, int value, int min, int max, IntSetter setter, Runnable settingsChanged) {
        if (slider(label, value, min, max, setter)) {
            settingsChanged.run();
        }
    }

    private static boolean slider(String label, float value, float min, float max, FloatSetter setter) {
        float[] valueRef = { value };
        if (ImGui.sliderFloat(label, valueRef, min, max)) {
            setter.set(valueRef[0]);
            return true;
        }
        return false;
    }

    private static boolean slider(String label, int value, int min, int max, IntSetter setter) {
        int[] valueRef = { value };
        if (ImGui.sliderInt(label, valueRef, min, max)) {
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
