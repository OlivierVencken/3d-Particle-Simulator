package com.particle.sim.input;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class HotkeyManager {
    private final List<HotkeyBinding> bindings = new ArrayList<>();
    private final EnumMap<HotkeyAction, Runnable> handlers = new EnumMap<>(HotkeyAction.class);
    private final InputState inputState = new InputState();

    public void bind(int key, HotkeyAction action, HotkeyContext context) {
        bindings.add(new HotkeyBinding(key, action, context, () -> true));
    }

    public void on(HotkeyAction action, Runnable handler) {
        handlers.put(action, handler);
    }

    public void update(long window, HotkeyContext activeContext) {
        inputState.beginFrame();
        for (HotkeyBinding binding : bindings) {
            boolean pressed = glfwGetKey(window, binding.key()) == GLFW_PRESS;
            inputState.setKeyState(binding.key(), pressed);
            if (inputState.wasPressed(binding.key()) && binding.enabled().getAsBoolean()
                    && (binding.context() == activeContext || binding.context() == HotkeyContext.GLOBAL)) {
                Runnable handler = handlers.get(binding.action());
                if (handler != null) {
                    handler.run();
                }
            }
        }
    }
}
