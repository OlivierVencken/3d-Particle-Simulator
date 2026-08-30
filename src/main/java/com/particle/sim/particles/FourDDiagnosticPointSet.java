package com.particle.sim.particles;

import java.util.Random;

/** Fixed four-dimensional geometry used only by the Phase 2 renderer harness. */
public final class FourDDiagnosticPointSet {
    private static final long HYPERSPHERE_SEED = 0x4D51_4D55_4CL;
    private final float[] positions;
    private final float[] velocities;
    private final int[] groups;

    private FourDDiagnosticPointSet(float[] positions, int[] groups) {
        this.positions = positions;
        this.velocities = new float[positions.length];
        this.groups = groups;
    }

    public static FourDDiagnosticPointSet tesseract(float extent) {
        requirePositiveFinite(extent, "Tesseract extent");
        float[] positions = new float[16 * 4];
        int[] groups = new int[16];
        for (int vertex = 0; vertex < 16; vertex++) {
            int base = vertex * 4;
            for (int axis = 0; axis < 4; axis++) {
                positions[base + axis] = (vertex & (1 << axis)) == 0 ? -extent : extent;
            }
            groups[vertex] = vertex;
        }
        return new FourDDiagnosticPointSet(positions, groups);
    }

    public static FourDDiagnosticPointSet hypersphere(float radius, int sampleCount) {
        requirePositiveFinite(radius, "Hypersphere radius");
        if (sampleCount <= 0) {
            throw new IllegalArgumentException("Hypersphere sample count must be positive");
        }

        float[] positions = new float[Math.multiplyExact(sampleCount, 4)];
        int[] groups = new int[sampleCount];
        Random random = new Random(HYPERSPHERE_SEED);
        for (int sample = 0; sample < sampleCount; sample++) {
            int base = sample * 4;
            double lengthSquared;
            do {
                lengthSquared = 0.0;
                for (int axis = 0; axis < 4; axis++) {
                    float value = (float) random.nextGaussian();
                    positions[base + axis] = value;
                    lengthSquared += value * value;
                }
            } while (lengthSquared < 1.0e-12);

            float scale = radius / (float) Math.sqrt(lengthSquared);
            for (int axis = 0; axis < 4; axis++) {
                positions[base + axis] *= scale;
            }
            groups[sample] = sample % 16;
        }
        return new FourDDiagnosticPointSet(positions, groups);
    }

    public static FourDDiagnosticPointSet standard() {
        return combine(tesseract(2.0f), hypersphere(2.75f, 256));
    }

    public static FourDDiagnosticPointSet of(float[] positions) {
        if (positions == null || positions.length == 0 || positions.length % 4 != 0) {
            throw new IllegalArgumentException("Diagnostic positions must contain one or more vec4 values");
        }
        float[] copiedPositions = positions.clone();
        int[] groups = new int[copiedPositions.length / 4];
        for (int particle = 0; particle < groups.length; particle++) {
            groups[particle] = particle % 16;
            for (int axis = 0; axis < 4; axis++) {
                if (!Float.isFinite(copiedPositions[particle * 4 + axis])) {
                    throw new IllegalArgumentException("Diagnostic positions must be finite");
                }
            }
        }
        return new FourDDiagnosticPointSet(copiedPositions, groups);
    }

    public static FourDDiagnosticPointSet combine(FourDDiagnosticPointSet... pointSets) {
        if (pointSets == null || pointSets.length == 0) {
            throw new IllegalArgumentException("At least one diagnostic point set is required");
        }
        int particleCount = 0;
        for (FourDDiagnosticPointSet pointSet : pointSets) {
            if (pointSet == null) {
                throw new IllegalArgumentException("Diagnostic point sets cannot contain null");
            }
            particleCount = Math.addExact(particleCount, pointSet.particleCount());
        }

        float[] positions = new float[Math.multiplyExact(particleCount, 4)];
        int[] groups = new int[particleCount];
        int particleOffset = 0;
        for (FourDDiagnosticPointSet pointSet : pointSets) {
            System.arraycopy(pointSet.positions, 0, positions, particleOffset * 4, pointSet.positions.length);
            System.arraycopy(pointSet.groups, 0, groups, particleOffset, pointSet.groups.length);
            particleOffset += pointSet.particleCount();
        }
        return new FourDDiagnosticPointSet(positions, groups);
    }

    public int particleCount() {
        return groups.length;
    }

    public float[] positions() {
        return positions.clone();
    }

    public float[] velocities() {
        return velocities.clone();
    }

    public int[] groups() {
        return groups.clone();
    }

    private static void requirePositiveFinite(float value, String label) {
        if (!Float.isFinite(value) || value <= 0.0f) {
            throw new IllegalArgumentException(label + " must be finite and positive");
        }
    }
}
