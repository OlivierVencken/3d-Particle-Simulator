package com.particle.sim.particles;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

public final class ParticleSpawner {

    public static void spawnParticles(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, SpawnMode mode, Random random) {
        spawnParticles(positions, velocities, groups, count, bounds, groupCount, mode,
                SimulationDimension.THREE_D, random);
    }

    public static void spawnParticles(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, SpawnMode mode, SimulationDimension dimension, Random random) {
        if (!mode.supportedIn(dimension)) {
            throw new IllegalArgumentException(mode + " spawning is not available in 4D");
        }
        if (dimension == SimulationDimension.FOUR_D) {
            spawnFourDimensional(positions, velocities, groups, count, bounds, groupCount, mode, random);
            return;
        }
        switch (mode) {
            case POINT:
                spawnPoint(positions, velocities, groups, count, groupCount, random);
                break;
            case SHELL:
                spawnShell(positions, velocities, groups, count, bounds, groupCount, random);
                break;
            case SPHERICAL:
                spawnSpherical(positions, velocities, groups, count, bounds, groupCount, random);
                break;
            case DISC:
                spawnDisc(positions, velocities, groups, count, bounds, groupCount, random);
                break;
            case SPIRAL:
                spawnSpiral(positions, velocities, groups, count, bounds, groupCount, random);
                break;
            case CLUSTERS:
                spawnClusters(positions, velocities, groups, count, bounds, groupCount, random);
                break;
            case GRID:
                spawnGrid(positions, velocities, groups, count, bounds, groupCount, random);
                break;
            case RANDOM:
            default:
                spawnRandom(positions, velocities, groups, count, bounds, groupCount, random);
                break;
        }
    }

    private static void spawnFourDimensional(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups,
            int count, float bounds, int groupCount, SpawnMode mode, Random random) {
        switch (mode) {
            case POINT -> {
                for (int i = 0; i < count; i++) {
                    writePosition4d(positions, groups, 0.0f, 0.0f, 0.0f, 0.0f, random.nextInt(groupCount));
                    writeVelocity4d(velocities, random);
                }
            }
            case RANDOM -> {
                for (int i = 0; i < count; i++) {
                    writePosition4d(positions, groups,
                            randomCoordinate(bounds, random), randomCoordinate(bounds, random),
                            randomCoordinate(bounds, random), randomCoordinate(bounds, random),
                            random.nextInt(groupCount));
                    writeVelocity4d(velocities, random);
                }
            }
            case SPHERICAL -> spawnFourBall(positions, velocities, groups, count, bounds, groupCount, random);
            case SHELL -> spawnThreeSphere(positions, velocities, groups, count, bounds, groupCount, random);
            case GRID -> spawnGrid4d(positions, velocities, groups, count, bounds, groupCount, random);
            case CLUSTERS -> spawnClusters4d(positions, velocities, groups, count, bounds, groupCount, random);
            case DISC, SPIRAL -> throw new IllegalArgumentException(mode + " spawning is not available in 4D");
        }
    }

    private static void spawnFourBall(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, Random random) {
        float[] direction = new float[4];
        for (int i = 0; i < count; i++) {
            randomUnitVector4d(direction, random);
            float radius = bounds * 0.6f * (float) Math.pow(random.nextDouble(), 0.25);
            writePosition4d(positions, groups, direction[0] * radius, direction[1] * radius,
                    direction[2] * radius, direction[3] * radius, random.nextInt(groupCount));
            writeVelocity4d(velocities, random);
        }
    }

    private static void spawnThreeSphere(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, Random random) {
        float[] direction = new float[4];
        float radius = bounds * 0.6f;
        for (int i = 0; i < count; i++) {
            randomUnitVector4d(direction, random);
            writePosition4d(positions, groups, direction[0] * radius, direction[1] * radius,
                    direction[2] * radius, direction[3] * radius, random.nextInt(groupCount));
            writeVelocity4d(velocities, random);
        }
    }

    private static void spawnGrid4d(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, Random random) {
        int gridSize = Math.max(1, (int) Math.ceil(Math.pow(count, 0.25)));
        float gridSpacing = bounds * 2.0f / gridSize;
        long gridSquared = (long) gridSize * gridSize;
        long gridCubed = gridSquared * gridSize;
        for (int i = 0; i < count; i++) {
            int ix = i % gridSize;
            int iy = (i / gridSize) % gridSize;
            int iz = (int) ((i / gridSquared) % gridSize);
            int iw = (int) (i / gridCubed);
            writePosition4d(positions, groups,
                    gridCoordinate(ix, gridSpacing, bounds), gridCoordinate(iy, gridSpacing, bounds),
                    gridCoordinate(iz, gridSpacing, bounds), gridCoordinate(iw, gridSpacing, bounds),
                    random.nextInt(groupCount));
            writeVelocity4d(velocities, random);
        }
    }

    private static void spawnClusters4d(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, Random random) {
        float[][] centers = new float[groupCount][4];
        float[] direction = new float[4];
        for (int group = 0; group < groupCount; group++) {
            randomUnitVector4d(direction, random);
            for (int axis = 0; axis < 4; axis++) {
                centers[group][axis] = direction[axis] * bounds * 0.5f;
            }
        }
        for (int i = 0; i < count; i++) {
            int group = random.nextInt(groupCount);
            writePosition4d(positions, groups,
                    centers[group][0] + jitter(bounds, random), centers[group][1] + jitter(bounds, random),
                    centers[group][2] + jitter(bounds, random), centers[group][3] + jitter(bounds, random), group);
            writeVelocity4d(velocities, random);
        }
    }

    private static void randomUnitVector4d(float[] result, Random random) {
        double squaredLength;
        do {
            squaredLength = 0.0;
            for (int axis = 0; axis < 4; axis++) {
                float component = (float) random.nextGaussian();
                result[axis] = component;
                squaredLength += component * component;
            }
        } while (squaredLength <= 1.0e-20);
        float inverseLength = (float) (1.0 / Math.sqrt(squaredLength));
        for (int axis = 0; axis < 4; axis++) {
            result[axis] *= inverseLength;
        }
    }

    private static float randomCoordinate(float bounds, Random random) {
        return (random.nextFloat() - 0.5f) * 2.0f * bounds;
    }

    private static float jitter(float bounds, Random random) {
        return (random.nextFloat() - 0.5f) * bounds * 0.2f;
    }

    private static float gridCoordinate(int index, float spacing, float bounds) {
        return -bounds + index * spacing + spacing * 0.5f;
    }

    private static void writePosition4d(FloatBuffer positions, IntBuffer groups, float x, float y, float z, float w,
            int group) {
        positions.put(x).put(y).put(z).put(w);
        groups.put(group);
    }

    private static void writeVelocity4d(FloatBuffer velocities, Random random) {
        velocities.put(randomCoordinate(0.1f, random)).put(randomCoordinate(0.1f, random))
                .put(randomCoordinate(0.1f, random)).put(randomCoordinate(0.1f, random));
    }

    private static void writePosition(FloatBuffer positions, IntBuffer groups, float x, float y, float z, int group) {
        positions.put(x).put(y).put(z).put(0.0f);
        groups.put(group);
    }

    private static void writeVelocity(FloatBuffer velocities, Random random) {
        velocities
                .put((random.nextFloat() - 0.5f) * 0.2f)
                .put((random.nextFloat() - 0.5f) * 0.2f)
                .put((random.nextFloat() - 0.5f) * 0.2f)
                .put(0.0f);
    }

    private static void spawnPoint(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            int groupCount, Random random) {
        for (int i = 0; i < count; i++) {
            writePosition(positions, groups, 0.0f, 0.0f, 0.0f, random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }

    private static void spawnShell(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, Random random) {
        float r = bounds * 0.6f;
        for (int i = 0; i < count; i++) {
            float theta = random.nextFloat() * (float) Math.PI * 2.0f;
            float phi = (float) Math.acos(2.0f * random.nextFloat() - 1.0f);
            float x = r * (float) Math.sin(phi) * (float) Math.cos(theta);
            float y = r * (float) Math.cos(phi);
            float z = r * (float) Math.sin(phi) * (float) Math.sin(theta);

            writePosition(positions, groups, x, y, z, random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }

    private static void spawnSpherical(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, Random random) {
        for (int i = 0; i < count; i++) {
            float r = random.nextFloat() * bounds * 0.6f;
            float theta = random.nextFloat() * (float) Math.PI * 2.0f;
            float phi = (float) Math.acos(2.0f * random.nextFloat() - 1.0f);
            float x = r * (float) Math.sin(phi) * (float) Math.cos(theta);
            float y = r * (float) Math.cos(phi);
            float z = r * (float) Math.sin(phi) * (float) Math.sin(theta);

            writePosition(positions, groups, x, y, z, random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }

    private static void spawnDisc(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, Random random) {
        for (int i = 0; i < count; i++) {
            float r = (float) Math.sqrt(random.nextFloat()) * bounds * 0.8f;
            float theta = random.nextFloat() * (float) Math.PI * 2.0f;
            float x = r * (float) Math.cos(theta);
            float y = (random.nextFloat() - 0.5f) * bounds * 0.05f;
            float z = r * (float) Math.sin(theta);

            writePosition(positions, groups, x, y, z, random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }

    private static void spawnSpiral(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, Random random) {
        int arms = 3;
        for (int i = 0; i < count; i++) {
            int group = random.nextInt(groupCount);
            float t = (float) i / count;
            float r = bounds * 0.8f * t;
            float theta = t * (float) Math.PI * 8.0f + (group % arms) * ((float) Math.PI * 2.0f / arms);
            float x = r * (float) Math.cos(theta);
            float y = (random.nextFloat() - 0.5f) * bounds * 0.2f * (1.0f - t);
            float z = r * (float) Math.sin(theta);

            writePosition(positions, groups, x, y, z, group);
            writeVelocity(velocities, random);
        }
    }

    private static void spawnClusters(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, Random random) {
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

            writePosition(positions, groups, x, y, z, group);
            writeVelocity(velocities, random);
        }
    }

    private static void spawnGrid(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, Random random) {
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

            writePosition(positions, groups, x, y, z, random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }

    private static void spawnRandom(FloatBuffer positions, FloatBuffer velocities, IntBuffer groups, int count,
            float bounds, int groupCount, Random random) {
        for (int i = 0; i < count; i++) {
            float x = (random.nextFloat() - 0.5f) * 2.0f * bounds;
            float y = (random.nextFloat() - 0.5f) * 2.0f * bounds;
            float z = (random.nextFloat() - 0.5f) * 2.0f * bounds;

            writePosition(positions, groups, x, y, z, random.nextInt(groupCount));
            writeVelocity(velocities, random);
        }
    }
}
