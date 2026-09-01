package com.particle.sim.ui;

import java.util.Objects;

/** Layout and coordinate-space decisions shared by scene and UI rendering for one frame. */
public record PreparedFrame(
        DisplayMetrics displayMetrics,
        Layout layout,
        FramebufferViewport simulationViewport,
        boolean uiVisible,
        InputOwnership inputOwnership) {
    public PreparedFrame {
        Objects.requireNonNull(displayMetrics, "displayMetrics");
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(simulationViewport, "simulationViewport");
        Objects.requireNonNull(inputOwnership, "inputOwnership");
    }
}
