package com.particle.sim.ui.workspace;

final class WorkspaceState {
    private UISection activeSection = UISection.SIMULATION;
    private boolean sidebarVisible = true;
    private boolean resetConfirmationOpen;
    private WorkspaceLayout.Mode layoutMode = WorkspaceLayout.Mode.WIDE;

    UISection activeSection() {
        return activeSection;
    }

    void select(UISection section) {
        activeSection = section == null ? UISection.SIMULATION : section;
        sidebarVisible = true;
    }

    boolean sidebarVisible() {
        return sidebarVisible;
    }

    void setSidebarVisible(boolean visible) {
        sidebarVisible = visible;
    }

    void toggleSidebar() {
        setSidebarVisible(!sidebarVisible);
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
