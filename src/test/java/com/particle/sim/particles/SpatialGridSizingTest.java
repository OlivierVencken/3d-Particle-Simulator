package com.particle.sim.particles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpatialGridSizingTest {
    @Test
    void gridSizeRoundsUpWorldDiameterByInteractionRange() {
        assertEquals(9, SpatialGridSizing.gridSize(4.0f, 0.95f));
        assertEquals(2, SpatialGridSizing.gridSize(4.0f, 4.0f));
    }

    @Test
    void gridCellCountMatchesCubedGridSize() {
        assertEquals(729, SpatialGridSizing.gridCellCount(4.0f, 0.95f));
    }

    @Test
    void gridCellCountSupportsWorstCaseSettings() {
        assertEquals(1_000_000, SpatialGridSizing.gridCellCount(10.0f, 0.2f));
    }
}
