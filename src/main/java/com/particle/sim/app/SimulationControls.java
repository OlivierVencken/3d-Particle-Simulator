package com.particle.sim.app;

import com.particle.sim.particles.DistanceMetric;
import com.particle.sim.particles.ParticleSystem;
import com.particle.sim.ui.SimulationView;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;

final class SimulationControls
        implements SimulationViewModel.Simulation, SimulationViewActions.Simulation {
    private final ParticleSystem particles;
    private final SimulationView ui;
    private final SettingsChangeHandler changes;

    SimulationControls(ParticleSystem particles, SimulationView ui, SettingsChangeHandler changes) {
        this.particles = particles;
        this.ui = ui;
        this.changes = changes;
    }

    @Override
    public void togglePause() {
        ui.togglePause();
    }

    @Override
    public void step() {
        particles.step();
    }

    @Override
    public void resetParticles() {
        particles.reset();
    }

    @Override
    public boolean toroidalWrap() {
        return particles.toroidalWrap();
    }

    @Override
    public float bounds() {
        return particles.bounds();
    }

    @Override
    public float boundaryBounce() {
        return particles.boundaryBounce();
    }

    @Override
    public float forceFactor() {
        return particles.forceFactor();
    }

    @Override
    public float interactionRange() {
        return particles.interactionRange();
    }

    @Override
    public float repulsionRadius() {
        return particles.repulsionRadius();
    }

    @Override
    public float velocityDamping() {
        return particles.velocityDamping();
    }

    @Override
    public float maxVelocity() {
        return particles.maxVelocity();
    }

    @Override
    public boolean densityRegulationEnabled() {
        return particles.densityRegulationEnabled();
    }

    @Override
    public float densityLimit() {
        return particles.densityLimit();
    }

    @Override
    public DistanceMetric distanceMetric() {
        return particles.distanceMetric();
    }

    @Override
    public void setToroidalWrap(boolean value) {
        changes.apply(() -> particles.toroidalWrap(value));
    }

    @Override
    public void setBounds(float value) {
        changes.apply(() -> particles.bounds(value));
    }

    @Override
    public void setBoundaryBounce(float value) {
        changes.apply(() -> particles.boundaryBounce(value));
    }

    @Override
    public void setForceFactor(float value) {
        changes.apply(() -> particles.forceFactor(value));
    }

    @Override
    public void setInteractionRange(float value) {
        changes.apply(() -> particles.interactionRange(value));
    }

    @Override
    public void setRepulsionRadius(float value) {
        changes.apply(() -> particles.repulsionRadius(value));
    }

    @Override
    public void setVelocityDamping(float value) {
        changes.apply(() -> particles.velocityDamping(value));
    }

    @Override
    public void setMaxVelocity(float value) {
        changes.apply(() -> particles.maxVelocity(value));
    }

    @Override
    public void setDensityRegulationEnabled(boolean value) {
        changes.apply(() -> particles.densityRegulationEnabled(value));
    }

    @Override
    public void setDensityLimit(float value) {
        changes.apply(() -> particles.densityLimit(value));
    }

    @Override
    public void setDistanceMetric(DistanceMetric value) {
        changes.apply(() -> particles.distanceMetric(value));
    }
}
