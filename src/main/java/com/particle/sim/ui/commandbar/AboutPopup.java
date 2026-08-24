package com.particle.sim.ui.commandbar;

import com.particle.sim.AppInfo;
import com.particle.sim.ui.components.PopupWindow;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UIText;
import com.particle.sim.ui.theme.UIComponentVariant;
import com.particle.sim.ui.theme.UIFonts;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;

import java.awt.Desktop;
import java.net.URI;

final class AboutPopup extends PopupWindow {
    private static final String REPOSITORY_URL =
            "https://github.com/OlivierVencken/3d-Particle-Simulator";

    private String linkError;

    AboutPopup() {
        super("About 3D Particle Simulator", "about-popup",
                UIDesignTokens.unscaled().popupWidth(), UIDesignTokens.unscaled().aboutPopupHeight());
    }

    @Override
    protected void renderContent() {
        UIDesignTokens tokens = UITheme.tokens();
        ImGui.pushFont(UIFonts.title());
        ImGui.textUnformatted("3D Particle Simulator");
        ImGui.popFont();
        ImGui.pushFont(UIFonts.medium());
        ImGui.textDisabled("Version " + AppInfo.version());
        UIText.divider();
        ImGui.textWrapped("An interactive GPU-powered 3D particle-life sandbox.");

        ImGui.spacing();
        ImGui.textDisabled(REPOSITORY_URL);
        if (UIButton.text("Open GitHub", "about-open-github", UIComponentVariant.PRIMARY,
                tokens.buttonWidthXl(), tokens.controlHeight())) {
            openRepository();
        }
        ImGui.sameLine();
        if (UIButton.text("Copy link", "about-copy-link", UIComponentVariant.SECONDARY,
                tokens.buttonWidthLg(), tokens.controlHeight())) {
            ImGui.setClipboardText(REPOSITORY_URL);
            linkError = null;
        }
        if (linkError != null) {
            UIText.error(linkError);
        }
        ImGui.popFont();
    }

    private void openRepository() {
        try {
            if (!Desktop.isDesktopSupported()) {
                linkError = "Could not open a browser. Copy the link above instead.";
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                linkError = "Could not open a browser. Copy the link above instead.";
                return;
            }
            desktop.browse(URI.create(REPOSITORY_URL));
            linkError = null;
        } catch (RuntimeException | java.io.IOException exception) {
            linkError = "Could not open a browser. Copy the link above instead.";
        }
    }
}
