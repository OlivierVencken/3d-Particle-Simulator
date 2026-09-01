package com.particle.sim.ui;

import com.particle.sim.ui.sidebar.SidebarSection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterfaceStateTest {
    @Test
    void selectingASectionReopensSidebar() {
        InterfaceState state = new InterfaceState();
        state.setSidebarVisible(false);

        state.select(SidebarSection.CAMERA);

        assertEquals(SidebarSection.CAMERA, state.activeSection());
        assertTrue(state.sidebarVisible());
    }

    @Test
    void sidebarCanBeMinimizedAndRestored() {
        InterfaceState state = new InterfaceState();

        assertTrue(state.sidebarVisible());

        state.toggleSidebar();
        assertFalse(state.sidebarVisible());

        state.toggleSidebar();
        assertTrue(state.sidebarVisible());
    }

    @Test
    void selectingNullFallsBackToSimulation() {
        InterfaceState state = new InterfaceState();
        state.select(SidebarSection.MATRIX);

        state.select(null);

        assertEquals(SidebarSection.SIMULATION, state.activeSection());
    }

    @Test
    void selectingASectionMakesItActiveImmediately() {
        InterfaceState state = new InterfaceState();

        state.select(SidebarSection.CAMERA);

        assertEquals(SidebarSection.CAMERA, state.activeSection());
    }

    @Test
    void restoringSidebarKeepsTheActiveSection() {
        InterfaceState state = new InterfaceState();
        state.select(SidebarSection.MATRIX);
        state.setSidebarVisible(false);

        state.setSidebarVisible(true);

        assertEquals(SidebarSection.MATRIX, state.activeSection());
    }

    @Test
    void tracksResponsiveModeWithoutPersistingIt() {
        InterfaceState state = new InterfaceState();

        state.setLayoutMode(Layout.Mode.COMPACT);

        assertEquals(Layout.Mode.COMPACT, state.layoutMode());
    }

    @Test
    void sidebarTransitionMovesTowardItsVisibilityTarget() {
        InterfaceState state = new InterfaceState();

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
        InterfaceState state = new InterfaceState();
        state.setAnimationsEnabled(false);

        state.setSidebarVisible(false);
        state.select(SidebarSection.CAMERA);

        assertEquals(1.0f, state.sidebarReveal());
        assertEquals(1.0f, state.sectionReveal());
    }
}
