package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import imgui.ImGui;
import imgui.type.ImBoolean;

final class SimulationMenuBar {
    void render(GpuParticleSystem particles, CameraController camera, SettingsSidebar settingsSidebar,
            ImBoolean showDebugPanel, Runnable settingsChanged, Runnable resetSettings, Runnable exitApplication,
            Runnable hideUi) {
        if (!ImGui.beginMainMenuBar()) {
            return;
        }

        renderSimulationMenu(particles, camera, settingsSidebar, settingsChanged, resetSettings, exitApplication);
        renderViewMenu(showDebugPanel, hideUi);
        renderThemeMenu();

        ImGui.endMainMenuBar();
    }

    private void renderSimulationMenu(GpuParticleSystem particles, CameraController camera,
            SettingsSidebar settingsSidebar, Runnable settingsChanged, Runnable resetSettings, Runnable exitApplication) {
        if (!ImGui.beginMenu("Simulation")) {
            return;
        }

        if (ImGui.menuItem(settingsSidebar.isPaused() ? "Resume" : "Pause")) {
            settingsSidebar.setPaused(!settingsSidebar.isPaused());
            settingsChanged.run();
        }
        if (ImGui.menuItem("Reset particles")) {
            particles.reset();
        }
        if (ImGui.menuItem("Reset camera")) {
            camera.reset();
        }
        if (ImGui.menuItem("Reset simulation settings")) {
            resetSettings.run();
        }
        ImGui.separator();
        if (ImGui.menuItem("Exit")) {
            exitApplication.run();
        }
        ImGui.endMenu();
    }

    private void renderViewMenu(ImBoolean showDebugPanel, Runnable hideUi) {
        if (ImGui.beginMenu("View")) {
            ImGui.menuItem("Debug", "", showDebugPanel);
            if (ImGui.menuItem("Hide UI")) {
                hideUi.run();
            }
            ImGui.endMenu();
        }
    }

    private void renderThemeMenu() {
        if (ImGui.beginMenu("Theme")) {
            ImGui.menuItem("Dark", "", true, false);
            ImGui.endMenu();
        }
    }
}
