package com.particle.sim;

import com.particle.sim.particles.SimulationDimension;
import com.particle.sim.particles.SpawnMode;

import java.nio.file.Path;
import java.util.Locale;

record ParticleBenchmarkOptions(Integer particleCount, int warmupSteps, int sampleSteps, Path outputPath,
        SimulationDimension simulationDimension, SpawnMode spawnMode) {
    private static final int DEFAULT_WARMUP_STEPS = 10;
    private static final int DEFAULT_SAMPLE_STEPS = 30;

    static ParticleBenchmarkOptions parse(String[] args) {
        Integer particleCount = null;
        int warmupSteps = DEFAULT_WARMUP_STEPS;
        int sampleSteps = DEFAULT_SAMPLE_STEPS;
        Path outputPath = null;
        SimulationDimension simulationDimension = SimulationDimension.THREE_D;
        SpawnMode spawnMode = SpawnMode.RANDOM;

        for (String argument : args) {
            if (argument.startsWith("--particles=")) {
                particleCount = positiveInt(argument, "--particles=");
            } else if (argument.startsWith("--warmup=")) {
                warmupSteps = nonNegativeInt(argument, "--warmup=");
            } else if (argument.startsWith("--samples=")) {
                sampleSteps = positiveInt(argument, "--samples=");
            } else if (argument.startsWith("--output=")) {
                String path = argument.substring("--output=".length()).trim();
                if (path.isEmpty()) {
                    throw new IllegalArgumentException("--output requires a path");
                }
                outputPath = Path.of(path);
            } else if (argument.startsWith("--dimension=")) {
                String value = argument.substring("--dimension=".length()).trim().toLowerCase(Locale.ROOT);
                simulationDimension = switch (value) {
                    case "3d", "three_d" -> SimulationDimension.THREE_D;
                    case "4d", "four_d" -> SimulationDimension.FOUR_D;
                    default -> throw new IllegalArgumentException("Unknown simulation dimension: " + value);
                };
            } else if (argument.startsWith("--spawn=")) {
                String value = argument.substring("--spawn=".length()).trim().toUpperCase(Locale.ROOT);
                try {
                    spawnMode = SpawnMode.valueOf(value);
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("Unknown spawn mode: " + value, exception);
                }
            } else if (!argument.equals("--benchmark")) {
                throw new IllegalArgumentException("Unknown benchmark option: " + argument);
            }
        }

        if (!spawnMode.supportedIn(simulationDimension)) {
            throw new IllegalArgumentException(spawnMode + " spawning is unavailable in "
                    + simulationDimension.componentCount() + "D");
        }
        return new ParticleBenchmarkOptions(particleCount, warmupSteps, sampleSteps, outputPath,
                simulationDimension, spawnMode);
    }

    private static int positiveInt(String argument, String prefix) {
        int value = parseInt(argument, prefix);
        if (value <= 0) {
            throw new IllegalArgumentException(prefix + " must be positive");
        }
        return value;
    }

    private static int nonNegativeInt(String argument, String prefix) {
        int value = parseInt(argument, prefix);
        if (value < 0) {
            throw new IllegalArgumentException(prefix + " must be non-negative");
        }
        return value;
    }

    private static int parseInt(String argument, String prefix) {
        try {
            return Integer.parseInt(argument.substring(prefix.length()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid integer option: " + argument, exception);
        }
    }
}
