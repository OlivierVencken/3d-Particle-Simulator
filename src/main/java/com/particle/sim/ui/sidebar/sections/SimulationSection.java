package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIControls;
import imgui.ImGui;

final class SimulationSection {
    private static final String[] DISTANCE_METRICS = UIControls.enumLabels(DistanceMetric.values());

    void render(SimulationUiModel.Simulation simulation, SimulationUiActions.Simulation actions) {
        UIControls.sectionHeading("World");
        UIControls.settingCheckbox("Wrap boundaries", "world-wrap", simulation.toroidalWrap(),
                actions::setToroidalWrap);
        UIControls.settingSlider("Bounds", "world-bounds", simulation.bounds(), 2.0f, 10.0f, 1,
                actions::setBounds);
        if (!simulation.toroidalWrap()) {
            UIControls.settingSlider("Boundary bounce", "world-bounce", simulation.boundaryBounce(), 0.0f, 1.0f, 2,
                    actions::setBoundaryBounce);
        }

        ImGui.separatorText("");

        UIControls.sectionHeading("Dynamics");
        UIControls.settingSlider("Force", "dynamics-force", simulation.forceFactor(), 0.0f, 10.0f, 1,
                actions::setForceFactor);
        UIControls.settingSlider("Interaction range", "dynamics-range", simulation.interactionRange(), 0.2f, 3.0f, 2,
                actions::setInteractionRange);
        UIControls.settingSlider("Repulsion radius", "dynamics-repulsion", simulation.repulsionRadius(), 0.02f, 0.95f,
                2, actions::setRepulsionRadius);
        UIControls.settingSlider("Velocity damping", "dynamics-damping", simulation.velocityDamping(), 0.85f, 1.0f,
                3, actions::setVelocityDamping);
        UIControls.settingSlider("Max velocity", "dynamics-max-velocity", simulation.maxVelocity(), 0.5f, 16.0f, 1,
                actions::setMaxVelocity);
        UIControls.settingCheckbox("Density regulation", "dynamics-density",
                simulation.densityRegulationEnabled(), actions::setDensityRegulationEnabled);
        if (simulation.densityRegulationEnabled()) {
            UIControls.settingSlider("Density limit", "dynamics-density-limit", simulation.densityLimit(), 0.0f,
                    500.0f, 0, actions::setDensityLimit);
        }
        UIControls.settingCombo("Distance metric", "dynamics-distance", simulation.distanceMetric().ordinal(),
                DISTANCE_METRICS, value -> actions.setDistanceMetric(DistanceMetric.values()[value]));
    }
}
