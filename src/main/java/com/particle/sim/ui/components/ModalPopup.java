package com.particle.sim.ui.components;

import imgui.ImGui;
import imgui.flag.ImGuiFocusedFlags;
import imgui.flag.ImGuiKey;

/**
 * Base for modal popups that require a decision before the user can continue.
 */
public abstract class ModalPopup extends PopupBase {
    private boolean openRequested;
    private boolean open;

    protected ModalPopup(String title, String id) {
        super(title, id, 0.0f, 0.0f);
    }

    protected ModalPopup(String title, String id, float defaultWidth, float defaultHeight) {
        super(title, id, defaultWidth, defaultHeight);
    }

    protected ModalPopup(String title, String id, String returnFocusWindow) {
        super(title, id, 0.0f, 0.0f, returnFocusWindow);
    }

    protected ModalPopup(String title, String id, float defaultWidth, float defaultHeight,
            String returnFocusWindow) {
        super(title, id, defaultWidth, defaultHeight, returnFocusWindow);
    }

    public final void open() {
        openRequested = true;
        open = true;
    }

    public final boolean isOpen() {
        return openRequested || open;
    }

    public final void render() {
        if (openRequested) {
            ImGui.openPopup(label());
            openRequested = false;
        }

        if (!ImGui.isPopupOpen(label())) {
            if (open) {
                requestFocusRestore();
            }
            open = false;
            restoreFocusIfRequested();
            return;
        }

        prepareWindow();
        pushPopupStyle();
        if (ImGui.beginPopupModal(label(), resolvedWindowFlags())) {
            renderContent();
            if (ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows)
                    && ImGui.isKeyPressed(ImGuiKey.Escape)) {
                close();
            }
            ImGui.endPopup();
        }
        popPopupStyle();
        restoreFocusIfRequested();
    }

    protected final void close() {
        open = false;
        ImGui.closeCurrentPopup();
        requestFocusRestore();
    }
}
