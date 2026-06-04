package com.particle.sim.input;

import com.particle.sim.ParticleSimulatorApp;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;

public final class AppHotkeys {

    private AppHotkeys() {
    }

    public static void register(HotkeyManager hotkeyManager, ParticleSimulatorApp app) {
        hotkeyManager.bind(
                GLFW_KEY_F11,
                HotkeyAction.TOGGLE_FULLSCREEN,
                HotkeyContext.GLOBAL);

        hotkeyManager.bind(
                GLFW_KEY_ESCAPE,
                HotkeyAction.SHOW_UI,
                HotkeyContext.GLOBAL);

        hotkeyManager.bind(
                GLFW_KEY_SPACE,
                HotkeyAction.TOGGLE_PAUSE,
                HotkeyContext.SIMULATION);

        hotkeyManager.bind(
                GLFW_KEY_R,
                HotkeyAction.RESET_PARTICLES,
                HotkeyContext.SIMULATION);

        hotkeyManager.on(
                HotkeyAction.TOGGLE_FULLSCREEN, app.getWindow()::toggleFullscreen);
        hotkeyManager.on(
                HotkeyAction.TOGGLE_PAUSE, app.getUi()::togglePause);
        hotkeyManager.on(
                HotkeyAction.SHOW_UI, app.getUi()::show);
        hotkeyManager.on(
                HotkeyAction.RESET_PARTICLES, app.getParticles()::reset);
    }
}
