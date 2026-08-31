package com.particle.sim.settings;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.ColorMode;
import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.EffectMode;
import com.particle.sim.particles.FourDViewConfiguration;
import com.particle.sim.particles.FourDViewController;
import com.particle.sim.particles.FourDViewState;
import com.particle.sim.particles.FourDVisualizationMode;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.ParticleSimulationConfig;
import com.particle.sim.particles.SimulationDimension;
import com.particle.sim.particles.SpawnMode;
import com.particle.sim.ui.SimulationUI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public final class AppSettings {
    public static final int VERSION = 2;
    public static final String PRESET_EXTENSION = ".3dps";

    private final ParticleSimulationConfig particleConfig = ParticleSimulationConfig.defaults();
    private float[] attractionMatrix;
    private FourDViewState fourDViewState = FourDViewState.defaults();

    private float cameraSensitivity = SimulationDefaults.CAMERA_SENSITIVITY;
    private float cameraFlySpeed = SimulationDefaults.CAMERA_FLY_SPEED;
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

        int loadedVersion = intProperty(properties, "version", 1);
        ParticleSimulationConfig particleConfig = settings.particleConfig;
        particleConfig.particleCount(intProperty(properties, "particleCount", particleConfig.particleCount()));
        particleConfig.pointSize(floatProperty(properties, "pointSize", particleConfig.pointSize()));
        particleConfig.fixedParticleScreenSize(booleanProperty(properties, "fixedParticleScreenSize",
                particleConfig.fixedParticleScreenSize()));
        particleConfig.effectModes(effectModesProperty(properties.getProperty("effectModes")));
        particleConfig.glowBlurPasses(intProperty(properties, "glowBlurPasses", particleConfig.glowBlurPasses()));
        particleConfig.glowStrength(floatProperty(properties, "glowStrength", particleConfig.glowStrength()));
        particleConfig.glowRadius(floatProperty(properties, "glowRadius", particleConfig.glowRadius()));
        particleConfig.glowFalloff(floatProperty(properties, "glowFalloff", particleConfig.glowFalloff()));
        particleConfig.trailLength(intProperty(properties, "trailLength", particleConfig.trailLength()));
        particleConfig.trailThickness(floatProperty(properties, "trailThickness", particleConfig.trailThickness()));
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
        particleConfig.distanceMetric(enumProperty(properties, "distanceMetric", DistanceMetric.class,
                particleConfig.distanceMetric()));
        particleConfig.groupCount(intProperty(properties, "groupCount", particleConfig.groupCount()));
        particleConfig.colorMode(enumProperty(properties, "colorMode", ColorMode.class, particleConfig.colorMode()));
        particleConfig.simulationDimension(loadedVersion >= 2
                ? enumProperty(properties, "simulationDimension", SimulationDimension.class,
                        SimulationDimension.THREE_D)
                : SimulationDimension.THREE_D);
        particleConfig.spawnMode(enumProperty(properties, "spawnMode", SpawnMode.class, particleConfig.spawnMode()));
        settings.fourDViewState = loadedVersion >= 2
                ? fourDViewStateProperty(properties, particleConfig.bounds())
                : FourDViewState.defaults();
        settings.cameraSensitivity = floatProperty(properties, "cameraSensitivity", settings.cameraSensitivity);
        settings.cameraFlySpeed = floatProperty(properties, "cameraFlySpeed", settings.cameraFlySpeed);
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
        save(path, "3D Particle Simulator settings", null);
    }

    public void savePreset(Path path, String presetName) {
        save(path, "3D Particle Simulator preset", presetName);
    }

    public static Path ensurePresetExtension(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.toLowerCase(Locale.ROOT).endsWith(PRESET_EXTENSION)) {
            return path;
        }
        return path.resolveSibling(fileName + PRESET_EXTENSION);
    }

    public static String presetNameFromPath(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.toLowerCase(Locale.ROOT).endsWith(PRESET_EXTENSION)) {
            fileName = fileName.substring(0, fileName.length() - PRESET_EXTENSION.length());
        }
        if (fileName.isBlank()) {
            return "Untitled preset";
        }
        return fileName;
    }

    private void save(Path path, String comment, String presetName) {
        Properties properties = new Properties();
        properties.setProperty("version", Integer.toString(VERSION));
        if (presetName != null && !presetName.isBlank()) {
            properties.setProperty("presetName", presetName.trim());
        }
        properties.setProperty("particleCount", Integer.toString(particleConfig.particleCount()));
        properties.setProperty("pointSize", Float.toString(particleConfig.pointSize()));
        properties.setProperty("fixedParticleScreenSize", Boolean.toString(particleConfig.fixedParticleScreenSize()));
        properties.setProperty("effectModes", effectModesString(particleConfig.effectModes()));
        properties.setProperty("glowBlurPasses", Integer.toString(particleConfig.glowBlurPasses()));
        properties.setProperty("glowStrength", Float.toString(particleConfig.glowStrength()));
        properties.setProperty("glowRadius", Float.toString(particleConfig.glowRadius()));
        properties.setProperty("glowFalloff", Float.toString(particleConfig.glowFalloff()));
        properties.setProperty("trailLength", Integer.toString(particleConfig.trailLength()));
        properties.setProperty("trailThickness", Float.toString(particleConfig.trailThickness()));
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
        properties.setProperty("distanceMetric", particleConfig.distanceMetric().name());
        properties.setProperty("groupCount", Integer.toString(particleConfig.groupCount()));
        properties.setProperty("colorMode", particleConfig.colorMode().name());
        properties.setProperty("simulationDimension", particleConfig.simulationDimension().name());
        properties.setProperty("spawnMode", particleConfig.spawnMode().name());
        saveFourDViewState(properties, fourDViewState);
        properties.setProperty("cameraSensitivity", Float.toString(cameraSensitivity));
        properties.setProperty("cameraFlySpeed", Float.toString(cameraFlySpeed));
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
                properties.store(stream, comment);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save settings to " + path, e);
        }
    }

    public void applyTo(GpuParticleSystem particles, CameraController camera, SimulationUI ui) {
        applySimulationTo(particles, camera, ui);
        particles.setAttractionMatrix(attractionMatrix);
    }

    public void applySimulationTo(GpuParticleSystem particles, CameraController camera, SimulationUI ui) {
        sanitize();
        particles.applyConfig(particleConfig);
        particles.fourDViewState(fourDViewState);

        camera.setSensitivity(cameraSensitivity);
        camera.setFlySpeed(cameraFlySpeed);
        ui.setPaused(paused);
        ui.setFpsCap(fpsCap);
        ui.setMatrixEditStep(matrixEditStep);
        ui.setCustomSpawnAmount(customSpawnAmount);
    }

    public static AppSettings capture(GpuParticleSystem particles, CameraController camera, SimulationUI ui) {
        AppSettings settings = defaults();
        settings.particleConfig.applyFrom(particles.config());
        settings.fourDViewState = particles.fourDViewState();
        int attractionIndex = 0;
        for (int row = 0; row < settings.particleConfig.groupCount(); row++) {
            for (int column = 0; column < settings.particleConfig.groupCount(); column++) {
                settings.attractionMatrix[attractionIndex] = particles.attraction(row, column);
                attractionIndex++;
            }
        }

        settings.cameraSensitivity = camera.getSensitivity();
        settings.cameraFlySpeed = camera.getFlySpeed();
        settings.paused = ui.isPaused();
        settings.fpsCap = ui.fpsCap();
        settings.matrixEditStep = ui.matrixEditStep();
        settings.customSpawnAmount = ui.customSpawnAmount();
        settings.sanitize();
        return settings;
    }

    private void sanitize() {
        particleConfig.sanitize();
        fourDViewState = sanitizedFourDViewState(fourDViewState, particleConfig.bounds());
        cameraSensitivity = Math.max(0.0001f, cameraSensitivity);
        cameraFlySpeed = Math.max(0.1f, cameraFlySpeed);
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

    private static double doubleProperty(Properties properties, String key, double fallback) {
        try {
            double value = Double.parseDouble(properties.getProperty(key, Double.toString(fallback)));
            return Double.isFinite(value) ? value : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static FourDViewState fourDViewStateProperty(Properties properties, float bounds) {
        FourDViewState defaults = FourDViewState.defaults();
        FourDViewConfiguration defaultView = defaults.configuration();
        double[] rotation = defaultView.rotationMatrix();
        for (int index = 0; index < rotation.length; index++) {
            rotation[index] = doubleProperty(properties, "fourDRotation." + index, rotation[index]);
        }

        try {
            FourDViewConfiguration configuration = new FourDViewConfiguration(
                    enumProperty(properties, "fourDVisualizationMode", FourDVisualizationMode.class,
                            defaultView.visualizationMode()),
                    rotation,
                    doubleProperty(properties, "fourDPerspectiveDistance", defaultView.perspectiveDistance()),
                    doubleProperty(properties, "fourDSliceCenterW", defaultView.sliceCenterW()),
                    doubleProperty(properties, "fourDSliceThickness", defaultView.sliceThickness()),
                    doubleProperty(properties, "fourDSliceFeather", defaultView.sliceFeather()),
                    doubleProperty(properties, "fourDColorRange", defaultView.colorRange()));
            return sanitizedFourDViewState(new FourDViewState(configuration,
                    doubleProperty(properties, "fourDXwAngleRadians", defaults.xwAngle()),
                    doubleProperty(properties, "fourDYwAngleRadians", defaults.ywAngle()),
                    doubleProperty(properties, "fourDZwAngleRadians", defaults.zwAngle()),
                    doubleProperty(properties, "fourDXwAutoSpeedRadians", defaults.xwAutoSpeed()),
                    doubleProperty(properties, "fourDYwAutoSpeedRadians", defaults.ywAutoSpeed()),
                    doubleProperty(properties, "fourDZwAutoSpeedRadians", defaults.zwAutoSpeed()),
                    booleanProperty(properties, "fourDXwAutoEnabled", defaults.xwAutoEnabled()),
                    booleanProperty(properties, "fourDYwAutoEnabled", defaults.ywAutoEnabled()),
                    booleanProperty(properties, "fourDZwAutoEnabled", defaults.zwAutoEnabled()),
                    booleanProperty(properties, "fourDViewMotionPaused", defaults.motionPaused()),
                    booleanProperty(properties, "fourDSliceSweepEnabled", defaults.sliceSweepEnabled()),
                    doubleProperty(properties, "fourDSliceSweepSpeed", defaults.sliceSweepSpeed())), bounds);
        } catch (IllegalArgumentException exception) {
            return defaults;
        }
    }

    private static void saveFourDViewState(Properties properties, FourDViewState state) {
        FourDViewConfiguration view = state.configuration();
        properties.setProperty("fourDVisualizationMode", view.visualizationMode().name());
        double[] rotation = view.rotationMatrix();
        for (int index = 0; index < rotation.length; index++) {
            properties.setProperty("fourDRotation." + index, Double.toString(rotation[index]));
        }
        properties.setProperty("fourDPerspectiveDistance", Double.toString(view.perspectiveDistance()));
        properties.setProperty("fourDSliceCenterW", Double.toString(view.sliceCenterW()));
        properties.setProperty("fourDSliceThickness", Double.toString(view.sliceThickness()));
        properties.setProperty("fourDSliceFeather", Double.toString(view.sliceFeather()));
        properties.setProperty("fourDColorRange", Double.toString(view.colorRange()));
        properties.setProperty("fourDXwAngleRadians", Double.toString(state.xwAngle()));
        properties.setProperty("fourDYwAngleRadians", Double.toString(state.ywAngle()));
        properties.setProperty("fourDZwAngleRadians", Double.toString(state.zwAngle()));
        properties.setProperty("fourDXwAutoSpeedRadians", Double.toString(state.xwAutoSpeed()));
        properties.setProperty("fourDYwAutoSpeedRadians", Double.toString(state.ywAutoSpeed()));
        properties.setProperty("fourDZwAutoSpeedRadians", Double.toString(state.zwAutoSpeed()));
        properties.setProperty("fourDXwAutoEnabled", Boolean.toString(state.xwAutoEnabled()));
        properties.setProperty("fourDYwAutoEnabled", Boolean.toString(state.ywAutoEnabled()));
        properties.setProperty("fourDZwAutoEnabled", Boolean.toString(state.zwAutoEnabled()));
        properties.setProperty("fourDViewMotionPaused", Boolean.toString(state.motionPaused()));
        properties.setProperty("fourDSliceSweepEnabled", Boolean.toString(state.sliceSweepEnabled()));
        properties.setProperty("fourDSliceSweepSpeed", Double.toString(state.sliceSweepSpeed()));
    }

    private static FourDViewState sanitizedFourDViewState(FourDViewState state, float bounds) {
        FourDViewState fallback = state == null ? FourDViewState.defaults() : state;
        FourDViewConfiguration view = fallback.configuration();
        double safeBounds = Math.max(0.1, Math.abs(bounds));
        double perspectiveDistance = Math.max(view.perspectiveDistance(),
                FourDViewController.minimumPerspectiveDistance(safeBounds));
        double sliceThickness = clamp(view.sliceThickness(), 0.05, safeBounds * 2.0);
        FourDViewConfiguration sanitizedView = new FourDViewConfiguration(view.visualizationMode(),
                view.rotationMatrix(), perspectiveDistance,
                clamp(view.sliceCenterW(), -safeBounds, safeBounds), sliceThickness,
                clamp(view.sliceFeather(), 0.0, sliceThickness * 0.5),
                clamp(view.colorRange(), 0.05, safeBounds * 2.0));
        double maximumAutoSpeed = Math.toRadians(90.0);
        return new FourDViewState(sanitizedView,
                fallback.xwAngle(), fallback.ywAngle(), fallback.zwAngle(),
                clamp(fallback.xwAutoSpeed(), -maximumAutoSpeed, maximumAutoSpeed),
                clamp(fallback.ywAutoSpeed(), -maximumAutoSpeed, maximumAutoSpeed),
                clamp(fallback.zwAutoSpeed(), -maximumAutoSpeed, maximumAutoSpeed),
                fallback.xwAutoEnabled(), fallback.ywAutoEnabled(), fallback.zwAutoEnabled(),
                fallback.motionPaused(), fallback.sliceSweepEnabled(),
                clamp(fallback.sliceSweepSpeed(), 0.0, safeBounds * 2.0));
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

    private static Set<EffectMode> effectModesProperty(String value) {
        EnumSet<EffectMode> effectModes = EnumSet.noneOf(EffectMode.class);
        if (value == null || value.isBlank()) {
            return effectModes;
        }

        for (String token : value.split(",")) {
            try {
                EffectMode effectMode = EffectMode.valueOf(token.trim());
                effectModes.add(effectMode);
            } catch (IllegalArgumentException e) {
                // Ignore unknown future effect names so older builds can still load the rest of the preset.
            }
        }
        return effectModes;
    }

    private static String effectModesString(Set<EffectMode> effectModes) {
        return effectModes.stream()
                .map(EffectMode::name)
                .collect(Collectors.joining(","));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
