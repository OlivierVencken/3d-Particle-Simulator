package com.particle.sim.particles;

public final class SpatialGridSizing {
    private SpatialGridSizing() {
    }

    public static int gridSize(float bounds, float interactionRange) {
        return Math.max(1, (int) Math.ceil((bounds * 2.0f) / interactionRange));
    }

    public static int gridCellCount(float bounds, float interactionRange) {
        long size = gridSize(bounds, interactionRange);
        long cellCount = size * size * size;
        if (cellCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Spatial grid exceeds supported integer indexing");
        }
        return (int) cellCount;
    }
}
