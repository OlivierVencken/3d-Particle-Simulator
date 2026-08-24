package com.particle.sim.ui;

/** Current-frame ownership decisions shared by camera and hotkey routing. */
public record UiInputOwnership(
        boolean pointerInSimulationViewport,
        boolean pointerOwnedByUi,
        boolean keyboardOwnedByUi,
        boolean modalOpen) {

    public static UiInputOwnership resolve(UILayout layout, boolean uiVisible,
            float pointerX, float pointerY,
            boolean imguiWantsPointer, boolean imguiWantsKeyboard,
            boolean popupOpen, boolean modalOpen) {
        boolean pointerInSimulation = layout.simulation().contains(pointerX, pointerY);
        boolean pointerOverUiPanel = uiVisible
                && (layout.commandBar().contains(pointerX, pointerY)
                || layout.sidebar().contains(pointerX, pointerY));
        boolean uiOwnsPointer = uiVisible
                && (pointerOverUiPanel || imguiWantsPointer || popupOpen || modalOpen);
        boolean uiOwnsKeyboard = uiVisible && (imguiWantsKeyboard || popupOpen || modalOpen);
        return new UiInputOwnership(
                pointerInSimulation,
                uiOwnsPointer,
                uiOwnsKeyboard,
                uiVisible && modalOpen);
    }

    public boolean canStartCameraCapture() {
        return pointerInSimulationViewport && !pointerOwnedByUi && !modalOpen;
    }

    public boolean allowsSimulationKeyboard() {
        return !keyboardOwnedByUi && !modalOpen;
    }
}
