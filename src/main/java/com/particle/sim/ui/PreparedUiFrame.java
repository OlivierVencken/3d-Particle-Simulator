package com.particle.sim.ui;

import java.util.Objects;

/** Layout and coordinate-space decisions shared by scene and UI rendering for one frame. */
public record PreparedUiFrame(
        UiDisplayMetrics displayMetrics,
        UILayout layout,
        FramebufferViewport simulationViewport,
        boolean uiVisible) {

    public PreparedUiFrame {
        Objects.requireNonNull(displayMetrics, "displayMetrics");
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(simulationViewport, "simulationViewport");
    }
}
