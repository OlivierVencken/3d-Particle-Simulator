package com.particle.sim.ui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
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
            centerText("WORKSPACE");
            ImGui.dummy(0.0f, 8.0f);
            for (UiSection section : UiSection.values()) {
                boolean selected = state.activeSection() == section;
                ImVec2 itemOrigin = ImGui.getCursorScreenPos();
                float itemWidth = ImGui.getContentRegionAvailX();
                ImGui.pushFont(UiFonts.section());
                ImGui.pushStyleVar(ImGuiStyleVar.SelectableTextAlign, 0.5f, 0.5f);
                if (selected) {
                    int selectedBackground = ImGui.getColorU32(UiPalette.SURFACE_ACTIVE.vec4());
                    int accent = ImGui.getColorU32(UiPalette.ACCENT.vec4());
                    ImGui.getWindowDrawList().addRectFilled(
                            itemOrigin.x + 4.0f, itemOrigin.y,
                            itemOrigin.x + itemWidth - 4.0f, itemOrigin.y + 44.0f,
                            selectedBackground, 5.0f);
                    ImGui.getWindowDrawList().addRectFilled(
                            itemOrigin.x + 4.0f, itemOrigin.y + 7.0f,
                            itemOrigin.x + 7.0f, itemOrigin.y + 37.0f,
                            accent, 2.0f);
                    ImGui.pushStyleColor(ImGuiCol.Header, UiPalette.CLEAR.vec4());
                    ImGui.pushStyleColor(ImGuiCol.HeaderHovered, UiPalette.CLEAR.vec4());
                    ImGui.pushStyleColor(ImGuiCol.Text, UiPalette.ACCENT_BRIGHT.vec4());
                }
                if (ImGui.selectable(section.label() + "##navigation-" + section.name(),
                        selected, 0, itemWidth, 44.0f)) {
                    state.select(section);
                }
                if (selected) {
                    ImGui.popStyleColor(3);
                }
                ImGui.popStyleVar();
                ImGui.popFont();
                ImGui.dummy(0.0f, 4.0f);
            }
        }
        ImGui.end();
    }

    private void centerText(String text) {
        float textWidth = ImGui.calcTextSize(text).x;
        ImGui.setCursorPosX(Math.max(ImGui.getCursorPosX(),
                (ImGui.getWindowContentRegionMinX() + ImGui.getWindowContentRegionMaxX() - textWidth) * 0.5f));
        ImGui.textDisabled(text);
    }
}
