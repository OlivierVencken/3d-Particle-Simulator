package com.particle.sim.input;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AppHotkeysTest {
    @Test
    void defaultHotkeysCoverEveryAction() {
        EnumSet<HotkeyAction> actions = EnumSet.noneOf(HotkeyAction.class);

        for (HotkeyDefinition hotkey : AppHotkeys.defaultHotkeys()) {
            actions.add(hotkey.action());
        }

        assertEquals(EnumSet.allOf(HotkeyAction.class), actions);
    }

    @Test
    void everyActionHasDisplayName() {
        for (HotkeyAction action : HotkeyAction.values()) {
            assertFalse(action.displayName().isBlank());
        }
    }
}
