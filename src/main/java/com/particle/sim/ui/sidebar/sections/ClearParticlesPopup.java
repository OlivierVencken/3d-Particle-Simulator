package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.components.ModalPopup;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UIPopupActions;
import com.particle.sim.ui.theme.UIComponentVariant;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UIFonts;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

final class ClearParticlesPopup extends ModalPopup {
    private SimulationUiActions.Particles actions;
    private int particleCount;

    ClearParticlesPopup() {
        super("Clear all particles?", "clear-particles-popup");
    }

    void open(int particleCount) {
        this.particleCount = Math.max(0, particleCount);
        open();
    }

    void render(SimulationUiActions.Particles actions) {
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
        try {
            ImGui.textUnformatted("Remove %,d particles from the simulation?".formatted(particleCount));
            ImGui.textDisabled("This cannot be undone, but new particles can be spawned afterwards.");
            UIPopupActions.row(() -> renderActions(tokens));
        } finally {
            ImGui.popFont();
        }
    }

    private void renderActions(UIDesignTokens tokens) {
        if (UIButton.text("Clear particles", "confirm-clear-particles", UIComponentVariant.DESTRUCTIVE,
                tokens.buttonWidthXxl(), tokens.controlHeight())) {
            actions.clear();
            close();
        }
        ImGui.sameLine();
        if (UIButton.text("Cancel", "cancel-clear-particles", UIComponentVariant.SECONDARY,
                tokens.buttonWidthMd(), tokens.controlHeight())) {
            close();
        }
    }
}
