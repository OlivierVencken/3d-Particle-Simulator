package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.SimulationDimension;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraSectionTest {
    @Test
    void fourDControlsAreOnlyRelevantInFourDMode() {
        assertFalse(CameraSection.showsFourDControls(SimulationDimension.THREE_D));
        assertTrue(CameraSection.showsFourDControls(SimulationDimension.FOUR_D));
    }
}
