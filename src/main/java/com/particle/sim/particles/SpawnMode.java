package com.particle.sim.particles;

public enum SpawnMode {
    RANDOM,
    SPHERICAL,
    GRID,
    SHELL,
    SPIRAL,
    DISC,
    CLUSTERS,
    POINT;

    public boolean supportedIn(SimulationDimension dimension) {
        return dimension != SimulationDimension.FOUR_D || (this != DISC && this != SPIRAL);
    }
}
