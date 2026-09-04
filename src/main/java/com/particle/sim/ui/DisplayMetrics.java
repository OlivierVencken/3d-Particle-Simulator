package com.particle.sim.ui;

import com.particle.sim.graphics.FramebufferViewport;
import imgui.ImGuiIO;

/** Explicit logical-display, design-scale, and framebuffer-pixel metrics. */
public record DisplayMetrics(
        float logicalWidth,
        float logicalHeight,
        float uiScale,
        float framebufferScaleX,
        float framebufferScaleY,
        int framebufferWidth,
        int framebufferHeight) {
    public DisplayMetrics {
        logicalWidth = finiteNonNegative(logicalWidth);
        logicalHeight = finiteNonNegative(logicalHeight);
        uiScale = positiveOrOne(uiScale);
        framebufferScaleX = positiveOrOne(framebufferScaleX);
        framebufferScaleY = positiveOrOne(framebufferScaleY);
        framebufferWidth = Math.max(0, framebufferWidth);
        framebufferHeight = Math.max(0, framebufferHeight);
    }

    public static DisplayMetrics from(ImGuiIO io, float uiScale) {
        float width = io.getDisplaySizeX();
        float height = io.getDisplaySizeY();
        float scaleX = positiveOrOne(io.getDisplayFramebufferScaleX());
        float scaleY = positiveOrOne(io.getDisplayFramebufferScaleY());
        return from(
                io,
                uiScale,
                Math.max(0, Math.round(width * scaleX)),
                Math.max(0, Math.round(height * scaleY)));
    }

    public static DisplayMetrics from(
            ImGuiIO io, float uiScale, int framebufferWidth, int framebufferHeight) {
        float width = io.getDisplaySizeX();
        float height = io.getDisplaySizeY();
        int safeFramebufferWidth = Math.max(0, framebufferWidth);
        int safeFramebufferHeight = Math.max(0, framebufferHeight);
        float scaleX =
                width > 0.0f && safeFramebufferWidth > 0
                        ? safeFramebufferWidth / width
                        : io.getDisplayFramebufferScaleX();
        float scaleY =
                height > 0.0f && safeFramebufferHeight > 0
                        ? safeFramebufferHeight / height
                        : io.getDisplayFramebufferScaleY();
        return new DisplayMetrics(
                width,
                height,
                uiScale,
                scaleX,
                scaleY,
                safeFramebufferWidth,
                safeFramebufferHeight);
    }

    public FramebufferViewport toFramebuffer(Layout.Panel panel) {
        int left = clamp(Math.round(panel.x() * framebufferScaleX), 0, framebufferWidth);
        int right = clamp(Math.round(panel.right() * framebufferScaleX), left, framebufferWidth);
        int top = clamp(Math.round(panel.y() * framebufferScaleY), 0, framebufferHeight);
        int bottom = clamp(Math.round(panel.bottom() * framebufferScaleY), top, framebufferHeight);
        return new FramebufferViewport(
                left, framebufferHeight - bottom, right - left, bottom - top);
    }

    private static float finiteNonNegative(float value) {
        return Float.isFinite(value) ? Math.max(0.0f, value) : 0.0f;
    }

    private static float positiveOrOne(float value) {
        return Float.isFinite(value) && value > 0.0f ? value : 1.0f;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
