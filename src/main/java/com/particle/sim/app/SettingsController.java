package com.particle.sim.app;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.ParticleSystem;
import com.particle.sim.settings.AppSettings;
import com.particle.sim.settings.DebouncedSettingsSaver;
import com.particle.sim.settings.SettingsActions;
import com.particle.sim.ui.SimulationView;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.DoubleSupplier;

/** Maps persisted settings to runtime components at the application boundary. */
public final class SettingsController implements SettingsActions {
    private static final double SETTINGS_SAVE_DEBOUNCE_SECONDS = 0.5;
    private final Path settingsPath = AppSettings.defaultPath();
    private final DebouncedSettingsSaver settingsSaver;
    private final ParticleSystem particles;
    private final CameraController camera;
    private final SimulationView ui;
    private final DoubleSupplier timeSource;

    public SettingsController(
            ParticleSystem particles, CameraController camera, SimulationView ui) {
        this(particles, camera, ui, () -> System.nanoTime() / 1_000_000_000.0);
    }

    SettingsController(
            ParticleSystem particles,
            CameraController camera,
            SimulationView ui,
            DoubleSupplier timeSource) {
        this.particles = particles;
        this.camera = camera;
        this.ui = ui;
        this.timeSource = timeSource;
        settingsSaver = new DebouncedSettingsSaver(SETTINGS_SAVE_DEBOUNCE_SECONDS, this::saveNow);
    }

    public void load() {
        if (Files.exists(settingsPath)) {
            apply(AppSettings.load(settingsPath), true);
        }
    }

    @Override
    public void onSettingsChanged() {
        settingsSaver.requestSave(timeSource.getAsDouble());
    }

    @Override
    public void onResetRequested() {
        apply(AppSettings.defaults(), false);
        particles.reset();
        onSettingsChanged();
    }

    public void savePresetTo(Path path) {
        Path target = AppSettings.ensurePresetExtension(path);
        String presetName = AppSettings.presetNameFromPath(target);
        capture().savePreset(target, presetName);
    }

    public void loadPresetFrom(Path path) {
        if (!Files.isRegularFile(path)) {
            return;
        }
        apply(AppSettings.load(path), true);
        onSettingsChanged();
    }

    public void tick() {
        settingsSaver.saveIfDue(timeSource.getAsDouble());
    }

    public void flush() {
        settingsSaver.saveIfDue(Double.POSITIVE_INFINITY);
    }

    private void saveNow() {
        capture().save(settingsPath);
    }

    private AppSettings capture() {
        return AppSettingsMapper.capture(particles, camera, ui);
    }

    private void apply(AppSettings settings, boolean includeAttractionMatrix) {
        if (includeAttractionMatrix) {
            AppSettingsMapper.applyTo(settings, particles, camera, ui);
        } else {
            AppSettingsMapper.applySimulationTo(settings, particles, camera, ui);
        }
    }
}
