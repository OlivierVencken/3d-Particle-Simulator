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
    void resetConfirmationIsExplicitlyOpenedAndClosed() {
        UIState state = new UIState();

        state.requestResetConfirmation();
        assertTrue(state.resetConfirmationOpen());

        state.closeResetConfirmation();
        assertFalse(state.resetConfirmationOpen());
    }

    @Test
    void tracksResponsiveModeWithoutPersistingIt() {
        UIState state = new UIState();

        state.setLayoutMode(UILayout.Mode.COMPACT);

        assertEquals(UILayout.Mode.COMPACT, state.layoutMode());
    }
}
