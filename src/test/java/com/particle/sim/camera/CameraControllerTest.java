package com.particle.sim.camera;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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

        assertArrayEquals(
                new float[] {
                    -1.0f, 0.0f, -0.0f, 0.0f, 0.0f, 1.0f, -0.0f, 0.0f, 0.0f, -0.0f, -1.0f, 0.0f,
                    -0.0f, -0.0f, -18.5f, 1.0f
                },
                camera.viewMatrix(),
                EPSILON);
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

    @Test
    void neutralInputSnapshotControlsCaptureRotationAndRelease() {
        CameraController camera = new CameraController();

        CameraCaptureTransition captured =
                camera.update(
                        0.0f,
                        input(
                                new CameraInput.Pointer(true, false, false, 4.0f, -2.0f),
                                noMovement(),
                                true,
                                true));

        assertEquals(CameraCaptureTransition.CAPTURED, captured);
        assertTrue(camera.isMouseCaptured());
        float[] rotatedView = camera.viewMatrix();

        CameraCaptureTransition released =
                camera.update(
                        0.0f,
                        input(
                                new CameraInput.Pointer(true, true, false, 0.0f, 0.0f),
                                noMovement(),
                                true,
                                true));

        assertEquals(CameraCaptureTransition.RELEASED, released);
        assertFalse(camera.isMouseCaptured());
        assertNotEquals(new CameraController().viewMatrix()[0], rotatedView[0]);
    }

    @Test
    void keyboardMovementDoesNotRequirePlatformWindowAccess() {
        CameraController camera = new CameraController();
        float[] before = camera.viewMatrix();

        camera.update(
                1.0f,
                input(
                        new CameraInput.Pointer(false, false, false, 0.0f, 0.0f),
                        new CameraInput.Movement(true, false, false, false, false, false, false),
                        true,
                        false));

        assertNotEquals(before[14], camera.viewMatrix()[14]);
    }

    private static CameraInput input(
            CameraInput.Pointer pointer,
            CameraInput.Movement movement,
            boolean keyboardAllowed,
            boolean captureAllowed) {
        return new CameraInput(pointer, movement, true, captureAllowed, keyboardAllowed, false);
    }

    private static CameraInput.Movement noMovement() {
        return new CameraInput.Movement(false, false, false, false, false, false, false);
    }
}
