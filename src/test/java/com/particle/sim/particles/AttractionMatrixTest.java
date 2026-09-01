package com.particle.sim.particles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AttractionMatrixTest {
    private static final float EPSILON = 0.0001f;

    @Test
    void setterAndAdjustClampValuesToSimulationRange() {
        AttractionMatrix matrix = new AttractionMatrix(3, 16);

        matrix.attraction(0, 1, 2.0f);
        matrix.adjustAttraction(1, 0, -5.0f);

        assertEquals(1.0f, matrix.attraction(0, 1), EPSILON);
        assertEquals(-1.0f, matrix.attraction(1, 0), EPSILON);
    }

    @Test
    void zeroClearsOnlyActiveGroupCells() {
        AttractionMatrix matrix = new AttractionMatrix(2, 4);
        float[] flat = matrix.values();
        flat[15] = 0.75f;

        matrix.zero();

        assertEquals(0.0f, matrix.attraction(0, 0), EPSILON);
        assertEquals(0.0f, matrix.attraction(0, 1), EPSILON);
        assertEquals(0.0f, matrix.attraction(1, 0), EPSILON);
        assertEquals(0.0f, matrix.attraction(1, 1), EPSILON);
        assertEquals(0.75f, flat[15], EPSILON);
    }

    @Test
    void symmetrizeAveragesOpposingPairsAndPreservesDiagonal() {
        AttractionMatrix matrix = new AttractionMatrix(3, 16);
        matrix.zero();
        matrix.attraction(0, 0, 0.8f);
        matrix.attraction(0, 2, 0.6f);
        matrix.attraction(2, 0, -0.2f);

        matrix.symmetrize();

        assertEquals(0.8f, matrix.attraction(0, 0), EPSILON);
        assertEquals(0.2f, matrix.attraction(0, 2), EPSILON);
        assertEquals(0.2f, matrix.attraction(2, 0), EPSILON);
    }

    @Test
    void invertFlipsActiveAttractions() {
        AttractionMatrix matrix = new AttractionMatrix(2, 16);
        matrix.zero();
        matrix.attraction(0, 0, 0.4f);
        matrix.attraction(1, 0, -0.7f);

        matrix.invert();

        assertEquals(-0.4f, matrix.attraction(0, 0), EPSILON);
        assertEquals(0.7f, matrix.attraction(1, 0), EPSILON);
    }

    @Test
    void setActiveValuesCopiesAndClampsActiveCells() {
        AttractionMatrix matrix = new AttractionMatrix(2, 16);
        matrix.zero();

        matrix.activeValues(new float[] {-2.0f, -0.25f, 0.5f, 2.0f});

        assertEquals(-1.0f, matrix.attraction(0, 0), EPSILON);
        assertEquals(-0.25f, matrix.attraction(0, 1), EPSILON);
        assertEquals(0.5f, matrix.attraction(1, 0), EPSILON);
        assertEquals(1.0f, matrix.attraction(1, 1), EPSILON);
    }

    @Test
    void setActiveValuesKeepsMissingCellsUnchanged() {
        AttractionMatrix matrix = new AttractionMatrix(2, 16);
        matrix.zero();
        matrix.attraction(1, 1, 0.4f);

        matrix.activeValues(new float[] {0.2f});

        assertEquals(0.2f, matrix.attraction(0, 0), EPSILON);
        assertEquals(0.0f, matrix.attraction(0, 1), EPSILON);
        assertEquals(0.0f, matrix.attraction(1, 0), EPSILON);
        assertEquals(0.4f, matrix.attraction(1, 1), EPSILON);
    }

    @Test
    void randomizePopulatesActiveCellsWithinExpectedRange() {
        AttractionMatrix matrix = new AttractionMatrix(6, 16);

        matrix.randomize();

        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 6; column++) {
                float value = matrix.attraction(row, column);
                float min = row == column ? -0.35f : -0.6f;
                float max = row == column ? 1.05f : 0.8f;
                assertTrue(
                        value >= min && value < max,
                        "cell " + row + "," + column + " was outside randomized range: " + value);
            }
        }
    }

    @Test
    void flatArrayExposesBackingStorageForGpuUpload() {
        AttractionMatrix matrix = new AttractionMatrix(6, 16);

        assertSame(matrix.values(), matrix.values());
        assertEquals(16 * 16, matrix.values().length);
    }

    @Test
    void changingGroupCountPreservesExistingMaxStrideCells() {
        AttractionMatrix matrix = new AttractionMatrix(6, 16);
        matrix.zero();
        matrix.attraction(5, 5, 0.7f);

        matrix.groupCount(8);

        assertEquals(0.7f, matrix.attraction(5, 5), EPSILON);
    }

    @Test
    void structuredGeneratorsProduceTheirDefiningRelationships() {
        AttractionMatrix matrix = new AttractionMatrix(6, 16);

        matrix.generate(AttractionPattern.STABLE, 0.0f);
        assertEquals(0.55f, matrix.attraction(2, 2), EPSILON);
        assertEquals(-0.04f, matrix.attraction(2, 4), EPSILON);

        matrix.generate(AttractionPattern.PREDATOR_PREY, 0.0f);
        assertEquals(0.8f, matrix.attraction(0, 1), EPSILON);
        assertEquals(-0.7f, matrix.attraction(1, 0), EPSILON);
        assertEquals(0.8f, matrix.attraction(5, 0), EPSILON);

        matrix.generate(AttractionPattern.ROCK_PAPER_SCISSORS, 0.0f);
        assertEquals(0.75f, matrix.attraction(0, 1), EPSILON);
        assertEquals(-0.65f, matrix.attraction(1, 0), EPSILON);
        assertEquals(0.35f, matrix.attraction(0, 3), EPSILON);
    }

    @Test
    void symmetricAndMutualismGeneratorsMirrorPairs() {
        AttractionMatrix matrix = new AttractionMatrix(5, 16);
        matrix.randomSeed(42L);

        matrix.generate(AttractionPattern.SYMMETRIC, 0.2f);
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                assertEquals(
                        matrix.attraction(row, column), matrix.attraction(column, row), EPSILON);
            }
        }

        matrix.generate(AttractionPattern.MUTUALISM, 0.1f);
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                assertTrue(matrix.attraction(row, column) > 0.0f);
                assertEquals(
                        matrix.attraction(row, column), matrix.attraction(column, row), EPSILON);
            }
        }
    }

    @Test
    void undoAndRedoRestoreValues() {
        AttractionMatrix matrix = new AttractionMatrix(2, 4);
        matrix.zero();
        matrix.attraction(0, 1, 0.4f);
        matrix.invert();

        matrix.undo();
        assertEquals(0.4f, matrix.attraction(0, 1), EPSILON);
        matrix.redo();
        assertEquals(-0.4f, matrix.attraction(0, 1), EPSILON);
    }

    @Test
    void normalizeScalesStrengthAndAnimatedMutationMovesGradually() {
        AttractionMatrix matrix = new AttractionMatrix(2, 4);
        matrix.zero();
        matrix.attraction(0, 0, 0.2f);
        matrix.attraction(0, 1, -0.5f);

        matrix.normalize();
        assertEquals(0.4f, matrix.attraction(0, 0), EPSILON);
        assertEquals(-1.0f, matrix.attraction(0, 1), EPSILON);

        matrix.randomSeed(7L);
        matrix.animatedMutation(true);
        float before = matrix.attraction(1, 1);
        matrix.advanceAnimation(0.1f);
        assertNotEquals(before, matrix.attraction(1, 1));
    }
}
