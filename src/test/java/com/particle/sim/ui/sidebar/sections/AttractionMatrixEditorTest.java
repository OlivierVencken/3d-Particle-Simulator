package com.particle.sim.ui.sidebar.sections;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AttractionMatrixEditorTest {
    private static final float EPSILON = 0.0001f;

    @Test
    void cellSizeFillsAvailableWidthForEverySupportedGroupCount() {
        float availableWidth = 404.0f;

        for (int groupCount = 1; groupCount <= 16; groupCount++) {
            float cellSize = AttractionMatrixEditor.fittedCellSize(availableWidth, groupCount);
            float matrixWidth = (groupCount + 1) * cellSize + groupCount * 3.2f;

            assertEquals(availableWidth, matrixWidth, EPSILON,
                    "Matrix width for " + groupCount + " groups");
        }
    }

    @Test
    void cellSizeIsSafeForEmptyOrUnavailableLayouts() {
        assertEquals(0.0f, AttractionMatrixEditor.fittedCellSize(404.0f, 0), EPSILON);
        assertEquals(0.0f, AttractionMatrixEditor.fittedCellSize(0.0f, 16), EPSILON);
    }

    @Test
    void cellSizeAccountsForScaledMatrixSpacing() {
        float availableWidth = 808.0f;
        float scaledGap = 8.0f;
        int groupCount = 16;

        float cellSize = AttractionMatrixEditor.fittedCellSize(availableWidth, groupCount, scaledGap);

        assertEquals(availableWidth,
                (groupCount + 1) * cellSize + groupCount * scaledGap,
                EPSILON);
    }
}
