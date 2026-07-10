package com.particle.sim.ui;

import imgui.ImFont;

final class UiFonts {
    private static ImFont medium;

    private UiFonts() {
    }

    static void setMedium(ImFont font) {
        medium = font;
    }

    static ImFont medium() {
        return medium;
    }
}
