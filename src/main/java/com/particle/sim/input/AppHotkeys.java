package com.particle.sim.input;

import com.particle.sim.ParticleSimulatorApp;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F3;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;

import java.util.List;

public final class AppHotkeys {
    private static final List<HotkeyDefinition> DEFAULT_HOTKEYS = List.of(
            new HotkeyDefinition(GLFW_KEY_F11, HotkeyAction.TOGGLE_FULLSCREEN, HotkeyContext.GLOBAL),
            new HotkeyDefinition(GLFW_KEY_ESCAPE, HotkeyAction.SHOW_UI, HotkeyContext.GLOBAL),
            new HotkeyDefinition(GLFW_KEY_F, HotkeyAction.TOGGLE_UI, HotkeyContext.GLOBAL),
            new HotkeyDefinition(GLFW_KEY_F3, HotkeyAction.TOGGLE_DEBUG, HotkeyContext.GLOBAL),
            new HotkeyDefinition(GLFW_KEY_SPACE, HotkeyAction.TOGGLE_PAUSE, HotkeyContext.SIMULATION),
            new HotkeyDefinition(GLFW_KEY_R, HotkeyAction.RESET_PARTICLES, HotkeyContext.SIMULATION),
            new HotkeyDefinition(GLFW_KEY_RIGHT, HotkeyAction.SIMULATION_STEP_FORWARD, HotkeyContext.SIMULATION));

    private AppHotkeys() {
    }

    public static List<HotkeyDefinition> defaultHotkeys() {
        return DEFAULT_HOTKEYS;
    }

    public static void register(HotkeyManager hotkeyManager, ParticleSimulatorApp app) {
        for (HotkeyDefinition hotkey : DEFAULT_HOTKEYS) {
            hotkeyManager.bind(hotkey.key(), hotkey.action(), hotkey.context());
        }

        hotkeyManager.on(
                HotkeyAction.TOGGLE_FULLSCREEN, app.getWindow()::toggleFullscreen);
        hotkeyManager.on(
                HotkeyAction.TOGGLE_PAUSE, app.getUi()::togglePause);
        hotkeyManager.on(
                HotkeyAction.TOGGLE_UI, app.getUi()::toggleUi);
        hotkeyManager.on(
                HotkeyAction.TOGGLE_DEBUG, app.getUi()::toggleDebug);
        hotkeyManager.on(
                HotkeyAction.SHOW_UI, app.getUi()::show);
        hotkeyManager.on(
                HotkeyAction.RESET_PARTICLES, app.getParticles()::reset);
        hotkeyManager.on(
                HotkeyAction.SIMULATION_STEP_FORWARD, app.getParticles()::step);
    }
}
