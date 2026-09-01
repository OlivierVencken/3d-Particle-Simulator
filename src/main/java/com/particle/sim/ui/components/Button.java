package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.ComponentPalette;
import com.particle.sim.ui.theme.ComponentVariant;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;

/** Native ImGui buttons with the application's semantic visual variants. */
public final class Button {
    private Button() {
    }

    public static boolean text(String label, String id, ComponentVariant variant) {
        return text(label, id, variant, 0.0f, Theme.tokens().controlHeight(), true);
    }

    public static boolean text(String label, String id, ComponentVariant variant, float width, float height) {
        return text(label, id, variant, width, height, true);
    }

    public static boolean text(String label, String id, ComponentVariant variant,
            float width, float height, boolean enabled) {
        pushStyle(enabled ? variant : ComponentVariant.DISABLED);
        ImGui.beginDisabled(!enabled);
        boolean clicked;
        try {
            clicked = ImGui.button(itemLabel(label, "button-" + id), width, height);
        } finally {
            ImGui.endDisabled();
            popStyle();
        }
        return enabled && clicked;
    }

    public static boolean icon(String accessibleLabel, String id, long textureId, boolean selected,
            boolean enabled) {
        DesignTokens tokens = Theme.tokens();
        ComponentVariant variant = selected ? ComponentVariant.SELECTED : ComponentVariant.GHOST;
        pushStyle(enabled ? variant : ComponentVariant.DISABLED);
        ImGui.beginDisabled(!enabled);
        boolean clicked;
        try {
            clicked = ImGui.button(itemLabel("", "icon-button-" + id),
                    tokens.compactControlHeight(), tokens.compactControlHeight());
        } finally {
            ImGui.endDisabled();
            popStyle();
        }

        float centerX = (ImGui.getItemRectMinX() + ImGui.getItemRectMaxX()) * 0.5f;
        float centerY = (ImGui.getItemRectMinY() + ImGui.getItemRectMaxY()) * 0.5f;
        float iconSize = tokens.iconSize();
        int color = ImGui.getColorU32(enabled ? ImGuiCol.Text : ImGuiCol.TextDisabled);
        ImGui.getWindowDrawList().addImage(textureId,
                centerX - iconSize * 0.5f, centerY - iconSize * 0.5f,
                centerX + iconSize * 0.5f, centerY + iconSize * 0.5f,
                0.0f, 0.0f, 1.0f, 1.0f, color);
        return enabled && clicked;
    }

    static String itemLabel(String visibleLabel, String stableId) {
        return visibleLabel + "###" + stableId;
    }

    private static void pushStyle(ComponentVariant variant) {
        ComponentPalette palette = Theme.palette(variant);
        ImGui.pushStyleColor(ImGuiCol.Button, palette.background().vec4());
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, palette.hovered().vec4());
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, palette.active().vec4());
        ImGui.pushStyleColor(ImGuiCol.Border, palette.border().vec4());
        ImGui.pushStyleColor(ImGuiCol.Text, palette.text().vec4());
        ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize,
                variant == ComponentVariant.GHOST ? 0.0f : Theme.tokens().borderWidth());
    }

    private static void popStyle() {
        ImGui.popStyleVar();
        ImGui.popStyleColor(5);
    }
}
