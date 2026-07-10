package com.particle.sim;

import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.particles.PerformanceSnapshot;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL43C.GL_RENDERER;
import static org.lwjgl.opengl.GL43C.GL_VENDOR;
import static org.lwjgl.opengl.GL43C.GL_VERSION;
import static org.lwjgl.opengl.GL43C.glFinish;
import static org.lwjgl.opengl.GL43C.glGetString;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class ParticleBenchmarkRunner {
    private static final double TARGET_MEDIAN_MILLISECONDS = 16.0;
    private static final double TARGET_P95_MILLISECONDS = 1000.0 / 60.0;
    private static final int SEARCH_ALIGNMENT = 1_024;
    private static final long BENCHMARK_SEED = 0x5EED_1234_ABCDL;

    private ParticleBenchmarkRunner() {
    }

    public static boolean requested(String[] args) {
        for (String argument : args) {
            if (argument.equals("--benchmark")) {
                return true;
            }
        }
        return false;
    }

    public static void run(String[] args) {
        ParticleBenchmarkOptions options = ParticleBenchmarkOptions.parse(args);
        if (!glfwInit()) {
            throw new IllegalStateException("Could not initialize GLFW for benchmark mode");
        }

        long window = NULL;
        GpuParticleSystem system = null;
        try {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            window = glfwCreateWindow(64, 64, "Particle benchmark", NULL, NULL);
            if (window == NULL) {
                throw new IllegalStateException("Could not create an OpenGL 4.3 benchmark context");
            }

            glfwMakeContextCurrent(window);
            GL.createCapabilities();
            system = new GpuParticleSystem();
            system.setParticleCount(Math.min(options.particleCount() == null ? 65_536 : options.particleCount(),
                    system.maxParticleCount()));
            system.init();
            configureDeterministicScene(system);

            List<Measurement> measurements = options.particleCount() == null
                    ? searchCapacity(system, options)
                    : List.of(measure(system, options.particleCount(), options));
            int sustainedParticleCount = measurements.stream()
                    .filter(Measurement::meetsTarget)
                    .map(Measurement::particleCount)
                    .max(Comparator.naturalOrder())
                    .orElse(0);

            String json = toJson(system, measurements, sustainedParticleCount);
            System.out.println(json);
            if (options.outputPath() != null) {
                writeOutput(options.outputPath(), json, measurements);
            }
        } finally {
            if (system != null) {
                system.dispose();
            }
            if (window != NULL) {
                glfwMakeContextCurrent(NULL);
                glfwDestroyWindow(window);
            }
            glfwTerminate();
        }
    }

    private static List<Measurement> searchCapacity(GpuParticleSystem system, ParticleBenchmarkOptions options) {
        List<Measurement> measurements = new ArrayList<>();
        int hardwareMaximum = system.maxParticleCount();
        int lower = Math.min(65_536, hardwareMaximum);
        Measurement lowerMeasurement = measure(system, lower, options);
        measurements.add(lowerMeasurement);

        while (!lowerMeasurement.meetsTarget() && lower > SEARCH_ALIGNMENT) {
            lower = aligned(Math.max(SEARCH_ALIGNMENT, lower / 2));
            lowerMeasurement = measure(system, lower, options);
            measurements.add(lowerMeasurement);
        }
        if (!lowerMeasurement.meetsTarget()) {
            return measurements;
        }

        int upper = lower;
        Measurement upperMeasurement = lowerMeasurement;
        while (upperMeasurement.meetsTarget() && upper < hardwareMaximum) {
            lower = upper;
            upper = (int) Math.min(hardwareMaximum, (long) upper * 2L);
            upperMeasurement = measure(system, upper, options);
            measurements.add(upperMeasurement);
        }
        if (upperMeasurement.meetsTarget()) {
            return measurements;
        }

        for (int iteration = 0; iteration < 6 && upper - lower > SEARCH_ALIGNMENT; iteration++) {
            int candidate = aligned(lower + (upper - lower) / 2);
            if (candidate <= lower || candidate >= upper) {
                break;
            }
            Measurement measurement = measure(system, candidate, options);
            measurements.add(measurement);
            if (measurement.meetsTarget()) {
                lower = candidate;
            } else {
                upper = candidate;
            }
        }
        return measurements;
    }

    private static Measurement measure(GpuParticleSystem system, int requestedCount,
            ParticleBenchmarkOptions options) {
        int count = Math.max(1, Math.min(requestedCount, system.maxParticleCount()));
        system.randomSeed(BENCHMARK_SEED);
        system.setParticleCount(count);

        for (int step = 0; step < options.warmupSteps(); step++) {
            system.step();
        }
        glFinish();

        double[] wallMilliseconds = new double[options.sampleSteps()];
        double[] gpuMilliseconds = new double[options.sampleSteps()];
        for (int step = 0; step < options.sampleSteps(); step++) {
            long start = System.nanoTime();
            system.step();
            glFinish();
            wallMilliseconds[step] = (System.nanoTime() - start) / 1_000_000.0;
            PerformanceSnapshot snapshot = system.performanceSnapshot();
            gpuMilliseconds[step] = snapshot.simulationMilliseconds();
        }

        return new Measurement(count, median(wallMilliseconds), percentile(wallMilliseconds, 0.95),
                median(gpuMilliseconds), percentile(gpuMilliseconds, 0.95));
    }

    private static void configureDeterministicScene(GpuParticleSystem system) {
        system.randomSeed(BENCHMARK_SEED);
        system.zeroAttractionMatrix();
        for (int row = 0; row < system.groupCount(); row++) {
            for (int column = 0; column < system.groupCount(); column++) {
                float attraction = ((row * 7 + column * 3) % 11 - 5) / 5.0f;
                system.attraction(row, column, attraction);
            }
        }
        system.reset();
    }

    private static double median(double[] values) {
        return percentile(values, 0.5);
    }

    private static double percentile(double[] values, double percentile) {
        double[] sorted = values.clone();
        java.util.Arrays.sort(sorted);
        int index = Math.min(sorted.length - 1, Math.max(0, (int) Math.ceil(percentile * sorted.length) - 1));
        return sorted[index];
    }

    private static int aligned(int value) {
        return Math.max(SEARCH_ALIGNMENT, value / SEARCH_ALIGNMENT * SEARCH_ALIGNMENT);
    }

    private static String toJson(GpuParticleSystem system, List<Measurement> measurements,
            int sustainedParticleCount) {
        StringBuilder json = new StringBuilder(1024);
        json.append("{\n");
        appendJsonString(json, "vendor", glGetString(GL_VENDOR), true);
        appendJsonString(json, "renderer", glGetString(GL_RENDERER), true);
        appendJsonString(json, "openGlVersion", glGetString(GL_VERSION), true);
        json.append("  \"runtimeParticleLimit\": ").append(system.maxParticleCount()).append(",\n");
        json.append("  \"targetP95Milliseconds\": ")
                .append(format(TARGET_P95_MILLISECONDS)).append(",\n");
        json.append("  \"maxSustainedParticles\": ").append(sustainedParticleCount).append(",\n");
        json.append("  \"measurements\": [\n");
        for (int i = 0; i < measurements.size(); i++) {
            Measurement measurement = measurements.get(i);
            json.append("    {\"particles\": ").append(measurement.particleCount())
                    .append(", \"wallMedianMs\": ").append(format(measurement.wallMedianMilliseconds()))
                    .append(", \"wallP95Ms\": ").append(format(measurement.wallP95Milliseconds()))
                    .append(", \"gpuMedianMs\": ").append(format(measurement.gpuMedianMilliseconds()))
                    .append(", \"gpuP95Ms\": ").append(format(measurement.gpuP95Milliseconds()))
                    .append(", \"meets60Hz\": ").append(measurement.meetsTarget()).append('}');
            json.append(i + 1 < measurements.size() ? ",\n" : "\n");
        }
        json.append("  ]\n}");
        return json.toString();
    }

    private static void appendJsonString(StringBuilder json, String name, String value, boolean comma) {
        String escaped = value == null ? "unknown" : value.replace("\\", "\\\\").replace("\"", "\\\"");
        json.append("  \"").append(name).append("\": \"").append(escaped).append('"');
        json.append(comma ? ",\n" : "\n");
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }

    private static void writeOutput(Path outputPath, String json, List<Measurement> measurements) {
        try {
            Path parent = outputPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            String fileName = outputPath.getFileName().toString().toLowerCase(Locale.ROOT);
            Files.writeString(outputPath, fileName.endsWith(".csv") ? toCsv(measurements) : json);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write benchmark result to " + outputPath, exception);
        }
    }

    private static String toCsv(List<Measurement> measurements) {
        StringBuilder csv = new StringBuilder(
                "particles,wall_median_ms,wall_p95_ms,gpu_median_ms,gpu_p95_ms,meets_60hz\n");
        for (Measurement measurement : measurements) {
            csv.append(measurement.particleCount()).append(',')
                    .append(format(measurement.wallMedianMilliseconds())).append(',')
                    .append(format(measurement.wallP95Milliseconds())).append(',')
                    .append(format(measurement.gpuMedianMilliseconds())).append(',')
                    .append(format(measurement.gpuP95Milliseconds())).append(',')
                    .append(measurement.meetsTarget()).append('\n');
        }
        return csv.toString();
    }

    private record Measurement(int particleCount, double wallMedianMilliseconds, double wallP95Milliseconds,
            double gpuMedianMilliseconds, double gpuP95Milliseconds) {
        boolean meetsTarget() {
            return wallMedianMilliseconds <= TARGET_MEDIAN_MILLISECONDS
                    && wallP95Milliseconds <= TARGET_P95_MILLISECONDS;
        }
    }
}
