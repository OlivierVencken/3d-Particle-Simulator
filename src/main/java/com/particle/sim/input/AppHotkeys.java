package com.particle.sim.input;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

public final class AppHotkeys {
    private final List<HotkeyBinding> bindings = new ArrayList<>();

    public AppHotkeys(Runnable toggleFullscreen, BooleanSupplier canCloseWindow, Runnable closeWindow) {
        onPress(GLFW_KEY_F11, toggleFullscreen);
        onPress(GLFW_KEY_ESCAPE, canCloseWindow, closeWindow);
    }

    public void update(long window) {
        for (HotkeyBinding binding : bindings) {
            binding.update(window);
        }
    }

    private void onPress(int key, Runnable action) {
        onPress(key, () -> true, action);
    }

    private void onPress(int key, BooleanSupplier enabled, Runnable action) {
        bindings.add(new HotkeyBinding(key, enabled, action));
    }

    private static final class HotkeyBinding {
        private final int key;
        private final BooleanSupplier enabled;
        private final Runnable action;
        private boolean wasPressed;

        private HotkeyBinding(int key, BooleanSupplier enabled, Runnable action) {
            this.key = key;
            this.enabled = enabled;
            this.action = action;
        }

        private void update(long window) {
            boolean pressed = glfwGetKey(window, key) == GLFW_PRESS;
            if (pressed && !wasPressed && enabled.getAsBoolean()) {
                action.run();
            }
            wasPressed = pressed;
        }
    }
}
