package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.ui.SimulationUiActions;
import com.particle.sim.ui.SimulationUiModel;
import com.particle.sim.ui.components.UIControls;
import com.particle.sim.ui.components.UIText;

final class SimulationSection {
    private static final String[] DISTANCE_METRICS = UIControls.enumLabels(DistanceMetric.values());
    private final UIControls controls = new UIControls();

    void render(SimulationUiModel.Simulation simulation, SimulationUiActions.Simulation actions) {
        UIControls.sectionHeading("World");
        controls.settingCheckbox("Wrap boundaries", "world-wrap", simulation.toroidalWrap(),
                actions::setToroidalWrap);
        controls.settingSlider("Bounds", "world-bounds", simulation.bounds(), 2.0f, 10.0f, 1,
                actions::setBounds);
        if (!simulation.toroidalWrap()) {
            controls.settingSlider("Boundary bounce", "world-bounce", simulation.boundaryBounce(), 0.0f, 1.0f, 2,
                    actions::setBoundaryBounce);
        }

        UIText.divider();

        UIControls.sectionHeading("Dynamics");
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
        controls.settingCheckbox("Density regulation", "dynamics-density",
                simulation.densityRegulationEnabled(), actions::setDensityRegulationEnabled);
        if (simulation.densityRegulationEnabled()) {
            controls.settingSlider("Density limit", "dynamics-density-limit", simulation.densityLimit(), 0.0f,
                    500.0f, 0, actions::setDensityLimit);
        }
        controls.settingCombo("Distance metric", "dynamics-distance", simulation.distanceMetric().ordinal(),
                DISTANCE_METRICS, value -> actions.setDistanceMetric(DistanceMetric.values()[value]));
    }
}
