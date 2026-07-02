package com.particle.sim.input;

public record HotkeyDefinition(
        int key,
        HotkeyAction action,
        HotkeyContext context
) {
}
