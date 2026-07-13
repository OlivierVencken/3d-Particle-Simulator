package com.particle.sim.ui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UILayoutCalculatorTest {
    @ParameterizedTest
    @CsvSource({
            "2560, 1440, WIDE, 420",
            "1920, 1080, WIDE, 420",
            "1366, 768, MEDIUM, 420",
            "1024, 768, COMPACT, 420",
            "640, 640, FOCUS, 420",
            "320, 480, FOCUS, 320"
    })
    void calculatesOneLeftSidebarAndNoBottomBar(float width, float height, UILayout.Mode mode,
            float sidebarWidth) {
        UILayout layout = UILayoutCalculator.calculate(width, height, true);

        assertEquals(mode, layout.mode());
        assertEquals(sidebarWidth, layout.sidebar().width());
        assertEquals(0.0f, layout.sidebar().x());
        assertEquals(layout.commandBar().bottom(), layout.sidebar().y());
        assertEquals(layout.sidebar().right(), layout.simulation().x());
        assertEquals(layout.sidebar().y(), layout.simulation().y());
        assertEquals(height, layout.sidebar().bottom());
        assertEquals(height, layout.simulation().bottom());
        assertPanelsStayInBounds(layout, width, height);
    }

    @ParameterizedTest
    @CsvSource({
            "2560, 1440",
            "1024, 768",
            "640, 640",
            "0, 0"
    })
    void minimizedSidebarReturnsAllContentSpaceToTheSimulation(float width, float height) {
        UILayout layout = UILayoutCalculator.calculate(width, height, false);

        assertFalse(layout.sidebar().visible());
        assertEquals(0.0f, layout.simulation().x());
        assertEquals(width, layout.simulation().width());
        assertEquals(layout.commandBar().bottom(), layout.simulation().y());
        assertEquals(height, layout.simulation().bottom());
        assertPanelsStayInBounds(layout, width, height);
    }

    private void assertPanelsStayInBounds(UILayout layout, float width, float height) {
        for (UILayout.Panel panel : new UILayout.Panel[] {
                layout.commandBar(), layout.sidebar(), layout.simulation() }) {
            assertTrue(panel.x() >= 0.0f);
            assertTrue(panel.y() >= 0.0f);
            assertTrue(panel.right() <= width);
            assertTrue(panel.bottom() <= height);
        }
        assertEquals(width, layout.commandBar().width());
        assertEquals(0.0f, layout.commandBar().y());
    }
}
