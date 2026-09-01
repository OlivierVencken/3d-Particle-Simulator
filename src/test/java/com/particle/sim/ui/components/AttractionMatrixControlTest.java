package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.particle.sim.ui.theme.Colors;
import org.junit.jupiter.api.Test;

class AttractionMatrixControlTest {
    private static final float EPSILON = 0.0001f;

    @Test
    void sizingAccountsForHeadersCellsAndGaps() {
        int groups = 16;
        float width = 808.0f;
        float gap = 8.0f;
        float size = AttractionMatrixControl.fittedCellSize(width, groups, gap);
        assertEquals(width, (groups + 1) * size + groups * gap, EPSILON);
        assertEquals(0.0f, AttractionMatrixControl.fittedCellSize(width, 0, gap), EPSILON);
    }

    @Test
    void colorRulePreservesAttractionNeutralAndRepulsionMeaning() {
        assertEquals(Colors.INTERACTION_ATTRACTION, AttractionMatrixControl.attractionColor(1.0f));
        assertEquals(Colors.INTERACTION_NEUTRAL_SURFACE, AttractionMatrixControl.attractionColor(0.0f));
        assertEquals(Colors.INTERACTION_REPULSION, AttractionMatrixControl.attractionColor(-1.0f));
    }

    @Test
    void actionButtonsWrapBeforeCrossingContentEdge() {
        assertEquals(true, AttractionMatrixControl.fitsOnLine(100.0f, 60.0f, 8.0f, 168.0f));
        assertEquals(false, AttractionMatrixControl.fitsOnLine(100.0f, 61.0f, 8.0f, 168.0f));
    }

    @Test
    void cellsRemainUsableWhenTheMatrixNeedsScrolling() {
        assertEquals(32.0f,
                AttractionMatrixControl.resolvedCellSize(360.0f, 16, 4.0f, 32.0f, 44.0f), EPSILON);
        assertEquals(44.0f,
                AttractionMatrixControl.resolvedCellSize(800.0f, 6, 4.0f, 32.0f, 44.0f), EPSILON);
        assertEquals(0.0f,
                AttractionMatrixControl.resolvedCellSize(0.0f, 6, 4.0f, 32.0f, 44.0f), EPSILON);
    }
}
