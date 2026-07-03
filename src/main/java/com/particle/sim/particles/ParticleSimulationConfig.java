package com.particle.sim.particles;

import com.particle.sim.settings.SimulationDefaults;

import imgui.ImVec4;

public final class ParticleSimulationConfig {
    private int particleCount = SimulationDefaults.PARTICLE_COUNT;
    private float pointSize = SimulationDefaults.POINT_SIZE;
    private boolean fixedParticleScreenSize = SimulationDefaults.FIXED_PARTICLE_SCREEN_SIZE;
    private EffectMode effectMode = SimulationDefaults.EFFECT_MODE;
    private int glowBlurPasses = SimulationDefaults.GLOW_BLUR_PASSES;
    private float glowStrength = SimulationDefaults.GLOW_STRENGTH;
    private float glowRadius = SimulationDefaults.GLOW_RADIUS;
    private float glowFalloff = SimulationDefaults.GLOW_FALLOFF;
    private int trailLength = SimulationDefaults.TRAIL_LENGTH;
    private float trailThickness = SimulationDefaults.TRAIL_THICKNESS;
    private float bounds = SimulationDefaults.BOUNDS;
    private float forceFactor = SimulationDefaults.FORCE_FACTOR;
    private float velocityDamping = SimulationDefaults.VELOCITY_DAMPING;
    private float interactionRange = SimulationDefaults.INTERACTION_RANGE;
    private float repulsionRadius = SimulationDefaults.REPULSION_RADIUS;
    private float maxVelocity = SimulationDefaults.MAX_VELOCITY;
    private float boundaryBounce = SimulationDefaults.BOUNDARY_BOUNCE;
    private boolean toroidalWrap = SimulationDefaults.TOROIDAL_WRAP;
    private boolean densityRegulationEnabled = SimulationDefaults.DENSITY_REGULATION_ENABLED;
    private float densityLimit = SimulationDefaults.DENSITY_LIMIT;
    private DistanceMetric distanceMetric = SimulationDefaults.DISTANCE_METRIC;
    private int groupCount = SimulationDefaults.GROUP_COUNT;
    private ColorMode colorMode = SimulationDefaults.COLOR_MODE;
    private SpawnMode spawnMode = SimulationDefaults.SPAWN_MODE;
    private ImVec4[] groupColors = SimulationDefaults.GROUP_COLORS;

    public static ParticleSimulationConfig defaults() {
        return new ParticleSimulationConfig();
    }

    public ParticleSimulationConfig copy() {
        ParticleSimulationConfig copy = defaults();
        copy.applyFrom(this);
        return copy;
    }

    public void applyFrom(ParticleSimulationConfig source) {
        particleCount(source.particleCount);
        pointSize(source.pointSize);
        fixedParticleScreenSize(source.fixedParticleScreenSize);
        effectMode(source.effectMode);
        glowBlurPasses(source.glowBlurPasses);
        glowStrength(source.glowStrength);
        glowRadius(source.glowRadius);
        glowFalloff(source.glowFalloff);
        trailLength(source.trailLength);
        trailThickness(source.trailThickness);
        bounds(source.bounds);
        forceFactor(source.forceFactor);
        velocityDamping(source.velocityDamping);
        interactionRange(source.interactionRange);
        repulsionRadius(source.repulsionRadius);
        maxVelocity(source.maxVelocity);
        boundaryBounce(source.boundaryBounce);
        toroidalWrap(source.toroidalWrap);
        densityRegulationEnabled(source.densityRegulationEnabled);
        densityLimit(source.densityLimit);
        distanceMetric(source.distanceMetric);
        groupCount(source.groupCount);
        colorMode(source.colorMode);
        spawnMode(source.spawnMode);
    }

    public int particleCount() {
        return particleCount;
    }

    public void particleCount(int particleCount) {
        this.particleCount = Math.max(0, Math.min(SimulationDefaults.MAX_PARTICLE_COUNT, particleCount));
    }

    public float pointSize() {
        return pointSize;
    }

    public void pointSize(float pointSize) {
        this.pointSize = pointSize;
        trailThickness(trailThickness);
    }

    public boolean fixedParticleScreenSize() {
        return fixedParticleScreenSize;
    }

    public void fixedParticleScreenSize(boolean fixedParticleScreenSize) {
        this.fixedParticleScreenSize = fixedParticleScreenSize;
    }

    public EffectMode effectMode() {
        return effectMode;
    }

    public void effectMode(EffectMode effectMode) {
        this.effectMode = effectMode == null ? SimulationDefaults.EFFECT_MODE : effectMode;
    }

    public GlowSettings glowSettings() {
        return new GlowSettings(glowBlurPasses, glowStrength, glowRadius, glowFalloff);
    }

    public int glowBlurPasses() {
        return glowBlurPasses;
    }

    public void glowBlurPasses(int glowBlurPasses) {
        this.glowBlurPasses = Math.max(1,
                Math.min(64, glowBlurPasses));
    }

    public float glowStrength() {
        return glowStrength;
    }

    public void glowStrength(float glowStrength) {
        this.glowStrength = glowStrength;
    }

    public float glowRadius() {
        return glowRadius;
    }

    public void glowRadius(float glowRadius) {
        this.glowRadius = glowRadius;
    }

    public float glowFalloff() {
        return glowFalloff;
    }

    public void glowFalloff(float glowFalloff) {
        this.glowFalloff = glowFalloff;
    }

    public TrailSettings trailSettings() {
        return new TrailSettings(trailLength, trailThickness);
    }

    public int trailLength() {
        return trailLength;
    }

    public void trailLength(int trailLength) {
        this.trailLength = Math.max(SimulationDefaults.MIN_TRAIL_LENGTH,
                Math.min(SimulationDefaults.MAX_TRAIL_LENGTH, trailLength));
    }

    public float trailThickness() {
        return trailThickness;
    }

    public void trailThickness(float trailThickness) {
        this.trailThickness = clamp(trailThickness, SimulationDefaults.MIN_TRAIL_THICKNESS, pointSize);
    }

    public float bounds() {
        return bounds;
    }

    public void bounds(float bounds) {
        this.bounds = bounds;
    }

    public float forceFactor() {
        return forceFactor;
    }

    public void forceFactor(float forceFactor) {
        this.forceFactor = forceFactor;
    }

    public float velocityDamping() {
        return velocityDamping;
    }

    public void velocityDamping(float velocityDamping) {
        this.velocityDamping = velocityDamping;
    }

    public float interactionRange() {
        return interactionRange;
    }

    public void interactionRange(float interactionRange) {
        this.interactionRange = interactionRange;
    }

    public float repulsionRadius() {
        return repulsionRadius;
    }

    public void repulsionRadius(float repulsionRadius) {
        this.repulsionRadius = repulsionRadius;
    }

    public float maxVelocity() {
        return maxVelocity;
    }

    public void maxVelocity(float maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public float boundaryBounce() {
        return boundaryBounce;
    }

    public void boundaryBounce(float boundaryBounce) {
        this.boundaryBounce = boundaryBounce;
    }

    public boolean toroidalWrap() {
        return toroidalWrap;
    }

    public void toroidalWrap(boolean toroidalWrap) {
        this.toroidalWrap = toroidalWrap;
    }

    public boolean densityRegulationEnabled() {
        return densityRegulationEnabled;
    }

    public void densityRegulationEnabled(boolean densityRegulationEnabled) {
        this.densityRegulationEnabled = densityRegulationEnabled;
    }

    public float densityLimit() {
        return densityLimit;
    }

    public void densityLimit(float densityLimit) {
        this.densityLimit = densityLimit;
    }

    public DistanceMetric distanceMetric() {
        return distanceMetric;
    }

    public void distanceMetric(DistanceMetric distanceMetric) {
        this.distanceMetric = distanceMetric == null ? SimulationDefaults.DISTANCE_METRIC : distanceMetric;
    }

    public int groupCount() {
        return groupCount;
    }

    public void groupCount(int groupCount) {
        this.groupCount = Math.max(1, Math.min(SimulationDefaults.MAX_GROUP_COUNT, groupCount));
    }

    public ColorMode colorMode() {
        return colorMode;
    }

    public void colorMode(ColorMode colorMode) {
        this.colorMode = colorMode == null ? SimulationDefaults.COLOR_MODE : colorMode;
    }

    public SpawnMode spawnMode() {
        return spawnMode;
    }

    public void spawnMode(SpawnMode spawnMode) {
        this.spawnMode = spawnMode == null ? SimulationDefaults.SPAWN_MODE : spawnMode;
    }

    public ImVec4[] groupColors() {
        return groupColors;
    }

    public void groupColors(ImVec4[] groupColors) {
        if (groupColors == null || groupColors.length == 0) {
            this.groupColors = SimulationDefaults.GROUP_COLORS;
        } else {
            this.groupColors = groupColors;
        }
    }

    public void sanitize() {
        particleCount(particleCount);
        pointSize = clamp(pointSize, 1.0f, 8.0f);
        glowBlurPasses(glowBlurPasses);
        glowStrength = clamp(glowStrength, 0.0f, 6.0f);
        glowRadius = clamp(glowRadius, 0.5f, 12.0f);
        glowFalloff = clamp(glowFalloff, 0.05f, 3.0f);
        trailLength(trailLength);
        trailThickness(trailThickness);
        bounds = clamp(bounds, 2.0f, 10.0f);
        forceFactor = clamp(forceFactor, 0.0f, 10.0f);
        velocityDamping = clamp(velocityDamping, 0.85f, 1.0f);
        interactionRange = clamp(interactionRange, 0.2f, 3.0f);
        repulsionRadius = clamp(repulsionRadius, 0.02f, 0.95f);
        maxVelocity = clamp(maxVelocity, 0.5f, 16.0f);
        boundaryBounce = clamp(boundaryBounce, 0.0f, 1.0f);
        densityLimit = clamp(densityLimit, 0.0f, 500.0f);
        groupCount(groupCount);
        groupColors(groupColors);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
