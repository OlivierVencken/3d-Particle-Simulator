package com.particle.sim.ui.workspace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceStateTest {
    @Test
    void selectingASectionReopensSidebar() {
        WorkspaceState state = new WorkspaceState();
        state.setSidebarVisible(false);

        state.select(UISection.CAMERA);

        assertEquals(UISection.CAMERA, state.activeSection());
        assertTrue(state.sidebarVisible());
    }

    @Test
    void sidebarCanBeMinimizedAndRestored() {
        WorkspaceState state = new WorkspaceState();

        assertTrue(state.sidebarVisible());

        state.toggleSidebar();
        assertFalse(state.sidebarVisible());

        state.toggleSidebar();
        assertTrue(state.sidebarVisible());
    }

    @Test
    void selectingNullFallsBackToSimulation() {
        WorkspaceState state = new WorkspaceState();
        state.select(UISection.INTERACTIONS);

        state.select(null);

        assertEquals(UISection.SIMULATION, state.activeSection());
    }

    @Test
    void pendingTabSelectionCannotBeOverwrittenByThePreviouslyVisibleTab() {
        WorkspaceState state = new WorkspaceState();
        state.activate(UISection.SIMULATION);

        state.select(UISection.CAMERA);
        state.activate(UISection.SIMULATION);

        assertEquals(UISection.CAMERA, state.activeSection());
        assertTrue(state.selectionRequested(UISection.CAMERA));

        state.activate(UISection.CAMERA);
        assertFalse(state.selectionRequested(UISection.CAMERA));
    }

    @Test
    void restoringSidebarRequestsTheActiveTab() {
        WorkspaceState state = new WorkspaceState();
        state.activate(UISection.SIMULATION);
        state.select(UISection.INTERACTIONS);
        state.activate(UISection.INTERACTIONS);
        state.setSidebarVisible(false);

        state.setSidebarVisible(true);

        assertTrue(state.selectionRequested(UISection.INTERACTIONS));
    }

    @Test
    void resetConfirmationIsExplicitlyOpenedAndClosed() {
        WorkspaceState state = new WorkspaceState();

        state.requestResetConfirmation();
        assertTrue(state.resetConfirmationOpen());

        state.closeResetConfirmation();
        assertFalse(state.resetConfirmationOpen());
    }

    @Test
    void tracksResponsiveModeWithoutPersistingIt() {
        WorkspaceState state = new WorkspaceState();

        state.setLayoutMode(WorkspaceLayout.Mode.COMPACT);

        assertEquals(WorkspaceLayout.Mode.COMPACT, state.layoutMode());
    }
}
