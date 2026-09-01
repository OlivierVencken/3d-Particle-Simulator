package com.particle.sim.ui.commandbar;

import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.components.Button;
import com.particle.sim.ui.components.ModalPopup;
import com.particle.sim.ui.components.PopupActions;
import com.particle.sim.ui.theme.ComponentVariant;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Fonts;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

final class ResetSettingsPopup extends ModalPopup {
    private SimulationViewActions.Application actions;

    ResetSettingsPopup() {
        super("Reset simulation settings?", "reset-settings-popup", "##command-bar");
    }

    void render(SimulationViewActions.Application actions) {
        this.actions = actions;
        super.render();
    }

    @Override
    protected int windowFlags() {
        return ImGuiWindowFlags.AlwaysAutoResize;
    }

    @Override
    protected void renderContent() {
        DesignTokens tokens = Theme.tokens();
        ImGui.pushFont(Fonts.medium());
        ImGui.textUnformatted("Restore every simulation setting to its default value?");
        ImGui.textDisabled("This also regenerates the default particle population.");
        PopupActions.row(() -> renderActions(tokens));
        ImGui.popFont();
    }

    private void renderActions(DesignTokens tokens) {
        if (Button.text(
                "Reset settings",
                "confirm-reset-settings",
                ComponentVariant.DESTRUCTIVE,
                tokens.buttonWidthXxl(),
                tokens.controlHeight())) {
            actions.resetSettings();
            close();
        }
        ImGui.sameLine();
        if (Button.text(
                "Cancel",
                "cancel-reset-settings",
                ComponentVariant.SECONDARY,
                tokens.buttonWidthMd(),
                tokens.controlHeight())) {
            close();
        }
    }
}
