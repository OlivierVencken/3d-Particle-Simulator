package com.particle.sim.ui.commandbar;

import com.particle.sim.AppInfo;
import com.particle.sim.ui.components.PopupWindow;
import com.particle.sim.ui.theme.UIFonts;
import imgui.ImGui;

import java.awt.Desktop;
import java.net.URI;

final class AboutPopup extends PopupWindow {
    private static final String REPOSITORY_URL =
            "https://github.com/OlivierVencken/3d-Particle-Simulator";

    private String linkError;

    AboutPopup() {
        super("About 3D Particle Simulator", "about-popup", 430.0f, 230.0f);
    }

    @Override
    protected void renderContent() {
        ImGui.pushFont(UIFonts.title());
        ImGui.textUnformatted("3D Particle Simulator");
        ImGui.popFont();
        ImGui.pushFont(UIFonts.medium());
        ImGui.textDisabled("Version " + AppInfo.version());
        ImGui.separator();
        ImGui.textWrapped("An interactive GPU-powered 3D particle-life sandbox.");

        ImGui.spacing();
        ImGui.textDisabled(REPOSITORY_URL);
        if (ImGui.button("Open GitHub", 112.0f, 32.0f)) {
            openRepository();
        }
        ImGui.sameLine();
        if (ImGui.button("Copy link", 96.0f, 32.0f)) {
            ImGui.setClipboardText(REPOSITORY_URL);
            linkError = null;
        }
        if (linkError != null) {
            ImGui.textWrapped(linkError);
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
