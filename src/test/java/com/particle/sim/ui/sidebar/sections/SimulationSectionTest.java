package com.particle.sim.ui.sidebar.sections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.particle.sim.ui.theme.DesignTokens;
import org.junit.jupiter.api.Test;

class SimulationSectionTest {
    @Test
    void playbackActionsStackWhenTwoUsableButtonsCannotFit() {
        DesignTokens tokens = DesignTokens.unscaled();
        float threshold = tokens.pairedControlMinimumWidth() * 2.0f + tokens.spaceMd();

        assertFalse(SimulationSection.playbackControlsFitInline(threshold - 0.01f, tokens));
        assertTrue(SimulationSection.playbackControlsFitInline(threshold, tokens));
    }
}
