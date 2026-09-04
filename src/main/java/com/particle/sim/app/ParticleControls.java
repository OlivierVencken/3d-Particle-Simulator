package com.particle.sim.app;

import com.particle.sim.graphics.RgbaColor;
import com.particle.sim.particles.ParticleSystem;
import com.particle.sim.particles.attraction.AttractionPattern;
import com.particle.sim.particles.spawning.SpawnMode;
import com.particle.sim.ui.SimulationView;
import com.particle.sim.ui.SimulationViewActions;
import com.particle.sim.ui.SimulationViewModel;

final class ParticleControls
        implements SimulationViewModel.Particles, SimulationViewActions.Particles {
    private final ParticleSystem particles;
    private final SimulationView ui;
    private final SettingsChangeHandler changes;

    ParticleControls(ParticleSystem particles, SimulationView ui, SettingsChangeHandler changes) {
        this.particles = particles;
        this.ui = ui;
        this.changes = changes;
    }

    @Override
    public int particleCount() {
        return particles.particleCount();
    }

    @Override
    public int maximumParticleCount() {
        return particles.maximumParticleCount();
    }

    @Override
    public int groupCount() {
        return particles.groupCount();
    }

    @Override
    public int maximumGroupCount() {
        return particles.maximumGroupCount();
    }

    @Override
    public SpawnMode spawnMode() {
        return particles.spawnMode();
    }

    @Override
    public int customSpawnAmount() {
        return ui.customSpawnAmount();
    }

    @Override
    public float matrixEditStep() {
        return ui.matrixEditStep();
    }

    @Override
    public float attraction(int row, int column) {
        return particles.attraction(row, column);
    }

    @Override
    public boolean canUndoAttractionMatrix() {
        return particles.canUndoAttractionMatrix();
    }

    @Override
    public boolean canRedoAttractionMatrix() {
        return particles.canRedoAttractionMatrix();
    }

    @Override
    public boolean attractionMutationAnimated() {
        return particles.attractionMutationAnimated();
    }

    @Override
    public RgbaColor groupColor(int group) {
        return particles.groupColor(group);
    }

    @Override
    public void setGroupCount(int value) {
        changes.apply(() -> particles.groupCount(value));
    }

    @Override
    public void setSpawnMode(SpawnMode value) {
        changes.apply(() -> particles.spawnMode(value));
    }

    @Override
    public void add(int amount) {
        changes.apply(() -> particles.addParticles(amount));
    }

    @Override
    public void remove(int amount) {
        changes.apply(() -> particles.removeParticles(amount));
    }

    @Override
    public void clear() {
        changes.apply(particles::clearParticles);
    }

    @Override
    public void setCustomSpawnAmount(int value) {
        changes.apply(() -> ui.setCustomSpawnAmount(value));
    }

    @Override
    public void setMatrixEditStep(float value) {
        changes.apply(() -> ui.setMatrixEditStep(value));
    }

    @Override
    public void adjustAttraction(int row, int column, float delta) {
        changes.apply(() -> particles.adjustAttraction(row, column, delta));
    }

    @Override
    public void randomizeAttractionMatrix() {
        changes.apply(particles::randomizeAttractionMatrix);
    }

    @Override
    public void zeroAttractionMatrix() {
        changes.apply(particles::zeroAttractionMatrix);
    }

    @Override
    public void symmetrizeAttractionMatrix() {
        changes.apply(particles::symmetrizeAttractionMatrix);
    }

    @Override
    public void invertAttractionMatrix() {
        changes.apply(particles::invertAttractionMatrix);
    }

    @Override
    public void generateAttractionMatrix(AttractionPattern pattern, float variation) {
        changes.apply(() -> particles.generateAttractionMatrix(pattern, variation));
    }

    @Override
    public void mutateAttractionMatrix(float amount) {
        changes.apply(() -> particles.mutateAttractionMatrix(amount));
    }

    @Override
    public void normalizeAttractionMatrix() {
        changes.apply(particles::normalizeAttractionMatrix);
    }

    @Override
    public void undoAttractionMatrix() {
        changes.apply(particles::undoAttractionMatrix);
    }

    @Override
    public void redoAttractionMatrix() {
        changes.apply(particles::redoAttractionMatrix);
    }

    @Override
    public void setAttractionMutationAnimated(boolean enabled) {
        changes.apply(() -> particles.attractionMutationAnimated(enabled));
    }
}
