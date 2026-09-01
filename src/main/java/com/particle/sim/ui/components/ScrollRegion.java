package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.Colors;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

/** Independently scrolling content region intended to sit below a fixed header. */
public final class ScrollRegion {
    private ScrollRegion() {
    }

    public static void render(String id, Runnable content) {
        ImGui.pushStyleColor(ImGuiCol.ChildBg, Colors.TRANSPARENT.vec4());
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
