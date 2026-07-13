package com.particle.sim.ui.workspace;

enum UISection {
    SIMULATION("Simulation"),
    PARTICLES("Particles"),
    VISUALS("Visuals"),
    CAMERA("Camera"),
    MATRIX("Attraction Matrix");

    private final String label;

    UISection(String label) {
        this.label = label;
    }

    String label() {
        return label;
    }
}
