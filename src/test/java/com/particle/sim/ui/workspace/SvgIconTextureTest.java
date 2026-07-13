package com.particle.sim.ui.workspace;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SvgIconTextureTest {
    @Test
    void rasterizesSidebarIconWithVisibleAndTransparentPixels() {
        ByteBuffer pixels = SvgIconTexture.rasterizeResource(
                "/assets/icons/sidebar-toggle.svg", 64);
        boolean hasVisiblePixel = false;
        boolean hasTransparentPixel = false;

        for (int alphaIndex = 3; alphaIndex < pixels.capacity(); alphaIndex += 4) {
            int alpha = Byte.toUnsignedInt(pixels.get(alphaIndex));
            hasVisiblePixel |= alpha > 0;
            hasTransparentPixel |= alpha == 0;
        }

        assertTrue(hasVisiblePixel);
        assertTrue(hasTransparentPixel);
    }
}
