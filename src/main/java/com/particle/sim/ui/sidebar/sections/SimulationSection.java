package com.particle.sim.ui.sidebar.sections;

import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.GpuParticleSystem;
import com.particle.sim.ui.components.UIControls;
import imgui.ImGui;

final class SimulationSection {
    private static final String[] DISTANCE_METRICS = UIControls.enumLabels(DistanceMetric.values());

    void render(GpuParticleSystem particles, Runnable settingsChanged) {
        UIControls.sectionHeading("World");
        UIControls.settingCheckbox("Wrap boundaries", "world-wrap", particles.toroidalWrap(),
                particles::toroidalWrap, settingsChanged);
        UIControls.settingSlider("Bounds", "world-bounds", particles.bounds(), 2.0f, 10.0f, 1,
                particles::bounds, settingsChanged);
        if (!particles.toroidalWrap()) {
            UIControls.settingSlider("Boundary bounce", "world-bounce", particles.boundaryBounce(), 0.0f, 1.0f, 2,
                    particles::boundaryBounce, settingsChanged);
        }

        ImGui.separatorText("");

        UIControls.sectionHeading("Dynamics");
        UIControls.settingSlider("Force", "dynamics-force", particles.forceFactor(), 0.0f, 10.0f, 1,
                particles::forceFactor, settingsChanged);
        UIControls.settingSlider("Interaction range", "dynamics-range", particles.interactionRange(), 0.2f, 3.0f, 2,
                particles::interactionRange, settingsChanged);
        UIControls.settingSlider("Repulsion radius", "dynamics-repulsion", particles.repulsionRadius(), 0.02f, 0.95f,
                2, particles::repulsionRadius, settingsChanged);
        UIControls.settingSlider("Velocity damping", "dynamics-damping", particles.velocityDamping(), 0.85f, 1.0f,
                3, particles::velocityDamping, settingsChanged);
        UIControls.settingSlider("Max velocity", "dynamics-max-velocity", particles.maxVelocity(), 0.5f, 16.0f, 1,
                particles::maxVelocity, settingsChanged);
        UIControls.settingCheckbox("Density regulation", "dynamics-density",
                particles.densityRegulationEnabled(), particles::densityRegulationEnabled, settingsChanged);
        if (particles.densityRegulationEnabled()) {
            UIControls.settingSlider("Density limit", "dynamics-density-limit", particles.densityLimit(), 0.0f,
                    500.0f, 0, particles::densityLimit, settingsChanged);
        }
        UIControls.settingCombo("Distance metric", "dynamics-distance", particles.distanceMetric().ordinal(),
                DISTANCE_METRICS, value -> particles.distanceMetric(DistanceMetric.values()[value]), settingsChanged);
    }
}
