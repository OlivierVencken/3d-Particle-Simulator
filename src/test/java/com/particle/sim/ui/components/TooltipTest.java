package com.particle.sim.ui.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TooltipTest {
    @Test
    void ignoresMissingTooltipContent() {
        assertFalse(Tooltip.hasContent(null));
        assertFalse(Tooltip.hasContent("  "));
        assertTrue(Tooltip.hasContent("Matrix value"));
    }
}
