package com.particle.sim.particles;

import com.particle.sim.settings.SimulationDefaults;
import org.junit.jupiter.api.Test;

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
        assertEquals(128, system.maxParticlesPerCell());
        assertEquals(ColorMode.GROUP, system.colorMode());
        assertEquals(EffectMode.NONE, system.effectMode());
        assertEquals(SpawnMode.RANDOM, system.spawnMode());
        assertFalse(system.toroidalWrap());
        assertFalse(system.fixedParticleScreenSize());
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
        system.effectMode(EffectMode.GLOW);
        system.spawnMode(SpawnMode.CLUSTERS);
        system.toroidalWrap(true);

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
        assertEquals(EffectMode.GLOW, system.effectMode());
        assertEquals(SpawnMode.CLUSTERS, system.spawnMode());
        assertTrue(system.toroidalWrap());
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
    void spatialMapSizeScalesWithLikelyOccupiedGridCells() {
        GpuParticleSystem system = new GpuParticleSystem();

        system.bounds(4.0f);
        system.interactionRange(0.95f);

        assertTrue(system.spatialMapSize() < 2_000);
    }

    @Test
    void spatialMapSizeStaysCappedForWorstCaseSettings() {
        GpuParticleSystem system = new GpuParticleSystem();

        system.setParticleCount(system.maxParticleCount());
        system.bounds(10.0f);
        system.interactionRange(0.2f);

        assertEquals(GpuParticleSystem.MAX_SPATIAL_MAP_SIZE, system.spatialMapSize());
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
