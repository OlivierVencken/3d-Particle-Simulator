package com.particle.sim.particles;

/**
 * Complete persistent state for the view-only part of the 4D experience.
 * The rotation matrix is authoritative; the three angles are retained as UI
 * orientation readouts because incremental 4D rotations are order-dependent.
 */
public record FourDViewState(
        FourDViewConfiguration configuration,
        double xwAngle,
        double ywAngle,
        double zwAngle,
        double xwAutoSpeed,
        double ywAutoSpeed,
        double zwAutoSpeed,
        boolean xwAutoEnabled,
        boolean ywAutoEnabled,
        boolean zwAutoEnabled,
        boolean motionPaused,
        boolean sliceSweepEnabled,
        double sliceSweepSpeed) {
    private static final double DEFAULT_XW_AUTO_SPEED = Math.toRadians(8.0);

    public FourDViewState {
        if (configuration == null) {
            throw new IllegalArgumentException("4D view configuration is required");
        }
        requireFinite(xwAngle, "XW angle");
        requireFinite(ywAngle, "YW angle");
        requireFinite(zwAngle, "ZW angle");
        requireFinite(xwAutoSpeed, "XW automatic rotation speed");
        requireFinite(ywAutoSpeed, "YW automatic rotation speed");
        requireFinite(zwAutoSpeed, "ZW automatic rotation speed");
        requireFinite(sliceSweepSpeed, "Slice sweep speed");
        if (sliceSweepSpeed < 0.0) {
            throw new IllegalArgumentException("Slice sweep speed must be non-negative");
        }
    }

    public static FourDViewState defaults() {
        return new FourDViewState(FourDViewConfiguration.defaults(),
                0.0, 0.0, 0.0,
                DEFAULT_XW_AUTO_SPEED, 0.0, 0.0,
                true, false, false, false, false, 1.0);
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
