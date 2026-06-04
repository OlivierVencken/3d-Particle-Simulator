package com.particle.sim.input;

import java.util.HashSet;
import java.util.Set;

public final class InputState {
    private final Set<Integer> downKeys = new HashSet<>();
    private final Set<Integer> pressedKeys = new HashSet<>();

    public void beginFrame() {
        pressedKeys.clear();
    }

    public void setKeyState(int key, boolean isDown) {
        boolean wasDown = downKeys.contains(key);
        if (isDown && !wasDown) {
            pressedKeys.add(key);
        }
        if (isDown) {
            downKeys.add(key);
        } else {
            downKeys.remove(key);
        }
    }

    public boolean wasPressed(int key) {
        return pressedKeys.contains(key);
    }
}
