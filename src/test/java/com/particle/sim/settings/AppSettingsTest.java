package com.particle.sim.settings;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.camera.CameraController;
import com.particle.sim.ui.SimulationUi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AppSettingsTest {
    private static final float EPSILON = 0.0001f;

    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsCurrentApplicationSettings() {
        GpuParticleSystem particles = new GpuParticleSystem();
        CameraController camera = new CameraController();
        SimulationUi ui = new SimulationUi();
        Path settingsFile = tempDir.resolve("settings.properties");

        particles.setParticleCount(1234);
        particles.pointSize(4.5f);
        particles.bounds(8.0f);
        particles.forceFactor(3.0f);
        particles.velocityDamping(0.9f);
        particles.interactionRange(1.5f);
        particles.repulsionRadius(0.4f);
        particles.maxVelocity(7.0f);
        particles.boundaryBounce(0.25f);
        particles.toroidalWrap(true);
        particles.groupCount(8);
        particles.colorMode(ColorMode.DENSITY);
        particles.spawnMode(SpawnMode.SPIRAL);
        particles.zeroAttractionMatrix();
        particles.attraction(2, 3, 0.75f);
        camera.setSensitivity(0.004f);
        ui.setPaused(true);
        ui.setMatrixEditStep(0.2f);
        ui.setCustomSpawnAmount(42);

        AppSettings.capture(particles, camera, ui).save(settingsFile);

        GpuParticleSystem loadedParticles = new GpuParticleSystem();
        CameraController loadedCamera = new CameraController();
        SimulationUi loadedUi = new SimulationUi();
        AppSettings.load(settingsFile).applyTo(loadedParticles, loadedCamera, loadedUi);

        assertEquals(1234, loadedParticles.particleCount());
        assertEquals(4.5f, loadedParticles.pointSize(), EPSILON);
        assertEquals(8.0f, loadedParticles.bounds(), EPSILON);
        assertEquals(3.0f, loadedParticles.forceFactor(), EPSILON);
        assertEquals(0.9f, loadedParticles.velocityDamping(), EPSILON);
        assertEquals(1.5f, loadedParticles.interactionRange(), EPSILON);
        assertEquals(0.4f, loadedParticles.repulsionRadius(), EPSILON);
        assertEquals(7.0f, loadedParticles.maxVelocity(), EPSILON);
        assertEquals(0.25f, loadedParticles.boundaryBounce(), EPSILON);
        assertEquals(true, loadedParticles.toroidalWrap());
        assertEquals(8, loadedParticles.groupCount());
        assertEquals(ColorMode.DENSITY, loadedParticles.colorMode());
        assertEquals(SpawnMode.SPIRAL, loadedParticles.spawnMode());
        assertEquals(0.75f, loadedParticles.attraction(2, 3), EPSILON);
        assertEquals(0.004f, loadedCamera.getSensitivity(), EPSILON);
        assertEquals(true, loadedUi.isPaused());
        assertEquals(0.2f, loadedUi.matrixEditStep(), EPSILON);
        assertEquals(42, loadedUi.customSpawnAmount());
    }

    @Test
    void defaultsResetSettingsBackToKnownValues() {
        GpuParticleSystem particles = new GpuParticleSystem();
        CameraController camera = new CameraController();
        SimulationUi ui = new SimulationUi();

        particles.setParticleCount(10);
        particles.pointSize(8.0f);
        particles.colorMode(ColorMode.DENSITY);
        particles.groupCount(12);
        particles.spawnMode(SpawnMode.GRID);
        particles.toroidalWrap(true);
        camera.setSensitivity(0.007f);
        ui.setPaused(true);
        ui.setMatrixEditStep(0.4f);
        ui.setCustomSpawnAmount(99);

        AppSettings.defaults().applySimulationTo(particles, camera, ui);

        assertEquals(SimulationDefaults.PARTICLE_COUNT, particles.particleCount());
        assertEquals(SimulationDefaults.POINT_SIZE, particles.pointSize(), EPSILON);
        assertEquals(SimulationDefaults.COLOR_MODE, particles.colorMode());
        assertEquals(SimulationDefaults.GROUP_COUNT, particles.groupCount());
        assertEquals(SimulationDefaults.SPAWN_MODE, particles.spawnMode());
        assertFalse(particles.toroidalWrap());
        assertEquals(SimulationDefaults.CAMERA_SENSITIVITY, camera.getSensitivity(), EPSILON);
        assertFalse(ui.isPaused());
        assertEquals(SimulationDefaults.MATRIX_EDIT_STEP, ui.matrixEditStep(), EPSILON);
        assertEquals(SimulationDefaults.CUSTOM_SPAWN_AMOUNT, ui.customSpawnAmount());
    }

    @Test
    void simulationDefaultsPreserveAttractionMatrix() {
        GpuParticleSystem particles = new GpuParticleSystem();
        CameraController camera = new CameraController();
        SimulationUi ui = new SimulationUi();

        particles.zeroAttractionMatrix();
        particles.attraction(1, 4, 0.65f);

        AppSettings.defaults().applySimulationTo(particles, camera, ui);

        assertEquals(0.65f, particles.attraction(1, 4), EPSILON);
    }

    @Test
    void clampsLoadedGroupCountToSupportedRange() throws Exception {
        Path settingsFile = tempDir.resolve("settings.properties");
        java.nio.file.Files.writeString(settingsFile, "groupCount=99\n");

        GpuParticleSystem particles = new GpuParticleSystem();
        AppSettings.load(settingsFile).applySimulationTo(particles, new CameraController(), new SimulationUi());

        assertEquals(SimulationDefaults.MAX_GROUP_COUNT, particles.groupCount());
    }
}
