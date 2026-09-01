package com.particle.sim.input;

import java.util.function.BooleanSupplier;

public record HotkeyBinding(
        int key,
        HotkeyAction action,
        HotkeyContext context,
        boolean activeWhileUiOwnsKeyboard,
        BooleanSupplier enabled) {}
