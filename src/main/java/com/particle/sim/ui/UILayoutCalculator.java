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
        return calculate(displayWidth, displayHeight, sidebarVisible, true, tokens);
    }

    static UILayout calculate(float displayWidth, float displayHeight, boolean sidebarVisible,
            boolean uiVisible, UIDesignTokens tokens) {
        float width = Math.max(0.0f, displayWidth);
        float height = Math.max(0.0f, displayHeight);
        UILayout.Mode mode = modeFor(width, tokens);
        if (!uiVisible) {
            return new UILayout(
                    mode,
                    UILayout.Panel.hidden(),
                    UILayout.Panel.hidden(),
                    new UILayout.Panel(0.0f, 0.0f, width, height));
        }

        float contentY = Math.min(tokens.commandBarHeight(), height);
        float contentHeight = Math.max(0.0f, height - contentY);

        float sidebarWidth = sidebarVisible ? sidebarWidth(mode, width, tokens) : 0.0f;
        boolean sidebarOverlaysSimulation = mode == UILayout.Mode.COMPACT || mode == UILayout.Mode.FOCUS;

        UILayout.Panel commandBar = new UILayout.Panel(0.0f, 0.0f, width, contentY);
        UILayout.Panel sidebar = sidebarWidth > 0.0f && contentHeight > 0.0f
                ? new UILayout.Panel(0.0f, contentY, sidebarWidth, contentHeight)
                : UILayout.Panel.hidden();
        float simulationX = sidebarOverlaysSimulation ? 0.0f : sidebarWidth;
        float simulationWidth = Math.max(0.0f, width - simulationX);
        UILayout.Panel simulation = new UILayout.Panel(
                simulationX, contentY, simulationWidth, contentHeight);

        return new UILayout(mode, commandBar, sidebar, simulation);
    }

    private static float sidebarWidth(UILayout.Mode mode, float displayWidth, UIDesignTokens tokens) {
        float preferredWidth = switch (mode) {
            case WIDE -> tokens.sidebarWidth();
            case MEDIUM -> tokens.mediumSidebarWidth();
            case COMPACT -> tokens.sidebarWidth();
            case FOCUS -> displayWidth;
        };
        return Math.min(preferredWidth, displayWidth);
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
