package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SimulationDefaults;

public final class SimulationUi {
    private final SimulationPanel simulationPanel = new SimulationPanel();
    private final AttractionMatrixPanel attractionMatrixPanel = new AttractionMatrixPanel();
    private final DebugPanel debugPanel = new DebugPanel();

    private float currentFps;
    private float fpsTimeAccumulator;
    private int fpsFrameAccumulator;
    private int fpsCap = SimulationDefaults.FPS_CAP;
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
        simulationPanel.render(particles, camera, settingsChanged, resetSettings);
        attractionMatrixPanel.render(particles, settingsChanged);
        debugPanel.render(deltaTime, currentFps, fpsCap, this::setFpsCap, settingsChanged, particles);
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

    public int fpsCap() {
        return fpsCap;
    }

    public void setFpsCap(int fpsCap) {
        if (fpsCap <= 0) {
            this.fpsCap = 0;
            return;
        }

        this.fpsCap = Math.max(SimulationDefaults.MIN_FPS_CAP,
                Math.min(SimulationDefaults.MAX_FPS_CAP, fpsCap));
    }
}
