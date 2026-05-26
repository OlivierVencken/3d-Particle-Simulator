package com.particle.sim.ui;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

final class FpsPanel {
    private static final float MARGIN = 6.0f;
    private static final float MENU_BAR_HEIGHT_FALLBACK = 24.0f;
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoDecoration
            | ImGuiWindowFlags.AlwaysAutoResize
            | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoFocusOnAppearing
            | ImGuiWindowFlags.NoNav
            | ImGuiWindowFlags.NoInputs;

    void render(float currentFps) {
        float displayWidth = ImGui.getIO().getDisplaySizeX();
        float menuBarHeight = Math.max(MENU_BAR_HEIGHT_FALLBACK, ImGui.getFrameHeight());

        ImGui.setNextWindowPos(displayWidth - MARGIN, menuBarHeight + MARGIN,
                ImGuiCond.Always, 1.0f, 0.0f);
        ImGui.setNextWindowBgAlpha(0.48f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8.0f, 5.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 4.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f);
        if (ImGui.begin("##fps-overlay", WINDOW_FLAGS)) {
            ImGui.text("FPS %.0f".formatted(currentFps));
        }
        ImGui.end();
        ImGui.popStyleVar(3);
    }
}
