package com.particle.sim.ui.workspace;

enum UISection {
    SIMULATION("Simulation"),
    PARTICLES("Particles"),
    APPEARANCE("Appearance"),
    CAMERA("Camera"),
    INTERACTIONS("Interactions");

    private final String label;

    UISection(String label) {
        this.label = label;
    }

    String label() {
        return label;
    }
}
