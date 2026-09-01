package com.particle.sim.ui.commandbar;

import com.particle.sim.ui.components.PopupWindow;
import com.particle.sim.ui.components.Button;
import com.particle.sim.ui.components.Text;
import com.particle.sim.ui.theme.ComponentVariant;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Fonts;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;

final class ErrorPopup extends PopupWindow {
    private String summary = "An unexpected error occurred.";
    private String details = "";

    ErrorPopup() {
        super("Action failed", "action-error-popup",
                DesignTokens.unscaled().popupWidth(),
                DesignTokens.unscaled().errorPopupHeight(),
                "##command-bar");
    }

    void open(String summary, String details) {
        this.summary = normalized(summary, "An unexpected error occurred.");
        this.details = normalized(details, "Try again or choose a different file.");
        open();
    }

    @Override
    protected void renderContent() {
        DesignTokens tokens = Theme.tokens();
        ImGui.pushFont(Fonts.medium());
        try {
            Text.error(summary);
            ImGui.spacing();
            ImGui.textWrapped(details);
            ImGui.spacing();
            if (Button.text("Close", "close-action-error", ComponentVariant.PRIMARY,
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
