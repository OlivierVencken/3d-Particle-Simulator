package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;
import com.particle.sim.ui.components.Controls;
import com.particle.sim.ui.components.Button;
import com.particle.sim.ui.components.Text;
import com.particle.sim.ui.theme.ComponentVariant;
import com.particle.sim.ui.theme.DesignTokens;
import com.particle.sim.ui.theme.Theme;
import imgui.ImGui;

final class SimulationSection {
    private static final String[] DISTANCE_METRICS = Controls.enumLabels(DistanceMetric.values());
    private final Controls controls = new Controls();

    void render(SimulationViewModel.Simulation simulation, SimulationViewModel.Application application,
            SimulationViewActions.Simulation actions) {
        renderPlayback(application, actions);
        Text.divider();

        Controls.sectionHeading("World space");
        controls.settingCheckbox("Wrap boundaries", "world-wrap", simulation.toroidalWrap(),
                actions::setToroidalWrap);
        controls.settingSlider("Bounds", "world-bounds", simulation.bounds(), 2.0f, 10.0f, 1,
                actions::setBounds);
        if (!simulation.toroidalWrap()) {
            controls.settingSlider("Boundary bounce", "world-bounce", simulation.boundaryBounce(), 0.0f, 1.0f, 2,
                    actions::setBoundaryBounce);
        }

        Text.divider();

        Controls.sectionHeading("Interaction forces");
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
        controls.settingCombo("Distance metric", "dynamics-distance", simulation.distanceMetric().ordinal(),
                DISTANCE_METRICS, value -> actions.setDistanceMetric(DistanceMetric.values()[value]));

        Text.divider();

        Controls.sectionHeading("Density regulation");
        controls.settingCheckbox("Density regulation", "dynamics-density",
                simulation.densityRegulationEnabled(), actions::setDensityRegulationEnabled);
        if (simulation.densityRegulationEnabled()) {
            controls.settingSlider("Density limit", "dynamics-density-limit", simulation.densityLimit(), 0.0f,
                    500.0f, 0, actions::setDensityLimit);
        }
    }

    private void renderPlayback(SimulationViewModel.Application application, SimulationViewActions.Simulation actions) {
        DesignTokens tokens = Theme.tokens();
        Controls.sectionHeading("Simulation actions");
        float availableWidth = ImGui.getContentRegionAvailX();
        boolean inline = playbackControlsFitInline(availableWidth, tokens);
        float buttonWidth = inline ? (availableWidth - tokens.spaceMd()) * 0.5f : availableWidth;
        if (Button.text(application.paused() ? "Resume" : "Pause", "sidebar-simulation-pause",
                application.paused() ? ComponentVariant.SELECTED : ComponentVariant.PRIMARY,
                buttonWidth, tokens.controlHeight())) {
            actions.togglePause();
        }
        if (inline) {
            ImGui.sameLine();
        }
        if (Button.text("Step once", "sidebar-simulation-step", ComponentVariant.SECONDARY,
                buttonWidth, tokens.controlHeight(), application.paused())) {
            actions.step();
        }
        if (Button.text("Reset particles", "sidebar-reset-particles", ComponentVariant.GHOST,
                availableWidth, tokens.controlHeight())) {
            actions.resetParticles();
        }
    }

    static boolean playbackControlsFitInline(float availableWidth, DesignTokens tokens) {
        return availableWidth >= tokens.pairedControlMinimumWidth() * 2.0f + tokens.spaceMd();
    }
}
