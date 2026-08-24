package com.particle.sim.ui.components;

import imgui.ImGui;
import imgui.flag.ImGuiFocusedFlags;
import imgui.flag.ImGuiKey;
import imgui.type.ImBoolean;

/**
 * Base for centered, non-modal popup windows with a title bar and close button.
 */
public abstract class PopupWindow extends PopupBase {
    private final ImBoolean open = new ImBoolean(false);

    protected PopupWindow(String title, String id, float defaultWidth, float defaultHeight) {
        super(title, id, defaultWidth, defaultHeight);
    }

    protected PopupWindow(String title, String id, float defaultWidth, float defaultHeight,
            String returnFocusWindow) {
        super(title, id, defaultWidth, defaultHeight, returnFocusWindow);
    }

    public final void open() {
        open.set(true);
    }

    public final boolean isOpen() {
        return open.get();
    }

    public final void render() {
        if (!open.get()) {
            restoreFocusIfRequested();
            return;
        }

        boolean openAtFrameStart = open.get();
        prepareWindow();
        pushPopupStyle();
        if (ImGui.begin(label(), open, resolvedWindowFlags())) {
            renderContent();
            if (ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows)
                    && ImGui.isKeyPressed(ImGuiKey.Escape)) {
                close();
            }
        }
        ImGui.end();
        popPopupStyle();
        if (openAtFrameStart && !open.get()) {
            requestFocusRestore();
        }
        restoreFocusIfRequested();
    }

    protected final void close() {
        open.set(false);
        requestFocusRestore();
    }
}
