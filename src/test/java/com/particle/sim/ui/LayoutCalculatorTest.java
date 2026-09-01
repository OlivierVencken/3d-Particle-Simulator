package com.particle.sim.ui;

import com.particle.sim.ui.theme.DesignTokens;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutCalculatorTest {
    @Test
    void scalesChromeAndResponsiveBreakpointsWithDisplayScale() {
        DesignTokens tokens = DesignTokens.atScale(2.0f);

        Layout layout = LayoutCalculator.calculate(1600.0f, 1080.0f, true, tokens);

        assertEquals(Layout.Mode.COMPACT, layout.mode());
        assertEquals(72.0f, layout.commandBar().height());
        assertEquals(756.0f, layout.sidebar().width());
        assertEquals(0.0f, layout.simulation().x());
        assertEquals(1600.0f, layout.simulation().width());
    }

    @ParameterizedTest
    @CsvSource({
            "2560, 1440, WIDE, 378, 378, 2182",
            "1920, 1080, WIDE, 378, 378, 1542",
            "1366, 768, WIDE, 378, 378, 988",
            "1024, 768, MEDIUM, 324, 324, 700",
            "640, 640, FOCUS, 640, 0, 640",
            "320, 480, FOCUS, 320, 0, 320"
    })
    void appliesResponsivePersistentAndOverlaySidebarPolicies(float width, float height, Layout.Mode mode,
            float sidebarWidth, float simulationX, float simulationWidth) {
        Layout layout = LayoutCalculator.calculate(width, height, true);

        assertEquals(mode, layout.mode());
        assertEquals(sidebarWidth, layout.sidebar().width());
        assertEquals(0.0f, layout.sidebar().x());
        assertEquals(layout.commandBar().bottom(), layout.sidebar().y());
        assertEquals(simulationX, layout.simulation().x());
        assertEquals(simulationWidth, layout.simulation().width());
        assertEquals(layout.sidebar().y(), layout.simulation().y());
        assertEquals(height, layout.sidebar().bottom());
        assertEquals(height, layout.simulation().bottom());
        assertPanelsStayInBounds(layout, width, height);
    }

    @Test
    void hiddenUiReturnsTheCompleteDisplayToTheSimulation() {
        Layout layout = LayoutCalculator.calculate(
                1366.0f, 768.0f, true, false, DesignTokens.unscaled());

        assertFalse(layout.commandBar().visible());
        assertFalse(layout.sidebar().visible());
        assertEquals(new Layout.Panel(0.0f, 0.0f, 1366.0f, 768.0f), layout.simulation());
    }

    @ParameterizedTest
    @CsvSource({
            "647, FOCUS",
            "648, COMPACT",
            "989, COMPACT",
            "990, MEDIUM",
            "1295, MEDIUM",
            "1296, WIDE"
    })
    void changesModeAtDocumentedBoundaries(float width, Layout.Mode expectedMode) {
        assertEquals(expectedMode, LayoutCalculator.calculate(width, 800.0f, true).mode());
    }

    @ParameterizedTest
    @CsvSource({
            "2560, 1440",
            "1024, 768",
            "640, 640",
            "0, 0"
    })
    void minimizedSidebarReturnsAllContentSpaceToTheSimulation(float width, float height) {
        Layout layout = LayoutCalculator.calculate(width, height, false);

        assertFalse(layout.sidebar().visible());
        assertEquals(0.0f, layout.simulation().x());
        assertEquals(width, layout.simulation().width());
        assertEquals(layout.commandBar().bottom(), layout.simulation().y());
        assertEquals(height, layout.simulation().bottom());
        assertPanelsStayInBounds(layout, width, height);
    }

    @Test
    void animatedPersistentSidebarKeepsItsVisibleEdgeAlignedWithTheSimulation() {
        Layout layout = LayoutCalculator.calculate(
                1366.0f, 768.0f, 0.5f, true, DesignTokens.unscaled());

        assertEquals(-189.0f, layout.sidebar().x());
        assertEquals(378.0f, layout.sidebar().width());
        assertEquals(189.0f, layout.sidebar().right());
        assertEquals(189.0f, layout.simulation().x());
        assertEquals(1177.0f, layout.simulation().width());
    }

    @Test
    void animatedOverlaySidebarDoesNotResizeTheSimulation() {
        Layout layout = LayoutCalculator.calculate(
                900.0f, 768.0f, 0.5f, true, DesignTokens.unscaled());

        assertEquals(Layout.Mode.COMPACT, layout.mode());
        assertEquals(-189.0f, layout.sidebar().x());
        assertEquals(189.0f, layout.sidebar().right());
        assertEquals(0.0f, layout.simulation().x());
        assertEquals(900.0f, layout.simulation().width());
    }

    private void assertPanelsStayInBounds(Layout layout, float width, float height) {
        for (Layout.Panel panel : new Layout.Panel[] {
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
