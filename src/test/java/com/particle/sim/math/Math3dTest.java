package com.particle.sim.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Math3dTest {
    private static final float EPSILON = 0.0001f;

    @Test
    void perspectiveBuildsExpectedProjectionMatrix() {
        float[] projection = Math3d.perspective((float) Math.toRadians(90.0), 16.0f / 9.0f, 0.1f, 100.0f);

        assertArrayEquals(new float[] {
                0.5625f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, -1.002002f, -1.0f,
                0.0f, 0.0f, -0.2002002f, 0.0f
        }, projection, EPSILON);
    }

    @Test
    void lookAtBuildsIdentityOrientationForOriginLookingForward() {
        float[] view = Math3d.lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);

        assertArrayEquals(new float[] {
                -1.0f, 0.0f, -0.0f, 0.0f,
                0.0f, 1.0f, -0.0f, 0.0f,
                0.0f, -0.0f, -1.0f, 0.0f,
                -0.0f, -0.0f, 0.0f, 1.0f
        }, view, EPSILON);
    }

    @Test
    void multiplyCombinesColumnMajorMatrices() {
        float[] scale = {
                2.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 3.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 4.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        };
        float[] translation = {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                5.0f, 6.0f, 7.0f, 1.0f
        };

        assertArrayEquals(new float[] {
                2.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 3.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 4.0f, 0.0f,
                10.0f, 18.0f, 28.0f, 1.0f
        }, Math3d.multiply(scale, translation), EPSILON);
    }

    @Test
    void normalizeHandlesRegularAndZeroVectors() {
        assertArrayEquals(new float[] { 0.6f, 0.8f, 0.0f }, Math3d.normalize(3.0f, 4.0f, 0.0f), EPSILON);
        assertArrayEquals(new float[] { 0.0f, 0.0f, 0.0f }, Math3d.normalize(0.0f, 0.0f, 0.0f), EPSILON);
    }

    @Test
    void crossReturnsPerpendicularVector() {
        assertArrayEquals(new float[] { 0.0f, 0.0f, 1.0f },
                Math3d.cross(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f), EPSILON);
    }

    @Test
    void clampBoundsValuesInclusively() {
        assertEquals(-1.0f, Math3d.clamp(-2.0f, -1.0f, 1.0f), EPSILON);
        assertEquals(0.25f, Math3d.clamp(0.25f, -1.0f, 1.0f), EPSILON);
        assertEquals(1.0f, Math3d.clamp(2.0f, -1.0f, 1.0f), EPSILON);
    }
}
