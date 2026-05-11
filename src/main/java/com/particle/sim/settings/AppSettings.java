package com.particle.sim.settings;

import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.camera.CameraController;
import com.particle.sim.ui.SimulationUi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppSettings {
    public static final int VERSION = 1;

    private int particleCount = SimulationDefaults.PARTICLE_COUNT;
    private float pointSize = SimulationDefaults.POINT_SIZE;
    private float bounds = SimulationDefaults.BOUNDS;
    private float forceFactor = SimulationDefaults.FORCE_FACTOR;
    private float velocityDamping = SimulationDefaults.VELOCITY_DAMPING;
    private float interactionRange = SimulationDefaults.INTERACTION_RANGE;
    private float repulsionRadius = SimulationDefaults.REPULSION_RADIUS;
    private float maxVelocity = SimulationDefaults.MAX_VELOCITY;
    private float boundaryBounce = SimulationDefaults.BOUNDARY_BOUNCE;
    private boolean toroidalWrap = SimulationDefaults.TOROIDAL_WRAP;
    private int groupCount = SimulationDefaults.GROUP_COUNT;
    private ColorMode colorMode = SimulationDefaults.COLOR_MODE;
    private SpawnMode spawnMode = SimulationDefaults.SPAWN_MODE;
    private float[] attractionMatrix;

    private float cameraSensitivity = SimulationDefaults.CAMERA_SENSITIVITY;
    private boolean paused = SimulationDefaults.PAUSED;
    private float matrixEditStep = SimulationDefaults.MATRIX_EDIT_STEP;
    private int customSpawnAmount = SimulationDefaults.CUSTOM_SPAWN_AMOUNT;

    public AppSettings() {
        attractionMatrix = new float[SimulationDefaults.MAX_GROUP_COUNT * SimulationDefaults.MAX_GROUP_COUNT];
    }

    public static Path defaultPath() {
        return Path.of(System.getProperty("user.home"), ".particle-simulator", "settings.properties");
    }

    public static AppSettings defaults() {
        return new AppSettings();
    }

    public static AppSettings load(Path path) {
        AppSettings settings = defaults();
        if (!Files.exists(path)) {
            return settings;
        }

        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(path)) {
            properties.load(stream);
        } catch (IOException e) {
            return settings;
        }

        settings.particleCount = intProperty(properties, "particleCount", settings.particleCount);
        settings.pointSize = floatProperty(properties, "pointSize", settings.pointSize);
        settings.bounds = floatProperty(properties, "bounds", settings.bounds);
        settings.forceFactor = floatProperty(properties, "forceFactor", settings.forceFactor);
        settings.velocityDamping = floatProperty(properties, "velocityDamping", settings.velocityDamping);
        settings.interactionRange = floatProperty(properties, "interactionRange", settings.interactionRange);
        settings.repulsionRadius = floatProperty(properties, "repulsionRadius", settings.repulsionRadius);
        settings.maxVelocity = floatProperty(properties, "maxVelocity", settings.maxVelocity);
        settings.boundaryBounce = floatProperty(properties, "boundaryBounce", settings.boundaryBounce);
        settings.toroidalWrap = booleanProperty(properties, "toroidalWrap", settings.toroidalWrap);
        settings.groupCount = intProperty(properties, "groupCount", settings.groupCount);
        settings.groupCount = Math.max(1, Math.min(SimulationDefaults.MAX_GROUP_COUNT, settings.groupCount));
        settings.colorMode = enumProperty(properties, "colorMode", ColorMode.class, settings.colorMode);
        settings.spawnMode = enumProperty(properties, "spawnMode", SpawnMode.class, settings.spawnMode);
        settings.cameraSensitivity = floatProperty(properties, "cameraSensitivity", settings.cameraSensitivity);
        settings.paused = booleanProperty(properties, "paused", settings.paused);
        settings.matrixEditStep = floatProperty(properties, "matrixEditStep", settings.matrixEditStep);
        settings.customSpawnAmount = intProperty(properties, "customSpawnAmount", settings.customSpawnAmount);

        int attractionValueCount = settings.groupCount * settings.groupCount;
        for (int i = 0; i < attractionValueCount; i++) {
            settings.attractionMatrix[i] = floatProperty(properties, "attraction." + i, settings.attractionMatrix[i]);
        }

        settings.sanitize();
        return settings;
    }

    public void save(Path path) {
        Properties properties = new Properties();
        properties.setProperty("version", Integer.toString(VERSION));
        properties.setProperty("particleCount", Integer.toString(particleCount));
        properties.setProperty("pointSize", Float.toString(pointSize));
        properties.setProperty("bounds", Float.toString(bounds));
        properties.setProperty("forceFactor", Float.toString(forceFactor));
        properties.setProperty("velocityDamping", Float.toString(velocityDamping));
        properties.setProperty("interactionRange", Float.toString(interactionRange));
        properties.setProperty("repulsionRadius", Float.toString(repulsionRadius));
        properties.setProperty("maxVelocity", Float.toString(maxVelocity));
        properties.setProperty("boundaryBounce", Float.toString(boundaryBounce));
        properties.setProperty("toroidalWrap", Boolean.toString(toroidalWrap));
        properties.setProperty("groupCount", Integer.toString(groupCount));
        properties.setProperty("colorMode", colorMode.name());
        properties.setProperty("spawnMode", spawnMode.name());
        properties.setProperty("cameraSensitivity", Float.toString(cameraSensitivity));
        properties.setProperty("paused", Boolean.toString(paused));
        properties.setProperty("matrixEditStep", Float.toString(matrixEditStep));
        properties.setProperty("customSpawnAmount", Integer.toString(customSpawnAmount));

        int attractionValueCount = groupCount * groupCount;
        for (int i = 0; i < attractionValueCount; i++) {
            properties.setProperty("attraction." + i, Float.toString(attractionMatrix[i]));
        }

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream stream = Files.newOutputStream(path)) {
                properties.store(stream, "3D Particle Simulator settings");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save settings to " + path, e);
        }
    }

    public void applyTo(GpuParticleSystem particles, CameraController camera, SimulationUi ui) {
        applySimulationTo(particles, camera, ui);
        particles.setAttractionMatrix(attractionMatrix);
    }

    public void applySimulationTo(GpuParticleSystem particles, CameraController camera, SimulationUi ui) {
        sanitize();
        particles.setParticleCount(particleCount);
        particles.pointSize(pointSize);
        particles.bounds(bounds);
        particles.forceFactor(forceFactor);
        particles.velocityDamping(velocityDamping);
        particles.interactionRange(interactionRange);
        particles.repulsionRadius(repulsionRadius);
        particles.maxVelocity(maxVelocity);
        particles.boundaryBounce(boundaryBounce);
        particles.toroidalWrap(toroidalWrap);
        particles.groupCount(groupCount);
        particles.colorMode(colorMode);
        particles.spawnMode(spawnMode);

        camera.setSensitivity(cameraSensitivity);
        ui.setPaused(paused);
        ui.setMatrixEditStep(matrixEditStep);
        ui.setCustomSpawnAmount(customSpawnAmount);
    }

    public static AppSettings capture(GpuParticleSystem particles, CameraController camera, SimulationUi ui) {
        AppSettings settings = defaults();
        settings.particleCount = particles.particleCount();
        settings.pointSize = particles.pointSize();
        settings.bounds = particles.bounds();
        settings.forceFactor = particles.forceFactor();
        settings.velocityDamping = particles.velocityDamping();
        settings.interactionRange = particles.interactionRange();
        settings.repulsionRadius = particles.repulsionRadius();
        settings.maxVelocity = particles.maxVelocity();
        settings.boundaryBounce = particles.boundaryBounce();
        settings.toroidalWrap = particles.toroidalWrap();
        settings.groupCount = particles.groupCount();
        settings.colorMode = particles.colorMode();
        settings.spawnMode = particles.spawnMode();
        int attractionIndex = 0;
        for (int row = 0; row < settings.groupCount; row++) {
            for (int column = 0; column < settings.groupCount; column++) {
                settings.attractionMatrix[attractionIndex] = particles.attraction(row, column);
                attractionIndex++;
            }
        }

        settings.cameraSensitivity = camera.getSensitivity();
        settings.paused = ui.isPaused();
        settings.matrixEditStep = ui.matrixEditStep();
        settings.customSpawnAmount = ui.customSpawnAmount();
        settings.sanitize();
        return settings;
    }

    private void sanitize() {
        particleCount = Math.max(0, Math.min(SimulationDefaults.MAX_PARTICLE_COUNT, particleCount));
        pointSize = clamp(pointSize, 1.0f, 8.0f);
        bounds = clamp(bounds, 2.0f, 10.0f);
        forceFactor = clamp(forceFactor, 0.0f, 10.0f);
        velocityDamping = clamp(velocityDamping, 0.85f, 1.0f);
        interactionRange = clamp(interactionRange, 0.2f, 3.0f);
        repulsionRadius = clamp(repulsionRadius, 0.02f, 0.95f);
        maxVelocity = clamp(maxVelocity, 0.5f, 16.0f);
        boundaryBounce = clamp(boundaryBounce, 0.0f, 1.0f);
        groupCount = Math.max(1, Math.min(SimulationDefaults.MAX_GROUP_COUNT, groupCount));
        cameraSensitivity = Math.max(0.0001f, cameraSensitivity);
        matrixEditStep = clamp(matrixEditStep, 0.01f, 0.5f);
        customSpawnAmount = Math.max(0, customSpawnAmount);

        int attractionValueCount = groupCount * groupCount;
        for (int i = 0; i < attractionValueCount; i++) {
            attractionMatrix[i] = clamp(attractionMatrix[i], -1.0f, 1.0f);
        }
    }

    private static int intProperty(Properties properties, String key, int fallback) {
        try {
            return Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static float floatProperty(Properties properties, String key, float fallback) {
        try {
            return Float.parseFloat(properties.getProperty(key, Float.toString(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean booleanProperty(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static <E extends Enum<E>> E enumProperty(Properties properties, String key, Class<E> type, E fallback) {
        try {
            return Enum.valueOf(type, properties.getProperty(key, fallback.name()));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
