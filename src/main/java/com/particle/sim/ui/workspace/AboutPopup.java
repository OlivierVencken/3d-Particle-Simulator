package com.particle.sim.ui.workspace;

import com.particle.sim.AppInfo;
import com.particle.sim.ui.theme.UIFonts;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;

import java.awt.Desktop;
import java.net.URI;

final class AboutPopup {
    private static final String REPOSITORY_URL =
            "https://github.com/OlivierVencken/3d-Particle-Simulator";

    private String linkError;

    void render(ImBoolean open) {
        if (!open.get()) {
            return;
        }

        ImGui.setNextWindowSize(430.0f, 230.0f, ImGuiCond.FirstUseEver);
        if (ImGui.begin("About 3D Particle Simulator", open)) {
            ImGui.pushFont(UIFonts.title());
            ImGui.textUnformatted("3D Particle Simulator");
            ImGui.popFont();
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
        }
        ImGui.end();
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
