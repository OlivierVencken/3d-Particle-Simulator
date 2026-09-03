package com.particle.sim.particles;

import com.particle.sim.particles.rendering.ColorMode;
import com.particle.sim.particles.rendering.EffectMode;
import com.particle.sim.particles.rendering.GlowSettings;
import com.particle.sim.particles.rendering.TrailSettings;
import com.particle.sim.particles.spawning.SpawnMode;
import com.particle.sim.settings.SimulationDefaults;
import imgui.ImVec4;
import java.util.EnumSet;
import java.util.Set;

public final class ParticleSimulationConfig {
    private int particleCount = SimulationDefaults.PARTICLE_COUNT;
    private float pointSize = SimulationDefaults.POINT_SIZE;
    private boolean fixedParticleScreenSize = SimulationDefaults.FIXED_PARTICLE_SCREEN_SIZE;
    private EnumSet<EffectMode> effectModes = EnumSet.noneOf(EffectMode.class);
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
    private ImVec4[] groupColors = SimulationDefaults.defaultGroupColors();

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
        effectModes(source.effectModes());
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
        groupColors(source.groupColors);
    }

    public int particleCount() {
        return particleCount;
    }

    public void particleCount(int particleCount) {
        this.particleCount =
                Math.max(0, Math.min(SimulationDefaults.MAX_PARTICLE_COUNT, particleCount));
    }

    public float pointSize() {
        return pointSize;
    }

    public void pointSize(float pointSize) {
        this.pointSize = clamp(pointSize, 1.0f, 8.0f, SimulationDefaults.POINT_SIZE);
        trailThickness(trailThickness);
    }

    public boolean fixedParticleScreenSize() {
        return fixedParticleScreenSize;
    }

    public void fixedParticleScreenSize(boolean fixedParticleScreenSize) {
        this.fixedParticleScreenSize = fixedParticleScreenSize;
    }

    public Set<EffectMode> effectModes() {
        return effectModes.isEmpty()
                ? EnumSet.noneOf(EffectMode.class)
                : EnumSet.copyOf(effectModes);
    }

    public void effectModes(Set<EffectMode> effectModes) {
        EnumSet<EffectMode> updatedEffectModes = EnumSet.noneOf(EffectMode.class);
        if (effectModes == null) {
            this.effectModes = updatedEffectModes;
            return;
        }
        for (EffectMode effectMode : effectModes) {
            if (effectMode != null) {
                updatedEffectModes.add(effectMode);
            }
        }
        this.effectModes = updatedEffectModes;
    }

    public boolean effectEnabled(EffectMode effectMode) {
        return effectMode != null && effectModes.contains(effectMode);
    }

    public void effectEnabled(EffectMode effectMode, boolean enabled) {
        if (effectMode == null) {
            return;
        }

        if (enabled) {
            effectModes.add(effectMode);
        } else {
            effectModes.remove(effectMode);
        }
    }

    public GlowSettings glowSettings() {
        return new GlowSettings(glowBlurPasses, glowStrength, glowRadius, glowFalloff);
    }

    public int glowBlurPasses() {
        return glowBlurPasses;
    }

    public void glowBlurPasses(int glowBlurPasses) {
        this.glowBlurPasses = Math.max(1, Math.min(64, glowBlurPasses));
    }

    public float glowStrength() {
        return glowStrength;
    }

    public void glowStrength(float glowStrength) {
        this.glowStrength = clamp(glowStrength, 0.0f, 6.0f, SimulationDefaults.GLOW_STRENGTH);
    }

    public float glowRadius() {
        return glowRadius;
    }

    public void glowRadius(float glowRadius) {
        this.glowRadius = clamp(glowRadius, 0.5f, 12.0f, SimulationDefaults.GLOW_RADIUS);
    }

    public float glowFalloff() {
        return glowFalloff;
    }

    public void glowFalloff(float glowFalloff) {
        this.glowFalloff = clamp(glowFalloff, 0.05f, 3.0f, SimulationDefaults.GLOW_FALLOFF);
    }

    public TrailSettings trailSettings() {
        return new TrailSettings(trailLength, trailThickness);
    }

    public int trailLength() {
        return trailLength;
    }

    public void trailLength(int trailLength) {
        this.trailLength =
                Math.max(
                        SimulationDefaults.MIN_TRAIL_LENGTH,
                        Math.min(SimulationDefaults.MAX_TRAIL_LENGTH, trailLength));
    }

    public float trailThickness() {
        return trailThickness;
    }

    public void trailThickness(float trailThickness) {
        this.trailThickness =
                clamp(
                        trailThickness,
                        SimulationDefaults.MIN_TRAIL_THICKNESS,
                        pointSize,
                        SimulationDefaults.TRAIL_THICKNESS);
    }

    public float bounds() {
        return bounds;
    }

    public void bounds(float bounds) {
        this.bounds = clamp(bounds, 2.0f, 10.0f, SimulationDefaults.BOUNDS);
    }

    public float forceFactor() {
        return forceFactor;
    }

    public void forceFactor(float forceFactor) {
        this.forceFactor = clamp(forceFactor, 0.0f, 10.0f, SimulationDefaults.FORCE_FACTOR);
    }

    public float velocityDamping() {
        return velocityDamping;
    }

    public void velocityDamping(float velocityDamping) {
        this.velocityDamping =
                clamp(velocityDamping, 0.85f, 1.0f, SimulationDefaults.VELOCITY_DAMPING);
    }

    public float interactionRange() {
        return interactionRange;
    }

    public void interactionRange(float interactionRange) {
        this.interactionRange =
                clamp(interactionRange, 0.2f, 3.0f, SimulationDefaults.INTERACTION_RANGE);
    }

    public float repulsionRadius() {
        return repulsionRadius;
    }

    public void repulsionRadius(float repulsionRadius) {
        this.repulsionRadius =
                clamp(repulsionRadius, 0.02f, 0.95f, SimulationDefaults.REPULSION_RADIUS);
    }

    public float maxVelocity() {
        return maxVelocity;
    }

    public void maxVelocity(float maxVelocity) {
        this.maxVelocity = clamp(maxVelocity, 0.5f, 16.0f, SimulationDefaults.MAX_VELOCITY);
    }

    public float boundaryBounce() {
        return boundaryBounce;
    }

    public void boundaryBounce(float boundaryBounce) {
        this.boundaryBounce = clamp(boundaryBounce, 0.0f, 1.0f, SimulationDefaults.BOUNDARY_BOUNCE);
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
        this.densityLimit = clamp(densityLimit, 0.0f, 500.0f, SimulationDefaults.DENSITY_LIMIT);
    }

    public DistanceMetric distanceMetric() {
        return distanceMetric;
    }

    public void distanceMetric(DistanceMetric distanceMetric) {
        this.distanceMetric =
                distanceMetric == null ? SimulationDefaults.DISTANCE_METRIC : distanceMetric;
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
        return copyColors(groupColors);
    }

    public void groupColors(ImVec4[] groupColors) {
        ImVec4[] defaults = SimulationDefaults.defaultGroupColors();
        if (groupColors != null) {
            int colorCount = Math.min(groupColors.length, defaults.length);
            for (int index = 0; index < colorCount; index++) {
                defaults[index] = sanitizeColor(groupColors[index], defaults[index]);
            }
        }
        this.groupColors = defaults;
    }

    public ImVec4 groupColor(int group) {
        ImVec4 color = groupColors[Math.floorMod(group, groupColors.length)];
        return copyColor(color);
    }

    public void groupColor(int group, ImVec4 color) {
        if (group < 0 || group >= groupColors.length || color == null) {
            return;
        }
        groupColors[group] = sanitizeColor(color, groupColors[group]);
    }

    public float[] groupColorRgbComponents() {
        float[] components = new float[groupColors.length * 3];
        for (int index = 0; index < groupColors.length; index++) {
            ImVec4 color = groupColors[index];
            int componentIndex = index * 3;
            components[componentIndex] = color.x;
            components[componentIndex + 1] = color.y;
            components[componentIndex + 2] = color.z;
        }
        return components;
    }

    public void sanitize() {
        particleCount(particleCount);
        effectModes(effectModes);
        pointSize(pointSize);
        glowBlurPasses(glowBlurPasses);
        glowStrength(glowStrength);
        glowRadius(glowRadius);
        glowFalloff(glowFalloff);
        trailLength(trailLength);
        trailThickness(trailThickness);
        bounds(bounds);
        forceFactor(forceFactor);
        velocityDamping(velocityDamping);
        interactionRange(interactionRange);
        repulsionRadius(repulsionRadius);
        maxVelocity(maxVelocity);
        boundaryBounce(boundaryBounce);
        densityLimit(densityLimit);
        groupCount(groupCount);
        groupColors(groupColors);
    }

    private static float clamp(float value, float min, float max, float fallback) {
        float finiteValue = Float.isFinite(value) ? value : fallback;
        return Math.max(min, Math.min(max, finiteValue));
    }

    private static ImVec4[] copyColors(ImVec4[] colors) {
        ImVec4[] copy = new ImVec4[colors.length];
        for (int index = 0; index < colors.length; index++) {
            copy[index] = copyColor(colors[index]);
        }
        return copy;
    }

    private static ImVec4 copyColor(ImVec4 color) {
        return new ImVec4(color.x, color.y, color.z, color.w);
    }

    private static ImVec4 sanitizeColor(ImVec4 color, ImVec4 fallback) {
        if (color == null) {
            return copyColor(fallback);
        }
        return new ImVec4(
                colorComponent(color.x, fallback.x),
                colorComponent(color.y, fallback.y),
                colorComponent(color.z, fallback.z),
                colorComponent(color.w, fallback.w));
    }

    private static float colorComponent(float value, float fallback) {
        return Float.isFinite(value) ? clamp(value, 0.0f, 1.0f, fallback) : fallback;
    }
}
