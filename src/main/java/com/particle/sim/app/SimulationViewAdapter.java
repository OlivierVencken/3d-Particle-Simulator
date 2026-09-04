package com.particle.sim.app;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.ParticleSystem;
import com.particle.sim.settings.SettingsActions;
import com.particle.sim.ui.SimulationView;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;
import java.util.Objects;

/** Composes runtime services into the presentation-facing model and action contracts. */
final class SimulationViewAdapter {
    private final ParticleSystem particles;
    private final PerformanceView performance = new PerformanceView();
    private final ApplicationControls application;
    private final SimulationViewModel model;
    private final SimulationViewActions actions;

    SimulationViewAdapter(
            ParticleSystem particles,
            CameraController camera,
            SimulationView ui,
            SettingsActions settingsActions) {
        this.particles = Objects.requireNonNull(particles, "particles");
        Objects.requireNonNull(camera, "camera");
        Objects.requireNonNull(ui, "ui");
        Objects.requireNonNull(settingsActions, "settingsActions");

        SettingsChangeHandler changes = settingsActions::onSettingsChanged;
        SimulationControls simulation = new SimulationControls(particles, ui, changes);
        ParticleControls particleControls = new ParticleControls(particles, ui, changes);
        VisualControls visuals = new VisualControls(particles, changes);
        CameraControls cameraControls = new CameraControls(camera, changes);
        application = new ApplicationControls(ui, settingsActions, changes);
        model =
                new ViewModel(
                        simulation,
                        particleControls,
                        visuals,
                        cameraControls,
                        performance,
                        application);
        actions =
                new ViewActions(simulation, particleControls, visuals, cameraControls, application);
    }

    SimulationViewModel model() {
        return model;
    }

    SimulationViewActions actions() {
        return actions;
    }

    void prepareFrame() {
        performance.update(particles.performanceSnapshot(), particles.gridSize());
    }

    void onSavePreset(Runnable action) {
        application.onSavePreset(action);
    }

    void onLoadPreset(Runnable action) {
        application.onLoadPreset(action);
    }

    void onExitApplication(Runnable action) {
        application.onExitApplication(action);
    }

    private record ViewModel(
            SimulationViewModel.Simulation simulation,
            SimulationViewModel.Particles particles,
            SimulationViewModel.Visuals visuals,
            SimulationViewModel.Camera camera,
            SimulationViewModel.Performance performance,
            SimulationViewModel.Application application)
            implements SimulationViewModel {}

    private record ViewActions(
            SimulationViewActions.Simulation simulation,
            SimulationViewActions.Particles particles,
            SimulationViewActions.Visuals visuals,
            SimulationViewActions.Camera camera,
            SimulationViewActions.Application application)
            implements SimulationViewActions {}
}
