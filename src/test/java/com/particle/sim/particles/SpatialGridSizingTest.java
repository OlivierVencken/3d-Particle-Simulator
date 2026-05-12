package com.particle.sim.particles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpatialGridSizingTest {
    @Test
    void gridSizeRoundsUpWorldDiameterByInteractionRange() {
        assertEquals(9, SpatialGridSizing.gridSize(4.0f, 0.95f));
        assertEquals(2, SpatialGridSizing.gridSize(4.0f, 4.0f));
    }

    @Test
    void spatialMapSizeScalesWithLikelyOccupiedGridCells() {
        assertTrue(SpatialGridSizing.spatialMapSize(65_536, 4.0f, 0.95f) < 2_000);
    }

    @Test
    void spatialMapSizeStaysCappedForWorstCaseSettings() {
        assertEquals(SpatialGridSizing.MAX_SPATIAL_MAP_SIZE,
                SpatialGridSizing.spatialMapSize(1_000_000, 10.0f, 0.2f));
    }
}
