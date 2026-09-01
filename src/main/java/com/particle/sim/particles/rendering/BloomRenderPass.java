package com.particle.sim.particles.rendering;

import static org.lwjgl.opengl.GL43C.GL_BLEND;
import static org.lwjgl.opengl.GL43C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL43C.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL43C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL43C.GL_FLOAT;
import static org.lwjgl.opengl.GL43C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL43C.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL43C.GL_LINEAR;
import static org.lwjgl.opengl.GL43C.GL_RGBA;
import static org.lwjgl.opengl.GL43C.GL_RGBA16F;
import static org.lwjgl.opengl.GL43C.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE1;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL43C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL43C.glActiveTexture;
import static org.lwjgl.opengl.GL43C.glBindFramebuffer;
import static org.lwjgl.opengl.GL43C.glBindTexture;
import static org.lwjgl.opengl.GL43C.glBindVertexArray;
import static org.lwjgl.opengl.GL43C.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL43C.glClear;
import static org.lwjgl.opengl.GL43C.glClearColor;
import static org.lwjgl.opengl.GL43C.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL43C.glDeleteProgram;
import static org.lwjgl.opengl.GL43C.glDeleteTextures;
import static org.lwjgl.opengl.GL43C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL43C.glDisable;
import static org.lwjgl.opengl.GL43C.glDrawArrays;
import static org.lwjgl.opengl.GL43C.glEnable;
import static org.lwjgl.opengl.GL43C.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL43C.glGenFramebuffers;
import static org.lwjgl.opengl.GL43C.glGenTextures;
import static org.lwjgl.opengl.GL43C.glGenVertexArrays;
import static org.lwjgl.opengl.GL43C.glGetUniformLocation;
import static org.lwjgl.opengl.GL43C.glIsEnabled;
import static org.lwjgl.opengl.GL43C.glScissor;
import static org.lwjgl.opengl.GL43C.glTexImage2D;
import static org.lwjgl.opengl.GL43C.glTexParameteri;
import static org.lwjgl.opengl.GL43C.glUniform1f;
import static org.lwjgl.opengl.GL43C.glUniform1i;
import static org.lwjgl.opengl.GL43C.glUniform2f;
import static org.lwjgl.opengl.GL43C.glUseProgram;
import static org.lwjgl.opengl.GL43C.glViewport;

import com.particle.sim.graphics.GpuTimerQuery;
import com.particle.sim.graphics.ShaderProgram;
import java.nio.ByteBuffer;

final class BloomRenderPass {
    private int extractProgram;
    private int blurProgram;
    private int compositeProgram;
    private int fullscreenVao;
    private int blurTextureLocation;
    private int blurDirectionLocation;
    private int blurRadiusLocation;
    private int blurFalloffLocation;
    private int compositeSceneLocation;
    private int compositeBloomLocation;
    private int compositeStrengthLocation;
    private int extractSceneLocation;
    private int effectWidth;
    private int effectHeight;
    private int bloomWidth;
    private int bloomHeight;
    private int effectiveDivisor = 4;
    private int sceneFramebuffer;
    private int sceneTexture;
    private final int[] pingPongFramebuffers = new int[2];
    private final int[] pingPongTextures = new int[2];
    private GpuTimerQuery timer;

    void init() {
        extractProgram =
                ShaderProgram.render("/shaders/fullscreen.vert", "/shaders/bloom_extract.frag");
        blurProgram = ShaderProgram.render("/shaders/fullscreen.vert", "/shaders/blur.frag");
        compositeProgram =
                ShaderProgram.render("/shaders/fullscreen.vert", "/shaders/bloom_composite.frag");
        timer = new GpuTimerQuery();
        fullscreenVao = glGenVertexArrays();

        blurTextureLocation = glGetUniformLocation(blurProgram, "uTexture");
        blurDirectionLocation = glGetUniformLocation(blurProgram, "uDirection");
        blurRadiusLocation = glGetUniformLocation(blurProgram, "uRadius");
        blurFalloffLocation = glGetUniformLocation(blurProgram, "uFalloff");
        compositeSceneLocation = glGetUniformLocation(compositeProgram, "uScene");
        compositeBloomLocation = glGetUniformLocation(compositeProgram, "uBloom");
        compositeStrengthLocation = glGetUniformLocation(compositeProgram, "uBloomStrength");
        extractSceneLocation = glGetUniformLocation(extractProgram, "uScene");
    }

    void beginScene(RenderFrame frame) {
        int width = frame.viewport().width();
        int height = frame.viewport().height();
        ensureTargets(width, height, frame.particleCount());

        glBindFramebuffer(GL_FRAMEBUFFER, sceneFramebuffer);
        setViewportAndScissor(0, 0, width, height);
        glClearColor(0.031f, 0.031f, 0.031f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
    }

    void composite(RenderFrame frame) {
        GlowSettings settings = frame.glowSettings();
        boolean blendEnabled = glIsEnabled(GL_BLEND);
        glDisable(GL_BLEND);
        try {
            timer.begin();
            extractBloom();
            int sourceTexture = blur(settings);

            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            setViewportAndScissor(
                    frame.viewport().x(),
                    frame.viewport().y(),
                    frame.viewport().width(),
                    frame.viewport().height());
            glUseProgram(compositeProgram);
            glBindVertexArray(fullscreenVao);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, sceneTexture);
            glUniform1i(compositeSceneLocation, 0);
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, sourceTexture);
            glUniform1i(compositeBloomLocation, 1);
            glUniform1f(compositeStrengthLocation, settings.strength());
            glDrawArrays(GL_TRIANGLES, 0, 3);
            timer.end();
        } finally {
            if (blendEnabled) {
                glEnable(GL_BLEND);
            }
        }
    }

    long allocatedBytes() {
        return ((long) effectWidth * effectHeight + (long) bloomWidth * bloomHeight * 2L)
                * 4L
                * Short.BYTES;
    }

    int effectiveDivisor() {
        return effectiveDivisor;
    }

    double latestMilliseconds() {
        return timer.latestMilliseconds();
    }

    void dispose() {
        deleteTargets();
        glDeleteVertexArrays(fullscreenVao);
        glDeleteProgram(extractProgram);
        glDeleteProgram(blurProgram);
        glDeleteProgram(compositeProgram);
        timer.dispose();
    }

    private int blur(GlowSettings settings) {
        int sourceIndex = 0;
        int sourceTexture = pingPongTextures[sourceIndex];
        for (int pass = 0; pass < settings.blurPasses(); pass++) {
            int target = 1 - sourceIndex;
            blurTo(
                    pingPongFramebuffers[target],
                    sourceTexture,
                    pass % 2 == 0 ? 1.0f : 0.0f,
                    pass % 2 == 0 ? 0.0f : 1.0f,
                    settings);
            sourceTexture = pingPongTextures[target];
            sourceIndex = target;
        }
        return sourceTexture;
    }

    private void blurTo(
            int targetFramebuffer,
            int sourceTexture,
            float directionX,
            float directionY,
            GlowSettings settings) {
        glBindFramebuffer(GL_FRAMEBUFFER, targetFramebuffer);
        setViewportAndScissor(0, 0, bloomWidth, bloomHeight);
        glUseProgram(blurProgram);
        glBindVertexArray(fullscreenVao);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sourceTexture);
        glUniform1i(blurTextureLocation, 0);
        glUniform2f(blurDirectionLocation, directionX, directionY);
        glUniform1f(blurRadiusLocation, settings.radius());
        glUniform1f(blurFalloffLocation, settings.falloff());
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private void extractBloom() {
        glBindFramebuffer(GL_FRAMEBUFFER, pingPongFramebuffers[0]);
        setViewportAndScissor(0, 0, bloomWidth, bloomHeight);
        glUseProgram(extractProgram);
        glBindVertexArray(fullscreenVao);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sceneTexture);
        glUniform1i(extractSceneLocation, 0);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private void ensureTargets(int width, int height, int particleCount) {
        int desiredDivisor = particleCount <= 50_000 ? 2 : particleCount <= 250_000 ? 4 : 8;
        if (width == effectWidth
                && height == effectHeight
                && desiredDivisor == effectiveDivisor
                && sceneFramebuffer != 0) {
            return;
        }

        deleteTargets();
        effectWidth = width;
        effectHeight = height;
        effectiveDivisor = desiredDivisor;
        bloomWidth = Math.max(1, Math.ceilDiv(width, effectiveDivisor));
        bloomHeight = Math.max(1, Math.ceilDiv(height, effectiveDivisor));

        sceneTexture = createColorTexture(width, height);
        sceneFramebuffer = createFramebuffer(sceneTexture);
        for (int i = 0; i < 2; i++) {
            pingPongTextures[i] = createColorTexture(bloomWidth, bloomHeight);
            pingPongFramebuffers[i] = createFramebuffer(pingPongTextures[i]);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private int createColorTexture(int width, int height) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA16F,
                width,
                height,
                0,
                GL_RGBA,
                GL_FLOAT,
                (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        return texture;
    }

    private int createFramebuffer(int texture) {
        int framebuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Could not create particle effect framebuffer.");
        }
        return framebuffer;
    }

    private void deleteTargets() {
        glDeleteFramebuffers(sceneFramebuffer);
        glDeleteTextures(sceneTexture);
        for (int i = 0; i < 2; i++) {
            glDeleteFramebuffers(pingPongFramebuffers[i]);
            glDeleteTextures(pingPongTextures[i]);
            pingPongFramebuffers[i] = 0;
            pingPongTextures[i] = 0;
        }
        sceneFramebuffer = 0;
        sceneTexture = 0;
        bloomWidth = 0;
        bloomHeight = 0;
    }

    private void setViewportAndScissor(int x, int y, int width, int height) {
        glViewport(x, y, width, height);
        glEnable(GL_SCISSOR_TEST);
        glScissor(x, y, width, height);
    }
}
