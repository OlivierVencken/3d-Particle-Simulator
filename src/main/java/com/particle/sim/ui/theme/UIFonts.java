package com.particle.sim.ui.theme;

import imgui.ImFont;

public final class UIFonts {
    private static ImFont medium;
    private static ImFont section;
    private static ImFont title;

    private UIFonts() {
    }

    public static void setMedium(ImFont font) {
        medium = font;
    }

    public static ImFont medium() {
        return medium;
    }

    public static void setSection(ImFont font) {
        section = font;
    }

    public static ImFont section() {
        return section;
    }

    public static void setTitle(ImFont font) {
        title = font;
    }

    public static ImFont title() {
        return title;
    }
}
