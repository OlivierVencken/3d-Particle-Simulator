package com.particle.sim.input;

/** Current-frame keyboard ownership used to filter application hotkeys. */
public record HotkeyRoutingContext(
        boolean simulationInputAllowed,
        boolean uiOwnsKeyboard,
        boolean modalOpen,
        boolean simulationCaptureActive) {
    boolean permits(HotkeyBinding binding) {
        if (binding.context() == HotkeyContext.SIMULATION) {
            return (simulationInputAllowed || simulationCaptureActive) && !modalOpen;
        }
        return ((!uiOwnsKeyboard || simulationCaptureActive) && !modalOpen)
                || binding.activeWhileUiOwnsKeyboard();
    }
}
