package com.particle.sim.math;

public enum RotationPlane4d {
    XY(0, 1),
    XZ(0, 2),
    YZ(1, 2),
    XW(0, 3),
    YW(1, 3),
    ZW(2, 3);

    private final int firstAxis;
    private final int secondAxis;

    RotationPlane4d(int firstAxis, int secondAxis) {
        this.firstAxis = firstAxis;
        this.secondAxis = secondAxis;
    }

    public int firstAxis() {
        return firstAxis;
    }

    public int secondAxis() {
        return secondAxis;
    }
}
