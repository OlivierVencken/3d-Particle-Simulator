package com.particle.sim.ui.commandbar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorPopupTest {
    @Test
    void blankMessagesUseReadableFallbacks() {
        assertEquals("fallback", ErrorPopup.normalized("  ", "fallback"));
        assertEquals("message", ErrorPopup.normalized(" message ", "fallback"));
    }
}
