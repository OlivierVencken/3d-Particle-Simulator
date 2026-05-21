package com.particle.sim;

final class FixedSimulationClock {
    private static final double MAX_ACCUMULATED_SECONDS = 0.25;

    private final double stepSeconds;
    private double accumulatedSeconds;
    private double elapsedSeconds;

    FixedSimulationClock(double stepSeconds) {
        if (stepSeconds <= 0.0) {
            throw new IllegalArgumentException("stepSeconds must be positive");
        }

        this.stepSeconds = stepSeconds;
    }

    void addFrameTime(double frameSeconds) {
        if (frameSeconds <= 0.0) {
            return;
        }

        accumulatedSeconds = Math.min(accumulatedSeconds + frameSeconds, MAX_ACCUMULATED_SECONDS);
    }

    boolean hasStep() {
        return accumulatedSeconds + 0.000000001 >= stepSeconds;
    }

    float consumeStep() {
        if (!hasStep()) {
            throw new IllegalStateException("No simulation step is ready");
        }

        accumulatedSeconds -= stepSeconds;
        elapsedSeconds += stepSeconds;
        return (float) elapsedSeconds;
    }

    float stepSeconds() {
        return (float) stepSeconds;
    }
}
