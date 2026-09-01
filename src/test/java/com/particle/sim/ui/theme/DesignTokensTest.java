package com.particle.sim.ui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DesignTokensTest {
    @ParameterizedTest
    @CsvSource({
        "1.0, 378, 36, 14.4",
        "1.25, 472.5, 45, 18",
        "1.5, 567, 54, 21.6",
        "2.0, 756, 72, 28.8"
    })
    void scalesSemanticMeasurementsFromTheCleanBase(
            float scale, float sidebarWidth, float commandBarHeight, float bodyFontSize) {
        DesignTokens tokens = DesignTokens.atScale(scale);

        assertEquals(sidebarWidth, tokens.sidebarWidth(), 0.0001f);
        assertEquals(commandBarHeight, tokens.commandBarHeight(), 0.0001f);
        assertEquals(bodyFontSize, tokens.bodyFontSize(), 0.0001f);
        assertTrue(tokens.controlHeight() >= tokens.minimumHitTarget());
    }

    @Test
    void invalidScaleFallsBackAndExtremeScaleIsBounded() {
        assertEquals(1.0f, DesignTokens.atScale(Float.NaN).scale());
        assertEquals(DesignTokens.MAXIMUM_SCALE, DesignTokens.atScale(99.0f).scale());
        assertEquals(DesignTokens.MINIMUM_SCALE, DesignTokens.atScale(0.1f).scale());
    }
}
