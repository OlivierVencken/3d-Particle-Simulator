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

    public void bind(HotkeyDefinition definition) {
        bindings.add(new HotkeyBinding(
                definition.key(),
                definition.action(),
                definition.context(),
                definition.activeWhileUiOwnsKeyboard(),
                () -> true));
    }

    public void on(HotkeyAction action, Runnable handler) {
        handlers.put(action, handler);
    }

    public void update(long window, HotkeyRoutingContext context) {
        inputState.beginFrame();
        for (HotkeyBinding binding : bindings) {
            boolean pressed = glfwGetKey(window, binding.key()) == GLFW_PRESS;
            inputState.setKeyState(binding.key(), pressed);
            if (inputState.wasPressed(binding.key()) && binding.enabled().getAsBoolean()
                    && context.permits(binding)) {
                Runnable handler = handlers.get(binding.action());
                if (handler != null) {
                    handler.run();
                }
            }
        }
    }
}
