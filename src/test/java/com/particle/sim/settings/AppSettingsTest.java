package com.particle.sim.settings;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.camera.CameraController;
import com.particle.sim.ui.SimulationUI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppSettingsTest {
    private static final float EPSILON = 0.0001f;

    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsCurrentApplicationSettings() {
        GpuParticleSystem particles = new GpuParticleSystem();
        CameraController camera = new CameraController();
        SimulationUI ui = new SimulationUI();
        Path settingsFile = tempDir.resolve("settings.properties");

        particles.setParticleCount(1234);
        particles.pointSize(4.5f);
        particles.fixedParticleScreenSize(true);
        particles.bounds(8.0f);
        particles.forceFactor(3.0f);
        particles.velocityDamping(0.9f);
        particles.interactionRange(1.5f);
        particles.repulsionRadius(0.4f);
        particles.maxVelocity(7.0f);
        particles.boundaryBounce(0.25f);
        particles.toroidalWrap(true);
        particles.densityRegulationEnabled(true);
        particles.densityLimit(200.0f);
        particles.distanceMetric(DistanceMetric.MANHATTAN);
        particles.groupCount(8);
        particles.colorMode(ColorMode.DENSITY);
        particles.effectEnabled(EffectMode.GLOW, true);
        particles.effectEnabled(EffectMode.TRAILS, true);
        particles.glowBlurPasses(24);
        particles.glowStrength(3.5f);
        particles.glowRadius(6.25f);
        particles.glowFalloff(1.2f);
        particles.trailLength(20);
        particles.trailThickness(2.1f);
        particles.spawnMode(SpawnMode.SPIRAL);
        particles.zeroAttractionMatrix();
        particles.attraction(2, 3, 0.75f);
        camera.setSensitivity(0.004f);
        camera.setFlySpeed(12.0f);
        ui.setPaused(true);
        ui.setFpsCap(240);
        ui.setMatrixEditStep(0.2f);
        ui.setCustomSpawnAmount(42);

        AppSettings.capture(particles, camera, ui).save(settingsFile);

        GpuParticleSystem loadedParticles = new GpuParticleSystem();
        CameraController loadedCamera = new CameraController();
        SimulationUI loadedUi = new SimulationUI();
        AppSettings.load(settingsFile).applyTo(loadedParticles, loadedCamera, loadedUi);

        assertEquals(1234, loadedParticles.particleCount());
        assertEquals(4.5f, loadedParticles.pointSize(), EPSILON);
        assertEquals(true, loadedParticles.fixedParticleScreenSize());
        assertEquals(8.0f, loadedParticles.bounds(), EPSILON);
        assertEquals(3.0f, loadedParticles.forceFactor(), EPSILON);
        assertEquals(0.9f, loadedParticles.velocityDamping(), EPSILON);
        assertEquals(1.5f, loadedParticles.interactionRange(), EPSILON);
        assertEquals(0.4f, loadedParticles.repulsionRadius(), EPSILON);
        assertEquals(7.0f, loadedParticles.maxVelocity(), EPSILON);
        assertEquals(0.25f, loadedParticles.boundaryBounce(), EPSILON);
        assertEquals(true, loadedParticles.toroidalWrap());
        assertEquals(true, loadedParticles.densityRegulationEnabled());
        assertEquals(200.0f, loadedParticles.densityLimit(), EPSILON);
        assertEquals(DistanceMetric.MANHATTAN, loadedParticles.distanceMetric());
        assertEquals(8, loadedParticles.groupCount());
        assertEquals(ColorMode.DENSITY, loadedParticles.colorMode());
        assertTrue(loadedParticles.effectEnabled(EffectMode.GLOW));
        assertTrue(loadedParticles.effectEnabled(EffectMode.TRAILS));
        assertEquals(24, loadedParticles.glowBlurPasses());
        assertEquals(3.5f, loadedParticles.glowStrength(), EPSILON);
        assertEquals(6.25f, loadedParticles.glowRadius(), EPSILON);
        assertEquals(1.2f, loadedParticles.glowFalloff(), EPSILON);
        assertEquals(20, loadedParticles.trailLength());
        assertEquals(2.1f, loadedParticles.trailThickness(), EPSILON);
        assertEquals(SpawnMode.SPIRAL, loadedParticles.spawnMode());
        assertEquals(0.75f, loadedParticles.attraction(2, 3), EPSILON);
        assertEquals(0.004f, loadedCamera.getSensitivity(), EPSILON);
        assertEquals(12.0f, loadedCamera.getFlySpeed(), EPSILON);
        assertEquals(true, loadedUi.isPaused());
        assertEquals(240, loadedUi.fpsCap());
        assertEquals(0.2f, loadedUi.matrixEditStep(), EPSILON);
        assertEquals(42, loadedUi.customSpawnAmount());
    }

    @Test
    void defaultsResetSettingsBackToKnownValues() {
        GpuParticleSystem particles = new GpuParticleSystem();
        CameraController camera = new CameraController();
        SimulationUI ui = new SimulationUI();

        particles.setParticleCount(10);
        particles.pointSize(8.0f);
        particles.fixedParticleScreenSize(true);
        particles.colorMode(ColorMode.DENSITY);
        particles.effectEnabled(EffectMode.GLOW, true);
        particles.glowBlurPasses(24);
        particles.glowStrength(3.5f);
        particles.glowRadius(6.25f);
        particles.glowFalloff(1.2f);
        particles.trailLength(20);
        particles.trailThickness(2.1f);
        particles.groupCount(12);
        particles.spawnMode(SpawnMode.GRID);
        particles.toroidalWrap(true);
        particles.densityRegulationEnabled(true);
        particles.densityLimit(200.0f);
        particles.distanceMetric(DistanceMetric.MANHATTAN);
        camera.setSensitivity(0.007f);
        camera.setFlySpeed(14.0f);
        ui.setPaused(true);
        ui.setFpsCap(90);
        ui.setMatrixEditStep(0.4f);
        ui.setCustomSpawnAmount(99);

        AppSettings.defaults().applySimulationTo(particles, camera, ui);

        assertEquals(SimulationDefaults.PARTICLE_COUNT, particles.particleCount());
        assertEquals(SimulationDefaults.POINT_SIZE, particles.pointSize(), EPSILON);
        assertFalse(particles.fixedParticleScreenSize());
        assertEquals(SimulationDefaults.COLOR_MODE, particles.colorMode());
        assertTrue(particles.effectModes().isEmpty());
        assertFalse(particles.effectEnabled(EffectMode.GLOW));
        assertFalse(particles.effectEnabled(EffectMode.TRAILS));
        assertEquals(SimulationDefaults.GLOW_BLUR_PASSES, particles.glowBlurPasses());
        assertEquals(SimulationDefaults.GLOW_STRENGTH, particles.glowStrength(), EPSILON);
        assertEquals(SimulationDefaults.GLOW_RADIUS, particles.glowRadius(), EPSILON);
        assertEquals(SimulationDefaults.GLOW_FALLOFF, particles.glowFalloff(), EPSILON);
        assertEquals(SimulationDefaults.TRAIL_LENGTH, particles.trailLength());
        assertEquals(SimulationDefaults.TRAIL_THICKNESS, particles.trailThickness(), EPSILON);
        assertEquals(SimulationDefaults.GROUP_COUNT, particles.groupCount());
        assertEquals(SimulationDefaults.SPAWN_MODE, particles.spawnMode());
        assertFalse(particles.toroidalWrap());
        assertFalse(particles.densityRegulationEnabled());
        assertEquals(SimulationDefaults.DENSITY_LIMIT, particles.densityLimit(), EPSILON);
        assertEquals(SimulationDefaults.DISTANCE_METRIC, particles.distanceMetric());
        assertEquals(SimulationDefaults.CAMERA_SENSITIVITY, camera.getSensitivity(), EPSILON);
        assertEquals(SimulationDefaults.CAMERA_FLY_SPEED, camera.getFlySpeed(), EPSILON);
        assertFalse(ui.isPaused());
        assertEquals(SimulationDefaults.FPS_CAP, ui.fpsCap());
        assertEquals(SimulationDefaults.MATRIX_EDIT_STEP, ui.matrixEditStep(), EPSILON);
        assertEquals(SimulationDefaults.CUSTOM_SPAWN_AMOUNT, ui.customSpawnAmount());
    }

    @Test
    void simulationDefaultsPreserveAttractionMatrix() {
        GpuParticleSystem particles = new GpuParticleSystem();
        CameraController camera = new CameraController();
        SimulationUI ui = new SimulationUI();

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
        AppSettings.load(settingsFile).applySimulationTo(particles, new CameraController(), new SimulationUI());

        assertEquals(SimulationDefaults.MAX_GROUP_COUNT, particles.groupCount());
    }

    @Test
    void clampsLoadedFpsCapToSupportedRange() throws Exception {
        Path settingsFile = tempDir.resolve("settings.properties");
        java.nio.file.Files.writeString(settingsFile, "fpsCap=999\n");

        SimulationUI ui = new SimulationUI();
        AppSettings.load(settingsFile).applySimulationTo(new GpuParticleSystem(), new CameraController(), ui);

        assertEquals(SimulationDefaults.MAX_FPS_CAP, ui.fpsCap());
    }

    @Test
    void allowsUnlimitedLoadedFpsCap() throws Exception {
        Path settingsFile = tempDir.resolve("settings.properties");
        java.nio.file.Files.writeString(settingsFile, "fpsCap=0\n");

        SimulationUI ui = new SimulationUI();
        AppSettings.load(settingsFile).applySimulationTo(new GpuParticleSystem(), new CameraController(), ui);

        assertEquals(0, ui.fpsCap());
    }
}
