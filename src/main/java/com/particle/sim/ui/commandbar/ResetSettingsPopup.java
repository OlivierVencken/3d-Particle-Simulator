package com.particle.sim.ui.commandbar;

import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.components.ModalPopup;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UIPopupActions;
import com.particle.sim.ui.theme.UIComponentVariant;
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
        UIPopupActions.row(() -> renderActions(tokens));
        ImGui.popFont();
    }

    private void renderActions(UIDesignTokens tokens) {
        if (UIButton.text("Reset settings", "confirm-reset-settings", UIComponentVariant.DESTRUCTIVE,
                tokens.buttonWidthXxl(), tokens.controlHeight())) {
            actions.resetSettings();
            close();
        }
        ImGui.sameLine();
        if (UIButton.text("Cancel", "cancel-reset-settings", UIComponentVariant.SECONDARY,
                tokens.buttonWidthMd(), tokens.controlHeight())) {
            close();
        }
    }
}
