package com.particle.sim.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationUiTest {
    @Test
    void uiCanBeHiddenAndShown() {
        SimulationUi ui = new SimulationUi();

        ui.hide();

        assertTrue(ui.isHidden());

        ui.show();

        assertFalse(ui.isHidden());
    }
}
