package com.particle.sim;

import java.nio.file.Path;

record ParticleBenchmarkOptions(Integer particleCount, int warmupSteps, int sampleSteps, Path outputPath) {
    private static final int DEFAULT_WARMUP_STEPS = 10;
    private static final int DEFAULT_SAMPLE_STEPS = 30;

    static ParticleBenchmarkOptions parse(String[] args) {
        Integer particleCount = null;
        int warmupSteps = DEFAULT_WARMUP_STEPS;
        int sampleSteps = DEFAULT_SAMPLE_STEPS;
        Path outputPath = null;

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
            } else if (!argument.equals("--benchmark")) {
                throw new IllegalArgumentException("Unknown benchmark option: " + argument);
            }
        }

        return new ParticleBenchmarkOptions(particleCount, warmupSteps, sampleSteps, outputPath);
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
