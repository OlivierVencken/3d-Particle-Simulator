package com.particle.sim.ui.theme;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIDesignTokensTest {
    @ParameterizedTest
    @CsvSource({
            "1.0, 336, 32, 12.8",
            "1.25, 420, 40, 16",
            "1.5, 504, 48, 19.2",
            "2.0, 672, 64, 25.6"
    })
    void scalesSemanticMeasurementsFromTheCleanBase(
            float scale, float sidebarWidth, float commandBarHeight, float bodyFontSize) {
        UIDesignTokens tokens = UIDesignTokens.atScale(scale);

        assertEquals(sidebarWidth, tokens.sidebarWidth());
        assertEquals(commandBarHeight, tokens.commandBarHeight());
        assertEquals(bodyFontSize, tokens.bodyFontSize());
        assertTrue(tokens.controlHeight() >= tokens.minimumHitTarget());
    }

    @Test
    void invalidScaleFallsBackAndExtremeScaleIsBounded() {
        assertEquals(1.0f, UIDesignTokens.atScale(Float.NaN).scale());
        assertEquals(UIDesignTokens.MAXIMUM_SCALE, UIDesignTokens.atScale(99.0f).scale());
        assertEquals(UIDesignTokens.MINIMUM_SCALE, UIDesignTokens.atScale(0.1f).scale());
    }
}
