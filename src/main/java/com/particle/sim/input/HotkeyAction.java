package com.particle.sim.input;

public enum HotkeyAction {
    TOGGLE_FULLSCREEN("Toggle fullscreen"),
    SHOW_UI("Show UI"),
    TOGGLE_UI("Hide/show UI"),
    TOGGLE_DEBUG("Toggle debug panel"),
    TOGGLE_PAUSE("Pause/resume simulation"),
    RESET_PARTICLES("Reset particles"),
    SIMULATION_STEP_FORWARD("Step simulation forward"),
    SIMULATION_STEP_BACKWARD("Step simulation backward");

    private final String displayName;

    HotkeyAction(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
