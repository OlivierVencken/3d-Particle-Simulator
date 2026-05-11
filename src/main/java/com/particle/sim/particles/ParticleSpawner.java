package com.particle.sim.particles;

import java.nio.FloatBuffer;
import java.util.Random;

public final class ParticleSpawner {

    public static void spawnParticles(FloatBuffer positions, FloatBuffer velocities, int count, float bounds,
            int groupCount, SpawnMode mode, Random random) {
        switch (mode) {
            case POINT:
                spawnPoint(positions, velocities, count, groupCount, random);
                break;
            case SHELL:
                spawnShell(positions, velocities, count, bounds, groupCount, random);
                break;
            case SPHERICAL:
                spawnSpherical(positions, velocities, count, bounds, groupCount, random);
                break;
            case DISC:
                spawnDisc(positions, velocities, count, bounds, groupCount, random);
                break;
            case SPIRAL:
                spawnSpiral(positions, velocities, count, bounds, groupCount, random);
                break;
            case CLUSTERS:
                spawnClusters(positions, velocities, count, bounds, groupCount, random);
                break;
            case GRID:
                spawnGrid(positions, velocities, count, bounds, groupCount, random);
                break;
            case RANDOM:
            default:
                spawnRandom(positions, velocities, count, bounds, groupCount, random);
                break;
        }
    }

    private static void writeVelocity(FloatBuffer velocities, Random random) {
        velocities
                .put((random.nextFloat() - 0.5f) * 0.2f)
                .put((random.nextFloat() - 0.5f) * 0.2f)
                .put((random.nextFloat() - 0.5f) * 0.2f)
                .put(0.0f);
    }

    private static void spawnPoint(FloatBuffer positions, FloatBuffer velocities, int count, int groupCount,
            Random random) {
        for (int i = 0; i < count; i++) {
            positions.put(0.0f).put(0.0f).put(0.0f).put(random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }

    private static void spawnShell(FloatBuffer positions, FloatBuffer velocities, int count, float bounds,
            int groupCount, Random random) {
        float r = bounds * 0.6f;
        for (int i = 0; i < count; i++) {
            float theta = random.nextFloat() * (float) Math.PI * 2.0f;
            float phi = (float) Math.acos(2.0f * random.nextFloat() - 1.0f);
            float x = r * (float) Math.sin(phi) * (float) Math.cos(theta);
            float y = r * (float) Math.cos(phi);
            float z = r * (float) Math.sin(phi) * (float) Math.sin(theta);

            positions.put(x).put(y).put(z).put(random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }

    private static void spawnSpherical(FloatBuffer positions, FloatBuffer velocities, int count, float bounds,
            int groupCount, Random random) {
        for (int i = 0; i < count; i++) {
            float r = random.nextFloat() * bounds * 0.6f;
            float theta = random.nextFloat() * (float) Math.PI * 2.0f;
            float phi = (float) Math.acos(2.0f * random.nextFloat() - 1.0f);
            float x = r * (float) Math.sin(phi) * (float) Math.cos(theta);
            float y = r * (float) Math.cos(phi);
            float z = r * (float) Math.sin(phi) * (float) Math.sin(theta);

            positions.put(x).put(y).put(z).put(random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }

    private static void spawnDisc(FloatBuffer positions, FloatBuffer velocities, int count, float bounds,
            int groupCount, Random random) {
        for (int i = 0; i < count; i++) {
            float r = (float) Math.sqrt(random.nextFloat()) * bounds * 0.8f;
            float theta = random.nextFloat() * (float) Math.PI * 2.0f;
            float x = r * (float) Math.cos(theta);
            float y = (random.nextFloat() - 0.5f) * bounds * 0.05f;
            float z = r * (float) Math.sin(theta);

            positions.put(x).put(y).put(z).put(random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }

    private static void spawnSpiral(FloatBuffer positions, FloatBuffer velocities, int count, float bounds,
            int groupCount, Random random) {
        int arms = 3;
        for (int i = 0; i < count; i++) {
            int group = random.nextInt(groupCount);
            float t = (float) i / count;
            float r = bounds * 0.8f * t;
            float theta = t * (float) Math.PI * 8.0f + (group % arms) * ((float) Math.PI * 2.0f / arms);
            float x = r * (float) Math.cos(theta);
            float y = (random.nextFloat() - 0.5f) * bounds * 0.2f * (1.0f - t);
            float z = r * (float) Math.sin(theta);

            positions.put(x).put(y).put(z).put(group);
            writeVelocity(velocities, random);
        }
    }

    private static void spawnClusters(FloatBuffer positions, FloatBuffer velocities, int count, float bounds,
            int groupCount, Random random) {
        float clusterRadius = bounds * 0.5f;
        for (int i = 0; i < count; i++) {
            int group = random.nextInt(groupCount);
            float clusterTheta = group * ((float) Math.PI * 2.0f / groupCount);
            float cx = clusterRadius * (float) Math.cos(clusterTheta);
            float cy = (group % 2 == 0 ? 1 : -1) * bounds * 0.3f;
            float cz = clusterRadius * (float) Math.sin(clusterTheta);

            float x = cx + (random.nextFloat() - 0.5f) * bounds * 0.2f;
            float y = cy + (random.nextFloat() - 0.5f) * bounds * 0.2f;
            float z = cz + (random.nextFloat() - 0.5f) * bounds * 0.2f;

            positions.put(x).put(y).put(z).put(group);
            writeVelocity(velocities, random);
        }
    }

    private static void spawnGrid(FloatBuffer positions, FloatBuffer velocities, int count, float bounds,
            int groupCount, Random random) {
        int gridSize = (int) Math.ceil(Math.pow(count, 1.0 / 3.0));
        if (gridSize == 0)
            gridSize = 1;
        float gridSpacing = (bounds * 2.0f) / gridSize;

        for (int i = 0; i < count; i++) {
            int ix = i % gridSize;
            int iy = (i / gridSize) % gridSize;
            int iz = i / (gridSize * gridSize);
            float x = -bounds + ix * gridSpacing + gridSpacing * 0.5f;
            float y = -bounds + iy * gridSpacing + gridSpacing * 0.5f;
            float z = -bounds + iz * gridSpacing + gridSpacing * 0.5f;

            positions.put(x).put(y).put(z).put(random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }

    private static void spawnRandom(FloatBuffer positions, FloatBuffer velocities, int count, float bounds,
            int groupCount, Random random) {
        for (int i = 0; i < count; i++) {
            float x = (random.nextFloat() - 0.5f) * 2.0f * bounds;
            float y = (random.nextFloat() - 0.5f) * 2.0f * bounds;
            float z = (random.nextFloat() - 0.5f) * 2.0f * bounds;

            positions.put(x).put(y).put(z).put(random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }
}
