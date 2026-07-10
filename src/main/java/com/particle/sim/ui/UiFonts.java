package com.particle.sim.ui;

import imgui.ImFont;

final class UiFonts {
    private static ImFont medium;
    private static ImFont section;
    private static ImFont title;

    private UiFonts() {
    }

    static void setMedium(ImFont font) {
        medium = font;
    }

    static ImFont medium() {
        return medium;
    }

    static void setSection(ImFont font) { section = font; }
    static ImFont section() { return section; }
    static void setTitle(ImFont font) { title = font; }
    static ImFont title() { return title; }
}
