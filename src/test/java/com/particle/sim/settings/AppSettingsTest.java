package com.particle.sim.settings;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.FourDViewState;
import com.particle.sim.particles.FourDVisualizationMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.SimulationDimension;
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
    void roundTripsFourDimensionalSimulationAndCompleteViewState() throws Exception {
        GpuParticleSystem particles = new GpuParticleSystem();
        particles.simulationDimension(SimulationDimension.FOUR_D);
        particles.spawnMode(SpawnMode.SHELL);
        particles.fourDVisualizationMode(FourDVisualizationMode.SLICE);
        particles.fourDXwAngle(0.42);
        particles.fourDYwAngle(-0.27);
        particles.fourDZwAngle(0.18);
        particles.fourDXwAutoSpeed(0.31);
        particles.fourDYwAutoSpeed(-0.22);
        particles.fourDZwAutoSpeed(0.13);
        particles.fourDXwAutoEnabled(false);
        particles.fourDYwAutoEnabled(true);
        particles.fourDZwAutoEnabled(true);
        particles.fourDViewMotionPaused(true);
        particles.fourDPerspectiveDistance(14.0);
        particles.fourDSlice(0.75, 1.5, 0.3);
        particles.fourDSliceSweepEnabled(true);
        particles.fourDSliceSweepSpeed(1.75);
        particles.fourDColorRange(3.25);
        FourDViewState expected = particles.fourDViewState();

        Path settingsFile = tempDir.resolve("four-dimensional.3dps");
        AppSettings.capture(particles, new CameraController(), new SimulationUI()).save(settingsFile);

        GpuParticleSystem loaded = new GpuParticleSystem();
        AppSettings.load(settingsFile).applySimulationTo(loaded, new CameraController(), new SimulationUI());
        FourDViewState actual = loaded.fourDViewState();

        assertEquals(SimulationDimension.FOUR_D, loaded.simulationDimension());
        assertEquals(SpawnMode.SHELL, loaded.spawnMode());
        assertEquals(expected.configuration(), actual.configuration());
        assertEquals(expected.xwAngle(), actual.xwAngle(), EPSILON);
        assertEquals(expected.ywAngle(), actual.ywAngle(), EPSILON);
        assertEquals(expected.zwAngle(), actual.zwAngle(), EPSILON);
        assertEquals(expected.xwAutoSpeed(), actual.xwAutoSpeed(), EPSILON);
        assertEquals(expected.ywAutoSpeed(), actual.ywAutoSpeed(), EPSILON);
        assertEquals(expected.zwAutoSpeed(), actual.zwAutoSpeed(), EPSILON);
        assertEquals(expected.xwAutoEnabled(), actual.xwAutoEnabled());
        assertEquals(expected.ywAutoEnabled(), actual.ywAutoEnabled());
        assertEquals(expected.zwAutoEnabled(), actual.zwAutoEnabled());
        assertEquals(expected.motionPaused(), actual.motionPaused());
        assertEquals(expected.sliceSweepEnabled(), actual.sliceSweepEnabled());
        assertEquals(expected.sliceSweepSpeed(), actual.sliceSweepSpeed(), EPSILON);
        assertTrue(java.nio.file.Files.readString(settingsFile).contains("version=2"));
    }

    @Test
    void oldPresetWithoutFourDimensionalKeysLoadsAsThreeDimensionalDefaults() throws Exception {
        Path preset = tempDir.resolve("old.3dps");
        java.nio.file.Files.writeString(preset,
                "version=1\nparticleCount=42\nspawnMode=SPIRAL\nsimulationDimension=FOUR_D\n"
                        + "fourDVisualizationMode=W_COLOR\n");

        GpuParticleSystem loaded = new GpuParticleSystem();
        AppSettings.load(preset).applySimulationTo(loaded, new CameraController(), new SimulationUI());

        assertEquals(42, loaded.particleCount());
        assertEquals(SimulationDimension.THREE_D, loaded.simulationDimension());
        assertEquals(SpawnMode.SPIRAL, loaded.spawnMode());
        assertEquals(FourDViewState.defaults(), loaded.fourDViewState());
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
