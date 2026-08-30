package com.particle.sim.particles;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FourDPhysicsBroadPhaseTest {
    @Test
    void xyzGridContainsEveryRandomizedValidFourDimensionalNeighbor() {
        Random random = new Random(0x4D_BA5EL);
        for (DistanceMetric metric : DistanceMetric.values()) {
            for (boolean toroidal : new boolean[] { false, true }) {
                for (int sample = 0; sample < 5_000; sample++) {
                    float bounds = 2.0f + random.nextFloat() * 8.0f;
                    float range = 0.2f + random.nextFloat() * 2.8f;
                    int gridSize = SpatialGridSizing.gridSize(bounds, range);
                    float[] from = randomPosition(bounds, random);
                    float[] delta = randomNeighborDelta(metric, range, random);
                    float[] to = new float[4];
                    boolean inside = true;
                    for (int axis = 0; axis < 4; axis++) {
                        to[axis] = from[axis] + delta[axis];
                        if (toroidal) {
                            to[axis] = wrap(to[axis], bounds);
                        } else {
                            inside &= to[axis] >= -bounds && to[axis] <= bounds;
                        }
                    }
                    if (!inside) {
                        sample--;
                        continue;
                    }

                    for (int axis = 0; axis < 3; axis++) {
                        int fromCell = gridCoordinate(from[axis], bounds, gridSize);
                        int toCell = gridCoordinate(to[axis], bounds, gridSize);
                        int cellDelta = Math.abs(fromCell - toCell);
                        boolean adjacent = cellDelta <= 1 || (toroidal && cellDelta == gridSize - 1);
                        assertTrue(adjacent, () -> "Valid " + metric + " 4D neighbor escaped XYZ broad phase");
                    }
                }
            }
        }
    }

    private static float[] randomPosition(float bounds, Random random) {
        return new float[] {
                coordinate(bounds, random), coordinate(bounds, random),
                coordinate(bounds, random), coordinate(bounds, random)
        };
    }

    private static float[] randomNeighborDelta(DistanceMetric metric, float range, Random random) {
        float[] delta = new float[4];
        for (int axis = 0; axis < 4; axis++) {
            delta[axis] = coordinate(range, random);
        }
        float distance = switch (metric) {
            case EUCLIDEAN -> (float) Math.sqrt(dot(delta));
            case MANHATTAN -> Math.abs(delta[0]) + Math.abs(delta[1]) + Math.abs(delta[2]) + Math.abs(delta[3]);
            case CHEBYSHEV -> Math.max(Math.max(Math.abs(delta[0]), Math.abs(delta[1])),
                    Math.max(Math.abs(delta[2]), Math.abs(delta[3])));
        };
        float scale = distance == 0.0f ? 0.0f : range * 0.99f * random.nextFloat() / distance;
        for (int axis = 0; axis < 4; axis++) {
            delta[axis] *= scale;
        }
        return delta;
    }

    private static float dot(float[] vector) {
        float result = 0.0f;
        for (float component : vector) {
            result += component * component;
        }
        return result;
    }

    private static int gridCoordinate(float position, float bounds, int gridSize) {
        float inverseCellWidth = gridSize / (bounds * 2.0f);
        int coordinate = (int) Math.floor((position + bounds) * inverseCellWidth);
        return Math.max(0, Math.min(gridSize - 1, coordinate));
    }

    private static float coordinate(float extent, Random random) {
        return (random.nextFloat() - 0.5f) * 2.0f * extent;
    }

    private static float wrap(float value, float bounds) {
        float worldSize = bounds * 2.0f;
        float shifted = value + bounds;
        return shifted - (float) Math.floor(shifted / worldSize) * worldSize - bounds;
    }
}
