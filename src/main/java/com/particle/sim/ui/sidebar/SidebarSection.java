package com.particle.sim.ui.sidebar;

public enum SidebarSection {
    SIMULATION("Simulation"),
    PARTICLES("Particles"),
    VISUALS("Visuals"),
    CAMERA("Camera"),
    MATRIX("Attraction Matrix");

    private final String label;

    SidebarSection(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
