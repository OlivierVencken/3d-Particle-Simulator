package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UIMetricTest {
    @Test
    void secondaryCardUsesRemainingWidthWithoutShrinkingBelowMinimum() {
        assertEquals(120.0f, UIMetric.secondaryWidth(400.0f, 272.0f, 8.0f, 88.0f));
        assertEquals(88.0f, UIMetric.secondaryWidth(300.0f, 250.0f, 8.0f, 88.0f));
    }
}
