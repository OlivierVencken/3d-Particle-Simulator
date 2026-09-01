package com.particle.sim.input;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HotkeyRoutingContextTest {
    private static final HotkeyBinding SIMULATION = binding(HotkeyContext.SIMULATION, false);
    private static final HotkeyBinding GLOBAL_TEXT_KEY = binding(HotkeyContext.GLOBAL, false);
    private static final HotkeyBinding GLOBAL_FUNCTION_KEY = binding(HotkeyContext.GLOBAL, true);

    @Test
    void normalSimulationContextPermitsBothScopes() {
        HotkeyRoutingContext context = new HotkeyRoutingContext(true, false, false, false);

        assertTrue(context.permits(SIMULATION));
        assertTrue(context.permits(GLOBAL_TEXT_KEY));
    }

    @Test
    void keyboardOwnedByUiBlocksSimulationAndTextGlobalKeys() {
        HotkeyRoutingContext context = new HotkeyRoutingContext(false, true, false, false);

        assertFalse(context.permits(SIMULATION));
        assertFalse(context.permits(GLOBAL_TEXT_KEY));
        assertTrue(context.permits(GLOBAL_FUNCTION_KEY));
    }

    @Test
    void modalBlocksSimulationButRetainsExplicitGlobalEscapeHatch() {
        HotkeyRoutingContext context = new HotkeyRoutingContext(false, true, true, false);

        assertFalse(context.permits(SIMULATION));
        assertFalse(context.permits(GLOBAL_TEXT_KEY));
        assertTrue(context.permits(GLOBAL_FUNCTION_KEY));
    }

    @Test
    void activeSimulationCaptureOverridesStaleUiKeyboardOwnership() {
        HotkeyRoutingContext context = new HotkeyRoutingContext(false, true, false, true);

        assertTrue(context.permits(SIMULATION));
        assertTrue(context.permits(GLOBAL_TEXT_KEY));
    }

    @Test
    void modalStillBlocksCapturedSimulationTextKeys() {
        HotkeyRoutingContext context = new HotkeyRoutingContext(false, true, true, true);

        assertFalse(context.permits(SIMULATION));
        assertFalse(context.permits(GLOBAL_TEXT_KEY));
        assertTrue(context.permits(GLOBAL_FUNCTION_KEY));
    }

    private static HotkeyBinding binding(HotkeyContext context, boolean activeWhileUiOwnsKeyboard) {
        return new HotkeyBinding(
                1, HotkeyAction.TOGGLE_UI, context, activeWhileUiOwnsKeyboard, () -> true);
    }
}
