package com.particle.sim.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DisplayMetricsTest {
    @ParameterizedTest
    @CsvSource({
        "1.0, 100, 460, 400, 300",
        "1.25, 125, 575, 500, 375",
        "1.5, 150, 690, 600, 450",
        "2.0, 200, 920, 800, 600"
    })
    void convertsTopLeftLogicalPanelToBottomLeftFramebufferPixels(
            float framebufferScale,
            int expectedX,
            int expectedY,
            int expectedWidth,
            int expectedHeight) {
        int framebufferWidth = Math.round(1000.0f * framebufferScale);
        int framebufferHeight = Math.round(800.0f * framebufferScale);
        DisplayMetrics metrics =
                new DisplayMetrics(
                        1000.0f,
                        800.0f,
                        framebufferScale,
                        framebufferScale,
                        framebufferScale,
                        framebufferWidth,
                        framebufferHeight);

        FramebufferViewport viewport =
                metrics.toFramebuffer(new Layout.Panel(100.0f, 40.0f, 400.0f, 300.0f));

        assertEquals(
                new FramebufferViewport(expectedX, expectedY, expectedWidth, expectedHeight),
                viewport);
    }

    @Test
    void roundsSharedPanelEdgesOnceWithoutGapsOrOverlap() {
        DisplayMetrics metrics = new DisplayMetrics(801.0f, 601.0f, 1.25f, 1.25f, 1.25f, 1001, 751);
        Layout.Panel left = new Layout.Panel(0.0f, 40.0f, 333.3f, 561.0f);
        Layout.Panel right = new Layout.Panel(333.3f, 40.0f, 467.7f, 561.0f);

        FramebufferViewport leftViewport = metrics.toFramebuffer(left);
        FramebufferViewport rightViewport = metrics.toFramebuffer(right);

        assertEquals(leftViewport.right(), rightViewport.x());
        assertEquals(1001, rightViewport.right());
        assertEquals(0, leftViewport.y());
        assertEquals(701, leftViewport.height());
    }

    @Test
    void clampsPanelsToTheActualFramebufferAndHandlesZeroSize() {
        DisplayMetrics metrics = new DisplayMetrics(0.0f, 0.0f, 1.0f, 1.5f, 1.5f, 0, 0);

        FramebufferViewport viewport =
                metrics.toFramebuffer(new Layout.Panel(-10.0f, -10.0f, 50.0f, 50.0f));

        assertEquals(new FramebufferViewport(0, 0, 0, 0), viewport);
        assertFalse(viewport.visible());
    }
}
