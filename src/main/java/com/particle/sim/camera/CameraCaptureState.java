package com.particle.sim.camera;

import com.particle.sim.ui.InputOwnership;

/** Pure state machine for initiating and releasing relative-mouse camera capture. */
final class CameraCaptureState {
    enum Transition {
        NONE,
        CAPTURED,
        RELEASED
    }

    private boolean captured;
    private boolean previousLeftDown;
    private boolean pointerPressArmed = true;

    Transition update(
            boolean leftDown,
            boolean rightDown,
            boolean escapeDown,
            boolean windowFocused,
            InputOwnership ownership) {
        boolean leftPressed = leftDown && !previousLeftDown;
        Transition transition = Transition.NONE;

        if (!windowFocused) {
            pointerPressArmed = false;
            if (captured) {
                captured = false;
                transition = Transition.RELEASED;
            }
        } else if (captured && (rightDown || escapeDown || ownership.modalOpen())) {
            captured = false;
            transition = Transition.RELEASED;
        } else if (!captured
                && pointerPressArmed
                && leftPressed
                && ownership.canStartCameraCapture()) {
            captured = true;
            transition = Transition.CAPTURED;
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
