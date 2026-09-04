package com.particle.sim.camera;

/** Pure state machine for initiating and releasing relative-mouse camera capture. */
final class CameraCaptureState {
    private boolean captured;
    private boolean previousLeftDown;
    private boolean pointerPressArmed = true;

    CameraCaptureTransition update(
            boolean leftDown,
            boolean rightDown,
            boolean escapeDown,
            boolean windowFocused,
            boolean captureAllowed,
            boolean modalOpen) {
        boolean leftPressed = leftDown && !previousLeftDown;
        CameraCaptureTransition transition = CameraCaptureTransition.NONE;

        if (!windowFocused) {
            pointerPressArmed = false;
            if (captured) {
                captured = false;
                transition = CameraCaptureTransition.RELEASED;
            }
        } else if (captured && (rightDown || escapeDown || modalOpen)) {
            captured = false;
            transition = CameraCaptureTransition.RELEASED;
        } else if (!captured && pointerPressArmed && leftPressed && captureAllowed) {
            captured = true;
            transition = CameraCaptureTransition.CAPTURED;
        }

        if (windowFocused && !leftDown && !rightDown) {
            pointerPressArmed = true;
        }
        previousLeftDown = leftDown;
        return transition;
    }

    boolean captured() {
        return captured;
    }
}
