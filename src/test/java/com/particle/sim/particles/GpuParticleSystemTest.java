package com.particle.sim.particles;

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
        assertEquals(SpawnMode.RANDOM, system.spawnMode());
        assertFalse(system.toroidalWrap());
    }

    @Test
    void setterBackedConfigurationRoundTrips() {
        GpuParticleSystem system = new GpuParticleSystem();

        system.pointSize(3.5f);
        system.forceFactor(2.0f);
        system.interactionRange(0.5f);
        system.velocityDamping(0.9f);
        system.repulsionRadius(0.2f);
        system.maxVelocity(9.0f);
        system.boundaryBounce(0.4f);
        system.bounds(7.0f);
        system.colorMode(ColorMode.DENSITY);
        system.spawnMode(SpawnMode.CLUSTERS);
        system.toroidalWrap(true);

        assertEquals(3.5f, system.pointSize(), EPSILON);
        assertEquals(2.0f, system.forceFactor(), EPSILON);
        assertEquals(0.5f, system.interactionRange(), EPSILON);
        assertEquals(0.9f, system.velocityDamping(), EPSILON);
        assertEquals(0.2f, system.repulsionRadius(), EPSILON);
        assertEquals(9.0f, system.maxVelocity(), EPSILON);
        assertEquals(0.4f, system.boundaryBounce(), EPSILON);
        assertEquals(7.0f, system.bounds(), EPSILON);
        assertEquals(ColorMode.DENSITY, system.colorMode());
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
