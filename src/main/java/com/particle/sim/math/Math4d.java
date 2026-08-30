package com.particle.sim.math;

public final class Math4d {
    public static final int MATRIX_ELEMENT_COUNT = 16;
    public static final int VECTOR_COMPONENT_COUNT = 4;
    private static final double MINIMUM_PERSPECTIVE_DENOMINATOR = 1.0e-9;

    private Math4d() {
    }

    public static double[] identity() {
        return new double[] {
                1.0, 0.0, 0.0, 0.0,
                0.0, 1.0, 0.0, 0.0,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 1.0
        };
    }

    public static double[] planeRotation(RotationPlane4d plane, double angleRadians) {
        if (plane == null) {
            throw new IllegalArgumentException("Rotation plane is required");
        }
        if (!Double.isFinite(angleRadians)) {
            throw new IllegalArgumentException("Rotation angle must be finite");
        }

        int firstAxis = plane.firstAxis();
        int secondAxis = plane.secondAxis();
        double cosine = Math.cos(angleRadians);
        double sine = Math.sin(angleRadians);
        double[] result = identity();
        result[index(firstAxis, firstAxis)] = cosine;
        result[index(secondAxis, secondAxis)] = cosine;
        result[index(firstAxis, secondAxis)] = -sine;
        result[index(secondAxis, firstAxis)] = sine;
        return result;
    }

    public static double[] multiply(double[] left, double[] right) {
        requireMatrix(left, "Left matrix");
        requireMatrix(right, "Right matrix");
        double[] result = new double[MATRIX_ELEMENT_COUNT];
        for (int column = 0; column < VECTOR_COMPONENT_COUNT; column++) {
            for (int row = 0; row < VECTOR_COMPONENT_COUNT; row++) {
                double value = 0.0;
                for (int component = 0; component < VECTOR_COMPONENT_COUNT; component++) {
                    value += left[index(row, component)] * right[index(component, column)];
                }
                result[index(row, column)] = value;
            }
        }
        return result;
    }

    public static double[] transform(double[] matrix, double[] vector) {
        requireMatrix(matrix, "Transformation matrix");
        requireVector(vector, "Vector");
        double[] result = new double[VECTOR_COMPONENT_COUNT];
        for (int row = 0; row < VECTOR_COMPONENT_COUNT; row++) {
            for (int component = 0; component < VECTOR_COMPONENT_COUNT; component++) {
                result[row] += matrix[index(row, component)] * vector[component];
            }
        }
        return result;
    }

    public static PerspectiveProjection perspectiveProject(double[] point, double projectionDistance) {
        requireVector(point, "Perspective point");
        if (!Double.isFinite(projectionDistance) || projectionDistance <= 0.0) {
            throw new IllegalArgumentException("Projection distance must be finite and positive");
        }

        double denominator = projectionDistance - point[3];
        if (!Double.isFinite(denominator) || denominator <= MINIMUM_PERSPECTIVE_DENOMINATOR) {
            return PerspectiveProjection.hidden();
        }

        double scale = projectionDistance / denominator;
        double x = point[0] * scale;
        double y = point[1] * scale;
        double z = point[2] * scale;
        if (!Double.isFinite(scale) || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            return PerspectiveProjection.hidden();
        }
        return new PerspectiveProjection(true, x, y, z, scale);
    }

    public static double sliceWeight(double w, double sliceCenterW, double thickness, double feather) {
        if (!Double.isFinite(w) || !Double.isFinite(sliceCenterW)) {
            return 0.0;
        }
        if (!Double.isFinite(thickness) || thickness <= 0.0) {
            throw new IllegalArgumentException("Slice thickness must be finite and positive");
        }
        if (!Double.isFinite(feather) || feather < 0.0) {
            throw new IllegalArgumentException("Slice feather must be finite and non-negative");
        }

        double halfThickness = thickness * 0.5;
        double distance = Math.abs(w - sliceCenterW);
        if (distance > halfThickness) {
            return 0.0;
        }

        double effectiveFeather = Math.min(feather, halfThickness);
        if (effectiveFeather == 0.0) {
            return 1.0;
        }
        double featherStart = halfThickness - effectiveFeather;
        if (distance <= featherStart) {
            return 1.0;
        }

        double t = (distance - featherStart) / effectiveFeather;
        return 1.0 - smoothstep(t);
    }

    public static double lengthSquared(double[] vector) {
        requireVector(vector, "Vector");
        double result = 0.0;
        for (double component : vector) {
            result += component * component;
        }
        return result;
    }

    private static double smoothstep(double value) {
        double clamped = Math.max(0.0, Math.min(1.0, value));
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }

    private static int index(int row, int column) {
        return column * VECTOR_COMPONENT_COUNT + row;
    }

    private static void requireMatrix(double[] matrix, String name) {
        if (matrix == null || matrix.length != MATRIX_ELEMENT_COUNT) {
            throw new IllegalArgumentException(name + " must contain 16 elements");
        }
        requireFinite(matrix, name);
    }

    private static void requireVector(double[] vector, String name) {
        if (vector == null || vector.length != VECTOR_COMPONENT_COUNT) {
            throw new IllegalArgumentException(name + " must contain four components");
        }
        requireFinite(vector, name);
    }

    private static void requireFinite(double[] values, String name) {
        for (double value : values) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException(name + " must contain only finite values");
            }
        }
    }

    public record PerspectiveProjection(boolean visible, double x, double y, double z, double scale) {
        private static PerspectiveProjection hidden() {
            return new PerspectiveProjection(false, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
