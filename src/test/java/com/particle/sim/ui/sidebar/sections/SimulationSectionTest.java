package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.ui.theme.UIDesignTokens;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationSectionTest {
    @Test
    void playbackActionsStackWhenTwoUsableButtonsCannotFit() {
        UIDesignTokens tokens = UIDesignTokens.unscaled();
        float threshold = tokens.pairedControlMinimumWidth() * 2.0f + tokens.spaceMd();

        assertFalse(SimulationSection.playbackControlsFitInline(threshold - 0.01f, tokens));
        assertTrue(SimulationSection.playbackControlsFitInline(threshold, tokens));
    }
}
