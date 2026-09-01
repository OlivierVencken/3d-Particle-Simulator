package com.particle.sim.ui.commandbar;

import com.particle.sim.ui.Layout;
import com.particle.sim.ui.theme.DesignTokens;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandBarTest {
    @Test
    void veryNarrowFocusLayoutsUseOneCompleteMenu() {
        DesignTokens tokens = DesignTokens.unscaled();

        assertTrue(CommandBar.usesUnifiedMenu(Layout.Mode.FOCUS, 377.0f, tokens));
        assertFalse(CommandBar.usesUnifiedMenu(Layout.Mode.FOCUS, 378.0f, tokens));
        assertFalse(CommandBar.usesUnifiedMenu(Layout.Mode.COMPACT, 377.0f, tokens));
    }

    @Test
    void compactThresholdScalesWithTheUi() {
        DesignTokens tokens = DesignTokens.atScale(1.5f);

        assertTrue(CommandBar.usesUnifiedMenu(Layout.Mode.FOCUS, 566.0f, tokens));
        assertFalse(CommandBar.usesUnifiedMenu(Layout.Mode.FOCUS, 567.0f, tokens));
    }
}
