package com.particle.sim.settings;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.ParticleSimulationConfig;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.ui.SimulationUi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppSettings {
    public static final int VERSION = 1;

    private final ParticleSimulationConfig particleConfig = ParticleSimulationConfig.defaults();
    private float[] attractionMatrix;

    private float cameraSensitivity = SimulationDefaults.CAMERA_SENSITIVITY;
    private boolean paused = SimulationDefaults.PAUSED;
    private int fpsCap = SimulationDefaults.FPS_CAP;
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

        ParticleSimulationConfig particleConfig = settings.particleConfig;
        particleConfig.particleCount(intProperty(properties, "particleCount", particleConfig.particleCount()));
        particleConfig.pointSize(floatProperty(properties, "pointSize", particleConfig.pointSize()));
        particleConfig.fixedParticleScreenSize(booleanProperty(properties, "fixedParticleScreenSize",
                particleConfig.fixedParticleScreenSize()));
        particleConfig.effectMode(enumProperty(properties, "effectMode", EffectMode.class, particleConfig.effectMode()));
        particleConfig.bounds(floatProperty(properties, "bounds", particleConfig.bounds()));
        particleConfig.forceFactor(floatProperty(properties, "forceFactor", particleConfig.forceFactor()));
        particleConfig.velocityDamping(floatProperty(properties, "velocityDamping", particleConfig.velocityDamping()));
        particleConfig.interactionRange(floatProperty(properties, "interactionRange", particleConfig.interactionRange()));
        particleConfig.repulsionRadius(floatProperty(properties, "repulsionRadius", particleConfig.repulsionRadius()));
        particleConfig.maxVelocity(floatProperty(properties, "maxVelocity", particleConfig.maxVelocity()));
        particleConfig.boundaryBounce(floatProperty(properties, "boundaryBounce", particleConfig.boundaryBounce()));
        particleConfig.toroidalWrap(booleanProperty(properties, "toroidalWrap", particleConfig.toroidalWrap()));
        particleConfig.densityRegulationEnabled(booleanProperty(properties, "densityRegulationEnabled",
                particleConfig.densityRegulationEnabled()));
        particleConfig.densityLimit(floatProperty(properties, "densityLimit", particleConfig.densityLimit()));
        particleConfig.groupCount(intProperty(properties, "groupCount", particleConfig.groupCount()));
        particleConfig.colorMode(enumProperty(properties, "colorMode", ColorMode.class, particleConfig.colorMode()));
        particleConfig.spawnMode(enumProperty(properties, "spawnMode", SpawnMode.class, particleConfig.spawnMode()));
        settings.cameraSensitivity = floatProperty(properties, "cameraSensitivity", settings.cameraSensitivity);
        settings.paused = booleanProperty(properties, "paused", settings.paused);
        settings.fpsCap = intProperty(properties, "fpsCap", settings.fpsCap);
        settings.matrixEditStep = floatProperty(properties, "matrixEditStep", settings.matrixEditStep);
        settings.customSpawnAmount = intProperty(properties, "customSpawnAmount", settings.customSpawnAmount);

        int attractionValueCount = settings.particleConfig.groupCount() * settings.particleConfig.groupCount();
        for (int i = 0; i < attractionValueCount; i++) {
            settings.attractionMatrix[i] = floatProperty(properties, "attraction." + i, settings.attractionMatrix[i]);
        }

        settings.sanitize();
        return settings;
    }

    public void save(Path path) {
        Properties properties = new Properties();
        properties.setProperty("version", Integer.toString(VERSION));
        properties.setProperty("particleCount", Integer.toString(particleConfig.particleCount()));
        properties.setProperty("pointSize", Float.toString(particleConfig.pointSize()));
        properties.setProperty("fixedParticleScreenSize", Boolean.toString(particleConfig.fixedParticleScreenSize()));
        properties.setProperty("effectMode", particleConfig.effectMode().name());
        properties.setProperty("bounds", Float.toString(particleConfig.bounds()));
        properties.setProperty("forceFactor", Float.toString(particleConfig.forceFactor()));
        properties.setProperty("velocityDamping", Float.toString(particleConfig.velocityDamping()));
        properties.setProperty("interactionRange", Float.toString(particleConfig.interactionRange()));
        properties.setProperty("repulsionRadius", Float.toString(particleConfig.repulsionRadius()));
        properties.setProperty("maxVelocity", Float.toString(particleConfig.maxVelocity()));
        properties.setProperty("boundaryBounce", Float.toString(particleConfig.boundaryBounce()));
        properties.setProperty("toroidalWrap", Boolean.toString(particleConfig.toroidalWrap()));
        properties.setProperty("densityRegulationEnabled",
                Boolean.toString(particleConfig.densityRegulationEnabled()));
        properties.setProperty("densityLimit", Float.toString(particleConfig.densityLimit()));
        properties.setProperty("groupCount", Integer.toString(particleConfig.groupCount()));
        properties.setProperty("colorMode", particleConfig.colorMode().name());
        properties.setProperty("spawnMode", particleConfig.spawnMode().name());
        properties.setProperty("cameraSensitivity", Float.toString(cameraSensitivity));
        properties.setProperty("paused", Boolean.toString(paused));
        properties.setProperty("fpsCap", Integer.toString(fpsCap));
        properties.setProperty("matrixEditStep", Float.toString(matrixEditStep));
        properties.setProperty("customSpawnAmount", Integer.toString(customSpawnAmount));

        int attractionValueCount = particleConfig.groupCount() * particleConfig.groupCount();
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
        particles.applyConfig(particleConfig);

        camera.setSensitivity(cameraSensitivity);
        ui.setPaused(paused);
        ui.setFpsCap(fpsCap);
        ui.setMatrixEditStep(matrixEditStep);
        ui.setCustomSpawnAmount(customSpawnAmount);
    }

    public static AppSettings capture(GpuParticleSystem particles, CameraController camera, SimulationUi ui) {
        AppSettings settings = defaults();
        settings.particleConfig.applyFrom(particles.config());
        int attractionIndex = 0;
        for (int row = 0; row < settings.particleConfig.groupCount(); row++) {
            for (int column = 0; column < settings.particleConfig.groupCount(); column++) {
                settings.attractionMatrix[attractionIndex] = particles.attraction(row, column);
                attractionIndex++;
            }
        }

        settings.cameraSensitivity = camera.getSensitivity();
        settings.paused = ui.isPaused();
        settings.fpsCap = ui.fpsCap();
        settings.matrixEditStep = ui.matrixEditStep();
        settings.customSpawnAmount = ui.customSpawnAmount();
        settings.sanitize();
        return settings;
    }

    private void sanitize() {
        particleConfig.sanitize();
        cameraSensitivity = Math.max(0.0001f, cameraSensitivity);
        fpsCap = fpsCap <= 0 ? 0 : Math.max(SimulationDefaults.MIN_FPS_CAP,
                Math.min(SimulationDefaults.MAX_FPS_CAP, fpsCap));
        matrixEditStep = clamp(matrixEditStep, 0.01f, 0.5f);
        customSpawnAmount = Math.max(0, customSpawnAmount);

        int attractionValueCount = particleConfig.groupCount() * particleConfig.groupCount();
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
