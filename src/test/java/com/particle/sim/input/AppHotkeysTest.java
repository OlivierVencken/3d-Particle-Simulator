package com.particle.sim.input;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void onlyNonTextGlobalKeysRemainActiveWhileUiOwnsKeyboard() {
        for (HotkeyDefinition hotkey : AppHotkeys.defaultHotkeys()) {
            if (hotkey.activeWhileUiOwnsKeyboard()) {
                assertEquals(HotkeyContext.GLOBAL, hotkey.context());
            }
        }

        assertTrue(definition(HotkeyAction.TOGGLE_FULLSCREEN).activeWhileUiOwnsKeyboard());
        assertTrue(definition(HotkeyAction.SHOW_UI).activeWhileUiOwnsKeyboard());
        assertTrue(definition(HotkeyAction.TOGGLE_DEBUG).activeWhileUiOwnsKeyboard());
        assertFalse(definition(HotkeyAction.TOGGLE_UI).activeWhileUiOwnsKeyboard());
    }

    private static HotkeyDefinition definition(HotkeyAction action) {
        return AppHotkeys.defaultHotkeys().stream()
                .filter(hotkey -> hotkey.action() == action)
                .findFirst()
                .orElseThrow();
    }
}
