package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UIIntegerInputTest {
    @Test
    void clampsTypedAndSteppedValues() {
        assertEquals(1, UIIntegerInput.clamp(-20, 1, 16));
        assertEquals(8, UIIntegerInput.clamp(8, 1, 16));
        assertEquals(16, UIIntegerInput.clamp(50, 1, 16));
    }

    @Test
    void invalidRangeFallsBackToMinimum() {
        assertEquals(10, UIIntegerInput.clamp(12, 10, 5));
    }
}
