package com.particle.sim.ui.workspace;

final class WorkspaceState {
    private UISection activeSection = UISection.SIMULATION;
    private boolean inspectorVisible = true;
    private boolean resetConfirmationOpen;
    private WorkspaceLayout.Mode layoutMode = WorkspaceLayout.Mode.WIDE;

    UISection activeSection() {
        return activeSection;
    }

    void select(UISection section) {
        activeSection = section == null ? UISection.SIMULATION : section;
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
