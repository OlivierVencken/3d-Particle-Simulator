package com.particle.sim.settings;

public final class DebouncedSettingsSaver {
    private final double debounceSeconds;
    private final Runnable save;
    private boolean pending;
    private double nextSaveTime;

    public DebouncedSettingsSaver(double debounceSeconds, Runnable save) {
        if (debounceSeconds < 0.0) {
            throw new IllegalArgumentException("debounceSeconds must be non-negative.");
        }
        this.debounceSeconds = debounceSeconds;
        this.save = save == null ? () -> {
        } : save;
    }

    public void requestSave(double now) {
        pending = true;
        nextSaveTime = now + debounceSeconds;
    }

    public void saveIfDue(double now) {
        if (pending && now >= nextSaveTime) {
            flush();
        }
    }

    public void flush() {
        if (pending) {
            save.run();
            pending = false;
        }
    }

    public boolean hasPendingSave() {
        return pending;
    }
}
