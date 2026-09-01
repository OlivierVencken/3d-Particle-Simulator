package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.Fonts;
import imgui.ImGui;
import imgui.type.ImInt;

/** Labelled integer entry with native stepping, keyboard entry, and clamping. */
public final class IntegerInput {
    private IntegerInput() {}

    public static boolean render(
            String label,
            String id,
            ImInt value,
            int step,
            int fastStep,
            int minimum,
            int maximum,
            float width) {
        return render(label, id, value, step, fastStep, minimum, maximum, width, true);
    }

    public static boolean render(
            String label,
            String id,
            ImInt value,
            int step,
            int fastStep,
            int minimum,
            int maximum,
            float width,
            boolean enabled) {
        ImGui.pushFont(Fonts.medium());
        try {
            if (label != null && !label.isBlank()) {
                ImGui.textDisabled(label);
            }
            ImGui.setNextItemWidth(width);
            ImGui.beginDisabled(!enabled);
            boolean changed;
            try {
                changed =
                        ImGui.inputInt(
                                Button.itemLabel("", "integer-input-" + id), value, step, fastStep);
            } finally {
                ImGui.endDisabled();
            }
            if (changed) {
                value.set(clamp(value.get(), minimum, maximum));
            }
            return enabled && changed;
        } finally {
            ImGui.popFont();
        }
    }

    static int clamp(int value, int minimum, int maximum) {
        if (maximum < minimum) {
            return minimum;
        }
        return Math.max(minimum, Math.min(maximum, value));
    }
}
