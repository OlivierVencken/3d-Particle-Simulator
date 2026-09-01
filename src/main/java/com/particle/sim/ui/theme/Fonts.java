package com.particle.sim.ui.theme;

import imgui.ImFont;

public final class Fonts {
    private static ImFont body;
    private static ImFont medium;
    private static ImFont commandBar;
    private static ImFont section;
    private static ImFont title;

    private Fonts() {}

    public static void setBody(ImFont font) {
        body = font;
    }

    public static ImFont body() {
        return body;
    }

    public static void setMedium(ImFont font) {
        medium = font;
    }

    public static ImFont medium() {
        return medium;
    }

    public static void setCommandBar(ImFont font) {
        commandBar = font;
    }

    public static ImFont commandBar() {
        return commandBar;
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

    public static void clear() {
        body = null;
        medium = null;
        commandBar = null;
        section = null;
        title = null;
    }
}
