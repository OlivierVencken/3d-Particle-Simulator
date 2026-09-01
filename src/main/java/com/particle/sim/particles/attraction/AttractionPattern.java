package com.particle.sim.particles.attraction;

/** Structured starting points for attraction-matrix generation. */
public enum AttractionPattern {
    RANDOM("Random"),
    STABLE("Mostly stable"),
    SYMMETRIC("Symmetric relationships"),
    PREDATOR_PREY("Predator-prey cycle"),
    ROCK_PAPER_SCISSORS("Rock-paper-scissors"),
    TEAMS("Teams / factions"),
    SPARSE("Sparse interactions"),
    MUTUALISM("Mutualism"),
    PARASITISM("Parasitism"),
    HIERARCHY("Hierarchy"),
    RING("Ring attraction"),
    CHAIN("Chain attraction");
    private final String label;

    AttractionPattern(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static String[] labels() {
        AttractionPattern[] values = values();
        String[] labels = new String[values.length];
        for (int index = 0; index < values.length; index++) {
            labels[index] = values[index].label;
        }
        return labels;
    }
}
