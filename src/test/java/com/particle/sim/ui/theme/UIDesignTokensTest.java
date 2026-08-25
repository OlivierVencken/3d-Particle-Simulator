package com.particle.sim.ui.theme;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIDesignTokensTest {
    @ParameterizedTest
    @CsvSource({
            "1.0, 378, 36, 14.4",
            "1.25, 472.5, 45, 18",
            "1.5, 567, 54, 21.6",
            "2.0, 756, 72, 28.8"
    })
    void scalesSemanticMeasurementsFromTheCleanBase(
            float scale, float sidebarWidth, float commandBarHeight, float bodyFontSize) {
        UIDesignTokens tokens = UIDesignTokens.atScale(scale);

        assertEquals(sidebarWidth, tokens.sidebarWidth(), 0.0001f);
        assertEquals(commandBarHeight, tokens.commandBarHeight(), 0.0001f);
        assertEquals(bodyFontSize, tokens.bodyFontSize(), 0.0001f);
        assertTrue(tokens.controlHeight() >= tokens.minimumHitTarget());
    }

    @Test
    void invalidScaleFallsBackAndExtremeScaleIsBounded() {
        assertEquals(1.0f, UIDesignTokens.atScale(Float.NaN).scale());
        assertEquals(UIDesignTokens.MAXIMUM_SCALE, UIDesignTokens.atScale(99.0f).scale());
        assertEquals(UIDesignTokens.MINIMUM_SCALE, UIDesignTokens.atScale(0.1f).scale());
    }
}
