package com.particle.sim.particles;

import java.util.Random;

public class AttractionMatrix {
    private final float[] matrix;
    private final int groupCount;
    private final Random random = new Random();

    public AttractionMatrix(int groupCount, int maxGroups) {
        this.groupCount = groupCount;
        this.matrix = new float[maxGroups * maxGroups];
        randomize();
    }

    public float attraction(int groupA, int groupB) {
        return matrix[groupA * groupCount + groupB];
    }

    public void attraction(int groupA, int groupB, float value) {
        matrix[groupA * groupCount + groupB] = clamp(value);
    }

    public void adjustAttraction(int groupA, int groupB, float delta) {
        attraction(groupA, groupB, attraction(groupA, groupB) + delta);
    }

    public void randomize() {
        for (int i = 0; i < groupCount; i++) {
            for (int j = 0; j < groupCount; j++) {
                float value = -0.6f + random.nextFloat() * 1.4f;
                if (i == j) {
                    value += 0.25f;
                }
                matrix[i * groupCount + j] = value;
            }
        }
    }

    public void zero() {
        for (int i = 0; i < groupCount; i++) {
            for (int j = 0; j < groupCount; j++) {
                attraction(i, j, 0.0f);
            }
        }
    }

    public void symmetrize() {
        for (int i = 0; i < groupCount; i++) {
            for (int j = i + 1; j < groupCount; j++) {
                float average = (attraction(i, j) + attraction(j, i)) * 0.5f;
                attraction(i, j, average);
                attraction(j, i, average);
            }
        }
    }

    public void invert() {
        for (int i = 0; i < groupCount; i++) {
            for (int j = 0; j < groupCount; j++) {
                attraction(i, j, -attraction(i, j));
            }
        }
    }

    public float[] getFlatArray() {
        return matrix;
    }

    private static float clamp(float value) {
        return Math.max(-1.0f, Math.min(1.0f, value));
    }
}
