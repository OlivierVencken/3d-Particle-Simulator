package com.particle.sim.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImGuiLayerTest {
    @Test
    void onlySchedulesAtlasRebuildForMaterialScaleChanges() {
        assertFalse(ImGuiLayer.materiallyDifferent(1.25f, 1.27f));
        assertTrue(ImGuiLayer.materiallyDifferent(1.25f, 1.5f));
    }
}
