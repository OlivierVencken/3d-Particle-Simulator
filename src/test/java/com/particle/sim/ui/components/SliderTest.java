package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import imgui.flag.ImGuiSliderFlags;
import org.junit.jupiter.api.Test;

class SliderTest {
    private static final float EPSILON = 0.0001f;

    @Test
    void normalizationClampsAndHandlesEmptyRanges() {
        assertEquals(0.0f, Slider.normalize(-1.0f, 0.0f, 10.0f), EPSILON);
        assertEquals(0.5f, Slider.normalize(5.0f, 0.0f, 10.0f), EPSILON);
        assertEquals(1.0f, Slider.normalize(11.0f, 0.0f, 10.0f), EPSILON);
        assertEquals(0.0f, Slider.normalize(5.0f, 10.0f, 10.0f), EPSILON);
    }

    @Test
    void numericEntryPolicyIsExplicit() {
        assertEquals(ImGuiSliderFlags.NoInput, Slider.NumericEntryPolicy.KEYBOARD_ADJUSTMENT_ONLY.flags());
        assertEquals(ImGuiSliderFlags.None, Slider.NumericEntryPolicy.DIRECT_ENTRY.flags());
    }
}
