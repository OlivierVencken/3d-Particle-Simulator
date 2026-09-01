package com.particle.sim.system;

public final class StartupFailureException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public StartupFailureException(String message) {
        super(message);
    }
}
