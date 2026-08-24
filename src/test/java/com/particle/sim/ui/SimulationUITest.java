package com.particle.sim.ui;

import org.junit.jupiter.api.Test;

import com.particle.sim.ui.testing.FakeSimulationUiModel;
import com.particle.sim.ui.testing.RecordingSimulationUiActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationUITest {
    @Test
    void acceptsNativeFreeModelAndActionFixtures() {
        SimulationUI ui = new SimulationUI();
        FakeSimulationUiModel model = new FakeSimulationUiModel();
        RecordingSimulationUiActions actions = new RecordingSimulationUiActions();

        ui.connect(model, actions);
        actions.application().setFpsCap(model.application().fpsCap());

        assertEquals(java.util.List.of("application.fpsCap"), actions.calls);
    }

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
