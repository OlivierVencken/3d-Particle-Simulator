package com.particle.sim.particles;

import org.junit.jupiter.api.Test;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticleSpawnerTest {
    private static final float EPSILON = 0.0001f;
    private static final float BOUNDS = 10.0f;
    private static final int GROUP_COUNT = 6;

    @Test
    void allSpawnModesFillPositionAndVelocityBuffers() {
        for (SpawnMode mode : SpawnMode.values()) {
            FloatBuffer positions = FloatBuffer.allocate(16);
            FloatBuffer velocities = FloatBuffer.allocate(16);
            IntBuffer groups = IntBuffer.allocate(4);

            ParticleSpawner.spawnParticles(positions, velocities, groups, 4, BOUNDS, GROUP_COUNT, mode,
                    new Random(12));

            assertEquals(16, positions.position(), mode + " did not write all positions");
            assertEquals(16, velocities.position(), mode + " did not write all velocities");
            assertEquals(4, groups.position(), mode + " did not write all groups");
        }
    }

    @Test
    void allSpawnModesAssignValidGroupsAndBoundedInitialVelocities() {
        for (SpawnMode mode : SpawnMode.values()) {
            SpawnedParticles particles = spawn(mode, 64, 99);

            for (int i = 0; i < 64; i++) {
                int group = particles.groups.get(i);
                assertTrue(group >= 0 && group < GROUP_COUNT,
                        mode + " assigned invalid group " + group);

                assertEquals(0.0f, particles.positions.get(i * 4 + 3), EPSILON);
                assertTrue(Math.abs(particles.velocities.get(i * 4)) <= 0.1f + EPSILON);
                assertTrue(Math.abs(particles.velocities.get(i * 4 + 1)) <= 0.1f + EPSILON);
                assertTrue(Math.abs(particles.velocities.get(i * 4 + 2)) <= 0.1f + EPSILON);
                assertEquals(0.0f, particles.velocities.get(i * 4 + 3), EPSILON);
            }
        }
    }

    @Test
    void pointModeSpawnsEverythingAtOrigin() {
        SpawnedParticles particles = spawn(SpawnMode.POINT, 8, 1);

        for (int i = 0; i < 8; i++) {
            assertEquals(0.0f, particles.positions.get(i * 4), EPSILON);
            assertEquals(0.0f, particles.positions.get(i * 4 + 1), EPSILON);
            assertEquals(0.0f, particles.positions.get(i * 4 + 2), EPSILON);
        }
    }

    @Test
    void randomModeSpawnsInsideBounds() {
        SpawnedParticles particles = spawn(SpawnMode.RANDOM, 64, 2);

        for (int i = 0; i < 64; i++) {
            assertWithin(-BOUNDS, BOUNDS, particles.positions.get(i * 4));
            assertWithin(-BOUNDS, BOUNDS, particles.positions.get(i * 4 + 1));
            assertWithin(-BOUNDS, BOUNDS, particles.positions.get(i * 4 + 2));
        }
    }

    @Test
    void shellModeSpawnsOnFixedRadius() {
        SpawnedParticles particles = spawn(SpawnMode.SHELL, 32, 3);

        for (int i = 0; i < 32; i++) {
            assertEquals(BOUNDS * 0.6f, radius(particles, i), EPSILON);
        }
    }

    @Test
    void sphericalModeSpawnsInsideSphere() {
        SpawnedParticles particles = spawn(SpawnMode.SPHERICAL, 64, 4);

        for (int i = 0; i < 64; i++) {
            assertTrue(radius(particles, i) <= BOUNDS * 0.6f + EPSILON);
        }
    }

    @Test
    void discModeKeepsParticlesInThinHorizontalDisc() {
        SpawnedParticles particles = spawn(SpawnMode.DISC, 64, 5);

        for (int i = 0; i < 64; i++) {
            float x = particles.positions.get(i * 4);
            float y = particles.positions.get(i * 4 + 1);
            float z = particles.positions.get(i * 4 + 2);
            assertTrue(Math.sqrt(x * x + z * z) <= BOUNDS * 0.8f + EPSILON);
            assertWithin(-BOUNDS * 0.025f, BOUNDS * 0.025f, y);
        }
    }

    @Test
    void spiralModeStartsAtCenterAndExpandsWithinBounds() {
        SpawnedParticles particles = spawn(SpawnMode.SPIRAL, 12, 6);

        assertEquals(0.0f, particles.positions.get(0), EPSILON);
        assertEquals(0.0f, particles.positions.get(2), EPSILON);

        for (int i = 0; i < 12; i++) {
            float x = particles.positions.get(i * 4);
            float y = particles.positions.get(i * 4 + 1);
            float z = particles.positions.get(i * 4 + 2);
            assertTrue(Math.sqrt(x * x + z * z) <= BOUNDS * 0.8f + EPSILON);
            assertWithin(-BOUNDS * 0.1f, BOUNDS * 0.1f, y);
        }
    }

    @Test
    void clusterModeSpawnsNearGroupClusterCenters() {
        SpawnedParticles particles = spawn(SpawnMode.CLUSTERS, 64, 7);

        for (int i = 0; i < 64; i++) {
            int group = particles.groups.get(i);
            float clusterTheta = group * ((float) Math.PI * 2.0f / GROUP_COUNT);
            float centerX = BOUNDS * 0.5f * (float) Math.cos(clusterTheta);
            float centerY = (group % 2 == 0 ? 1.0f : -1.0f) * BOUNDS * 0.3f;
            float centerZ = BOUNDS * 0.5f * (float) Math.sin(clusterTheta);

            assertWithin(centerX - BOUNDS * 0.1f, centerX + BOUNDS * 0.1f, particles.positions.get(i * 4));
            assertWithin(centerY - BOUNDS * 0.1f, centerY + BOUNDS * 0.1f, particles.positions.get(i * 4 + 1));
            assertWithin(centerZ - BOUNDS * 0.1f, centerZ + BOUNDS * 0.1f, particles.positions.get(i * 4 + 2));
        }
    }

    @Test
    void gridModePlacesParticlesAtCellCenters() {
        SpawnedParticles particles = spawn(SpawnMode.GRID, 8, 8);

        for (int i = 0; i < 8; i++) {
            assertTrue(Math.abs(particles.positions.get(i * 4)) == BOUNDS * 0.5f);
            assertTrue(Math.abs(particles.positions.get(i * 4 + 1)) == BOUNDS * 0.5f);
            assertTrue(Math.abs(particles.positions.get(i * 4 + 2)) == BOUNDS * 0.5f);
        }
    }

    private static SpawnedParticles spawn(SpawnMode mode, int count, long seed) {
        FloatBuffer positions = FloatBuffer.allocate(count * 4);
        FloatBuffer velocities = FloatBuffer.allocate(count * 4);
        IntBuffer groups = IntBuffer.allocate(count);
        ParticleSpawner.spawnParticles(positions, velocities, groups, count, BOUNDS, GROUP_COUNT, mode,
                new Random(seed));
        positions.flip();
        velocities.flip();
        groups.flip();
        return new SpawnedParticles(positions, velocities, groups);
    }

    private static float radius(SpawnedParticles particles, int index) {
        float x = particles.positions.get(index * 4);
        float y = particles.positions.get(index * 4 + 1);
        float z = particles.positions.get(index * 4 + 2);
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private static void assertWithin(float min, float max, float value) {
        assertTrue(value >= min - EPSILON && value <= max + EPSILON,
                value + " was not within [" + min + ", " + max + "]");
    }

    private record SpawnedParticles(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups) {
    }
}
