package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.particle.sim.ui.theme.UIDesignTokens;
import org.junit.jupiter.api.Test;

class UIComboTest {
    @Test
    void validatesSelectedIndex() {
        String[] values = {"One", "Two"};
        assertFalse(UICombo.isValidIndex(-1, values));
        assertTrue(UICombo.isValidIndex(0, values));
        assertTrue(UICombo.isValidIndex(1, values));
        assertFalse(UICombo.isValidIndex(2, values));
    }

    @Test
    void previewWidthReservesInsetsAndChevron() {
        assertEquals(62.0f, UICombo.previewTextWidth(100.0f, UIDesignTokens.unscaled()), 0.0001f);
        assertEquals(0.0f, UICombo.previewTextWidth(20.0f, UIDesignTokens.unscaled()), 0.0001f);
    }
}
