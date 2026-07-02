package com.particle.sim.ui;

import com.particle.sim.camera.CameraController;
import com.particle.sim.particles.GpuParticleSystem;
import imgui.ImGui;
import imgui.type.ImBoolean;

final class SimulationMenuBar {
    private final ImBoolean showHotkeyPopup = new ImBoolean(false);
    private final HotkeyPopup hotkeyPopup = new HotkeyPopup();

    void render(GpuParticleSystem particles, CameraController camera, SettingsSidebar settingsSidebar,
            ImBoolean showDebugPanel, Runnable settingsChanged, Runnable resetSettings, Runnable savePreset,
            Runnable loadPreset, Runnable exitApplication, Runnable hideUi) {
        if (!ImGui.beginMainMenuBar()) {
            hotkeyPopup.render(showHotkeyPopup);
            return;
        }

        renderSimulationMenu(particles, camera, settingsSidebar, settingsChanged, resetSettings, savePreset, loadPreset,
                exitApplication);
        renderViewMenu(showDebugPanel, hideUi);
        renderThemeMenu();
        renderInfoMenu();

        ImGui.endMainMenuBar();
        hotkeyPopup.render(showHotkeyPopup);
    }

    private void renderSimulationMenu(GpuParticleSystem particles, CameraController camera,
            SettingsSidebar settingsSidebar, Runnable settingsChanged, Runnable resetSettings, Runnable savePreset,
            Runnable loadPreset, Runnable exitApplication) {
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
        if (ImGui.menuItem("Save...")) {
            savePreset.run();
        }
        if (ImGui.menuItem("Load...")) {
            loadPreset.run();
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

    private void renderInfoMenu() {
        if (ImGui.beginMenu("Info")) {
            if (ImGui.menuItem("Hotkeys")) {
                showHotkeyPopup.set(true);
            }
            ImGui.endMenu();
        }
    }
}
