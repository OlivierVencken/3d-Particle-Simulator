package com.particle.sim.app;

import com.particle.sim.settings.SettingsActions;
import com.particle.sim.ui.SimulationView;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;

final class ApplicationControls
        implements SimulationViewModel.Application, SimulationViewActions.Application {
    private static final Runnable NO_ACTION = () -> {};
    private final SimulationView ui;
    private final SettingsActions settingsActions;
    private final SettingsChangeHandler changes;
    private Runnable savePreset = NO_ACTION;
    private Runnable loadPreset = NO_ACTION;
    private Runnable exitApplication = NO_ACTION;

    ApplicationControls(
            SimulationView ui, SettingsActions settingsActions, SettingsChangeHandler changes) {
        this.ui = ui;
        this.settingsActions = settingsActions;
        this.changes = changes;
    }

    void onSavePreset(Runnable action) {
        savePreset = action == null ? NO_ACTION : action;
    }

    void onLoadPreset(Runnable action) {
        loadPreset = action == null ? NO_ACTION : action;
    }

    void onExitApplication(Runnable action) {
        exitApplication = action == null ? NO_ACTION : action;
    }

    @Override
    public boolean paused() {
        return ui.isPaused();
    }

    @Override
    public int fpsCap() {
        return ui.fpsCap();
    }

    @Override
    public void setFpsCap(int value) {
        changes.apply(() -> ui.setFpsCap(value));
    }

    @Override
    public void resetSettings() {
        settingsActions.onResetRequested();
    }

    @Override
    public void savePreset() {
        savePreset.run();
    }

    @Override
    public void loadPreset() {
        loadPreset.run();
    }

    @Override
    public void hideUi() {
        ui.hide();
    }

    @Override
    public void exit() {
        exitApplication.run();
    }
}
