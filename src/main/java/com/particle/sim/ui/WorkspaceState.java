package com.particle.sim.ui;

final class WorkspaceState {
    private UiSection activeSection = UiSection.SIMULATION;
    private boolean inspectorVisible = true;
    private boolean resetConfirmationOpen;
    private WorkspaceLayout.Mode layoutMode = WorkspaceLayout.Mode.WIDE;

    UiSection activeSection() {
        return activeSection;
    }

    void select(UiSection section) {
        activeSection = section == null ? UiSection.SIMULATION : section;
        inspectorVisible = true;
    }

    boolean inspectorVisible() {
        return inspectorVisible;
    }

    void setInspectorVisible(boolean visible) {
        inspectorVisible = visible;
    }

    boolean resetConfirmationOpen() {
        return resetConfirmationOpen;
    }

    void requestResetConfirmation() {
        resetConfirmationOpen = true;
    }

    void closeResetConfirmation() {
        resetConfirmationOpen = false;
    }

    WorkspaceLayout.Mode layoutMode() {
        return layoutMode;
    }

    void setLayoutMode(WorkspaceLayout.Mode layoutMode) {
        this.layoutMode = layoutMode;
    }
}
