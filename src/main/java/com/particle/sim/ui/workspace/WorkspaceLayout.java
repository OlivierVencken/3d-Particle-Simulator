package com.particle.sim.ui.workspace;

record WorkspaceLayout(
        Mode mode,
        Panel commandBar,
        Panel navigation,
        Panel simulation,
        Panel inspector,
        Panel statusBar) {

    enum Mode {
        WIDE,
        MEDIUM,
        COMPACT,
        FOCUS
    }

    record Panel(float x, float y, float width, float height) {
        static Panel hidden() {
            return new Panel(0.0f, 0.0f, 0.0f, 0.0f);
        }

        boolean visible() {
            return width > 0.0f && height > 0.0f;
        }

        float right() {
            return x + width;
        }

        float bottom() {
            return y + height;
        }
    }
}
