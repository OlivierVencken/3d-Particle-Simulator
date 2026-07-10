package com.particle.sim.ui;

enum UiSection {
    SIMULATION("Simulation"),
    PARTICLES("Particles"),
    APPEARANCE("Appearance"),
    CAMERA("Camera"),
    INTERACTIONS("Interactions");

    private final String label;

    UiSection(String label) {
        this.label = label;
    }

    String label() {
        return label;
    }
}
