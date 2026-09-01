package com.particle.sim.ui.components;

import imgui.ImGui;

/** Consistent separation and spacing for popup confirmation actions. */
public final class PopupActions {
    private PopupActions() {}

    public static void row(Runnable actions) {
        Text.divider();
        ImGui.beginGroup();
        try {
            actions.run();
        } finally {
            ImGui.endGroup();
        }
    }
}
