package com.particle.sim.ui.commandbar;

import com.particle.sim.ui.UILayout;
import com.particle.sim.ui.theme.UIDesignTokens;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandBarTest {
    @Test
    void veryNarrowFocusLayoutsUseOneCompleteMenu() {
        UIDesignTokens tokens = UIDesignTokens.unscaled();

        assertTrue(CommandBar.usesUnifiedMenu(UILayout.Mode.FOCUS, 419.0f, tokens));
        assertFalse(CommandBar.usesUnifiedMenu(UILayout.Mode.FOCUS, 420.0f, tokens));
        assertFalse(CommandBar.usesUnifiedMenu(UILayout.Mode.COMPACT, 419.0f, tokens));
    }

    @Test
    void compactThresholdScalesWithTheUi() {
        UIDesignTokens tokens = UIDesignTokens.atScale(1.5f);

        assertTrue(CommandBar.usesUnifiedMenu(UILayout.Mode.FOCUS, 629.0f, tokens));
        assertFalse(CommandBar.usesUnifiedMenu(UILayout.Mode.FOCUS, 630.0f, tokens));
    }
}
