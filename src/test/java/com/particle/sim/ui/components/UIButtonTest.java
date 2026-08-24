package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UIButtonTest {
    @Test
    void visibleTextAndStableIdentityAreSeparated() {
        assertEquals("Pause###button-simulation-pause",
                UIButton.itemLabel("Pause", "button-simulation-pause"));
        assertEquals("Resume###button-simulation-pause",
                UIButton.itemLabel("Resume", "button-simulation-pause"));
    }
}
