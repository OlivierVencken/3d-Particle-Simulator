package com.particle.sim.particles;

public enum SimulationDimension {
    THREE_D(3),
    FOUR_D(4);

    public static final SimulationDimension DEFAULT = THREE_D;

    private final int componentCount;

    SimulationDimension(int componentCount) {
        this.componentCount = componentCount;
    }

    public int componentCount() {
        return componentCount;
    }
}
