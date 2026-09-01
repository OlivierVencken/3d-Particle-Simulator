package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.particle.sim.ui.theme.DesignTokens;
import org.junit.jupiter.api.Test;

class ComboTest {
    @Test
    void validatesSelectedIndex() {
        String[] values = {"One", "Two"};
        assertFalse(Combo.isValidIndex(-1, values));
        assertTrue(Combo.isValidIndex(0, values));
        assertTrue(Combo.isValidIndex(1, values));
        assertFalse(Combo.isValidIndex(2, values));
    }

    @Test
    void previewWidthReservesInsetsAndChevron() {
        assertEquals(65.8f, Combo.previewTextWidth(100.0f, DesignTokens.unscaled()), 0.0001f);
        assertEquals(0.0f, Combo.previewTextWidth(20.0f, DesignTokens.unscaled()), 0.0001f);
    }
}
