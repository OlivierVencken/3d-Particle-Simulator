package com.particle.sim.ui.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UITooltipTest {
    @Test
    void ignoresMissingTooltipContent() {
        assertFalse(UITooltip.hasContent(null));
        assertFalse(UITooltip.hasContent("  "));
        assertTrue(UITooltip.hasContent("Matrix value"));
    }
}
