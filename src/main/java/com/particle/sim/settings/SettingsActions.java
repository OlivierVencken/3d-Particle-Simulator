package com.particle.sim.settings;

/** Settings operations used by application-facing adapters. */
public interface SettingsActions {
    void onSettingsChanged();

    void onResetRequested();
}
