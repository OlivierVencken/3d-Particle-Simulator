package com.particle.sim.ui.theme;

import imgui.ImGui;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class UIThemeTest {
    @Test
    void reappliesScaledStyleFromTheCleanBase() {
        ImGui.createContext();
        try {
            UITheme.applyDarkTheme(2.0f);
            assertEquals(28.8f, ImGui.getStyle().getWindowPaddingX());

            UITheme.applyDarkTheme(1.25f);

            assertEquals(18.0f, ImGui.getStyle().getWindowPaddingX());
            assertEquals(9.0f, ImGui.getStyle().getFramePaddingX());
            assertEquals(1.125f, ImGui.getStyle().getFrameBorderSize());
            assertEquals(13.5f, ImGui.getStyle().getScrollbarSize());
        } finally {
            UITheme.applyDarkTheme(1.0f);
            ImGui.destroyContext();
        }
    }

    @Test
    void exposesStableAndDistinctSemanticComponentPalettes() {
        UIComponentPalette primary = UITheme.palette(UIComponentVariant.PRIMARY);

        assertSame(primary, UITheme.palette(UIComponentVariant.PRIMARY));
        assertNotEquals(primary.background(), UITheme.palette(UIComponentVariant.DESTRUCTIVE).background());
        assertEquals(UIColors.TEXT_MUTED, UITheme.palette(UIComponentVariant.DISABLED).text());
    }
}
