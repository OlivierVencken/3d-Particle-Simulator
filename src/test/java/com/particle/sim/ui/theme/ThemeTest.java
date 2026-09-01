package com.particle.sim.ui.theme;

import imgui.ImGui;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ThemeTest {
    @Test
    void reappliesScaledStyleFromTheCleanBase() {
        ImGui.createContext();
        try {
            Theme.applyDarkTheme(2.0f);
            assertEquals(28.8f, ImGui.getStyle().getWindowPaddingX());

            Theme.applyDarkTheme(1.25f);

            assertEquals(18.0f, ImGui.getStyle().getWindowPaddingX());
            assertEquals(9.0f, ImGui.getStyle().getFramePaddingX());
            assertEquals(1.125f, ImGui.getStyle().getFrameBorderSize());
            assertEquals(13.5f, ImGui.getStyle().getScrollbarSize());
        } finally {
            Theme.applyDarkTheme(1.0f);
            ImGui.destroyContext();
        }
    }

    @Test
    void exposesStableAndDistinctSemanticComponentPalettes() {
        ComponentPalette primary = Theme.palette(ComponentVariant.PRIMARY);

        assertSame(primary, Theme.palette(ComponentVariant.PRIMARY));
        assertNotEquals(primary.background(), Theme.palette(ComponentVariant.DESTRUCTIVE).background());
        assertEquals(Colors.TEXT_MUTED, Theme.palette(ComponentVariant.DISABLED).text());
    }
}
