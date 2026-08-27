package com.particle.sim.ui;

/** OpenGL viewport in framebuffer pixels with a bottom-left origin. */
public record FramebufferViewport(int x, int y, int width, int height) {
    public FramebufferViewport {
        x = Math.max(0, x);
        y = Math.max(0, y);
        width = Math.max(0, width);
        height = Math.max(0, height);
    }

    public boolean visible() {
        return width > 0 && height > 0;
    }

    public int right() {
        return x + width;
    }

    public int top() {
        return y + height;
    }
}
