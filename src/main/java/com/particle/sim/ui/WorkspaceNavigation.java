package com.particle.sim.ui;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

final class WorkspaceNavigation {
    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings;

    void render(WorkspaceLayout.Panel panel, WorkspaceState state) {
        if (!panel.visible()) {
            return;
        }
        ImGui.setNextWindowPos(panel.x(), panel.y());
        ImGui.setNextWindowSize(panel.width(), panel.height());
        if (ImGui.begin("##workspace-navigation", WINDOW_FLAGS)) {
            ImGui.textDisabled("WORKSPACE");
            ImGui.spacing();
            for (UiSection section : UiSection.values()) {
                ImGui.pushFont(UiFonts.medium());
                if (ImGui.selectable(section.label() + "##navigation-" + section.name(),
                        state.activeSection() == section, 0, ImGui.getContentRegionAvailX(), 36.0f)) {
                    state.select(section);
                }
                ImGui.popFont();
            }
        }
        ImGui.end();
    }
}
