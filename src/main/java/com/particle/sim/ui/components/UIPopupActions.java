package com.particle.sim.ui.components;

import imgui.ImGui;

/** Consistent separation and spacing for popup confirmation actions. */
public final class UIPopupActions {
    private UIPopupActions() {
    }

    public static void row(Runnable actions) {
        UIText.divider();
        ImGui.beginGroup();
        try {
            actions.run();
        } finally {
            ImGui.endGroup();
        }
    }
}
