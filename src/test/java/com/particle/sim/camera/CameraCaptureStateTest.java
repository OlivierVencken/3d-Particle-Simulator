package com.particle.sim.camera;

import static com.particle.sim.camera.CameraCaptureTransition.CAPTURED;
import static com.particle.sim.camera.CameraCaptureTransition.NONE;
import static com.particle.sim.camera.CameraCaptureTransition.RELEASED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CameraCaptureStateTest {
    @Test
    void initiatingClickMustBelongToSimulation() {
        CameraCaptureState capture = new CameraCaptureState();

        assertEquals(NONE, capture.update(true, false, false, true, false, false));
        assertFalse(capture.captured());

        capture.update(false, false, false, true, false, false);
        assertEquals(CAPTURED, capture.update(true, false, false, true, true, false));
        assertTrue(capture.captured());
    }

    @Test
    void draggingFromUiIntoViewportDoesNotStartCapture() {
        CameraCaptureState capture = new CameraCaptureState();

        capture.update(true, false, false, true, false, false);

        assertEquals(NONE, capture.update(true, false, false, true, true, false));
        assertFalse(capture.captured());
    }

    @Test
    void rightClickEscapeAndModalReliablyReleaseCapture() {
        assertReleaseOn(false, true, false, false);
        assertReleaseOn(false, false, true, false);
        assertReleaseOn(false, false, false, true);
    }

    @Test
    void focusLossReleasesAndRequiresButtonsToBeReleasedBeforeRecapture() {
        CameraCaptureState capture = capturedState();

        assertEquals(RELEASED, capture.update(true, false, false, false, true, false));
        assertFalse(capture.captured());
        assertEquals(NONE, capture.update(true, false, false, true, true, false));
        assertFalse(capture.captured());

        capture.update(false, false, false, true, true, false);
        assertEquals(CAPTURED, capture.update(true, false, false, true, true, false));
    }

    private static void assertReleaseOn(
            boolean left, boolean right, boolean escape, boolean modalOpen) {
        CameraCaptureState capture = capturedState();

        assertEquals(RELEASED, capture.update(left, right, escape, true, true, modalOpen));
        assertFalse(capture.captured());
    }

    private static CameraCaptureState capturedState() {
        CameraCaptureState capture = new CameraCaptureState();
        assertEquals(CAPTURED, capture.update(true, false, false, true, true, false));
        return capture;
    }
}
