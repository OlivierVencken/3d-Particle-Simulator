package com.particle.sim.ui;

final class UILayoutCalculator {
    static final float COMMAND_BAR_HEIGHT = 36.0f;
    static final float SIDEBAR_WIDTH = 420.0f;

    private UILayoutCalculator() {
    }

    static UILayout calculate(float displayWidth, float displayHeight, boolean sidebarVisible) {
        float width = Math.max(0.0f, displayWidth);
        float height = Math.max(0.0f, displayHeight);
        float contentY = Math.min(COMMAND_BAR_HEIGHT, height);
        float contentHeight = Math.max(0.0f, height - contentY);

        UILayout.Mode mode = modeFor(width);
        float sidebarWidth = sidebarVisible ? Math.min(SIDEBAR_WIDTH, width) : 0.0f;

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

    private static UILayout.Mode modeFor(float width) {
        if (width < 720.0f) {
            return UILayout.Mode.FOCUS;
        }
        if (width < 1100.0f) {
            return UILayout.Mode.COMPACT;
        }
        if (width < 1440.0f) {
            return UILayout.Mode.MEDIUM;
        }
        return UILayout.Mode.WIDE;
    }

}
