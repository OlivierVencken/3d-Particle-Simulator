package com.particle.sim.particles;

import com.particle.sim.settings.SimulationDefaults;

import imgui.ImVec4;

import java.util.Set;
import java.util.Random;

import static org.lwjgl.opengl.GL43C.GL_MAX_COMPUTE_WORK_GROUP_COUNT;
import static org.lwjgl.opengl.GL43C.GL_MAX_SHADER_STORAGE_BLOCK_SIZE;
import static org.lwjgl.opengl.GL43C.glGetInteger64;
import static org.lwjgl.opengl.GL43C.glGetIntegeri;

public final class GpuParticleSystem {
    private static final int COMPUTE_WORK_GROUP_SIZE = 256;
    private static final int BYTES_PER_PARTICLE = 4 * 4 * Float.BYTES + Integer.BYTES;

    private final ParticleBuffers particleBuffers = new ParticleBuffers();
    private final TrailHistoryBuffers trailHistoryBuffers = new TrailHistoryBuffers();
    private final SpatialGridBuffers spatialGridBuffers = new SpatialGridBuffers();
    private final ParticleRenderer renderer = new ParticleRenderer();
    private final ParticleCompute compute = new ParticleCompute();

    private final ParticleSimulationConfig config = ParticleSimulationConfig.defaults();
    private final AttractionMatrix attractionMatrix = new AttractionMatrix(
            SimulationDefaults.GROUP_COUNT,
            SimulationDefaults.MAX_GROUP_COUNT);
    private final Random particleRandom = new Random();
    private int maximumParticleCount = SimulationDefaults.MAX_PARTICLE_COUNT;
    private boolean initialized;

    public void init() {
        compute.init();
        renderer.init();
        maximumParticleCount = detectMaximumParticleCount();
        config.particleCount(Math.min(config.particleCount(), maximumParticleCount));
        spatialGridBuffers.init(particleCount(), gridCellCount());
        attractionMatrix.randomize();
        initialized = true;
        reset();
    }

    public void step() {
        advanceSimulation((float) SimulationDefaults.SIMULATION_STEP_SECONDS);
    }

    public void reset() {
        resizeParticles(particleCount(), false);
    }

    public void update(float deltaTime, float elapsedTime) {
        advanceSimulation(deltaTime);
    }

    private void advanceSimulation(float deltaTime) {
        if (!initialized || particleCount() == 0) {
            return;
        }

        spatialGridBuffers.ensureCapacity(particleCount(), gridCellCount());
        compute.buildGrid(this, particleBuffers, spatialGridBuffers);
        boolean captureTrail = effectEnabled(EffectMode.TRAILS)
                && trailHistoryBuffers.prepareCapture(particleCount(), trailLength());
        compute.integrate(this, particleBuffers, spatialGridBuffers, trailHistoryBuffers, captureTrail, deltaTime);
        particleBuffers.swapState();

        if (captureTrail) {
            trailHistoryBuffers.commitCapture();
        }
    }

    private void rebuildSpatialGrid() {
        if (particleCount() == 0) {
            return;
        }

        spatialGridBuffers.ensureCapacity(particleCount(), gridCellCount());
        compute.buildGrid(this, particleBuffers, spatialGridBuffers);
    }

    public void render(int width, int height, float[] viewMatrix) {
        renderer.render(width, height, viewMatrix, particleBuffers, spatialGridBuffers, particleCount(), pointSize(),
                fixedParticleScreenSize(), effectEnabled(EffectMode.GLOW), effectEnabled(EffectMode.TRAILS),
                colorMode().ordinal(), groupCount(), maxVelocity(), bounds(), interactionRange(),
                glowSettings(), trailSettings(), trailHistoryBuffers);
    }

    public void dispose() {
        particleBuffers.dispose();
        trailHistoryBuffers.dispose();
        spatialGridBuffers.dispose();
        compute.dispose();
        renderer.dispose();
    }

    public int particleCount() {
        return config.particleCount();
    }

    public int maxParticleCount() {
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
        if (initialized) {
            particleBuffers.resize(oldParticleCount, this.config.particleCount(), false, this.config, particleRandom);
            if (effectEnabled(EffectMode.TRAILS)) {
                trailHistoryBuffers.clear();
            } else {
                trailHistoryBuffers.dispose();
            }
        }
    }

    public void addParticles(int amount) {
        if (amount <= 0) {
            return;
        }
        long requestedParticleCount = (long) particleCount() + amount;
        setParticleCount((int) Math.min(requestedParticleCount, Integer.MAX_VALUE), true);
    }

    public void removeParticles(int amount) {
        if (amount <= 0) {
            return;
        }
        setParticleCount(particleCount() - amount, true);
    }

    public void clearParticles() {
        setParticleCount(0, false);
    }

    public void setParticleCount(int particleCount) {
        setParticleCount(particleCount, false);
    }

    private void setParticleCount(int requestedParticleCount, boolean preserveExisting) {
        int newParticleCount = Math.max(0, Math.min(maximumParticleCount, requestedParticleCount));
        if (initialized) {
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
        particleBuffers.resize(oldParticleCount, newParticleCount, preserveExisting, config, particleRandom);
        trailHistoryBuffers.clear();
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
        if (initialized && effectMode == EffectMode.TRAILS && !enabled) {
            trailHistoryBuffers.dispose();
        }
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
        return trailHistoryBuffers.sampleCapacity();
    }

    public int effectiveTrailParticleStride() {
        return renderer.effectiveTrailParticleStride();
    }

    public int effectiveBloomDivisor() {
        return renderer.effectiveBloomDivisor();
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

    public void setAttractionMatrix(float[] values) {
        attractionMatrix.setActiveValues(values);
    }

    public float[] getAttractionMatrix() {
        return attractionMatrix.getFlatArray();
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

    public int groupCount() {
        return config.groupCount();
    }

    public void groupCount(int groupCount) {
        int previousGroupCount = config.groupCount();
        config.groupCount(groupCount);
        attractionMatrix.groupCount(config.groupCount());
        if (initialized && previousGroupCount != config.groupCount()) {
            reset();
        }
    }

    public int maxGroupCount() {
        return attractionMatrix.maxGroups();
    }

    public ImVec4[] groupColors() {
        return config.groupColors();
    }

    public void groupColors(ImVec4[] groupColors) {
        config.groupColors(groupColors);
    }

    public int gridSize() {
        return SpatialGridSizing.gridSize(bounds(), interactionRange());
    }

    public int gridCellCount() {
        return SpatialGridSizing.gridCellCount(bounds(), interactionRange());
    }

    public PerformanceSnapshot performanceSnapshot() {
        if (!initialized) {
            return new PerformanceSnapshot(-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 0L, particleCount(),
                    maxParticleCount(), gridCellCount());
        }
        double countMilliseconds = compute.gridCountMilliseconds();
        double scanMilliseconds = compute.gridScanMilliseconds();
        double scatterMilliseconds = compute.gridScatterMilliseconds();
        double integrationMilliseconds = compute.integrationMilliseconds();
        double simulationMilliseconds = countMilliseconds < 0.0 || scanMilliseconds < 0.0
                || scatterMilliseconds < 0.0 || integrationMilliseconds < 0.0
                        ? -1.0
                        : countMilliseconds + scanMilliseconds + scatterMilliseconds + integrationMilliseconds;
        long allocatedBytes = particleBuffers.allocatedBytes() + spatialGridBuffers.allocatedBytes()
                + trailHistoryBuffers.allocatedBytes() + renderer.allocatedEffectBytes();
        return new PerformanceSnapshot(countMilliseconds, scanMilliseconds, scatterMilliseconds,
                integrationMilliseconds, simulationMilliseconds, renderer.particleRenderMilliseconds(),
                renderer.trailRenderMilliseconds(), renderer.bloomMilliseconds(), allocatedBytes, particleCount(),
                maxParticleCount(), gridCellCount());
    }

    float[] readPositions() {
        return particleBuffers.readPositions(particleCount());
    }

    float[] readVelocities() {
        return particleBuffers.readVelocities(particleCount());
    }

    int[] readGridCounts() {
        return spatialGridBuffers.readCounts(gridCellCount());
    }

    int[] readGridParticleIds() {
        return spatialGridBuffers.readParticleIds(particleCount());
    }

    private int detectMaximumParticleCount() {
        long storageBlockLimit = glGetInteger64(GL_MAX_SHADER_STORAGE_BLOCK_SIZE) / (4L * Float.BYTES);
        long dispatchLimit = (long) glGetIntegeri(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0) * COMPUTE_WORK_GROUP_SIZE;
        long memoryLimit = SimulationDefaults.SIMULATION_MEMORY_BUDGET_BYTES / BYTES_PER_PARTICLE;
        long supported = Math.min(SimulationDefaults.MAX_PARTICLE_COUNT,
                Math.min(storageBlockLimit, Math.min(dispatchLimit, memoryLimit)));
        return (int) Math.max(1L, supported);
    }
}
