package com.particle.sim.particles;

import static org.lwjgl.opengl.GL43C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL43C.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL43C.glBindFramebuffer;
import static org.lwjgl.opengl.GL43C.glDisable;
import static org.lwjgl.opengl.GL43C.glEnable;
import static org.lwjgl.opengl.GL43C.glScissor;
import static org.lwjgl.opengl.GL43C.glViewport;

import com.particle.sim.math.Math3d;

/** Coordinates the particle, trail, and bloom rendering passes. */
public final class ParticleRenderer {
    private final ParticleRenderPass particlePass = new ParticleRenderPass();
    private final TrailRenderPass trailPass = new TrailRenderPass();
    private final BloomRenderPass bloomPass = new BloomRenderPass();

    public void init() {
        particlePass.init();
        trailPass.init();
        bloomPass.init();
    }

    void render(RenderFrame frame) {
        if (!frame.viewport().visible() || frame.particleCount() == 0) {
            return;
        }

        float[] viewProjection = viewProjection(frame);
        try {
            if (frame.glowEnabled()) {
                bloomPass.beginScene(frame);
            } else {
                glBindFramebuffer(GL_FRAMEBUFFER, 0);
                setViewportAndScissor(frame);
            }

            if (frame.trailsEnabled()) {
                trailPass.render(frame, viewProjection);
            }
            particlePass.render(frame, viewProjection);

            if (frame.glowEnabled()) {
                bloomPass.composite(frame);
            }
        } finally {
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glDisable(GL_SCISSOR_TEST);
        }
    }

    public void dispose() {
        particlePass.dispose();
        trailPass.dispose();
        bloomPass.dispose();
    }

    long allocatedEffectBytes() {
        return bloomPass.allocatedBytes();
    }

    int effectiveBloomDivisor() {
        return bloomPass.effectiveDivisor();
    }

    int effectiveTrailParticleStride() {
        return trailPass.effectiveParticleStride();
    }

    double particleRenderMilliseconds() {
        return particlePass.latestMilliseconds();
    }

    double trailRenderMilliseconds() {
        return trailPass.latestMilliseconds();
    }

    double bloomMilliseconds() {
        return bloomPass.latestMilliseconds();
    }

    private float[] viewProjection(RenderFrame frame) {
        float aspect = frame.viewport().width() / (float) frame.viewport().height();
        return Math3d.multiply(
                Math3d.perspective((float) Math.toRadians(60.0), aspect, 0.1f, 100.0f),
                frame.viewMatrix());
    }

    private void setViewportAndScissor(RenderFrame frame) {
        glViewport(
                frame.viewport().x(),
                frame.viewport().y(),
                frame.viewport().width(),
                frame.viewport().height());
        glEnable(GL_SCISSOR_TEST);
        glScissor(
                frame.viewport().x(),
                frame.viewport().y(),
                frame.viewport().width(),
                frame.viewport().height());
    }
}
