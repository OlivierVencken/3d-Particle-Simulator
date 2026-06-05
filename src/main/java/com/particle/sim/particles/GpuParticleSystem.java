package com.particle.sim.particles;

import com.particle.sim.settings.SimulationDefaults;

import imgui.ImVec4;

import java.util.Random;

public final class GpuParticleSystem {
    private static final int WORK_GROUP_SIZE = 256;
    public static final int MAX_SPATIAL_MAP_SIZE = SpatialGridSizing.MAX_SPATIAL_MAP_SIZE;

    private final ParticleBuffers particleBuffers = new ParticleBuffers();
    private final SpatialGridBuffers spatialGridBuffers = new SpatialGridBuffers();
    private final ParticleRenderer renderer = new ParticleRenderer();
    private final ParticleCompute compute = new ParticleCompute();

    private final ParticleSimulationConfig config = ParticleSimulationConfig.defaults();
    private final AttractionMatrix attractionMatrix = new AttractionMatrix(
            SimulationDefaults.GROUP_COUNT,
            SimulationDefaults.MAX_GROUP_COUNT);
    private final Random particleRandom = new Random();
    private boolean initialized;

    public void init() {
        compute.init();
        renderer.init();
        spatialGridBuffers.init(desiredSpatialMapSize());
        attractionMatrix.randomize();
        initialized = true;
        reset();
    }

    public void step() {
        advanceSimulation((float) SimulationDefaults.SIMULATION_STEP_SECONDS);
    }

    public void stepBack() {
        if (!initialized || !particleBuffers.restoreSnapshot()) {
            return;
        }

        rebuildSpatialGrid();
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

        spatialGridBuffers.ensureCapacity(desiredSpatialMapSize());
        compute.bindBuffers(particleBuffers, spatialGridBuffers);

        particleBuffers.captureSnapshot();

        spatialGridBuffers.clear();
        compute.setUniforms(this, deltaTime, 0);
        compute.dispatch(particleCount(), WORK_GROUP_SIZE, false);

        compute.setUniforms(this, deltaTime, 1);
        compute.dispatch(particleCount(), WORK_GROUP_SIZE, true);
    }

    private void rebuildSpatialGrid() {
        if (particleCount() == 0) {
            spatialGridBuffers.clear();
            return;
        }

        spatialGridBuffers.ensureCapacity(desiredSpatialMapSize());
        compute.bindBuffers(particleBuffers, spatialGridBuffers);

        spatialGridBuffers.clear();
        compute.setUniforms(this, 0.0f, 0);
        compute.dispatch(particleCount(), WORK_GROUP_SIZE, false);
    }

    public void render(int width, int height, float[] viewMatrix) {
        renderer.render(width, height, viewMatrix, particleBuffers, spatialGridBuffers, particleCount(), pointSize(),
                fixedParticleScreenSize(), effectMode(), colorMode().ordinal(), groupCount(), maxVelocity(), bounds(),
                interactionRange(), spatialMapSize(), glowSettings());
    }

    public void dispose() {
        particleBuffers.dispose();
        spatialGridBuffers.dispose();
        compute.dispose();
        renderer.dispose();
    }

    public int particleCount() {
        return config.particleCount();
    }

    public int maxParticleCount() {
        return SimulationDefaults.MAX_PARTICLE_COUNT;
    }

    public ParticleSimulationConfig config() {
        return config.copy();
    }

    public void applyConfig(ParticleSimulationConfig config) {
        if (config == null) {
            return;
        }

        setParticleCount(config.particleCount());
        pointSize(config.pointSize());
        fixedParticleScreenSize(config.fixedParticleScreenSize());
        effectMode(config.effectMode());
        glowBlurPasses(config.glowBlurPasses());
        glowStrength(config.glowStrength());
        glowRadius(config.glowRadius());
        glowFalloff(config.glowFalloff());
        bounds(config.bounds());
        forceFactor(config.forceFactor());
        velocityDamping(config.velocityDamping());
        interactionRange(config.interactionRange());
        repulsionRadius(config.repulsionRadius());
        maxVelocity(config.maxVelocity());
        boundaryBounce(config.boundaryBounce());
        toroidalWrap(config.toroidalWrap());
        densityRegulationEnabled(config.densityRegulationEnabled());
        densityLimit(config.densityLimit());
        distanceMetric(config.distanceMetric());
        groupCount(config.groupCount());
        colorMode(config.colorMode());
        spawnMode(config.spawnMode());
        groupColors(config.groupColors());
    }

    public void addParticles(int amount) {
        if (amount <= 0) {
            return;
        }
        setParticleCount(particleCount() + amount, true);
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
        int newParticleCount = Math.max(0, Math.min(SimulationDefaults.MAX_PARTICLE_COUNT, requestedParticleCount));
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
        int newParticleCount = Math.max(0, Math.min(SimulationDefaults.MAX_PARTICLE_COUNT, requestedParticleCount));
        particleBuffers.resize(oldParticleCount, newParticleCount, preserveExisting, config, particleRandom);
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

    public EffectMode effectMode() {
        return config.effectMode();
    }

    public void effectMode(EffectMode effectMode) {
        config.effectMode(effectMode);
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

    public int spatialMapSize() {
        return initialized ? spatialGridBuffers.mapSize() : desiredSpatialMapSize();
    }

    private int desiredSpatialMapSize() {
        return SpatialGridSizing.spatialMapSize(particleCount(), bounds(), interactionRange());
    }

    public int maxParticlesPerCell() {
        return SpatialGridBuffers.MAX_PARTICLES_PER_CELL;
    }
}
