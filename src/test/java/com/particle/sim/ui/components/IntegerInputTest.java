package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IntegerInputTest {
    @Test
    void clampsTypedAndSteppedValues() {
        assertEquals(1, IntegerInput.clamp(-20, 1, 16));
        assertEquals(8, IntegerInput.clamp(8, 1, 16));
        assertEquals(16, IntegerInput.clamp(50, 1, 16));
    }

    @Test
    void invalidRangeFallsBackToMinimum() {
        assertEquals(10, IntegerInput.clamp(12, 10, 5));
    }
}
