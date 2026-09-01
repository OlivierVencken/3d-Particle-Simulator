package com.particle.sim.ui;

/** Current-frame ownership decisions shared by camera and hotkey routing. */
public record InputOwnership(
        boolean pointerInSimulationViewport,
        boolean pointerOwnedByUi,
        boolean keyboardOwnedByUi,
        boolean modalOpen) {
    public static InputOwnership resolve(
            Layout layout,
            boolean uiVisible,
            float pointerX,
            float pointerY,
            boolean imguiWantsPointer,
            boolean imguiWantsKeyboard,
            boolean popupOpen,
            boolean modalOpen) {
        boolean pointerInSimulation = layout.simulation().contains(pointerX, pointerY);
        boolean pointerOverUiPanel =
                uiVisible
                        && (layout.commandBar().contains(pointerX, pointerY)
                                || layout.sidebar().contains(pointerX, pointerY));
        boolean uiOwnsPointer =
                uiVisible && (pointerOverUiPanel || imguiWantsPointer || popupOpen || modalOpen);
        boolean uiOwnsKeyboard = uiVisible && (imguiWantsKeyboard || popupOpen || modalOpen);
        return new InputOwnership(
                pointerInSimulation, uiOwnsPointer, uiOwnsKeyboard, uiVisible && modalOpen);
    }

    public boolean canStartCameraCapture() {
        return pointerInSimulationViewport && !pointerOwnedByUi && !modalOpen;
    }

    public boolean allowsSimulationKeyboard() {
        return !keyboardOwnedByUi && !modalOpen;
    }
}
