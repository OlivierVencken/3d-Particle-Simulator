package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.workspace.WorkspaceShell;

public final class SimulationUI {
    private final WorkspaceShell workspace = new WorkspaceShell();

    private float currentFps;
    private float fpsTimeAccumulator;
    private int fpsFrameAccumulator;
    private int fpsCap = SimulationDefaults.FPS_CAP;
    private boolean paused;
    private boolean hidden;
    private Runnable settingsChanged = () -> {
    };
    private Runnable resetSettings = () -> {
    };
    private Runnable savePreset = () -> {
    };
    private Runnable loadPreset = () -> {
    };
    private Runnable exitApplication = () -> {
    };

    public void onSettingsChanged(Runnable settingsChanged) {
        this.settingsChanged = settingsChanged == null ? () -> {
        } : settingsChanged;
    }

    public void onResetSettings(Runnable resetSettings) {
        this.resetSettings = resetSettings == null ? () -> {
        } : resetSettings;
    }

    public void onSavePreset(Runnable savePreset) {
        this.savePreset = savePreset == null ? () -> {
        } : savePreset;
    }

    public void onLoadPreset(Runnable loadPreset) {
        this.loadPreset = loadPreset == null ? () -> {
        } : loadPreset;
    }

    public void onExitApplication(Runnable exitApplication) {
        this.exitApplication = exitApplication == null ? () -> {
        } : exitApplication;
    }

    public void render(float deltaTime, GpuParticleSystem particles, CameraController camera) {
        updateFps(deltaTime);
        if (hidden) {
            return;
        }

        workspace.render(deltaTime, currentFps, fpsCap, particles, camera,
                settingsChanged, resetSettings, savePreset, loadPreset, exitApplication, this::hide,
                this::setFpsCap);
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
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public float matrixEditStep() {
        return workspace.matrixEditStep();
    }

    public void setMatrixEditStep(float matrixEditStep) {
        workspace.setMatrixEditStep(matrixEditStep);
    }

    public int customSpawnAmount() {
        return workspace.customSpawnAmount();
    }

    public void setCustomSpawnAmount(int customSpawnAmount) {
        workspace.setCustomSpawnAmount(customSpawnAmount);
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

    public void togglePause() {
        setPaused(!isPaused());
    }

    public void toggleUi() {
        if (isHidden()) {
            show();
        } else {
            hide();
        }
    }

    public void toggleDebug() {
        workspace.toggleDebug();
    }

    public boolean isHidden() {
        return hidden;
    }

    public void hide() {
        hidden = true;
    }

    public void show() {
        hidden = false;
    }
}
