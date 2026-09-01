package com.particle.sim.ui.commandbar;

import com.particle.sim.AppInfo;
import com.particle.sim.ui.components.PopupWindow;
import com.particle.sim.ui.components.Button;
import com.particle.sim.ui.components.Text;
import com.particle.sim.ui.theme.ComponentVariant;
import com.particle.sim.ui.theme.Fonts;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;

import java.awt.Desktop;
import java.net.URI;

final class AboutPopup extends PopupWindow {
    private static final String REPOSITORY_URL =
            "https://github.com/OlivierVencken/3d-Particle-Simulator";

    private String linkError;

    AboutPopup() {
        super("About 3D Particle Simulator", "about-popup",
                DesignTokens.unscaled().popupWidth(), DesignTokens.unscaled().aboutPopupHeight(),
                "##command-bar");
    }

    @Override
    protected void renderContent() {
        DesignTokens tokens = Theme.tokens();
        ImGui.pushFont(Fonts.title());
        ImGui.textUnformatted("3D Particle Simulator");
        ImGui.popFont();
        ImGui.pushFont(Fonts.medium());
        ImGui.textDisabled("Version " + AppInfo.version());
        Text.divider();
        ImGui.textWrapped("An interactive GPU-powered 3D particle-life sandbox.");

        ImGui.spacing();
        ImGui.textDisabled(REPOSITORY_URL);
        if (Button.text("Open GitHub", "about-open-github", ComponentVariant.PRIMARY,
                tokens.buttonWidthXl(), tokens.controlHeight())) {
            openRepository();
        }
        ImGui.sameLine();
        if (Button.text("Copy link", "about-copy-link", ComponentVariant.SECONDARY,
                tokens.buttonWidthLg(), tokens.controlHeight())) {
            ImGui.setClipboardText(REPOSITORY_URL);
            linkError = null;
        }
        if (linkError != null) {
            Text.error(linkError);
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
