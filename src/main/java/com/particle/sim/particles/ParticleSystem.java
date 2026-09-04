package com.particle.sim.particles;

import com.particle.sim.diagnostics.PerformanceSnapshot;
import com.particle.sim.particles.attraction.AttractionMatrix;
import com.particle.sim.particles.attraction.AttractionPattern;
import com.particle.sim.particles.gpu.SpatialGridSizing;
import com.particle.sim.particles.rendering.ColorMode;
import com.particle.sim.particles.rendering.EffectMode;
import com.particle.sim.particles.rendering.GlowSettings;
import com.particle.sim.particles.rendering.TrailSettings;
import com.particle.sim.particles.spawning.SpawnMode;
import com.particle.sim.settings.SimulationDefaults;
import com.particle.sim.ui.FramebufferViewport;
import imgui.ImVec4;
import java.util.Random;
import java.util.Set;

public final class ParticleSystem {
    private final ParticleSystemRuntime runtime = new ParticleSystemRuntime();
    private final ParticleSimulationConfig config = ParticleSimulationConfig.defaults();
    private final AttractionMatrix attractionMatrix =
            new AttractionMatrix(
                    SimulationDefaults.GROUP_COUNT, SimulationDefaults.MAX_GROUP_COUNT);
    private final Random particleRandom = new Random();
    private int maximumParticleCount = SimulationDefaults.MAX_PARTICLE_COUNT;

    public void init() {
        maximumParticleCount = runtime.init(config, attractionMatrix, particleRandom);
    }

    public void step() {
        advanceSimulation((float) SimulationDefaults.SIMULATION_STEP_SECONDS);
    }

    public void reset() {
        resizeParticles(particleCount(), false);
    }

    public void update(float deltaTime) {
        advanceSimulation(deltaTime);
    }

    private void advanceSimulation(float deltaTime) {
        runtime.advance(config, attractionMatrix, deltaTime);
    }

    public void render(FramebufferViewport viewport, float[] viewMatrix) {
        runtime.render(viewport, viewMatrix, config);
    }

    public void dispose() {
        runtime.dispose();
    }

    public int particleCount() {
        return config.particleCount();
    }

    public int maximumParticleCount() {
        return maximumParticleCount;
    }

    public ParticleSimulationConfig config() {
        return config.copy();
    }

    public void applyConfig(ParticleSimulationConfig config) {
        if (config == null) {
            return;
        }

        ParticleSimulationConfig sanitized = config.copy();
        sanitized.sanitize();
        sanitized.particleCount(Math.min(sanitized.particleCount(), maximumParticleCount));

        int oldParticleCount = particleCount();
        this.config.applyFrom(sanitized);
        attractionMatrix.groupCount(this.config.groupCount());
        if (runtime.initialized()) {
            runtime.applyConfig(oldParticleCount, this.config, particleRandom);
        }
    }

    public void addParticles(int amount) {
        if (amount <= 0) {
            return;
        }
        long requestedParticleCount = (long) particleCount() + amount;
        updateParticleCount((int) Math.min(requestedParticleCount, Integer.MAX_VALUE), true);
    }

    public void removeParticles(int amount) {
        if (amount <= 0) {
            return;
        }
        updateParticleCount(particleCount() - amount, true);
    }

    public void clearParticles() {
        updateParticleCount(0, false);
    }

    public void particleCount(int particleCount) {
        updateParticleCount(particleCount, false);
    }

    private void updateParticleCount(int requestedParticleCount, boolean preserveExisting) {
        int newParticleCount = Math.max(0, Math.min(maximumParticleCount, requestedParticleCount));
        if (runtime.initialized()) {
            resizeParticles(newParticleCount, preserveExisting);
        } else {
            config.particleCount(newParticleCount);
        }
    }

    public ColorMode colorMode() {
        return config.colorMode();
    }

    public void colorMode(ColorMode colorMode) {
        config.colorMode(colorMode);
    }

    private void resizeParticles(int requestedParticleCount, boolean preserveExisting) {
        int oldParticleCount = particleCount();
        int newParticleCount = Math.max(0, Math.min(maximumParticleCount, requestedParticleCount));
        runtime.resizeParticles(
                oldParticleCount, newParticleCount, preserveExisting, config, particleRandom);
        config.particleCount(newParticleCount);
    }

    public float pointSize() {
        return config.pointSize();
    }

    public void pointSize(float pointSize) {
        config.pointSize(pointSize);
    }

    public boolean fixedParticleScreenSize() {
        return config.fixedParticleScreenSize();
    }

    public void fixedParticleScreenSize(boolean fixedParticleScreenSize) {
        config.fixedParticleScreenSize(fixedParticleScreenSize);
    }

    public Set<EffectMode> effectModes() {
        return config.effectModes();
    }

    public void effectModes(Set<EffectMode> effectModes) {
        config.effectModes(effectModes);
    }

    public boolean effectEnabled(EffectMode effectMode) {
        return config.effectEnabled(effectMode);
    }

    public void effectEnabled(EffectMode effectMode, boolean enabled) {
        config.effectEnabled(effectMode, enabled);
        runtime.effectChanged(effectMode, enabled);
    }

    public GlowSettings glowSettings() {
        return config.glowSettings();
    }

    public int glowBlurPasses() {
        return config.glowBlurPasses();
    }

    public void glowBlurPasses(int glowBlurPasses) {
        config.glowBlurPasses(glowBlurPasses);
    }

    public float glowStrength() {
        return config.glowStrength();
    }

    public void glowStrength(float glowStrength) {
        config.glowStrength(glowStrength);
    }

    public float glowRadius() {
        return config.glowRadius();
    }

    public void glowRadius(float glowRadius) {
        config.glowRadius(glowRadius);
    }

    public float glowFalloff() {
        return config.glowFalloff();
    }

    public void glowFalloff(float glowFalloff) {
        config.glowFalloff(glowFalloff);
    }

    public TrailSettings trailSettings() {
        return config.trailSettings();
    }

    public int trailLength() {
        return config.trailLength();
    }

    public void trailLength(int trailLength) {
        config.trailLength(trailLength);
    }

    public float trailThickness() {
        return config.trailThickness();
    }

    public int effectiveTrailLength() {
        return runtime.effectiveTrailLength();
    }

    public int effectiveTrailParticleStride() {
        return runtime.effectiveTrailParticleStride();
    }

    public int effectiveBloomDivisor() {
        return runtime.effectiveBloomDivisor();
    }

    public void trailThickness(float trailThickness) {
        config.trailThickness(trailThickness);
    }

    public float forceFactor() {
        return config.forceFactor();
    }

    public void forceFactor(float forceFactor) {
        config.forceFactor(forceFactor);
    }

    public float interactionRange() {
        return config.interactionRange();
    }

    public void interactionRange(float interactionRange) {
        config.interactionRange(interactionRange);
    }

    public float velocityDamping() {
        return config.velocityDamping();
    }

    public void velocityDamping(float velocityDamping) {
        config.velocityDamping(velocityDamping);
    }

    public float repulsionRadius() {
        return config.repulsionRadius();
    }

    public void repulsionRadius(float repulsionRadius) {
        config.repulsionRadius(repulsionRadius);
    }

    public float maxVelocity() {
        return config.maxVelocity();
    }

    public void maxVelocity(float maxVelocity) {
        config.maxVelocity(maxVelocity);
    }

    public float boundaryBounce() {
        return config.boundaryBounce();
    }

    public void boundaryBounce(float boundaryBounce) {
        config.boundaryBounce(boundaryBounce);
    }

    public float bounds() {
        return config.bounds();
    }

    public void bounds(float bounds) {
        config.bounds(bounds);
    }

    public SpawnMode spawnMode() {
        return config.spawnMode();
    }

    public void spawnMode(SpawnMode spawnMode) {
        config.spawnMode(spawnMode);
    }

    public boolean toroidalWrap() {
        return config.toroidalWrap();
    }

    public void toroidalWrap(boolean toroidalWrap) {
        config.toroidalWrap(toroidalWrap);
    }

    public boolean densityRegulationEnabled() {
        return config.densityRegulationEnabled();
    }

    public void densityRegulationEnabled(boolean densityRegulationEnabled) {
        config.densityRegulationEnabled(densityRegulationEnabled);
    }

    public float densityLimit() {
        return config.densityLimit();
    }

    public void densityLimit(float densityLimit) {
        config.densityLimit(densityLimit);
    }

    public DistanceMetric distanceMetric() {
        return config.distanceMetric();
    }

    public void distanceMetric(DistanceMetric distanceMetric) {
        config.distanceMetric(distanceMetric);
    }

    public float attraction(int groupA, int groupB) {
        return attractionMatrix.attraction(groupA, groupB);
    }

    public void attraction(int groupA, int groupB, float value) {
        attractionMatrix.attraction(groupA, groupB, value);
    }

    public void attractionMatrix(float[] values) {
        attractionMatrix.activeValues(values);
    }

    public float[] attractionMatrix() {
        return attractionMatrix.values();
    }

    public void adjustAttraction(int groupA, int groupB, float delta) {
        attractionMatrix.adjustAttraction(groupA, groupB, delta);
    }

    public void randomizeAttractionMatrix() {
        attractionMatrix.randomize();
    }

    public void randomSeed(long seed) {
        particleRandom.setSeed(seed);
    }

    public void zeroAttractionMatrix() {
        attractionMatrix.zero();
    }

    public void symmetrizeAttractionMatrix() {
        attractionMatrix.symmetrize();
    }

    public void invertAttractionMatrix() {
        attractionMatrix.invert();
    }

    public void generateAttractionMatrix(AttractionPattern pattern, float variation) {
        attractionMatrix.generate(pattern, variation);
    }

    public void mutateAttractionMatrix(float amount) {
        attractionMatrix.mutate(amount);
    }

    public void normalizeAttractionMatrix() {
        attractionMatrix.normalize();
    }

    public void undoAttractionMatrix() {
        attractionMatrix.undo();
    }

    public void redoAttractionMatrix() {
        attractionMatrix.redo();
    }

    public boolean canUndoAttractionMatrix() {
        return attractionMatrix.canUndo();
    }

    public boolean canRedoAttractionMatrix() {
        return attractionMatrix.canRedo();
    }

    public boolean attractionMutationAnimated() {
        return attractionMatrix.animatedMutation();
    }

    public void attractionMutationAnimated(boolean enabled) {
        attractionMatrix.animatedMutation(enabled);
    }

    public void advanceAttractionMatrixAnimation(float deltaTime) {
        attractionMatrix.advanceAnimation(deltaTime);
    }

    public int groupCount() {
        return config.groupCount();
    }

    public void groupCount(int groupCount) {
        int previousGroupCount = config.groupCount();
        config.groupCount(groupCount);
        attractionMatrix.groupCount(config.groupCount());
        if (runtime.initialized() && previousGroupCount != config.groupCount()) {
            reset();
        }
    }

    public int maximumGroupCount() {
        return attractionMatrix.maximumGroupCount();
    }

    public ImVec4[] groupColors() {
        return config.groupColors();
    }

    public void groupColors(ImVec4[] groupColors) {
        config.groupColors(groupColors);
    }

    public ImVec4 groupColor(int group) {
        return config.groupColor(group);
    }

    public void groupColor(int group, ImVec4 color) {
        config.groupColor(group, color);
    }

    public int gridSize() {
        return SpatialGridSizing.gridSize(bounds(), interactionRange());
    }

    public int gridCellCount() {
        return SpatialGridSizing.gridCellCount(bounds(), interactionRange());
    }

    public PerformanceSnapshot performanceSnapshot() {
        return runtime.performanceSnapshot(config, maximumParticleCount);
    }

    float[] readPositions() {
        return runtime.readPositions(particleCount());
    }

    float[] readVelocities() {
        return runtime.readVelocities(particleCount());
    }

    void replaceState(float[] positions, float[] velocities) {
        int expectedFloatCount = Math.multiplyExact(particleCount(), 4);
        if (!runtime.initialized()) {
            throw new IllegalStateException(
                    "Particle system must be initialized before replacing its state");
        }
        if (positions.length != expectedFloatCount || velocities.length != expectedFloatCount) {
            throw new IllegalArgumentException(
                    "Particle state must contain four floats per particle");
        }
        runtime.replaceState(positions, velocities);
    }

    int[] readGridCounts() {
        return runtime.readGridCounts(gridCellCount());
    }

    int[] readGridParticleIds() {
        return runtime.readGridParticleIds(particleCount());
    }
}
