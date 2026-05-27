package com.particle.sim.camera;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CameraControllerTest {
    private static final float EPSILON = 0.0001f;

    @Test
    void resetRestoresDefaultViewMatrix() {
        CameraController camera = new CameraController();
        float[] initialView = camera.viewMatrix();

        camera.reset();

        assertArrayEquals(initialView, camera.viewMatrix(), EPSILON);
    }

    @Test
    void defaultViewMatrixUsesExpectedCameraPositionAndPitch() {
        CameraController camera = new CameraController();

        assertArrayEquals(new float[] {
                -1.0f, 0.0f, -0.0f, 0.0f,
                0.0f, 1.0f, -0.0f, 0.0f,
                0.0f, -0.0f, -1.0f, 0.0f,
                -0.0f, -0.0f, -18.5f, 1.0f
        }, camera.viewMatrix(), EPSILON);
    }

    @Test
    void sensitivityCannotBeSetBelowMinimum() {
        CameraController camera = new CameraController();

        camera.setSensitivity(-1.0f);

        assertEquals(0.0001f, camera.getSensitivity(), EPSILON);

        camera.setSensitivity(0.005f);

        assertEquals(0.005f, camera.getSensitivity(), EPSILON);
    }

    @Test
    void flySpeedCannotBeSetBelowMinimum() {
        CameraController camera = new CameraController();

        camera.setFlySpeed(-1.0f);

        assertEquals(0.1f, camera.getFlySpeed(), EPSILON);

        camera.setFlySpeed(12.5f);

        assertEquals(12.5f, camera.getFlySpeed(), EPSILON);
    }
}
