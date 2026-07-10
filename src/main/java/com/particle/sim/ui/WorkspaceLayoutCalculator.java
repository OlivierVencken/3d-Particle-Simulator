package com.particle.sim.ui;

final class WorkspaceLayoutCalculator {
    static final float COMMAND_BAR_HEIGHT = 44.0f;
    static final float STATUS_BAR_HEIGHT = 26.0f;
    static final float MIN_SIMULATION_STRIP = 360.0f;

    private WorkspaceLayoutCalculator() {
    }

    static WorkspaceLayout calculate(float displayWidth, float displayHeight, UiSection section,
            boolean inspectorVisible) {
        float width = Math.max(0.0f, displayWidth);
        float height = Math.max(0.0f, displayHeight);
        float workspaceY = Math.min(COMMAND_BAR_HEIGHT, height);
        float statusHeight = Math.min(STATUS_BAR_HEIGHT, Math.max(0.0f, height - workspaceY));
        float workspaceHeight = Math.max(0.0f, height - workspaceY - statusHeight);

        WorkspaceLayout.Mode mode = modeFor(width);
        float navigationWidth = navigationWidth(mode);
        float inspectorWidth = inspectorVisible ? inspectorWidth(width, mode, section) : 0.0f;

        WorkspaceLayout.Panel commandBar = new WorkspaceLayout.Panel(0.0f, 0.0f, width, workspaceY);
        WorkspaceLayout.Panel statusBar = new WorkspaceLayout.Panel(
                0.0f, workspaceY + workspaceHeight, width, statusHeight);
        WorkspaceLayout.Panel navigation = navigationWidth > 0.0f
                ? new WorkspaceLayout.Panel(0.0f, workspaceY, navigationWidth, workspaceHeight)
                : WorkspaceLayout.Panel.hidden();
        WorkspaceLayout.Panel inspector = inspectorWidth > 0.0f
                ? new WorkspaceLayout.Panel(width - inspectorWidth, workspaceY, inspectorWidth, workspaceHeight)
                : WorkspaceLayout.Panel.hidden();
        float simulationX = navigationWidth;
        float simulationWidth = Math.max(0.0f, width - navigationWidth - inspectorWidth);
        WorkspaceLayout.Panel simulation = new WorkspaceLayout.Panel(
                simulationX, workspaceY, simulationWidth, workspaceHeight);

        return new WorkspaceLayout(mode, commandBar, navigation, simulation, inspector, statusBar);
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

    private static float navigationWidth(WorkspaceLayout.Mode mode) {
        return switch (mode) {
            case WIDE -> 176.0f;
            case MEDIUM -> 152.0f;
            case COMPACT, FOCUS -> 0.0f;
        };
    }

    private static float inspectorWidth(float displayWidth, WorkspaceLayout.Mode mode, UiSection section) {
        if (mode == WorkspaceLayout.Mode.FOCUS) {
            return displayWidth;
        }
        if (section != UiSection.INTERACTIONS) {
            return switch (mode) {
                case WIDE -> 368.0f;
                case MEDIUM -> 336.0f;
                case COMPACT -> Math.min(320.0f, displayWidth);
                case FOCUS -> displayWidth;
            };
        }

        float preferred = switch (mode) {
            case WIDE -> 600.0f;
            case MEDIUM -> 500.0f;
            case COMPACT -> 500.0f;
            case FOCUS -> displayWidth;
        };
        return Math.min(preferred, Math.max(0.0f, displayWidth - MIN_SIMULATION_STRIP));
    }
}
