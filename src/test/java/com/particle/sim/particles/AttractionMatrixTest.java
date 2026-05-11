package com.particle.sim.particles;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        float[] flat = matrix.getFlatArray();
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

        matrix.setActiveValues(new float[] { -2.0f, -0.25f, 0.5f, 2.0f });

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

        matrix.setActiveValues(new float[] { 0.2f });

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
                assertTrue(value >= min && value < max,
                        "cell " + row + "," + column + " was outside randomized range: " + value);
            }
        }
    }

    @Test
    void flatArrayExposesBackingStorageForGpuUpload() {
        AttractionMatrix matrix = new AttractionMatrix(6, 16);

        assertSame(matrix.getFlatArray(), matrix.getFlatArray());
        assertEquals(16 * 16, matrix.getFlatArray().length);
    }
}
