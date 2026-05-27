package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.settings.SimulationDefaults;
import imgui.type.ImBoolean;

public final class SimulationUi {
    private final SettingsSidebar settingsSidebar = new SettingsSidebar();
    private final SimulationMenuBar menuBar = new SimulationMenuBar();
    private final DebugPanel debugPanel = new DebugPanel();
    private final FpsPanel fpsPanel = new FpsPanel();
    private final ImBoolean showDebugPanel = new ImBoolean(false);

    private float currentFps;
    private float fpsTimeAccumulator;
    private int fpsFrameAccumulator;
    private int fpsCap = SimulationDefaults.FPS_CAP;
    private boolean hidden;
    private Runnable settingsChanged = () -> {
    };
    private Runnable resetSettings = () -> {
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

    public void onExitApplication(Runnable exitApplication) {
        this.exitApplication = exitApplication == null ? () -> {
        } : exitApplication;
    }

    public void render(float deltaTime, GpuParticleSystem particles, CameraController camera) {
        updateFps(deltaTime);
        if (hidden) {
            return;
        }

        menuBar.render(particles, camera, settingsSidebar, showDebugPanel, settingsChanged, resetSettings,
                exitApplication, this::hide);

        settingsSidebar.render(particles, camera, settingsChanged);
        if (showDebugPanel.get()) {
            debugPanel.render(deltaTime, currentFps, fpsCap, this::setFpsCap, settingsChanged, particles, showDebugPanel);
        }
        fpsPanel.render(currentFps);
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
        return settingsSidebar.isPaused();
    }

    public void setPaused(boolean paused) {
        settingsSidebar.setPaused(paused);
    }

    public float matrixEditStep() {
        return settingsSidebar.matrixEditStep();
    }

    public void setMatrixEditStep(float matrixEditStep) {
        settingsSidebar.setMatrixEditStep(matrixEditStep);
    }

    public int customSpawnAmount() {
        return settingsSidebar.customSpawnAmount();
    }

    public void setCustomSpawnAmount(int customSpawnAmount) {
        settingsSidebar.setCustomSpawnAmount(customSpawnAmount);
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
