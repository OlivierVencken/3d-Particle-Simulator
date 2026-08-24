package com.particle.sim.ui;

import com.particle.sim.ui.theme.UIDesignTokens;

final class UILayoutCalculator {
    private UILayoutCalculator() {
    }

    static UILayout calculate(float displayWidth, float displayHeight, boolean sidebarVisible) {
        return calculate(displayWidth, displayHeight, sidebarVisible, UIDesignTokens.unscaled());
    }

    static UILayout calculate(float displayWidth, float displayHeight, boolean sidebarVisible,
            UIDesignTokens tokens) {
        float width = Math.max(0.0f, displayWidth);
        float height = Math.max(0.0f, displayHeight);
        float contentY = Math.min(tokens.commandBarHeight(), height);
        float contentHeight = Math.max(0.0f, height - contentY);

        UILayout.Mode mode = modeFor(width, tokens);
        float sidebarWidth = sidebarVisible ? Math.min(tokens.sidebarWidth(), width) : 0.0f;

        UILayout.Panel commandBar = new UILayout.Panel(0.0f, 0.0f, width, contentY);
        UILayout.Panel sidebar = sidebarWidth > 0.0f && contentHeight > 0.0f
                ? new UILayout.Panel(0.0f, contentY, sidebarWidth, contentHeight)
                : UILayout.Panel.hidden();
        float simulationX = sidebarWidth;
        float simulationWidth = Math.max(0.0f, width - sidebarWidth);
        UILayout.Panel simulation = new UILayout.Panel(
                simulationX, contentY, simulationWidth, contentHeight);

        return new UILayout(mode, commandBar, sidebar, simulation);
    }

    private static UILayout.Mode modeFor(float width, UIDesignTokens tokens) {
        if (width < tokens.compactBreakpoint()) {
            return UILayout.Mode.FOCUS;
        }
        if (width < tokens.mediumBreakpoint()) {
            return UILayout.Mode.COMPACT;
        }
        if (width < tokens.wideBreakpoint()) {
            return UILayout.Mode.MEDIUM;
        }
        return UILayout.Mode.WIDE;
    }

}
