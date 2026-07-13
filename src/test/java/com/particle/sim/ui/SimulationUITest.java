package com.particle.sim.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationUITest {
    @Test
    void uiCanBeHiddenAndShown() {
        SimulationUI ui = new SimulationUI();

        ui.hide();

        assertTrue(ui.isHidden());

        ui.show();

        assertFalse(ui.isHidden());
    }

    @Test
    void uiCanBeToggledInBothDirections() {
        SimulationUI ui = new SimulationUI();

        ui.toggleUi();
        assertTrue(ui.isHidden());

        ui.toggleUi();
        assertFalse(ui.isHidden());
    }
}
