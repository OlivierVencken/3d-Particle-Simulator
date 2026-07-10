package com.particle.sim.ui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceLayoutCalculatorTest {
    @ParameterizedTest
    @CsvSource({
            "2560, 1440, WIDE, 176, 368",
            "1920, 1080, WIDE, 176, 368",
            "1366, 768, MEDIUM, 152, 336",
            "1024, 768, COMPACT, 0, 320",
            "640, 640, FOCUS, 0, 640"
    })
    void calculatesResponsivePanels(float width, float height, WorkspaceLayout.Mode mode,
            float navigationWidth, float inspectorWidth) {
        WorkspaceLayout layout = WorkspaceLayoutCalculator.calculate(width, height, UiSection.SIMULATION, true);

        assertEquals(mode, layout.mode());
        assertEquals(navigationWidth, layout.navigation().width());
        assertEquals(inspectorWidth, layout.inspector().width());
        assertPanelsStayInBounds(layout, width, height);
    }

    @ParameterizedTest
    @CsvSource({
            "2560, 1440, 600",
            "1920, 1080, 600",
            "1366, 768, 500",
            "1024, 768, 500",
            "700, 600, 700"
    })
    void interactionsUseExpandedInspectorAndPreserveSimulation(float width, float height, float inspectorWidth) {
        WorkspaceLayout layout = WorkspaceLayoutCalculator.calculate(width, height, UiSection.INTERACTIONS, true);

        assertEquals(inspectorWidth, layout.inspector().width());
        if (layout.mode() != WorkspaceLayout.Mode.FOCUS) {
            assertTrue(layout.simulation().width() >= WorkspaceLayoutCalculator.MIN_SIMULATION_STRIP);
        }
        assertPanelsStayInBounds(layout, width, height);
    }

    private void assertPanelsStayInBounds(WorkspaceLayout layout, float width, float height) {
        for (WorkspaceLayout.Panel panel : new WorkspaceLayout.Panel[] {
                layout.commandBar(), layout.navigation(), layout.simulation(), layout.inspector(), layout.statusBar() }) {
            assertTrue(panel.x() >= 0.0f);
            assertTrue(panel.y() >= 0.0f);
            assertTrue(panel.right() <= width);
            assertTrue(panel.bottom() <= height);
        }
        assertEquals(layout.commandBar().bottom(), layout.simulation().y());
        assertEquals(layout.statusBar().y(), layout.simulation().bottom());
    }
}
