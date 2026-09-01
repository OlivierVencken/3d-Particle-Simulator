package com.particle.sim.ui.commandbar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ErrorPopupTest {
    @Test
    void blankMessagesUseReadableFallbacks() {
        assertEquals("fallback", ErrorPopup.normalized("  ", "fallback"));
        assertEquals("message", ErrorPopup.normalized(" message ", "fallback"));
    }
}
