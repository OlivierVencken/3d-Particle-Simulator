package com.particle.sim.ui;

import com.particle.sim.ui.theme.UIDesignTokens;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UILayoutCalculatorTest {
    @Test
    void scalesChromeAndResponsiveBreakpointsWithDisplayScale() {
        UIDesignTokens tokens = UIDesignTokens.atScale(2.0f);

        UILayout layout = UILayoutCalculator.calculate(1600.0f, 1080.0f, true, tokens);

        assertEquals(UILayout.Mode.COMPACT, layout.mode());
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
    void appliesResponsivePersistentAndOverlaySidebarPolicies(float width, float height, UILayout.Mode mode,
            float sidebarWidth, float simulationX, float simulationWidth) {
        UILayout layout = UILayoutCalculator.calculate(width, height, true);

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
        UILayout layout = UILayoutCalculator.calculate(
                1366.0f, 768.0f, true, false, UIDesignTokens.unscaled());

        assertFalse(layout.commandBar().visible());
        assertFalse(layout.sidebar().visible());
        assertEquals(new UILayout.Panel(0.0f, 0.0f, 1366.0f, 768.0f), layout.simulation());
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
    void changesModeAtDocumentedBoundaries(float width, UILayout.Mode expectedMode) {
        assertEquals(expectedMode, UILayoutCalculator.calculate(width, 800.0f, true).mode());
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

    @Test
    void animatedPersistentSidebarKeepsItsVisibleEdgeAlignedWithTheSimulation() {
        UILayout layout = UILayoutCalculator.calculate(
                1366.0f, 768.0f, 0.5f, true, UIDesignTokens.unscaled());

        assertEquals(-189.0f, layout.sidebar().x());
        assertEquals(378.0f, layout.sidebar().width());
        assertEquals(189.0f, layout.sidebar().right());
        assertEquals(189.0f, layout.simulation().x());
        assertEquals(1177.0f, layout.simulation().width());
    }

    @Test
    void animatedOverlaySidebarDoesNotResizeTheSimulation() {
        UILayout layout = UILayoutCalculator.calculate(
                900.0f, 768.0f, 0.5f, true, UIDesignTokens.unscaled());

        assertEquals(UILayout.Mode.COMPACT, layout.mode());
        assertEquals(-189.0f, layout.sidebar().x());
        assertEquals(189.0f, layout.sidebar().right());
        assertEquals(0.0f, layout.simulation().x());
        assertEquals(900.0f, layout.simulation().width());
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
