package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.components.ModalPopup;
import com.particle.sim.ui.components.Button;
import com.particle.sim.ui.components.PopupActions;
import com.particle.sim.ui.theme.ComponentVariant;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Fonts;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

final class ClearParticlesPopup extends ModalPopup {
    private SimulationViewActions.Particles actions;
    private int particleCount;

    ClearParticlesPopup() {
        super("Clear all particles?", "clear-particles-popup", "##sidebar");
    }

    void open(int particleCount) {
        this.particleCount = Math.max(0, particleCount);
        open();
    }

    void render(SimulationViewActions.Particles actions) {
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
        try {
            ImGui.textUnformatted("Remove %,d particles from the simulation?".formatted(particleCount));
            ImGui.textDisabled("This cannot be undone, but new particles can be spawned afterwards.");
            PopupActions.row(() -> renderActions(tokens));
        } finally {
            ImGui.popFont();
        }
    }

    private void renderActions(DesignTokens tokens) {
        if (Button.text("Clear particles", "confirm-clear-particles", ComponentVariant.DESTRUCTIVE,
                tokens.buttonWidthXxl(), tokens.controlHeight())) {
            actions.clear();
            close();
        }
        ImGui.sameLine();
        if (Button.text("Cancel", "cancel-clear-particles", ComponentVariant.SECONDARY,
                tokens.buttonWidthMd(), tokens.controlHeight())) {
            close();
        }
    }
}
