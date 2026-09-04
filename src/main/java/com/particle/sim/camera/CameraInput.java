package com.particle.sim.camera;

import java.util.Objects;

/** Toolkit-neutral camera input sampled for one frame. */
public record CameraInput(
        Pointer pointer,
        Movement movement,
        boolean windowFocused,
        boolean captureAllowed,
        boolean keyboardAllowed,
        boolean modalOpen) {
    public CameraInput {
        Objects.requireNonNull(pointer, "pointer");
        Objects.requireNonNull(movement, "movement");
    }

    public record Pointer(
            boolean primaryDown,
            boolean secondaryDown,
            boolean escapeDown,
            float deltaX,
            float deltaY) {}

    public record Movement(
            boolean forward,
            boolean backward,
            boolean right,
            boolean left,
            boolean up,
            boolean down,
            boolean reset) {}
}
