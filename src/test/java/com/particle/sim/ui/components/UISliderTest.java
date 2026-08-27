package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import imgui.flag.ImGuiSliderFlags;
import org.junit.jupiter.api.Test;

class UISliderTest {
    private static final float EPSILON = 0.0001f;

    @Test
    void normalizationClampsAndHandlesEmptyRanges() {
        assertEquals(0.0f, UISlider.normalize(-1.0f, 0.0f, 10.0f), EPSILON);
        assertEquals(0.5f, UISlider.normalize(5.0f, 0.0f, 10.0f), EPSILON);
        assertEquals(1.0f, UISlider.normalize(11.0f, 0.0f, 10.0f), EPSILON);
        assertEquals(0.0f, UISlider.normalize(5.0f, 10.0f, 10.0f), EPSILON);
    }

    @Test
    void numericEntryPolicyIsExplicit() {
        assertEquals(ImGuiSliderFlags.NoInput, UISlider.NumericEntryPolicy.KEYBOARD_ADJUSTMENT_ONLY.flags());
        assertEquals(ImGuiSliderFlags.None, UISlider.NumericEntryPolicy.DIRECT_ENTRY.flags());
    }
}
