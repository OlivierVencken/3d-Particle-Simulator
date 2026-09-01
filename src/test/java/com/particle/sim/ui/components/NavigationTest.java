package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.particle.sim.ui.theme.ComponentVariant;
import org.junit.jupiter.api.Test;

class NavigationTest {
    @Test
    void selectedStateMapsToSelectedVariant() {
        assertEquals(ComponentVariant.GHOST, Navigation.variant(false));
        assertEquals(ComponentVariant.SELECTED, Navigation.variant(true));
    }
}
