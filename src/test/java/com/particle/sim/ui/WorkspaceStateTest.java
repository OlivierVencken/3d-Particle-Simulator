package com.particle.sim.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceStateTest {
    @Test
    void selectingASectionReopensInspector() {
        WorkspaceState state = new WorkspaceState();
        state.setInspectorVisible(false);

        state.select(UiSection.CAMERA);

        assertEquals(UiSection.CAMERA, state.activeSection());
        assertTrue(state.inspectorVisible());
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
