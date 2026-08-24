package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIComponentVariant;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;
import imgui.flag.ImGuiCol;

/** Shared popup menu items and separators with stable identifiers. */
public final class UIMenu {
    private UIMenu() {
    }

    public static boolean beginAnchored(String id, float x, float y) {
        if (ImGui.isPopupOpen(id)) {
            ImGui.setNextWindowPos(x, y);
        }
        return ImGui.beginPopup(id);
    }

    public static boolean item(String label, String id) {
        return item(label, id, false, true);
    }

    public static boolean item(String label, String id, boolean selected, boolean enabled) {
        var palette = UITheme.palette(selected ? UIComponentVariant.SELECTED : UIComponentVariant.SECONDARY);
        ImGui.pushStyleColor(ImGuiCol.Header, palette.background().vec4());
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered, palette.hovered().vec4());
        ImGui.pushStyleColor(ImGuiCol.HeaderActive, palette.active().vec4());
        try {
            return ImGui.menuItem(UIButton.itemLabel(label, "menu-item-" + id), "", selected, enabled);
        } finally {
            ImGui.popStyleColor(3);
        }
    }

    public static void separator() {
        ImGui.separator();
    }
}
