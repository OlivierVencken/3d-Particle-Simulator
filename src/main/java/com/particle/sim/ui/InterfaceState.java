package com.particle.sim.ui;

import com.particle.sim.ui.sidebar.SidebarSection;

public final class InterfaceState {
    private static final float SIDEBAR_TRANSITION_SECONDS = 0.24f;
    private static final float SECTION_TRANSITION_SECONDS = 0.16f;
    private SidebarSection activeSection = SidebarSection.SIMULATION;
    private boolean sidebarVisible = true;
    private Layout.Mode layoutMode = Layout.Mode.WIDE;
    private final Transition sidebarTransition = new Transition(1.0f, SIDEBAR_TRANSITION_SECONDS);
    private final Transition sectionTransition = new Transition(1.0f, SECTION_TRANSITION_SECONDS);
    private boolean animationsEnabled = !Boolean.getBoolean("particle.ui.reduceMotion");

    public SidebarSection activeSection() {
        return activeSection;
    }

    public void select(SidebarSection section) {
        SidebarSection nextSection = section == null ? SidebarSection.SIMULATION : section;
        if (nextSection != activeSection) {
            activeSection = nextSection;
            restartSectionTransition();
        }
        setSidebarVisible(true);
    }

    public boolean sidebarVisible() {
        return sidebarVisible;
    }

    public void setSidebarVisible(boolean visible) {
        sidebarVisible = visible;
        sidebarTransition.setTarget(visible ? 1.0f : 0.0f);
        if (!animationsEnabled) {
            sidebarTransition.snapTo(visible ? 1.0f : 0.0f);
        }
    }

    public void toggleSidebar() {
        setSidebarVisible(!sidebarVisible);
    }

    public Layout.Mode layoutMode() {
        return layoutMode;
    }

    public void setLayoutMode(Layout.Mode layoutMode) {
        this.layoutMode = layoutMode;
    }

    public float sidebarReveal() {
        return sidebarTransition.value();
    }

    public float sectionReveal() {
        return sectionTransition.value();
    }

    public void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
        if (!enabled) {
            sidebarTransition.snapTo(sidebarVisible ? 1.0f : 0.0f);
            sectionTransition.snapTo(1.0f);
        }
    }

    void advanceAnimations(float deltaTime) {
        if (!animationsEnabled) {
            return;
        }
        sidebarTransition.advance(deltaTime);
        sectionTransition.advance(deltaTime);
    }

    private void restartSectionTransition() {
        if (!animationsEnabled) {
            sectionTransition.snapTo(1.0f);
            return;
        }
        sectionTransition.snapTo(0.0f);
        sectionTransition.setTarget(1.0f);
    }
}
