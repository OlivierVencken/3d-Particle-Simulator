package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;

public final class SimulationUi {
    private final SimulationPanel simulationPanel = new SimulationPanel();
    private final AttractionMatrixPanel attractionMatrixPanel = new AttractionMatrixPanel();

    private float currentFps;
    private float fpsTimeAccumulator;
    private int fpsFrameAccumulator;
    private Runnable settingsChanged = () -> {
    };
    private Runnable resetSettings = () -> {
    };

    public void onSettingsChanged(Runnable settingsChanged) {
        this.settingsChanged = settingsChanged == null ? () -> {
        } : settingsChanged;
    }

    public void onResetSettings(Runnable resetSettings) {
        this.resetSettings = resetSettings == null ? () -> {
        } : resetSettings;
    }

    public void render(float deltaTime, GpuParticleSystem particles, CameraController camera) {
        updateFps(deltaTime);
        simulationPanel.render(deltaTime, currentFps, particles, camera, settingsChanged, resetSettings);
        attractionMatrixPanel.render(particles, settingsChanged);
    }

    private void updateFps(float deltaTime) {
        fpsTimeAccumulator += deltaTime;
        fpsFrameAccumulator++;

        if (fpsTimeAccumulator >= 0.35f) {
            currentFps = fpsFrameAccumulator / fpsTimeAccumulator;
            fpsTimeAccumulator = 0.0f;
            fpsFrameAccumulator = 0;
        }
    }

    public boolean isPaused() {
        return simulationPanel.isPaused();
    }

    public void setPaused(boolean paused) {
        simulationPanel.setPaused(paused);
    }

    public float matrixEditStep() {
        return attractionMatrixPanel.matrixEditStep();
    }

    public void setMatrixEditStep(float matrixEditStep) {
        attractionMatrixPanel.setMatrixEditStep(matrixEditStep);
    }

    public int customSpawnAmount() {
        return simulationPanel.customSpawnAmount();
    }

    public void setCustomSpawnAmount(int customSpawnAmount) {
        simulationPanel.setCustomSpawnAmount(customSpawnAmount);
    }
}
