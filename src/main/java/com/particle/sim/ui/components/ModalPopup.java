package com.particle.sim.ui.components;

import imgui.ImGui;

/**
 * Base for modal popups that require a decision before the user can continue.
 */
public abstract class ModalPopup extends PopupBase {
    private boolean openRequested;

    protected ModalPopup(String title, String id) {
        super(title, id, 0.0f, 0.0f);
    }

    protected ModalPopup(String title, String id, float defaultWidth, float defaultHeight) {
        super(title, id, defaultWidth, defaultHeight);
    }

    public final void open() {
        openRequested = true;
    }

    public final void render() {
        if (openRequested) {
            ImGui.openPopup(label());
            openRequested = false;
        }

        if (!ImGui.isPopupOpen(label())) {
            return;
        }

        prepareWindow();
        pushPopupStyle();
        if (ImGui.beginPopupModal(label(), resolvedWindowFlags())) {
            renderContent();
            ImGui.endPopup();
        }
        popPopupStyle();
    }

    protected final void close() {
        ImGui.closeCurrentPopup();
    }
}
