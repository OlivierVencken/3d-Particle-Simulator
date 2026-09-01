package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.Color;
import com.particle.sim.ui.theme.Colors;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

/** Shared placement and styling for modal and non-modal popup windows. */
public abstract class PopupBase {
    private static final Color OPAQUE_BACKGROUND = Colors.BACKGROUND_WINDOW.withAlpha(1.0f);
    private final String label;
    private final float defaultWidth;
    private final float defaultHeight;
    private final String returnFocusWindow;
    private boolean focusRestoreRequested;

    protected PopupBase(String title, String id, float defaultWidth, float defaultHeight) {
        this(title, id, defaultWidth, defaultHeight, null);
    }

    protected PopupBase(
            String title,
            String id,
            float defaultWidth,
            float defaultHeight,
            String returnFocusWindow) {
        this.label = title + "###" + id;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.returnFocusWindow = returnFocusWindow;
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
        return Theme.tokens().radiusLg();
    }

    protected int windowFlags() {
        return ImGuiWindowFlags.None;
    }

    protected final String label() {
        return label;
    }

    protected final void prepareWindow() {
        DesignTokens tokens = Theme.tokens();
        ImGuiViewport viewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(
                viewport.getWorkCenterX(),
                viewport.getWorkCenterY(),
                ImGuiCond.Appearing,
                0.5f,
                0.5f);

        if (defaultWidth > 0.0f && defaultHeight > 0.0f) {
            ImGui.setNextWindowSize(
                    tokens.dp(defaultWidth), tokens.dp(defaultHeight), ImGuiCond.Appearing);
        }

        if (resizable()) {
            ImGui.setNextWindowSizeConstraints(
                    tokens.dp(minimumWidth()),
                    tokens.dp(minimumHeight()),
                    scaledMaximum(tokens, maximumWidth()),
                    scaledMaximum(tokens, maximumHeight()));
        }
    }

    private float scaledMaximum(DesignTokens tokens, float maximum) {
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

    protected final void requestFocusRestore() {
        focusRestoreRequested = true;
    }

    protected final void restoreFocusIfRequested() {
        if (!focusRestoreRequested) {
            return;
        }
        focusRestoreRequested = false;
        if (returnFocusWindow != null && !returnFocusWindow.isBlank()) {
            ImGui.setWindowFocus(returnFocusWindow);
        }
    }
}
