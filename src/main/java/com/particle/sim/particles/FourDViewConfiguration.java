package com.particle.sim.particles;

import com.particle.sim.math.Math4d;

import java.util.Arrays;

public final class FourDViewConfiguration {
    public static final double DEFAULT_PERSPECTIVE_DISTANCE = 12.0;
    public static final double DEFAULT_SLICE_CENTER_W = 0.0;
    public static final double DEFAULT_SLICE_THICKNESS = 1.0;
    public static final double DEFAULT_SLICE_FEATHER = 0.2;
    public static final double DEFAULT_COLOR_RANGE = 4.0;

    private final FourDVisualizationMode visualizationMode;
    private final double[] rotationMatrix;
    private final double perspectiveDistance;
    private final double sliceCenterW;
    private final double sliceThickness;
    private final double sliceFeather;
    private final double colorRange;

    public FourDViewConfiguration(FourDVisualizationMode visualizationMode, double[] rotationMatrix,
            double perspectiveDistance, double sliceCenterW, double sliceThickness, double sliceFeather,
            double colorRange) {
        if (visualizationMode == null) {
            throw new IllegalArgumentException("Visualization mode is required");
        }
        validateRotationMatrix(rotationMatrix);
        requirePositiveFinite(perspectiveDistance, "Perspective distance");
        requireFinite(sliceCenterW, "Slice center");
        requirePositiveFinite(sliceThickness, "Slice thickness");
        requireNonNegativeFinite(sliceFeather, "Slice feather");
        requirePositiveFinite(colorRange, "Color range");

        this.visualizationMode = visualizationMode;
        this.rotationMatrix = rotationMatrix.clone();
        this.perspectiveDistance = perspectiveDistance;
        this.sliceCenterW = sliceCenterW;
        this.sliceThickness = sliceThickness;
        this.sliceFeather = Math.min(sliceFeather, sliceThickness * 0.5);
        this.colorRange = colorRange;
    }

    public static FourDViewConfiguration defaults() {
        return new FourDViewConfiguration(
                FourDVisualizationMode.PERSPECTIVE,
                Math4d.identity(),
                DEFAULT_PERSPECTIVE_DISTANCE,
                DEFAULT_SLICE_CENTER_W,
                DEFAULT_SLICE_THICKNESS,
                DEFAULT_SLICE_FEATHER,
                DEFAULT_COLOR_RANGE);
    }

    public FourDVisualizationMode visualizationMode() {
        return visualizationMode;
    }

    public double[] rotationMatrix() {
        return rotationMatrix.clone();
    }

    public double perspectiveDistance() {
        return perspectiveDistance;
    }

    public double sliceCenterW() {
        return sliceCenterW;
    }

    public double sliceThickness() {
        return sliceThickness;
    }

    public double sliceFeather() {
        return sliceFeather;
    }

    public double colorRange() {
        return colorRange;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FourDViewConfiguration configuration)) {
            return false;
        }
        return visualizationMode == configuration.visualizationMode
                && Arrays.equals(rotationMatrix, configuration.rotationMatrix)
                && Double.compare(perspectiveDistance, configuration.perspectiveDistance) == 0
                && Double.compare(sliceCenterW, configuration.sliceCenterW) == 0
                && Double.compare(sliceThickness, configuration.sliceThickness) == 0
                && Double.compare(sliceFeather, configuration.sliceFeather) == 0
                && Double.compare(colorRange, configuration.colorRange) == 0;
    }

    @Override
    public int hashCode() {
        int result = visualizationMode.hashCode();
        result = 31 * result + Arrays.hashCode(rotationMatrix);
        result = 31 * result + Double.hashCode(perspectiveDistance);
        result = 31 * result + Double.hashCode(sliceCenterW);
        result = 31 * result + Double.hashCode(sliceThickness);
        result = 31 * result + Double.hashCode(sliceFeather);
        result = 31 * result + Double.hashCode(colorRange);
        return result;
    }

    private static void validateRotationMatrix(double[] matrix) {
        if (matrix == null || matrix.length != Math4d.MATRIX_ELEMENT_COUNT) {
            throw new IllegalArgumentException("Rotation matrix must contain 16 elements");
        }
        for (double value : matrix) {
            requireFinite(value, "Rotation matrix");
        }
    }

    private static void requirePositiveFinite(double value, String name) {
        requireFinite(value, name);
        if (value <= 0.0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static void requireNonNegativeFinite(double value, String name) {
        requireFinite(value, name);
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
