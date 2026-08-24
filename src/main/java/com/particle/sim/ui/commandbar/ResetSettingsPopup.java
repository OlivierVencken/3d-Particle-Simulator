package com.particle.sim.ui.commandbar;

import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.components.ModalPopup;
import com.particle.sim.ui.theme.UIFonts;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;

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
        UIDesignTokens tokens = UITheme.tokens();
        ImGui.pushFont(UIFonts.medium());
        ImGui.textUnformatted("Restore every simulation setting to its default value?");
        ImGui.textDisabled("This also regenerates the default particle population.");
        ImGui.spacing();
        if (ImGui.button("Reset settings", tokens.buttonWidthXxl(), tokens.controlHeight())) {
            actions.resetSettings();
            close();
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel", tokens.buttonWidthMd(), tokens.controlHeight())) {
            close();
        }
        ImGui.popFont();
    }
}
