package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ButtonTest {
    @Test
    void visibleTextAndStableIdentityAreSeparated() {
        assertEquals("Save###button-save-preset", Button.itemLabel("Save", "button-save-preset"));
        assertEquals(
                "Save as###button-save-preset", Button.itemLabel("Save as", "button-save-preset"));
    }
}
