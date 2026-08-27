package com.particle.sim.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiInputOwnershipTest {
    private static final UILayout PERSISTENT_LAYOUT = new UILayout(
            UILayout.Mode.WIDE,
            new UILayout.Panel(0.0f, 0.0f, 1200.0f, 40.0f),
            new UILayout.Panel(0.0f, 40.0f, 300.0f, 760.0f),
            new UILayout.Panel(300.0f, 40.0f, 900.0f, 760.0f));

    @Test
    void cameraCaptureStartsOnlyInsideSimulationViewport() {
        UiInputOwnership simulation = resolve(PERSISTENT_LAYOUT, 301.0f, 41.0f);
        UiInputOwnership sidebar = resolve(PERSISTENT_LAYOUT, 299.0f, 41.0f);
        UiInputOwnership commandBar = resolve(PERSISTENT_LAYOUT, 500.0f, 39.0f);

        assertTrue(simulation.canStartCameraCapture());
        assertFalse(sidebar.canStartCameraCapture());
        assertFalse(commandBar.canStartCameraCapture());
    }

    @Test
    void overlaySidebarOwnsPointerEvenThoughSimulationContinuesBehindIt() {
        UILayout overlayLayout = new UILayout(
                UILayout.Mode.COMPACT,
                new UILayout.Panel(0.0f, 0.0f, 1000.0f, 40.0f),
                new UILayout.Panel(0.0f, 40.0f, 300.0f, 760.0f),
                new UILayout.Panel(0.0f, 40.0f, 1000.0f, 760.0f));

        UiInputOwnership ownership = resolve(overlayLayout, 150.0f, 200.0f);

        assertTrue(ownership.pointerInSimulationViewport());
        assertTrue(ownership.pointerOwnedByUi());
        assertFalse(ownership.canStartCameraCapture());
    }

    @Test
    void popupsAndModalsBlockSimulationInput() {
        UiInputOwnership popup = UiInputOwnership.resolve(
                PERSISTENT_LAYOUT, true, 500.0f, 500.0f,
                false, false, true, false);
        UiInputOwnership modal = UiInputOwnership.resolve(
                PERSISTENT_LAYOUT, true, 500.0f, 500.0f,
                false, false, true, true);

        assertFalse(popup.canStartCameraCapture());
        assertFalse(popup.allowsSimulationKeyboard());
        assertFalse(modal.canStartCameraCapture());
        assertFalse(modal.allowsSimulationKeyboard());
    }

    @Test
    void hiddenUiReleasesItsStaleCaptureFlags() {
        UILayout fullDisplay = new UILayout(
                UILayout.Mode.WIDE,
                UILayout.Panel.hidden(),
                UILayout.Panel.hidden(),
                new UILayout.Panel(0.0f, 0.0f, 1200.0f, 800.0f));

        UiInputOwnership ownership = UiInputOwnership.resolve(
                fullDisplay, false, 10.0f, 10.0f,
                true, true, true, true);

        assertTrue(ownership.canStartCameraCapture());
        assertTrue(ownership.allowsSimulationKeyboard());
    }

    private static UiInputOwnership resolve(UILayout layout, float x, float y) {
        return UiInputOwnership.resolve(layout, true, x, y,
                false, false, false, false);
    }
}
