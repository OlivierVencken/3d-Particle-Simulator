package com.particle.sim.ui.workspace;

final class WorkspaceLayoutCalculator {
    static final float COMMAND_BAR_HEIGHT = 44.0f;
    static final float SIDEBAR_WIDTH = 420.0f;

    private WorkspaceLayoutCalculator() {
    }

    static WorkspaceLayout calculate(float displayWidth, float displayHeight, boolean sidebarVisible) {
        float width = Math.max(0.0f, displayWidth);
        float height = Math.max(0.0f, displayHeight);
        float workspaceY = Math.min(COMMAND_BAR_HEIGHT, height);
        float workspaceHeight = Math.max(0.0f, height - workspaceY);

        WorkspaceLayout.Mode mode = modeFor(width);
        float sidebarWidth = sidebarVisible ? Math.min(SIDEBAR_WIDTH, width) : 0.0f;

        WorkspaceLayout.Panel commandBar = new WorkspaceLayout.Panel(0.0f, 0.0f, width, workspaceY);
        WorkspaceLayout.Panel sidebar = sidebarWidth > 0.0f && workspaceHeight > 0.0f
                ? new WorkspaceLayout.Panel(0.0f, workspaceY, sidebarWidth, workspaceHeight)
                : WorkspaceLayout.Panel.hidden();
        float simulationX = sidebarWidth;
        float simulationWidth = Math.max(0.0f, width - sidebarWidth);
        WorkspaceLayout.Panel simulation = new WorkspaceLayout.Panel(
                simulationX, workspaceY, simulationWidth, workspaceHeight);

        return new WorkspaceLayout(mode, commandBar, sidebar, simulation);
    }

    private static WorkspaceLayout.Mode modeFor(float width) {
        if (width < 720.0f) {
            return WorkspaceLayout.Mode.FOCUS;
        }
        if (width < 1100.0f) {
            return WorkspaceLayout.Mode.COMPACT;
        }
        if (width < 1440.0f) {
            return WorkspaceLayout.Mode.MEDIUM;
        }
        return WorkspaceLayout.Mode.WIDE;
    }

}
