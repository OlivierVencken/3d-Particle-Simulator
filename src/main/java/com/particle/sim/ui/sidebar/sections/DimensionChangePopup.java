package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.SimulationDimension;
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

final class DimensionChangePopup extends ModalPopup {
    private SimulationDimension target = SimulationDimension.THREE_D;
    private int particleCount;
    private SimulationUiActions.Simulation actions;

    DimensionChangePopup() {
        super("Change simulation dimensions?", "dimension-change-popup", "##sidebar");
    }

    void open(SimulationDimension target, int particleCount) {
        this.target = target;
        this.particleCount = Math.max(0, particleCount);
        open();
    }

    void render(SimulationUiActions.Simulation actions) {
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
            ImGui.textUnformatted("Reset %,d particles and switch to %s?".formatted(particleCount, label(target)));
            ImGui.textDisabled("Changing dimensions replaces all particle positions and clears trail history.");
            UIPopupActions.row(() -> renderActions(tokens));
        } finally {
            ImGui.popFont();
        }
    }

    private void renderActions(UIDesignTokens tokens) {
        if (UIButton.text("Reset and switch", "confirm-dimension-change", UIComponentVariant.DESTRUCTIVE,
                tokens.buttonWidthXxl(), tokens.controlHeight())) {
            actions.setSimulationDimension(target);
            close();
        }
        ImGui.sameLine();
        if (UIButton.text("Cancel", "cancel-dimension-change", UIComponentVariant.SECONDARY,
                tokens.buttonWidthMd(), tokens.controlHeight())) {
            close();
        }
    }

    private static String label(SimulationDimension dimension) {
        return dimension == SimulationDimension.FOUR_D ? "4D" : "3D";
    }
}
