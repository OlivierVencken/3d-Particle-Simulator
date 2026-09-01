package com.particle.sim.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InputOwnershipTest {
    private static final Layout PERSISTENT_LAYOUT =
            new Layout(
                    Layout.Mode.WIDE,
                    new Layout.Panel(0.0f, 0.0f, 1200.0f, 40.0f),
                    new Layout.Panel(0.0f, 40.0f, 300.0f, 760.0f),
                    new Layout.Panel(300.0f, 40.0f, 900.0f, 760.0f));

    @Test
    void cameraCaptureStartsOnlyInsideSimulationViewport() {
        InputOwnership simulation = resolve(PERSISTENT_LAYOUT, 301.0f, 41.0f);
        InputOwnership sidebar = resolve(PERSISTENT_LAYOUT, 299.0f, 41.0f);
        InputOwnership commandBar = resolve(PERSISTENT_LAYOUT, 500.0f, 39.0f);

        assertTrue(simulation.canStartCameraCapture());
        assertFalse(sidebar.canStartCameraCapture());
        assertFalse(commandBar.canStartCameraCapture());
    }

    @Test
    void overlaySidebarOwnsPointerEvenThoughSimulationContinuesBehindIt() {
        Layout overlayLayout =
                new Layout(
                        Layout.Mode.COMPACT,
                        new Layout.Panel(0.0f, 0.0f, 1000.0f, 40.0f),
                        new Layout.Panel(0.0f, 40.0f, 300.0f, 760.0f),
                        new Layout.Panel(0.0f, 40.0f, 1000.0f, 760.0f));

        InputOwnership ownership = resolve(overlayLayout, 150.0f, 200.0f);

        assertTrue(ownership.pointerInSimulationViewport());
        assertTrue(ownership.pointerOwnedByUi());
        assertFalse(ownership.canStartCameraCapture());
    }

    @Test
    void popupsAndModalsBlockSimulationInput() {
        InputOwnership popup =
                InputOwnership.resolve(
                        PERSISTENT_LAYOUT, true, 500.0f, 500.0f, false, false, true, false);
        InputOwnership modal =
                InputOwnership.resolve(
                        PERSISTENT_LAYOUT, true, 500.0f, 500.0f, false, false, true, true);

        assertFalse(popup.canStartCameraCapture());
        assertFalse(popup.allowsSimulationKeyboard());
        assertFalse(modal.canStartCameraCapture());
        assertFalse(modal.allowsSimulationKeyboard());
    }

    @Test
    void hiddenUiReleasesItsStaleCaptureFlags() {
        Layout fullDisplay =
                new Layout(
                        Layout.Mode.WIDE,
                        Layout.Panel.hidden(),
                        Layout.Panel.hidden(),
                        new Layout.Panel(0.0f, 0.0f, 1200.0f, 800.0f));

        InputOwnership ownership =
                InputOwnership.resolve(fullDisplay, false, 10.0f, 10.0f, true, true, true, true);

        assertTrue(ownership.canStartCameraCapture());
        assertTrue(ownership.allowsSimulationKeyboard());
    }

    private static InputOwnership resolve(Layout layout, float x, float y) {
        return InputOwnership.resolve(layout, true, x, y, false, false, false, false);
    }
}
