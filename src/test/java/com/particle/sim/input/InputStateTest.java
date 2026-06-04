package com.particle.sim.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputStateTest {
    @Test
    void wasPressedOnlyReturnsTrueOnTransitionToDown() {
        InputState input = new InputState();

        input.beginFrame();
        input.setKeyState(32, true);
        assertTrue(input.wasPressed(32));

        input.beginFrame();
        input.setKeyState(32, true);
        assertFalse(input.wasPressed(32));

        input.beginFrame();
        input.setKeyState(32, false);
        assertFalse(input.wasPressed(32));

        input.beginFrame();
        input.setKeyState(32, true);
        assertTrue(input.wasPressed(32));
    }
}
