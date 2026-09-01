package com.particle.sim.camera;

import static com.particle.sim.camera.CameraCaptureState.Transition.CAPTURED;
import static com.particle.sim.camera.CameraCaptureState.Transition.NONE;
import static com.particle.sim.camera.CameraCaptureState.Transition.RELEASED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.particle.sim.ui.InputOwnership;
import org.junit.jupiter.api.Test;

class CameraCaptureStateTest {
    private static final InputOwnership SIMULATION = new InputOwnership(true, false, false, false);
    private static final InputOwnership UI = new InputOwnership(true, true, true, false);
    private static final InputOwnership MODAL = new InputOwnership(true, true, true, true);

    @Test
    void initiatingClickMustBelongToSimulation() {
        CameraCaptureState capture = new CameraCaptureState();

        assertEquals(NONE, capture.update(true, false, false, true, UI));
        assertFalse(capture.captured());

        capture.update(false, false, false, true, UI);
        assertEquals(CAPTURED, capture.update(true, false, false, true, SIMULATION));
        assertTrue(capture.captured());
    }

    @Test
    void draggingFromUiIntoViewportDoesNotStartCapture() {
        CameraCaptureState capture = new CameraCaptureState();

        capture.update(true, false, false, true, UI);

        assertEquals(NONE, capture.update(true, false, false, true, SIMULATION));
        assertFalse(capture.captured());
    }

    @Test
    void rightClickEscapeAndModalReliablyReleaseCapture() {
        assertReleaseOn(false, true, false, SIMULATION);
        assertReleaseOn(false, false, true, SIMULATION);
        assertReleaseOn(false, false, false, MODAL);
    }

    @Test
    void focusLossReleasesAndRequiresButtonsToBeReleasedBeforeRecapture() {
        CameraCaptureState capture = capturedState();

        assertEquals(RELEASED, capture.update(true, false, false, false, SIMULATION));
        assertFalse(capture.captured());
        assertEquals(NONE, capture.update(true, false, false, true, SIMULATION));
        assertFalse(capture.captured());

        capture.update(false, false, false, true, SIMULATION);
        assertEquals(CAPTURED, capture.update(true, false, false, true, SIMULATION));
    }

    private static void assertReleaseOn(
            boolean left, boolean right, boolean escape, InputOwnership ownership) {
        CameraCaptureState capture = capturedState();

        assertEquals(RELEASED, capture.update(left, right, escape, true, ownership));
        assertFalse(capture.captured());
    }

    private static CameraCaptureState capturedState() {
        CameraCaptureState capture = new CameraCaptureState();
        assertEquals(CAPTURED, capture.update(true, false, false, true, SIMULATION));
        return capture;
    }
}
