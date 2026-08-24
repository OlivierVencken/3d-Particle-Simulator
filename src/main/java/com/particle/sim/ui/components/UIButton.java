package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIComponentPalette;
import com.particle.sim.ui.theme.UIComponentVariant;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;

/** Native ImGui buttons with the application's semantic visual variants. */
public final class UIButton {
    private UIButton() {
    }

    public static boolean text(String label, String id, UIComponentVariant variant) {
        return text(label, id, variant, 0.0f, UITheme.tokens().controlHeight(), true);
    }

    public static boolean text(String label, String id, UIComponentVariant variant, float width, float height) {
        return text(label, id, variant, width, height, true);
    }

    public static boolean text(String label, String id, UIComponentVariant variant,
            float width, float height, boolean enabled) {
        pushStyle(enabled ? variant : UIComponentVariant.DISABLED);
        ImGui.beginDisabled(!enabled);
        boolean clicked;
        try {
            clicked = ImGui.button(itemLabel(label, "button-" + id), width, height);
        } finally {
            ImGui.endDisabled();
            popStyle();
        }
        if (labelOverflows(label, width, UITheme.tokens())) {
            UITooltip.forLastItem(label);
        }
        return enabled && clicked;
    }

    public static boolean icon(String accessibleLabel, String id, long textureId, boolean selected,
            boolean enabled) {
        UIDesignTokens tokens = UITheme.tokens();
        UIComponentVariant variant = selected ? UIComponentVariant.SELECTED : UIComponentVariant.GHOST;
        pushStyle(enabled ? variant : UIComponentVariant.DISABLED);
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
        UITooltip.forLastItem(accessibleLabel);
        return enabled && clicked;
    }

    static String itemLabel(String visibleLabel, String stableId) {
        return visibleLabel + "###" + stableId;
    }

    static boolean labelOverflows(String label, float width, UIDesignTokens tokens) {
        if (label == null || width <= 0.0f) {
            return false;
        }
        return ImGui.calcTextSize(label).x > Math.max(0.0f, width - tokens.frameInsetHorizontal() * 2.0f);
    }

    private static void pushStyle(UIComponentVariant variant) {
        UIComponentPalette palette = UITheme.palette(variant);
        ImGui.pushStyleColor(ImGuiCol.Button, palette.background().vec4());
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, palette.hovered().vec4());
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, palette.active().vec4());
        ImGui.pushStyleColor(ImGuiCol.Border, palette.border().vec4());
        ImGui.pushStyleColor(ImGuiCol.Text, palette.text().vec4());
        ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize,
                variant == UIComponentVariant.GHOST ? 0.0f : UITheme.tokens().borderWidth());
    }

    private static void popStyle() {
        ImGui.popStyleVar();
        ImGui.popStyleColor(5);
    }
}
