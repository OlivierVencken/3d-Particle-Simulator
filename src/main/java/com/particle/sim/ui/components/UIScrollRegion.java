package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIColors;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

/** Independently scrolling content region intended to sit below a fixed header. */
public final class UIScrollRegion {
    private UIScrollRegion() {
    }

    public static void render(String id, Runnable content) {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, UIColors.TRANSPARENT.vec4());
        boolean visible = ImGui.beginChild(
                "###scroll-region-" + id, 0.0f, 0.0f, false, ImGuiWindowFlags.None);
        try {
            if (visible) {
                content.run();
            }
        } finally {
            ImGui.endChild();
            ImGui.popStyleColor();
        }
    }
}
