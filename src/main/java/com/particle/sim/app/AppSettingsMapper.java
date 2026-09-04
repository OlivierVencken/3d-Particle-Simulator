package com.particle.sim.app;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.ParticleSimulationConfig;
import com.particle.sim.particles.ParticleSystem;
import com.particle.sim.settings.AppSettings;
import com.particle.sim.ui.SimulationView;

/** Maps toolkit-neutral persisted settings to concrete application components. */
public final class AppSettingsMapper {
    private AppSettingsMapper() {}

    public static AppSettings capture(
            ParticleSystem particles, CameraController camera, SimulationView ui) {
        ParticleSimulationConfig config = particles.config();
        int groupCount = config.groupCount();
        float[] attraction = new float[groupCount * groupCount];
        int index = 0;
        for (int row = 0; row < groupCount; row++) {
            for (int column = 0; column < groupCount; column++) {
                attraction[index++] = particles.attraction(row, column);
            }
        }
        return AppSettings.capture(
                config,
                attraction,
                new AppSettings.CameraSettings(camera.getSensitivity(), camera.getFlySpeed()),
                new AppSettings.InterfaceSettings(
                        ui.isPaused(), ui.fpsCap(), ui.matrixEditStep(), ui.customSpawnAmount()));
    }

    public static void applyTo(
            AppSettings settings,
            ParticleSystem particles,
            CameraController camera,
            SimulationView ui) {
        applySimulationTo(settings, particles, camera, ui);
        particles.attractionMatrix(settings.attractionMatrix());
    }

    public static void applySimulationTo(
            AppSettings settings,
            ParticleSystem particles,
            CameraController camera,
            SimulationView ui) {
        particles.applyConfig(settings.particleConfig());
        AppSettings.CameraSettings cameraSettings = settings.camera();
        camera.setSensitivity(cameraSettings.sensitivity());
        camera.setFlySpeed(cameraSettings.flySpeed());
        AppSettings.InterfaceSettings interfaceSettings = settings.interfaceSettings();
        ui.setPaused(interfaceSettings.paused());
        ui.setFpsCap(interfaceSettings.fpsCap());
        ui.setMatrixEditStep(interfaceSettings.matrixEditStep());
        ui.setCustomSpawnAmount(interfaceSettings.customSpawnAmount());
    }
}
