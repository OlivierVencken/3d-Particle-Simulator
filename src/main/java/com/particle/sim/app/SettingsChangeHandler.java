package com.particle.sim.app;

@FunctionalInterface
interface SettingsChangeHandler {
    void changed();

    default void apply(Runnable mutation) {
        mutation.run();
        changed();
    }
}
