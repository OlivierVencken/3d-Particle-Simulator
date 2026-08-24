package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIColor;
import com.particle.sim.ui.theme.UIColors;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

/**
 * Shared placement and styling for modal and non-modal popup windows.
 */
public abstract class PopupBase {
    private static final UIColor OPAQUE_BACKGROUND = UIColors.BACKGROUND_WINDOW.withAlpha(1.0f);

    private final String label;
    private final float defaultWidth;
    private final float defaultHeight;

    protected PopupBase(String title, String id, float defaultWidth, float defaultHeight) {
        this.label = title + "###" + id;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
    }

    protected abstract void renderContent();

    protected boolean resizable() {
        return false;
    }

    protected float minimumWidth() {
        return defaultWidth;
    }

    protected float minimumHeight() {
        return defaultHeight;
    }

    protected float maximumWidth() {
        return Float.MAX_VALUE;
    }

    protected float maximumHeight() {
        return Float.MAX_VALUE;
    }

    protected float windowRounding() {
        return UITheme.tokens().radiusLg();
    }

    protected int windowFlags() {
        return ImGuiWindowFlags.None;
    }

    protected final String label() {
        return label;
    }

    protected final void prepareWindow() {
        UIDesignTokens tokens = UITheme.tokens();
        ImGuiViewport viewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(
                viewport.getWorkCenterX(), viewport.getWorkCenterY(),
                ImGuiCond.Appearing, 0.5f, 0.5f);

        if (defaultWidth > 0.0f && defaultHeight > 0.0f) {
            ImGui.setNextWindowSize(tokens.dp(defaultWidth), tokens.dp(defaultHeight), ImGuiCond.Appearing);
        }

        if (resizable()) {
            ImGui.setNextWindowSizeConstraints(
                    tokens.dp(minimumWidth()), tokens.dp(minimumHeight()),
                    scaledMaximum(tokens, maximumWidth()), scaledMaximum(tokens, maximumHeight()));
        }
    }

    private float scaledMaximum(UIDesignTokens tokens, float maximum) {
        return maximum == Float.MAX_VALUE ? Float.MAX_VALUE : tokens.dp(maximum);
    }

    protected final int resolvedWindowFlags() {
        int flags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoSavedSettings | windowFlags();
        return resizable() ? flags : flags | ImGuiWindowFlags.NoResize;
    }

    protected final void pushPopupStyle() {
        ImGui.pushStyleColor(ImGuiCol.WindowBg, OPAQUE_BACKGROUND.vec4());
        ImGui.pushStyleColor(ImGuiCol.PopupBg, OPAQUE_BACKGROUND.vec4());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, windowRounding());
    }

    protected final void popPopupStyle() {
        ImGui.popStyleVar();
        ImGui.popStyleColor(2);
    }
}
