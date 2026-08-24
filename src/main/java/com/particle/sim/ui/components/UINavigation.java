package com.particle.sim.ui.components;

import com.particle.sim.ui.theme.UIComponentVariant;

/** Sidebar navigation item built on the shared button states. */
public final class UINavigation {
    private UINavigation() {
    }

    public static boolean item(String label, String id, boolean selected, float height) {
        return UIButton.text(label, "navigation-" + id, variant(selected),
                0.0f, height, true);
    }

    static UIComponentVariant variant(boolean selected) {
        return selected ? UIComponentVariant.SELECTED : UIComponentVariant.GHOST;
    }
}
