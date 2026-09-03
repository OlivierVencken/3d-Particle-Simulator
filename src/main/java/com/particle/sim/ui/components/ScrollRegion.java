package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.Colors;
import imgui.ImGui;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

/**
 * Independently scrolling content region intended to sit below a fixed header.
 *
 * <p>The child spans the full parent window width while retaining the parent's horizontal content
 * inset. This keeps its scrollbar against the window edge without changing the width or alignment
 * of the content inside it.
 */
public final class ScrollRegion {
    private ScrollRegion() {}

    public static void render(String id, Runnable content) {
        float horizontalInset = ImGui.getStyle().getWindowPaddingX();
        float width = ImGui.getWindowWidth();

        ImGui.pushStyleColor(ImGuiCol.ChildBg, Colors.TRANSPARENT.vec4());
        ImGui.pushStyleColor(ImGuiCol.ScrollbarBg, Colors.TRANSPARENT.vec4());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, horizontalInset, 0.0f);
        ImGui.setCursorPosX(0.0f);
        boolean visible =
                ImGui.beginChild(
                        "###scroll-region-" + id,
                        width,
                        0.0f,
                        ImGuiChildFlags.AlwaysUseWindowPadding,
                        ImGuiWindowFlags.None);
        ImGui.popStyleVar();
        try {
            if (visible) {
                content.run();
            }
        } finally {
            ImGui.endChild();
            ImGui.popStyleColor(2);
        }
    }
}
