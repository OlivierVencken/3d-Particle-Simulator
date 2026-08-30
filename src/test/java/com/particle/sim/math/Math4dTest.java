package com.particle.sim.math;

import com.particle.sim.math.Math4d.PerspectiveProjection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Math4dTest {
    private static final double EPSILON = 1.0e-10;
    private static final double[] VECTOR = { 1.25, -2.5, 3.75, -4.125 };

    @Test
    void everyPlaneRotationPreservesVectorLength() {
        double expectedLengthSquared = Math4d.lengthSquared(VECTOR);

        for (RotationPlane4d plane : RotationPlane4d.values()) {
            double[] rotated = Math4d.transform(Math4d.planeRotation(plane, 0.731), VECTOR);

            assertEquals(expectedLengthSquared, Math4d.lengthSquared(rotated), EPSILON,
                    plane + " rotation changed vector length");
        }
    }

    @Test
    void quarterTurnMapsEachPlanesFirstAxisOntoItsSecondAxis() {
        for (RotationPlane4d plane : RotationPlane4d.values()) {
            double[] basis = new double[4];
            basis[plane.firstAxis()] = 1.0;

            double[] rotated = Math4d.transform(Math4d.planeRotation(plane, Math.PI * 0.5), basis);
            double[] expected = new double[4];
            expected[plane.secondAxis()] = 1.0;

            assertArrayEquals(expected, rotated, EPSILON, plane + " quarter turn used the wrong axes or sign");
        }
    }

    @Test
    void rotationFollowedByInverseRestoresOriginalVector() {
        for (RotationPlane4d plane : RotationPlane4d.values()) {
            double[] rotation = Math4d.planeRotation(plane, 1.137);
            double[] inverse = Math4d.planeRotation(plane, -1.137);
            double[] restored = Math4d.transform(inverse, Math4d.transform(rotation, VECTOR));

            assertArrayEquals(VECTOR, restored, EPSILON, plane + " inverse did not restore the vector");
        }
    }

    @Test
    void matrixMultiplicationComposesRotationsInColumnMajorOrder() {
        double[] xw = Math4d.planeRotation(RotationPlane4d.XW, 0.45);
        double[] yw = Math4d.planeRotation(RotationPlane4d.YW, -0.82);

        double[] sequential = Math4d.transform(yw, Math4d.transform(xw, VECTOR));
        double[] composed = Math4d.transform(Math4d.multiply(yw, xw), VECTOR);

        assertArrayEquals(sequential, composed, EPSILON);
    }

    @Test
    void perspectiveProjectionMapsVisiblePoint() {
        PerspectiveProjection projection = Math4d.perspectiveProject(new double[] { 1.0, -2.0, 3.0, 2.0 }, 4.0);

        assertTrue(projection.visible());
        assertEquals(2.0, projection.scale(), EPSILON);
        assertEquals(2.0, projection.x(), EPSILON);
        assertEquals(-4.0, projection.y(), EPSILON);
        assertEquals(6.0, projection.z(), EPSILON);
    }

    @Test
    void perspectiveProjectionRejectsPlaneBehindPlaneAndNearSingularitySafely() {
        for (double w : new double[] { 4.0, 5.0, 4.0 - 1.0e-12 }) {
            PerspectiveProjection projection = Math4d.perspectiveProject(
                    new double[] { Double.MAX_VALUE, 1.0, -1.0, w }, 4.0);

            assertFalse(projection.visible());
            assertTrue(Double.isFinite(projection.x()));
            assertTrue(Double.isFinite(projection.y()));
            assertTrue(Double.isFinite(projection.z()));
            assertTrue(Double.isFinite(projection.scale()));
        }
    }

    @Test
    void perspectiveProjectionRejectsOverflowWithoutReturningInvalidCoordinates() {
        PerspectiveProjection projection = Math4d.perspectiveProject(
                new double[] { Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, 0.5 }, 1.0);

        assertFalse(projection.visible());
        assertEquals(0.0, projection.x());
        assertEquals(0.0, projection.y());
        assertEquals(0.0, projection.z());
        assertEquals(0.0, projection.scale());
    }

    @Test
    void sliceWeightHasSolidCenterFeatheredEdgeAndZeroExterior() {
        assertEquals(1.0, Math4d.sliceWeight(0.0, 0.0, 2.0, 0.25), EPSILON);
        assertEquals(1.0, Math4d.sliceWeight(0.75, 0.0, 2.0, 0.25), EPSILON);
        assertTrue(Math4d.sliceWeight(0.875, 0.0, 2.0, 0.25) > 0.0);
        assertTrue(Math4d.sliceWeight(0.875, 0.0, 2.0, 0.25) < 1.0);
        assertEquals(0.0, Math4d.sliceWeight(1.0, 0.0, 2.0, 0.25), EPSILON);
        assertEquals(0.0, Math4d.sliceWeight(1.01, 0.0, 2.0, 0.25), EPSILON);
    }

    @Test
    void sliceWeightIsContinuousAtFeatherBoundaries() {
        double epsilon = 1.0e-6;
        double beforeFeather = Math4d.sliceWeight(0.75 - epsilon, 0.0, 2.0, 0.25);
        double afterFeather = Math4d.sliceWeight(0.75 + epsilon, 0.0, 2.0, 0.25);
        double beforeExterior = Math4d.sliceWeight(1.0 - epsilon, 0.0, 2.0, 0.25);
        double afterExterior = Math4d.sliceWeight(1.0 + epsilon, 0.0, 2.0, 0.25);

        assertEquals(beforeFeather, afterFeather, 1.0e-5);
        assertEquals(beforeExterior, afterExterior, 1.0e-5);
    }

    @Test
    void invalidInputsAreRejectedAtTheMathBoundary() {
        assertThrows(IllegalArgumentException.class,
                () -> Math4d.planeRotation(RotationPlane4d.XW, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> Math4d.multiply(new double[15], Math4d.identity()));
        assertThrows(IllegalArgumentException.class,
                () -> Math4d.perspectiveProject(VECTOR, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> Math4d.sliceWeight(0.0, 0.0, -1.0, 0.0));
    }
}
