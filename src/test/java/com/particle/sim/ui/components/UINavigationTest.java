package com.particle.sim.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.particle.sim.ui.theme.UIComponentVariant;
import org.junit.jupiter.api.Test;

class UINavigationTest {
    @Test
    void selectedStateMapsToSelectedVariant() {
        assertEquals(UIComponentVariant.GHOST, UINavigation.variant(false));
        assertEquals(UIComponentVariant.SELECTED, UINavigation.variant(true));
    }
}
