package com.particle.sim.particles;

import com.particle.sim.math.Math4d;
import com.particle.sim.math.RotationPlane4d;

/**
 * Small diagnostic controller for incrementally orienting a four-dimensional view.
 * It deliberately exposes only the three planes involving W; normal XYZ navigation
 * remains the responsibility of the existing camera.
 */
public final class FourDViewController {
    private static final int REORTHONORMALIZE_INTERVAL = 256;
    private static final double MINIMUM_PERSPECTIVE_MARGIN = 0.25;
    private static final double MOTION_STEP_SECONDS = 1.0 / 240.0;
    private FourDViewConfiguration configuration = FourDViewConfiguration.defaults();
    private int rotationsSinceOrthonormalization;
    private double xwAngle;
    private double ywAngle;
    private double zwAngle;
    private double xwAutoSpeed = FourDViewState.defaults().xwAutoSpeed();
    private double ywAutoSpeed;
    private double zwAutoSpeed;
    private boolean xwAutoEnabled = true;
    private boolean ywAutoEnabled;
    private boolean zwAutoEnabled;
    private boolean motionPaused;
    private boolean sliceSweepEnabled;
    private double sliceSweepSpeed = 1.0;
    private int sliceSweepDirection = 1;
    private double motionAccumulator;

    public FourDViewConfiguration configuration() {
        return configuration;
    }

    public FourDViewState state() {
        return new FourDViewState(configuration, xwAngle, ywAngle, zwAngle,
                xwAutoSpeed, ywAutoSpeed, zwAutoSpeed,
                xwAutoEnabled, ywAutoEnabled, zwAutoEnabled,
                motionPaused, sliceSweepEnabled, sliceSweepSpeed);
    }

    public void applyState(FourDViewState state) {
        if (state == null) {
            throw new IllegalArgumentException("4D view state is required");
        }
        configuration = state.configuration();
        xwAngle = wrapAngle(state.xwAngle());
        ywAngle = wrapAngle(state.ywAngle());
        zwAngle = wrapAngle(state.zwAngle());
        xwAutoSpeed = state.xwAutoSpeed();
        ywAutoSpeed = state.ywAutoSpeed();
        zwAutoSpeed = state.zwAutoSpeed();
        xwAutoEnabled = state.xwAutoEnabled();
        ywAutoEnabled = state.ywAutoEnabled();
        zwAutoEnabled = state.zwAutoEnabled();
        motionPaused = state.motionPaused();
        sliceSweepEnabled = state.sliceSweepEnabled();
        sliceSweepSpeed = state.sliceSweepSpeed();
        sliceSweepDirection = 1;
        rotationsSinceOrthonormalization = 0;
        motionAccumulator = 0.0;
    }

    public void resetOrientation() {
        rotationsSinceOrthonormalization = 0;
        xwAngle = 0.0;
        ywAngle = 0.0;
        zwAngle = 0.0;
        xwAutoSpeed = 0.0;
        ywAutoSpeed = 0.0;
        zwAutoSpeed = 0.0;
        xwAutoEnabled = false;
        ywAutoEnabled = false;
        zwAutoEnabled = false;
        motionAccumulator = 0.0;
        replace(configuration.visualizationMode(), Math4d.identity());
    }

    public void rotateXw(double angleRadians) {
        requireFinite(angleRadians, "XW rotation");
        xwAngle = wrapAngle(xwAngle + angleRadians);
        rotate(RotationPlane4d.XW, angleRadians);
    }

    public void rotateYw(double angleRadians) {
        requireFinite(angleRadians, "YW rotation");
        ywAngle = wrapAngle(ywAngle + angleRadians);
        rotate(RotationPlane4d.YW, angleRadians);
    }

    public void rotateZw(double angleRadians) {
        requireFinite(angleRadians, "ZW rotation");
        zwAngle = wrapAngle(zwAngle + angleRadians);
        rotate(RotationPlane4d.ZW, angleRadians);
    }

    public double xwAngle() {
        return xwAngle;
    }

    public void xwAngle(double angleRadians) {
        requireFinite(angleRadians, "XW angle");
        rotateXw(wrapAngle(angleRadians) - xwAngle);
        xwAngle = wrapAngle(angleRadians);
    }

    public double ywAngle() {
        return ywAngle;
    }

    public void ywAngle(double angleRadians) {
        requireFinite(angleRadians, "YW angle");
        rotateYw(wrapAngle(angleRadians) - ywAngle);
        ywAngle = wrapAngle(angleRadians);
    }

    public double zwAngle() {
        return zwAngle;
    }

    public void zwAngle(double angleRadians) {
        requireFinite(angleRadians, "ZW angle");
        rotateZw(wrapAngle(angleRadians) - zwAngle);
        zwAngle = wrapAngle(angleRadians);
    }

    public double xwAutoSpeed() {
        return xwAutoSpeed;
    }

    public void xwAutoSpeed(double radiansPerSecond) {
        requireFinite(radiansPerSecond, "XW automatic rotation speed");
        xwAutoSpeed = radiansPerSecond;
        motionAccumulator = 0.0;
    }

    public boolean xwAutoEnabled() {
        return xwAutoEnabled;
    }

    public void xwAutoEnabled(boolean enabled) {
        xwAutoEnabled = enabled;
        motionAccumulator = 0.0;
    }

    public double ywAutoSpeed() {
        return ywAutoSpeed;
    }

    public void ywAutoSpeed(double radiansPerSecond) {
        requireFinite(radiansPerSecond, "YW automatic rotation speed");
        ywAutoSpeed = radiansPerSecond;
        motionAccumulator = 0.0;
    }

    public boolean ywAutoEnabled() {
        return ywAutoEnabled;
    }

    public void ywAutoEnabled(boolean enabled) {
        ywAutoEnabled = enabled;
        motionAccumulator = 0.0;
    }

    public double zwAutoSpeed() {
        return zwAutoSpeed;
    }

    public void zwAutoSpeed(double radiansPerSecond) {
        requireFinite(radiansPerSecond, "ZW automatic rotation speed");
        zwAutoSpeed = radiansPerSecond;
        motionAccumulator = 0.0;
    }

    public boolean zwAutoEnabled() {
        return zwAutoEnabled;
    }

    public void zwAutoEnabled(boolean enabled) {
        zwAutoEnabled = enabled;
        motionAccumulator = 0.0;
    }

    public boolean motionPaused() {
        return motionPaused;
    }

    public void motionPaused(boolean paused) {
        motionPaused = paused;
    }

    public boolean sliceSweepEnabled() {
        return sliceSweepEnabled;
    }

    public void sliceSweepEnabled(boolean enabled) {
        sliceSweepEnabled = enabled;
        motionAccumulator = 0.0;
    }

    public double sliceSweepSpeed() {
        return sliceSweepSpeed;
    }

    public void sliceSweepSpeed(double unitsPerSecond) {
        requireFinite(unitsPerSecond, "Slice sweep speed");
        if (unitsPerSecond < 0.0) {
            throw new IllegalArgumentException("Slice sweep speed must be non-negative");
        }
        sliceSweepSpeed = unitsPerSecond;
        motionAccumulator = 0.0;
    }

    /** Advances view-only movement independently from simulation time. */
    public void update(double deltaSeconds, double sliceExtent) {
        requireFinite(deltaSeconds, "4D view delta time");
        requireFinite(sliceExtent, "Slice extent");
        if (deltaSeconds < 0.0) {
            throw new IllegalArgumentException("4D view delta time must be non-negative");
        }
        if (sliceExtent <= 0.0) {
            throw new IllegalArgumentException("Slice extent must be positive");
        }
        if (motionPaused || deltaSeconds == 0.0) {
            return;
        }
        boolean sliceMoves = configuration.visualizationMode() == FourDVisualizationMode.SLICE
                && sliceSweepEnabled && sliceSweepSpeed > 0.0;
        if (!xwAutoEnabled && !ywAutoEnabled && !zwAutoEnabled && !sliceMoves) {
            return;
        }

        motionAccumulator += deltaSeconds;
        int stepCount = (int) Math.floor((motionAccumulator + 1.0e-12) / MOTION_STEP_SECONDS);
        motionAccumulator -= stepCount * MOTION_STEP_SECONDS;
        for (int step = 0; step < stepCount; step++) {
            if (xwAutoEnabled) {
                rotateXw(xwAutoSpeed * MOTION_STEP_SECONDS);
            }
            if (ywAutoEnabled) {
                rotateYw(ywAutoSpeed * MOTION_STEP_SECONDS);
            }
            if (zwAutoEnabled) {
                rotateZw(zwAutoSpeed * MOTION_STEP_SECONDS);
            }
            if (sliceMoves) {
                advanceSlice(MOTION_STEP_SECONDS, sliceExtent);
            }
        }
    }

    public static double minimumPerspectiveDistance(double simulationBounds) {
        requireFinite(simulationBounds, "Simulation bounds");
        return Math.max(MINIMUM_PERSPECTIVE_MARGIN,
                Math.abs(simulationBounds) * 2.0 + MINIMUM_PERSPECTIVE_MARGIN);
    }

    public void visualizationMode(FourDVisualizationMode mode) {
        replace(mode, configuration.rotationMatrix());
    }

    public void perspectiveDistance(double distance) {
        configuration = new FourDViewConfiguration(configuration.visualizationMode(),
                configuration.rotationMatrix(), distance, configuration.sliceCenterW(),
                configuration.sliceThickness(), configuration.sliceFeather(), configuration.colorRange());
    }

    public void slice(double centerW, double thickness, double feather) {
        configuration = new FourDViewConfiguration(configuration.visualizationMode(),
                configuration.rotationMatrix(), configuration.perspectiveDistance(), centerW,
                thickness, feather, configuration.colorRange());
    }

    public void colorRange(double range) {
        configuration = new FourDViewConfiguration(configuration.visualizationMode(),
                configuration.rotationMatrix(), configuration.perspectiveDistance(),
                configuration.sliceCenterW(), configuration.sliceThickness(), configuration.sliceFeather(), range);
    }

    private void rotate(RotationPlane4d plane, double angleRadians) {
        if (angleRadians == 0.0) {
            return;
        }
        double[] increment = Math4d.planeRotation(plane, angleRadians);
        double[] orientation = Math4d.multiply(increment, configuration.rotationMatrix());
        rotationsSinceOrthonormalization++;
        if (rotationsSinceOrthonormalization >= REORTHONORMALIZE_INTERVAL) {
            orientation = Math4d.orthonormalize(orientation);
            rotationsSinceOrthonormalization = 0;
        }
        replace(configuration.visualizationMode(), orientation);
    }

    private void replace(FourDVisualizationMode mode, double[] rotationMatrix) {
        configuration = new FourDViewConfiguration(mode, rotationMatrix, configuration.perspectiveDistance(),
                configuration.sliceCenterW(), configuration.sliceThickness(), configuration.sliceFeather(),
                configuration.colorRange());
    }

    private void advanceSlice(double deltaSeconds, double extent) {
        double span = extent * 2.0;
        double center = Math.max(-extent, Math.min(extent, configuration.sliceCenterW()));
        double offset = center + extent;
        double phase = sliceSweepDirection > 0 ? offset : span * 2.0 - offset;
        phase = positiveModulo(phase + sliceSweepSpeed * deltaSeconds, span * 2.0);

        if (phase <= span) {
            center = -extent + phase;
            sliceSweepDirection = 1;
        } else {
            center = extent - (phase - span);
            sliceSweepDirection = -1;
        }
        slice(center, configuration.sliceThickness(), configuration.sliceFeather());
    }

    private static double wrapAngle(double angle) {
        double wrapped = positiveModulo(angle + Math.PI, Math.PI * 2.0) - Math.PI;
        return wrapped == -Math.PI && angle > 0.0 ? Math.PI : wrapped;
    }

    private static double positiveModulo(double value, double modulus) {
        double result = value % modulus;
        return result < 0.0 ? result + modulus : result;
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
