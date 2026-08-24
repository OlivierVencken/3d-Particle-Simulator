package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIControls;
import com.particle.sim.ui.components.UIButton;
import com.particle.sim.ui.components.UIText;
import com.particle.sim.ui.theme.UIComponentVariant;
import com.particle.sim.ui.theme.UIDesignTokens;
import com.particle.sim.ui.theme.UITheme;
import imgui.ImGui;

final class SimulationSection {
    private static final String[] DISTANCE_METRICS = UIControls.enumLabels(DistanceMetric.values());
    private final UIControls controls = new UIControls();

    void render(SimulationUiModel.Simulation simulation, SimulationUiModel.Application application,
            SimulationUiActions.Simulation actions) {
        renderPlayback(application, actions);
        UIText.divider();

        UIControls.sectionHeading("World space");
        controls.settingCheckbox("Wrap boundaries", "world-wrap", simulation.toroidalWrap(),
                actions::setToroidalWrap);
        controls.settingSlider("Bounds", "world-bounds", simulation.bounds(), 2.0f, 10.0f, 1,
                actions::setBounds);
        UIText.helper("Half-size of the cubic world: 2.0–10.0 units");
        if (!simulation.toroidalWrap()) {
            controls.settingSlider("Boundary bounce", "world-bounce", simulation.boundaryBounce(), 0.0f, 1.0f, 2,
                    actions::setBoundaryBounce);
        } else {
            UIText.helper("Particles crossing an edge re-enter from the opposite side.");
        }

        UIText.divider();

        UIControls.sectionHeading("Interaction forces");
        controls.settingSlider("Force", "dynamics-force", simulation.forceFactor(), 0.0f, 10.0f, 1,
                actions::setForceFactor);
        controls.settingSlider("Interaction range", "dynamics-range", simulation.interactionRange(), 0.2f, 3.0f, 2,
                actions::setInteractionRange);
        controls.settingSlider("Repulsion radius", "dynamics-repulsion", simulation.repulsionRadius(), 0.02f, 0.95f,
                2, actions::setRepulsionRadius);
        controls.settingSlider("Velocity damping", "dynamics-damping", simulation.velocityDamping(), 0.85f, 1.0f,
                3, actions::setVelocityDamping);
        controls.settingSlider("Max velocity", "dynamics-max-velocity", simulation.maxVelocity(), 0.5f, 16.0f, 1,
                actions::setMaxVelocity);
        UIText.helper("Distances and velocity are measured in world units.");
        controls.settingCombo("Distance metric", "dynamics-distance", simulation.distanceMetric().ordinal(),
                DISTANCE_METRICS, value -> actions.setDistanceMetric(DistanceMetric.values()[value]));

        UIText.divider();

        UIControls.sectionHeading("Density regulation");
        controls.settingCheckbox("Density regulation", "dynamics-density",
                simulation.densityRegulationEnabled(), actions::setDensityRegulationEnabled);
        if (simulation.densityRegulationEnabled()) {
            controls.settingSlider("Density limit", "dynamics-density-limit", simulation.densityLimit(), 0.0f,
                    500.0f, 0, actions::setDensityLimit);
            UIText.helper("Soft particle limit per interaction neighborhood.");
        } else {
            UIText.helper("Enable to reduce crowding in dense neighborhoods.");
        }
    }

    private void renderPlayback(SimulationUiModel.Application application, SimulationUiActions.Simulation actions) {
        UIDesignTokens tokens = UITheme.tokens();
        UIControls.sectionHeading("Simulation actions");
        float buttonWidth = Math.max(tokens.pairedControlMinimumWidth(),
                (ImGui.getContentRegionAvailX() - tokens.spaceMd()) * 0.5f);
        if (UIButton.text("Step once", "sidebar-simulation-step", UIComponentVariant.SECONDARY,
                buttonWidth, tokens.controlHeight(), application.paused())) {
            actions.step();
        }
        ImGui.sameLine();
        if (UIButton.text("Reset particles", "sidebar-reset-particles", UIComponentVariant.GHOST,
                buttonWidth, tokens.controlHeight())) {
            actions.resetParticles();
        }
        if (!application.paused()) {
            UIText.helper("Pause with Space to advance one fixed step at a time.");
        }
    }
}
