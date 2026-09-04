package com.particle.sim.particles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.particle.sim.graphics.RgbaColor;
import com.particle.sim.particles.rendering.ColorMode;
import com.particle.sim.particles.rendering.EffectMode;
import com.particle.sim.particles.spawning.SpawnMode;
import com.particle.sim.settings.SimulationDefaults;
import org.junit.jupiter.api.Test;

class ParticleSystemTest {
    private static final float EPSILON = 0.0001f;

    @Test
    void startsWithExpectedDefaultsWithoutInitializingOpenGlResources() {
        ParticleSystem system = new ParticleSystem();

        assertEquals(65_536, system.particleCount());
        assertEquals(16_000_000, system.maximumParticleCount());
        assertEquals(6, system.groupCount());
        assertEquals(ColorMode.GROUP, system.colorMode());
        assertTrue(system.effectModes().isEmpty());
        assertFalse(system.effectEnabled(EffectMode.GLOW));
        assertFalse(system.effectEnabled(EffectMode.TRAILS));
        assertEquals(SpawnMode.RANDOM, system.spawnMode());
        assertEquals(DistanceMetric.EUCLIDEAN, system.distanceMetric());
        assertEquals(SimulationDefaults.GLOW_BLUR_PASSES, system.glowBlurPasses());
        assertEquals(SimulationDefaults.GLOW_STRENGTH, system.glowStrength(), EPSILON);
        assertEquals(SimulationDefaults.GLOW_RADIUS, system.glowRadius(), EPSILON);
        assertEquals(SimulationDefaults.GLOW_FALLOFF, system.glowFalloff(), EPSILON);
        assertEquals(SimulationDefaults.TRAIL_LENGTH, system.trailLength());
        assertEquals(SimulationDefaults.TRAIL_THICKNESS, system.trailThickness(), EPSILON);
        assertFalse(system.toroidalWrap());
        assertFalse(system.fixedParticleScreenSize());
        assertFalse(system.densityRegulationEnabled());
    }

    @Test
    void groupCountCanBeChangedWithinSupportedRange() {
        ParticleSystem system = new ParticleSystem();

        system.groupCount(10);
        assertEquals(10, system.groupCount());

        system.groupCount(0);
        assertEquals(1, system.groupCount());

        system.groupCount(99);
        assertEquals(SimulationDefaults.MAX_GROUP_COUNT, system.groupCount());
    }

    @Test
    void setterBackedConfigurationRoundTrips() {
        ParticleSystem system = new ParticleSystem();

        system.pointSize(3.5f);
        system.fixedParticleScreenSize(true);
        system.forceFactor(2.0f);
        system.interactionRange(0.5f);
        system.velocityDamping(0.9f);
        system.repulsionRadius(0.2f);
        system.maxVelocity(9.0f);
        system.boundaryBounce(0.4f);
        system.bounds(7.0f);
        system.colorMode(ColorMode.DENSITY);
        system.effectEnabled(EffectMode.GLOW, true);
        system.effectEnabled(EffectMode.TRAILS, true);
        system.glowBlurPasses(24);
        system.glowStrength(3.5f);
        system.glowRadius(6.25f);
        system.glowFalloff(1.2f);
        system.trailLength(20);
        system.trailThickness(2.1f);
        system.spawnMode(SpawnMode.CLUSTERS);
        system.distanceMetric(DistanceMetric.MANHATTAN);
        system.toroidalWrap(true);
        system.densityRegulationEnabled(true);
        system.densityLimit(200.0f);

        assertEquals(3.5f, system.pointSize(), EPSILON);
        assertTrue(system.fixedParticleScreenSize());
        assertEquals(2.0f, system.forceFactor(), EPSILON);
        assertEquals(0.5f, system.interactionRange(), EPSILON);
        assertEquals(0.9f, system.velocityDamping(), EPSILON);
        assertEquals(0.2f, system.repulsionRadius(), EPSILON);
        assertEquals(9.0f, system.maxVelocity(), EPSILON);
        assertEquals(0.4f, system.boundaryBounce(), EPSILON);
        assertEquals(7.0f, system.bounds(), EPSILON);
        assertEquals(ColorMode.DENSITY, system.colorMode());
        assertTrue(system.effectEnabled(EffectMode.GLOW));
        assertTrue(system.effectEnabled(EffectMode.TRAILS));
        assertEquals(24, system.glowBlurPasses());
        assertEquals(3.5f, system.glowStrength(), EPSILON);
        assertEquals(6.25f, system.glowRadius(), EPSILON);
        assertEquals(1.2f, system.glowFalloff(), EPSILON);
        assertEquals(20, system.trailLength());
        assertEquals(2.1f, system.trailThickness(), EPSILON);
        assertEquals(SpawnMode.CLUSTERS, system.spawnMode());
        assertEquals(DistanceMetric.MANHATTAN, system.distanceMetric());
        assertTrue(system.toroidalWrap());
        assertTrue(system.densityRegulationEnabled());
        assertEquals(200.0f, system.densityLimit(), EPSILON);
    }

    @Test
    void trailThicknessCannotExceedParticleSize() {
        ParticleSystem system = new ParticleSystem();

        system.pointSize(3.0f);
        system.trailThickness(8.0f);

        assertEquals(3.0f, system.trailThickness(), EPSILON);

        system.pointSize(1.5f);

        assertEquals(1.5f, system.trailThickness(), EPSILON);
    }

    @Test
    void gridSizeRoundsDownSoUniformCellsAreAtLeastTheInteractionRange() {
        ParticleSystem system = new ParticleSystem();

        system.bounds(4.0f);
        system.interactionRange(0.95f);

        assertEquals(8, system.gridSize());

        system.interactionRange(3.0f);

        assertEquals(2, system.gridSize());
    }

    @Test
    void simulationInputsAreClampedBeforeTheyReachGridSizing() {
        ParticleSystem system = new ParticleSystem();

        system.bounds(-10.0f);
        system.interactionRange(0.0f);

        assertEquals(2.0f, system.bounds(), EPSILON);
        assertEquals(0.2f, system.interactionRange(), EPSILON);
        assertEquals(8_000, system.gridCellCount());
    }

    @Test
    void gridCellCountMatchesGridVolume() {
        ParticleSystem system = new ParticleSystem();

        system.bounds(4.0f);
        system.interactionRange(0.95f);

        assertEquals(512, system.gridCellCount());
    }

    @Test
    void stepMethodIsSafeBeforeInitialization() {
        ParticleSystem system = new ParticleSystem();

        assertDoesNotThrow(
                () -> {
                    system.step();
                });
        assertEquals(65_536, system.particleCount());
    }

    @Test
    void attractionMatrixOperationsAreExposedThroughSystem() {
        ParticleSystem system = new ParticleSystem();

        system.zeroAttractionMatrix();
        system.attraction(1, 2, 0.6f);
        system.adjustAttraction(2, 1, -0.2f);
        system.symmetrizeAttractionMatrix();

        assertEquals(0.2f, system.attraction(1, 2), EPSILON);
        assertEquals(0.2f, system.attraction(2, 1), EPSILON);

        system.invertAttractionMatrix();

        assertEquals(-0.2f, system.attraction(1, 2), EPSILON);
        assertSame(system.attractionMatrix(), system.attractionMatrix());
    }

    @Test
    void groupColorsAreClampedAndStoredAsImmutableValues() {
        ParticleSystem system = new ParticleSystem();
        float fallbackBlue = system.groupColor(1).blue();
        RgbaColor requested = new RgbaColor(2.0f, -1.0f, Float.NaN, 0.4f);

        system.groupColor(1, requested);
        RgbaColor stored = system.groupColor(1);

        assertEquals(1.0f, stored.red(), EPSILON);
        assertEquals(0.0f, stored.green(), EPSILON);
        assertEquals(fallbackBlue, stored.blue(), EPSILON);
        assertEquals(0.4f, stored.alpha(), EPSILON);
        assertEquals(stored, system.groupColor(1));
        assertEquals(SimulationDefaults.MAX_GROUP_COUNT, system.groupColors().length);
    }

    @Test
    void nonFiniteSimulationValuesFallBackToDefaults() {
        ParticleSystem system = new ParticleSystem();

        system.pointSize(Float.NaN);
        system.glowStrength(Float.POSITIVE_INFINITY);
        system.bounds(Float.NEGATIVE_INFINITY);
        system.interactionRange(Float.NaN);
        system.attraction(0, 0, Float.POSITIVE_INFINITY);

        assertEquals(SimulationDefaults.POINT_SIZE, system.pointSize(), EPSILON);
        assertEquals(SimulationDefaults.GLOW_STRENGTH, system.glowStrength(), EPSILON);
        assertEquals(SimulationDefaults.BOUNDS, system.bounds(), EPSILON);
        assertEquals(SimulationDefaults.INTERACTION_RANGE, system.interactionRange(), EPSILON);
        assertEquals(0.0f, system.attraction(0, 0), EPSILON);
    }
}
