package com.particle.sim.ui.theme;

/** Colors for one semantic component variant and its interaction states. */
public record UIComponentPalette(
        UIColor background,
        UIColor hovered,
        UIColor active,
        UIColor border,
        UIColor text) {
}
