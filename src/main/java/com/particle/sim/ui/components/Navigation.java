package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.ComponentVariant;

/** Sidebar navigation item built on the shared button states. */
public final class Navigation {
    private Navigation() {}

    public static boolean item(String label, String id, boolean selected, float height) {
        return Button.text(label, "navigation-" + id, variant(selected), 0.0f, height, true);
    }

    static ComponentVariant variant(boolean selected) {
        return selected ? ComponentVariant.SELECTED : ComponentVariant.GHOST;
    }
}
