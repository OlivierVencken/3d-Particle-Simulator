package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.particle.sim.ui.theme.UIColors;
import org.junit.jupiter.api.Test;

class UIAttractionMatrixTest {
    private static final float EPSILON = 0.0001f;

    @Test
    void sizingAccountsForHeadersCellsAndGaps() {
        int groups = 16;
        float width = 808.0f;
        float gap = 8.0f;
        float size = UIAttractionMatrix.fittedCellSize(width, groups, gap);
        assertEquals(width, (groups + 1) * size + groups * gap, EPSILON);
        assertEquals(0.0f, UIAttractionMatrix.fittedCellSize(width, 0, gap), EPSILON);
    }

    @Test
    void colorRulePreservesAttractionNeutralAndRepulsionMeaning() {
        assertEquals(UIColors.INTERACTION_ATTRACTION, UIAttractionMatrix.attractionColor(1.0f));
        assertEquals(UIColors.INTERACTION_NEUTRAL_SURFACE, UIAttractionMatrix.attractionColor(0.0f));
        assertEquals(UIColors.INTERACTION_REPULSION, UIAttractionMatrix.attractionColor(-1.0f));
    }

    @Test
    void actionButtonsWrapBeforeCrossingContentEdge() {
        assertEquals(true, UIAttractionMatrix.fitsOnLine(100.0f, 60.0f, 8.0f, 168.0f));
        assertEquals(false, UIAttractionMatrix.fitsOnLine(100.0f, 61.0f, 8.0f, 168.0f));
    }

    @Test
    void cellsRemainUsableWhenTheMatrixNeedsScrolling() {
        assertEquals(32.0f,
                UIAttractionMatrix.resolvedCellSize(360.0f, 16, 4.0f, 32.0f, 44.0f), EPSILON);
        assertEquals(44.0f,
                UIAttractionMatrix.resolvedCellSize(800.0f, 6, 4.0f, 32.0f, 44.0f), EPSILON);
        assertEquals(0.0f,
                UIAttractionMatrix.resolvedCellSize(0.0f, 6, 4.0f, 32.0f, 44.0f), EPSILON);
    }
}
