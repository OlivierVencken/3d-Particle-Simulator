package com.particle.sim.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.particle.sim.ui.testing.FakeSimulationViewModel;
import com.particle.sim.ui.testing.RecordingSimulationViewActions;
import org.junit.jupiter.api.Test;

class SimulationViewTest {
    @Test
    void acceptsNativeFreeModelAndActionFixtures() {
        SimulationView ui = new SimulationView();
        FakeSimulationViewModel model = new FakeSimulationViewModel();
        RecordingSimulationViewActions actions = new RecordingSimulationViewActions();

        ui.connect(model, actions);
        actions.application().setFpsCap(model.application().fpsCap());

        assertEquals(java.util.List.of("application.fpsCap"), actions.calls);
    }

    @Test
    void uiCanBeHiddenAndShown() {
        SimulationView ui = new SimulationView();

        ui.hide();

        assertTrue(ui.isHidden());

        ui.show();

        assertFalse(ui.isHidden());
    }

    @Test
    void uiCanBeToggledInBothDirections() {
        SimulationView ui = new SimulationView();

        ui.toggleUi();
        assertTrue(ui.isHidden());

        ui.toggleUi();
        assertFalse(ui.isHidden());
    }

    @Test
    void animationPreferenceCanBeChangedWithoutRenderingAFrame() {
        SimulationView ui = new SimulationView();

        ui.setAnimationsEnabled(false);
        ui.setAnimationsEnabled(true);
    }
}
