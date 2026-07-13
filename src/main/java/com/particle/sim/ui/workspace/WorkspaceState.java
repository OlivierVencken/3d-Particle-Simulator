package com.particle.sim.ui.workspace;

final class WorkspaceState {
    private UISection activeSection = UISection.SIMULATION;
    private boolean sidebarVisible = true;
    private UISection requestedSection = UISection.SIMULATION;
    private boolean resetConfirmationOpen;
    private WorkspaceLayout.Mode layoutMode = WorkspaceLayout.Mode.WIDE;

    UISection activeSection() {
        return activeSection;
    }

    void select(UISection section) {
        activeSection = section == null ? UISection.SIMULATION : section;
        sidebarVisible = true;
        requestedSection = activeSection;
    }

    void activate(UISection section) {
        UISection target = section == null ? UISection.SIMULATION : section;
        if (requestedSection != null && requestedSection != target) {
            return;
        }
        activeSection = target;
        requestedSection = null;
    }

    boolean selectionRequested(UISection section) {
        return requestedSection == section;
    }

    boolean sidebarVisible() {
        return sidebarVisible;
    }

    void setSidebarVisible(boolean visible) {
        if (visible && !sidebarVisible) {
            requestedSection = activeSection;
        }
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
