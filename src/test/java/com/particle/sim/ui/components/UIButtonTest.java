package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UIButtonTest {
    @Test
    void visibleTextAndStableIdentityAreSeparated() {
        assertEquals("Save###button-save-preset",
                UIButton.itemLabel("Save", "button-save-preset"));
        assertEquals("Save as###button-save-preset",
                UIButton.itemLabel("Save as", "button-save-preset"));
    }
}
