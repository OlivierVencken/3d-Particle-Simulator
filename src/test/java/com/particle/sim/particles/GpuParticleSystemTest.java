package com.particle.sim.particles;

import com.particle.sim.settings.SimulationDefaults;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GpuParticleSystemTest {
    private static final float EPSILON = 0.0001f;

    @Test
    void startsWithExpectedDefaultsWithoutInitializingOpenGlResources() {
        GpuParticleSystem system = new GpuParticleSystem();

        assertEquals(65_536, system.particleCount());
        assertEquals(1_000_000, system.maxParticleCount());
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
        GpuParticleSystem system = new GpuParticleSystem();

        system.groupCount(10);
        assertEquals(10, system.groupCount());

        system.groupCount(0);
        assertEquals(1, system.groupCount());

        system.groupCount(99);
        assertEquals(SimulationDefaults.MAX_GROUP_COUNT, system.groupCount());
    }

    @Test
    void setterBackedConfigurationRoundTrips() {
        GpuParticleSystem system = new GpuParticleSystem();

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
        GpuParticleSystem system = new GpuParticleSystem();

        system.pointSize(3.0f);
        system.trailThickness(8.0f);

        assertEquals(3.0f, system.trailThickness(), EPSILON);

        system.pointSize(1.5f);

        assertEquals(1.5f, system.trailThickness(), EPSILON);
    }

    @Test
    void gridSizeRoundsUpWorldDiameterByInteractionRange() {
        GpuParticleSystem system = new GpuParticleSystem();

        system.bounds(4.0f);
        system.interactionRange(0.95f);

        assertEquals(9, system.gridSize());

        system.interactionRange(4.0f);

        assertEquals(2, system.gridSize());
    }

    @Test
    void gridCellCountMatchesGridVolume() {
        GpuParticleSystem system = new GpuParticleSystem();

        system.bounds(4.0f);
        system.interactionRange(0.95f);

        assertEquals(729, system.gridCellCount());
    }

    @Test
    void stepMethodIsSafeBeforeInitialization() {
        GpuParticleSystem system = new GpuParticleSystem();

        assertDoesNotThrow(() -> {
            system.step();
        });
        assertEquals(65_536, system.particleCount());
    }

    @Test
    void attractionMatrixOperationsAreExposedThroughSystem() {
        GpuParticleSystem system = new GpuParticleSystem();

        system.zeroAttractionMatrix();
        system.attraction(1, 2, 0.6f);
        system.adjustAttraction(2, 1, -0.2f);
        system.symmetrizeAttractionMatrix();

        assertEquals(0.2f, system.attraction(1, 2), EPSILON);
        assertEquals(0.2f, system.attraction(2, 1), EPSILON);

        system.invertAttractionMatrix();

        assertEquals(-0.2f, system.attraction(1, 2), EPSILON);
        assertSame(system.getAttractionMatrix(), system.getAttractionMatrix());
    }
}
