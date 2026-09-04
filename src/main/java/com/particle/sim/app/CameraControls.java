package com.particle.sim.app;

import com.particle.sim.camera.CameraController;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;

final class CameraControls implements SimulationViewModel.Camera, SimulationViewActions.Camera {
    private final CameraController camera;
    private final SettingsChangeHandler changes;

    CameraControls(CameraController camera, SettingsChangeHandler changes) {
        this.camera = camera;
        this.changes = changes;
    }

    @Override
    public float sensitivity() {
        return camera.getSensitivity();
    }

    @Override
    public float flySpeed() {
        return camera.getFlySpeed();
    }

    @Override
    public void setSensitivity(float value) {
        changes.apply(() -> camera.setSensitivity(value));
    }

    @Override
    public void setFlySpeed(float value) {
        changes.apply(() -> camera.setFlySpeed(value));
    }

    @Override
    public void reset() {
        camera.reset();
    }
}
