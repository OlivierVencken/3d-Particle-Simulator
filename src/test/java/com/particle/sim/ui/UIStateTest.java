package com.particle.sim.ui;

import com.particle.sim.ui.sidebar.SidebarSection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIStateTest {
    @Test
    void selectingASectionReopensSidebar() {
        UIState state = new UIState();
        state.setSidebarVisible(false);

        state.select(SidebarSection.CAMERA);

        assertEquals(SidebarSection.CAMERA, state.activeSection());
        assertTrue(state.sidebarVisible());
    }

    @Test
    void sidebarCanBeMinimizedAndRestored() {
        UIState state = new UIState();

        assertTrue(state.sidebarVisible());

        state.toggleSidebar();
        assertFalse(state.sidebarVisible());

        state.toggleSidebar();
        assertTrue(state.sidebarVisible());
    }

    @Test
    void selectingNullFallsBackToSimulation() {
        UIState state = new UIState();
        state.select(SidebarSection.MATRIX);

        state.select(null);

        assertEquals(SidebarSection.SIMULATION, state.activeSection());
    }

    @Test
    void selectingASectionMakesItActiveImmediately() {
        UIState state = new UIState();

        state.select(SidebarSection.CAMERA);

        assertEquals(SidebarSection.CAMERA, state.activeSection());
    }

    @Test
    void restoringSidebarKeepsTheActiveSection() {
        UIState state = new UIState();
        state.select(SidebarSection.MATRIX);
        state.setSidebarVisible(false);

        state.setSidebarVisible(true);

        assertEquals(SidebarSection.MATRIX, state.activeSection());
    }

    @Test
    void tracksResponsiveModeWithoutPersistingIt() {
        UIState state = new UIState();

        state.setLayoutMode(UILayout.Mode.COMPACT);

        assertEquals(UILayout.Mode.COMPACT, state.layoutMode());
    }

    @Test
    void sidebarTransitionMovesTowardItsVisibilityTarget() {
        UIState state = new UIState();

        state.setSidebarVisible(false);
        state.advanceAnimations(0.08f);

        assertTrue(state.sidebarReveal() > 0.0f);
        assertTrue(state.sidebarReveal() < 1.0f);

        state.advanceAnimations(1.0f);
        state.advanceAnimations(1.0f);
        state.advanceAnimations(1.0f);
        assertEquals(0.0f, state.sidebarReveal());
    }

    @Test
    void reducedMotionSnapsTransitionsToTheirTargets() {
        UIState state = new UIState();
        state.setAnimationsEnabled(false);

        state.setSidebarVisible(false);
        state.select(SidebarSection.CAMERA);

        assertEquals(1.0f, state.sidebarReveal());
        assertEquals(1.0f, state.sectionReveal());
    }
}
