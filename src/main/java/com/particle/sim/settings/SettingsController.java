package com.particle.sim.settings;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

import java.nio.file.Files;
import java.nio.file.Path;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.ui.SimulationUI;

public class SettingsController {
    private static final double SETTINGS_SAVE_DEBOUNCE_SECONDS = 0.5;
    private final Path settingsPath = AppSettings.defaultPath();

    private final DebouncedSettingsSaver settingsSaver;
    private final GpuParticleSystem particles;
    private final CameraController camera;
    private final SimulationUI ui;

    public SettingsController(GpuParticleSystem particles, CameraController camera, SimulationUI ui) {
        this.particles = particles;
        this.camera = camera;
        this.ui = ui;
        this.settingsSaver = new DebouncedSettingsSaver(SETTINGS_SAVE_DEBOUNCE_SECONDS, this::saveNow);
    }

    public void load() {
        if (Files.exists(settingsPath)) {
            AppSettings.load(settingsPath).applyTo(particles, camera, ui);
        }
    }

    public void onSettingsChanged() {
        settingsSaver.requestSave(glfwGetTime());
    }

    public void onResetRequested() {
        AppSettings.defaults().applySimulationTo(particles, camera, ui);
        particles.reset();
        onSettingsChanged();
    }

    public void savePresetTo(Path path) {
        Path target = AppSettings.ensurePresetExtension(path);
        String presetName = AppSettings.presetNameFromPath(target);
        AppSettings.capture(particles, camera, ui).savePreset(target, presetName);
    }

    public void loadPresetFrom(Path path) {
        if (!Files.isRegularFile(path)) {
            return;
        }

        AppSettings.load(path).applyTo(particles, camera, ui);
        onSettingsChanged();
    }

    public void tick(double now) {
        settingsSaver.saveIfDue(now);
    }

    public void flush() {
        settingsSaver.saveIfDue(Double.POSITIVE_INFINITY);
    }

    private void saveNow() {
        AppSettings.capture(particles, camera, ui).save(settingsPath);
    }
}
