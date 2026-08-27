package com.particle.sim.ui.commandbar;

import com.particle.sim.ui.components.PopupWindow;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UIText;
import com.particle.sim.ui.theme.UIComponentVariant;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UIFonts;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;

final class ErrorPopup extends PopupWindow {
    private String summary = "An unexpected error occurred.";
    private String details = "";

    ErrorPopup() {
        super("Action failed", "action-error-popup",
                UIDesignTokens.unscaled().popupWidth(),
                UIDesignTokens.unscaled().errorPopupHeight(),
                "##command-bar");
    }

    void open(String summary, String details) {
        this.summary = normalized(summary, "An unexpected error occurred.");
        this.details = normalized(details, "Try again or choose a different file.");
        open();
    }

    @Override
    protected void renderContent() {
        UIDesignTokens tokens = UITheme.tokens();
        ImGui.pushFont(UIFonts.medium());
        try {
            UIText.error(summary);
            ImGui.spacing();
            ImGui.textWrapped(details);
            ImGui.spacing();
            if (UIButton.text("Close", "close-action-error", UIComponentVariant.PRIMARY,
                    tokens.buttonWidthMd(), tokens.controlHeight())) {
                close();
            }
        } finally {
            ImGui.popFont();
        }
    }

    static String normalized(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
