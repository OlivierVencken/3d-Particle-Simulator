package com.particle.sim.ui.commandbar;

import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.components.ModalPopup;
import com.particle.sim.ui.theme.UIFonts;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

final class ResetSettingsPopup extends ModalPopup {
    private SimulationUiActions.Application actions;

    ResetSettingsPopup() {
        super("Reset simulation settings?", "reset-settings-popup");
    }

    void render(SimulationUiActions.Application actions) {
        this.actions = actions;
        super.render();
    }

    @Override
    protected int windowFlags() {
        return ImGuiWindowFlags.AlwaysAutoResize;
    }

    @Override
    protected void renderContent() {
        ImGui.pushFont(UIFonts.medium());
        ImGui.textUnformatted("Restore every simulation setting to its default value?");
        ImGui.textDisabled("This also regenerates the default particle population.");
        ImGui.spacing();
        if (ImGui.button("Reset settings", 128.0f, 32.0f)) {
            actions.resetSettings();
            close();
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel", 88.0f, 32.0f)) {
            close();
        }
        ImGui.popFont();
    }
}
