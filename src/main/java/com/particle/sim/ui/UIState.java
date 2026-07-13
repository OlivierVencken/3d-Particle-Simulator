package com.particle.sim.ui;

import com.particle.sim.ui.sidebar.SidebarSection;

public final class UIState {
    private SidebarSection activeSection = SidebarSection.SIMULATION;
    private boolean sidebarVisible = true;
    private UILayout.Mode layoutMode = UILayout.Mode.WIDE;

    public SidebarSection activeSection() {
        return activeSection;
    }

    public void select(SidebarSection section) {
        activeSection = section == null ? SidebarSection.SIMULATION : section;
        sidebarVisible = true;
    }

    public boolean sidebarVisible() {
        return sidebarVisible;
    }

    public void setSidebarVisible(boolean visible) {
        sidebarVisible = visible;
    }

    public void toggleSidebar() {
        setSidebarVisible(!sidebarVisible);
    }

    public UILayout.Mode layoutMode() {
        return layoutMode;
    }

    public void setLayoutMode(UILayout.Mode layoutMode) {
        this.layoutMode = layoutMode;
    }
}
