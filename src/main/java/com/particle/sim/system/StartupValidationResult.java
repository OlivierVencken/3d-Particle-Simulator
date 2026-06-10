package com.particle.sim.system;

public final class StartupValidationResult {
    public final boolean ok;
    public final String title;
    public final String message;

    private StartupValidationResult(boolean ok, String title, String message) {
        this.ok = ok;
        this.title = title;
        this.message = message;
    }

    public static StartupValidationResult ok() {
        return new StartupValidationResult(true, null, null);
    }

    public static StartupValidationResult fail(String title, String message) {
        return new StartupValidationResult(false, title, message);
    }
}