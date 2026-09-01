package com.particle.sim.ui;

import com.particle.sim.ui.theme.DesignTokens;

final class LayoutCalculator {
    private LayoutCalculator() {
    }

    static Layout calculate(float displayWidth, float displayHeight, boolean sidebarVisible) {
        return calculate(displayWidth, displayHeight, sidebarVisible, DesignTokens.unscaled());
    }

    static Layout calculate(float displayWidth, float displayHeight, boolean sidebarVisible,
            DesignTokens tokens) {
        return calculate(displayWidth, displayHeight, sidebarVisible, true, tokens);
    }

    static Layout calculate(float displayWidth, float displayHeight, boolean sidebarVisible,
            boolean uiVisible, DesignTokens tokens) {
        return calculate(displayWidth, displayHeight, sidebarVisible ? 1.0f : 0.0f, uiVisible, tokens);
    }

    static Layout calculate(float displayWidth, float displayHeight, float sidebarReveal,
            boolean uiVisible, DesignTokens tokens) {
        float width = Math.max(0.0f, displayWidth);
        float height = Math.max(0.0f, displayHeight);
        Layout.Mode mode = modeFor(width, tokens);
        if (!uiVisible) {
            return new Layout(
                    mode,
                    Layout.Panel.hidden(),
                    Layout.Panel.hidden(),
                    new Layout.Panel(0.0f, 0.0f, width, height));
        }

        float contentY = Math.min(tokens.commandBarHeight(), height);
        float contentHeight = Math.max(0.0f, height - contentY);

        float reveal = clamp01(sidebarReveal);
        float fullSidebarWidth = sidebarWidth(mode, width, tokens);
        float revealedSidebarWidth = fullSidebarWidth * reveal;
        boolean sidebarOverlaysSimulation = mode == Layout.Mode.COMPACT || mode == Layout.Mode.FOCUS;

        Layout.Panel commandBar = new Layout.Panel(0.0f, 0.0f, width, contentY);
        Layout.Panel sidebar = revealedSidebarWidth > 0.0f && contentHeight > 0.0f
                ? new Layout.Panel(revealedSidebarWidth - fullSidebarWidth, contentY,
                        fullSidebarWidth, contentHeight)
                : Layout.Panel.hidden();
        float simulationX = sidebarOverlaysSimulation ? 0.0f : revealedSidebarWidth;
        float simulationWidth = Math.max(0.0f, width - simulationX);
        Layout.Panel simulation = new Layout.Panel(
                simulationX, contentY, simulationWidth, contentHeight);

        return new Layout(mode, commandBar, sidebar, simulation);
    }

    private static float clamp01(float value) {
        if (!Float.isFinite(value)) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float sidebarWidth(Layout.Mode mode, float displayWidth, DesignTokens tokens) {
        float preferredWidth = switch (mode) {
            case WIDE -> tokens.sidebarWidth();
            case MEDIUM -> tokens.mediumSidebarWidth();
            case COMPACT -> tokens.sidebarWidth();
            case FOCUS -> displayWidth;
        };
        return Math.min(preferredWidth, displayWidth);
    }

    private static Layout.Mode modeFor(float width, DesignTokens tokens) {
        if (width < tokens.compactBreakpoint()) {
            return Layout.Mode.FOCUS;
        }
        if (width < tokens.mediumBreakpoint()) {
            return Layout.Mode.COMPACT;
        }
        if (width < tokens.wideBreakpoint()) {
            return Layout.Mode.MEDIUM;
        }
        return Layout.Mode.WIDE;
    }

}
