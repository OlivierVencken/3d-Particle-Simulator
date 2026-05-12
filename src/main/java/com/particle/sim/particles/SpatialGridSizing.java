package com.particle.sim.particles;

import com.particle.sim.math.Math3d;

public final class SpatialGridSizing {
    public static final int MAX_SPATIAL_MAP_SIZE = 524287;
    private static final int MIN_SPATIAL_MAP_SIZE = 1021;
    private static final float SPATIAL_GRID_MAX_LOAD = 0.5f;

    private SpatialGridSizing() {
    }

    public static int gridSize(float bounds, float interactionRange) {
        return Math.max(1, (int) Math.ceil((bounds * 2.0f) / interactionRange));
    }

    public static int spatialMapSize(int particleCount, float bounds, float interactionRange) {
        long gridCellCount = gridCellCount(bounds, interactionRange);
        long occupiedCellLimit = Math.max(1L, Math.min((long) Math.max(particleCount, 1), gridCellCount));
        long targetBuckets = (long) Math.ceil(occupiedCellLimit / SPATIAL_GRID_MAX_LOAD);
        int clampedBuckets = (int) Math.max(MIN_SPATIAL_MAP_SIZE, Math.min(MAX_SPATIAL_MAP_SIZE, targetBuckets));
        return Math3d.previousPrime(clampedBuckets);
    }

    private static long gridCellCount(float bounds, float interactionRange) {
        long size = gridSize(bounds, interactionRange);
        return size * size * size;
    }
}
