package com.particle.sim.ui;

/** A small, frame-rate-independent eased transition for immediate-mode UI state. */
final class Transition {
    private final float durationSeconds;
    private float value;
    private float startValue;
    private float targetValue;
    private float elapsedSeconds;

    Transition(float initialValue, float durationSeconds) {
        this.durationSeconds = Math.max(0.001f, durationSeconds);
        snapTo(initialValue);
    }

    float value() {
        return value;
    }

    void setTarget(float target) {
        float clampedTarget = clamp01(target);
        if (Float.compare(clampedTarget, targetValue) == 0) {
            return;
        }
        startValue = value;
        targetValue = clampedTarget;
        elapsedSeconds = 0.0f;
    }

    void advance(float deltaTime) {
        if (Float.compare(value, targetValue) == 0
                || !Float.isFinite(deltaTime)
                || deltaTime <= 0.0f) {
            return;
        }

        elapsedSeconds = Math.min(durationSeconds, elapsedSeconds + Math.min(deltaTime, 0.1f));
        float time = elapsedSeconds / durationSeconds;
        float easedTime = 1.0f - (float) Math.pow(1.0f - time, 3.0f);
        value = startValue + (targetValue - startValue) * easedTime;
        if (elapsedSeconds >= durationSeconds) {
            value = targetValue;
        }
    }

    void snapTo(float target) {
        value = clamp01(target);
        startValue = value;
        targetValue = value;
        elapsedSeconds = durationSeconds;
    }

    private static float clamp01(float value) {
        if (!Float.isFinite(value)) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
