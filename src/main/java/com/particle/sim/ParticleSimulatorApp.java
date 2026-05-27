package com.particle.sim;

import com.particle.sim.camera.CameraController;
import com.particle.sim.input.AppHotkeys;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.AppSettings;
import com.particle.sim.settings.DebouncedSettingsSaver;
import com.particle.sim.ui.ImguiLayer;
import com.particle.sim.ui.SimulationUi;
import com.particle.sim.window.WindowManager;
import org.lwjgl.opengl.GL;

import java.nio.file.Files;
import java.nio.file.Path;

import static imgui.ImGui.getIO;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL43C.GL_BLEND;
import static org.lwjgl.opengl.GL43C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL43C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL43C.GL_PROGRAM_POINT_SIZE;
import static org.lwjgl.opengl.GL43C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL43C.glBlendFunc;
import static org.lwjgl.opengl.GL43C.glEnable;

public final class ParticleSimulatorApp {
    private static final String WINDOW_TITLE = "3D Particle Simulator";
    private static final double SETTINGS_SAVE_DEBOUNCE_SECONDS = 0.5;

    private final WindowManager window = new WindowManager(WINDOW_TITLE);
    private final ImguiLayer imgui = new ImguiLayer();
    private final CameraController camera = new CameraController();
    private final GpuParticleSystem particles = new GpuParticleSystem();
    private final SimulationUi ui = new SimulationUi();
    private final AppHotkeys hotkeys = new AppHotkeys(
            window::toggleFullscreen,
            () -> !camera.isMouseCaptured(),
            window::requestClose,
            () -> !getIO().getWantCaptureKeyboard(),
            this::togglePause,
            particles::reset);
    private final Path settingsPath = AppSettings.defaultPath();
    private final DebouncedSettingsSaver settingsSaver = new DebouncedSettingsSaver(
            SETTINGS_SAVE_DEBOUNCE_SECONDS,
            this::saveSettings);

    public static void main(String[] args) {
        new ParticleSimulatorApp().run();
    }

    private void run() {
        window.init();
        initOpenGl();
        imgui.init(window.handle());
        particles.init();
        initSettings();

        new ApplicationRuntime(
                window,
                imgui,
                hotkeys,
                camera,
                particles,
                ui,
                this::saveSettingsIfDue)
                .run();

        dispose();
    }

    private void initOpenGl() {
        GL.createCapabilities();
        glEnable(GL_PROGRAM_POINT_SIZE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void initSettings() {
        ui.onSettingsChanged(this::requestSettingsSave);
        ui.onResetSettings(this::resetSettings);
        ui.onExitApplication(window::requestClose);

        if (Files.exists(settingsPath)) {
            AppSettings.load(settingsPath).applyTo(particles, camera, ui);
        }
    }

    private void saveSettings() {
        AppSettings.capture(particles, camera, ui).save(settingsPath);
    }

    private void requestSettingsSave() {
        settingsSaver.requestSave(glfwGetTime());
    }

    private void saveSettingsIfDue(double now) {
        settingsSaver.saveIfDue(now);
    }

    private void resetSettings() {
        AppSettings.defaults().applySimulationTo(particles, camera, ui);
        particles.reset();
        requestSettingsSave();
    }

    private void togglePause() {
        ui.setPaused(!ui.isPaused());
        requestSettingsSave();
    }

    private void dispose() {
        settingsSaver.flush();
        particles.dispose();
        imgui.dispose();
        window.dispose();
    }
}
