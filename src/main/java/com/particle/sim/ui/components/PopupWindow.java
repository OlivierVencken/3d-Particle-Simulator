package com.particle.sim.ui.components;

import imgui.ImGui;
import imgui.type.ImBoolean;

/**
 * Base for centered, non-modal popup windows with a title bar and close button.
 */
public abstract class PopupWindow extends PopupBase {
    private final ImBoolean open = new ImBoolean(false);

    protected PopupWindow(String title, String id, float defaultWidth, float defaultHeight) {
        super(title, id, defaultWidth, defaultHeight);
    }

    public final void open() {
        open.set(true);
    }

    public final void render() {
        if (!open.get()) {
            return;
        }

        prepareWindow();
        pushPopupStyle();
        if (ImGui.begin(label(), open, resolvedWindowFlags())) {
            renderContent();
        }
        ImGui.end();
        popPopupStyle();
    }

    protected final void close() {
        open.set(false);
    }
}
