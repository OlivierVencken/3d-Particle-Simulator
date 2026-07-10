package com.particle.sim.particles;

import com.particle.sim.graphics.ShaderProgram;
import com.particle.sim.graphics.GpuTimerQuery;
import com.particle.sim.math.Math3d;
import com.particle.sim.settings.SimulationDefaults;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL43C.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL43C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL43C.GL_BLEND;
import static org.lwjgl.opengl.GL43C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL43C.GL_FLOAT;
import static org.lwjgl.opengl.GL43C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL43C.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL43C.GL_LINEAR;
import static org.lwjgl.opengl.GL43C.GL_POINTS;
import static org.lwjgl.opengl.GL43C.GL_RGBA;
import static org.lwjgl.opengl.GL43C.GL_RGBA16F;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE1;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL43C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL43C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL43C.glActiveTexture;
import static org.lwjgl.opengl.GL43C.glBindBufferBase;
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
import static org.lwjgl.opengl.GL43C.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL43C.glEnable;
import static org.lwjgl.opengl.GL43C.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL43C.glGenFramebuffers;
import static org.lwjgl.opengl.GL43C.glGenTextures;
import static org.lwjgl.opengl.GL43C.glGenVertexArrays;
import static org.lwjgl.opengl.GL43C.glGetUniformLocation;
import static org.lwjgl.opengl.GL43C.glIsEnabled;
import static org.lwjgl.opengl.GL43C.glTexImage2D;
import static org.lwjgl.opengl.GL43C.glTexParameteri;
import static org.lwjgl.opengl.GL43C.glUniform1f;
import static org.lwjgl.opengl.GL43C.glUniform1i;
import static org.lwjgl.opengl.GL43C.glUniform2f;
import static org.lwjgl.opengl.GL43C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL43C.glUseProgram;
import static org.lwjgl.opengl.GL43C.glViewport;

public final class ParticleRenderer {
    private static final int MAX_TRAIL_SEGMENTS = 4_000_000;
    private int renderProgram;
    private int trailProgram;
    private int bloomExtractProgram;
    private int blurProgram;
    private int glowCompositeProgram;

    private int particleVao;
    private int fullscreenVao;

    private int uViewProjectionLoc;
    private int uViewLoc;
    private int uPointSizeLoc;
    private int uFixedParticleScreenSizeLoc;
    private int uPointSizeReferenceDistanceLoc;
    private int uColorModeLoc;
    private int uGroupCountLoc;
    private int uMaxVelocityLoc;
    private int uBoundsLoc;
    private int uInteractionRangeLoc;
    private int uGridSizeLoc;

    private int uTrailViewProjectionLoc;
    private int uTrailViewLoc;
    private int uTrailViewportLoc;
    private int uTrailPointSizeLoc;
    private int uTrailFixedParticleScreenSizeLoc;
    private int uTrailPointSizeReferenceDistanceLoc;
    private int uTrailThicknessLoc;
    private int uTrailParticleCountLoc;
    private int uTrailParticleCapacityLoc;
    private int uTrailSampleCapacityLoc;
    private int uTrailNewestSampleIndexLoc;
    private int uTrailSampleCountLoc;
    private int uTrailRenderedParticleCountLoc;
    private int uTrailParticleStrideLoc;
    private int uTrailColorModeLoc;
    private int uTrailGroupCountLoc;
    private int uTrailMaxVelocityLoc;
    private int uTrailBoundsLoc;
    private int uTrailInteractionRangeLoc;
    private int uTrailGridSizeLoc;

    private int uBlurTextureLoc;
    private int uBlurDirectionLoc;
    private int uBlurRadiusLoc;
    private int uBlurFalloffLoc;
    private int uGlowSceneLoc;
    private int uGlowTextureLoc;
    private int uGlowStrengthLoc;
    private int uExtractSceneLoc;

    private int effectWidth;
    private int effectHeight;
    private int bloomWidth;
    private int bloomHeight;
    private int effectiveBloomDivisor = 4;
    private int sceneFbo;
    private int sceneTexture;
    private final int[] pingPongFbos = new int[2];
    private final int[] pingPongTextures = new int[2];
    private int effectiveTrailParticleStride = 1;
    private GpuTimerQuery particleTimer;
    private GpuTimerQuery trailTimer;
    private GpuTimerQuery bloomTimer;
    private float[] frameViewProjection;

    public void init() {
        renderProgram = ShaderProgram.render("/shaders/particle.vert", "/shaders/particle.frag");
        trailProgram = ShaderProgram.render("/shaders/trail.vert", "/shaders/trail.frag");
        bloomExtractProgram = ShaderProgram.render("/shaders/fullscreen.vert", "/shaders/bloom_extract.frag");
        blurProgram = ShaderProgram.render("/shaders/fullscreen.vert", "/shaders/blur.frag");
        glowCompositeProgram = ShaderProgram.render("/shaders/fullscreen.vert", "/shaders/bloom_composite.frag");
        particleTimer = new GpuTimerQuery();
        trailTimer = new GpuTimerQuery();
        bloomTimer = new GpuTimerQuery();

        particleVao = glGenVertexArrays();
        fullscreenVao = glGenVertexArrays();

        uViewProjectionLoc = glGetUniformLocation(renderProgram, "uViewProjection");
        uViewLoc = glGetUniformLocation(renderProgram, "uView");
        uPointSizeLoc = glGetUniformLocation(renderProgram, "uPointSize");
        uFixedParticleScreenSizeLoc = glGetUniformLocation(renderProgram, "uFixedParticleScreenSize");
        uPointSizeReferenceDistanceLoc = glGetUniformLocation(renderProgram, "uPointSizeReferenceDistance");
        uColorModeLoc = glGetUniformLocation(renderProgram, "uColorMode");
        uGroupCountLoc = glGetUniformLocation(renderProgram, "uGroupCount");
        uMaxVelocityLoc = glGetUniformLocation(renderProgram, "uMaxVelocity");
        uBoundsLoc = glGetUniformLocation(renderProgram, "uBounds");
        uInteractionRangeLoc = glGetUniformLocation(renderProgram, "uInteractionRange");
        uGridSizeLoc = glGetUniformLocation(renderProgram, "uGridSize");

        uTrailViewProjectionLoc = glGetUniformLocation(trailProgram, "uViewProjection");
        uTrailViewLoc = glGetUniformLocation(trailProgram, "uView");
        uTrailViewportLoc = glGetUniformLocation(trailProgram, "uViewport");
        uTrailPointSizeLoc = glGetUniformLocation(trailProgram, "uPointSize");
        uTrailFixedParticleScreenSizeLoc = glGetUniformLocation(trailProgram, "uFixedParticleScreenSize");
        uTrailPointSizeReferenceDistanceLoc = glGetUniformLocation(trailProgram, "uPointSizeReferenceDistance");
        uTrailThicknessLoc = glGetUniformLocation(trailProgram, "uTrailThickness");
        uTrailParticleCountLoc = glGetUniformLocation(trailProgram, "uParticleCount");
        uTrailParticleCapacityLoc = glGetUniformLocation(trailProgram, "uParticleCapacity");
        uTrailSampleCapacityLoc = glGetUniformLocation(trailProgram, "uSampleCapacity");
        uTrailNewestSampleIndexLoc = glGetUniformLocation(trailProgram, "uNewestSampleIndex");
        uTrailSampleCountLoc = glGetUniformLocation(trailProgram, "uSampleCount");
        uTrailRenderedParticleCountLoc = glGetUniformLocation(trailProgram, "uRenderedParticleCount");
        uTrailParticleStrideLoc = glGetUniformLocation(trailProgram, "uParticleStride");
        uTrailColorModeLoc = glGetUniformLocation(trailProgram, "uColorMode");
        uTrailGroupCountLoc = glGetUniformLocation(trailProgram, "uGroupCount");
        uTrailMaxVelocityLoc = glGetUniformLocation(trailProgram, "uMaxVelocity");
        uTrailBoundsLoc = glGetUniformLocation(trailProgram, "uBounds");
        uTrailInteractionRangeLoc = glGetUniformLocation(trailProgram, "uInteractionRange");
        uTrailGridSizeLoc = glGetUniformLocation(trailProgram, "uGridSize");

        uBlurTextureLoc = glGetUniformLocation(blurProgram, "uTexture");
        uBlurDirectionLoc = glGetUniformLocation(blurProgram, "uDirection");
        uBlurRadiusLoc = glGetUniformLocation(blurProgram, "uRadius");
        uBlurFalloffLoc = glGetUniformLocation(blurProgram, "uFalloff");
        uGlowSceneLoc = glGetUniformLocation(glowCompositeProgram, "uScene");
        uGlowTextureLoc = glGetUniformLocation(glowCompositeProgram, "uBloom");
        uGlowStrengthLoc = glGetUniformLocation(glowCompositeProgram, "uBloomStrength");
        uExtractSceneLoc = glGetUniformLocation(bloomExtractProgram, "uScene");
    }

    public void render(int width, int height, float[] viewMatrix, ParticleBuffers particleBuffers,
            SpatialGridBuffers spatialGridBuffers, int particleCount, float pointSize, boolean fixedParticleScreenSize,
            boolean glowEnabled, boolean trailsEnabled, int colorMode, int groupCount, float maxVelocity, float bounds,
            float interactionRange, GlowSettings glowSettings, TrailSettings trailSettings,
            TrailHistoryBuffers trailHistoryBuffers) {
        if (particleCount == 0) {
            return;
        }
        frameViewProjection = viewProjection(width, height, viewMatrix);

        if (glowEnabled) {
            renderGlow(width, height, viewMatrix, particleBuffers, spatialGridBuffers, particleCount, pointSize,
                    fixedParticleScreenSize, colorMode, groupCount, maxVelocity, bounds, interactionRange,
                    glowSettings, trailsEnabled, trailSettings, trailHistoryBuffers);
            return;
        }

        if (trailsEnabled) {
            renderTrails(width, height, viewMatrix, particleBuffers, spatialGridBuffers, trailHistoryBuffers,
                    particleCount, pointSize, fixedParticleScreenSize, colorMode, groupCount, maxVelocity, bounds,
                    interactionRange, trailSettings);
        }

        renderParticles(width, height, viewMatrix, particleBuffers, spatialGridBuffers, particleCount, pointSize,
                fixedParticleScreenSize, colorMode, groupCount, maxVelocity, bounds, interactionRange);
    }

    private void renderGlow(int width, int height, float[] viewMatrix, ParticleBuffers particleBuffers,
            SpatialGridBuffers spatialGridBuffers, int particleCount, float pointSize, boolean fixedParticleScreenSize,
            int colorMode, int groupCount, float maxVelocity, float bounds, float interactionRange,
            GlowSettings glowSettings, boolean trailsEnabled, TrailSettings trailSettings,
            TrailHistoryBuffers trailHistoryBuffers) {
        ensureGlowTargets(width, height, particleCount);

        glBindFramebuffer(GL_FRAMEBUFFER, sceneFbo);
        glViewport(0, 0, width, height);
        glClearColor(0.015f, 0.018f, 0.024f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        if (trailsEnabled) {
            renderTrails(width, height, viewMatrix, particleBuffers, spatialGridBuffers, trailHistoryBuffers,
                    particleCount, pointSize, fixedParticleScreenSize, colorMode, groupCount, maxVelocity, bounds,
                    interactionRange, trailSettings);
        }
        renderParticles(width, height, viewMatrix, particleBuffers, spatialGridBuffers, particleCount, pointSize,
                fixedParticleScreenSize, colorMode, groupCount, maxVelocity, bounds, interactionRange);

        boolean blendEnabled = glIsEnabled(GL_BLEND);
        glDisable(GL_BLEND);
        int sourceTexture;
        try {
            bloomTimer.begin();
            extractBloom();
            int sourceIndex = 0;
            sourceTexture = pingPongTextures[sourceIndex];
            for (int pass = 0; pass < glowSettings.blurPasses(); pass++) {
                int target = 1 - sourceIndex;
                blurTo(pingPongFbos[target], sourceTexture, pass % 2 == 0 ? 1.0f : 0.0f,
                        pass % 2 == 0 ? 0.0f : 1.0f, glowSettings);
                sourceTexture = pingPongTextures[target];
                sourceIndex = target;
            }

            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glViewport(0, 0, width, height);
            glUseProgram(glowCompositeProgram);
            glBindVertexArray(fullscreenVao);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, sceneTexture);
            glUniform1i(uGlowSceneLoc, 0);
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, sourceTexture);
            glUniform1i(uGlowTextureLoc, 1);
            glUniform1f(uGlowStrengthLoc, glowSettings.strength());
            glDrawArrays(GL_TRIANGLES, 0, 3);
            bloomTimer.end();
        } finally {
            if (blendEnabled) {
                glEnable(GL_BLEND);
            }
        }
    }

    private void renderParticles(int width, int height, float[] viewMatrix, ParticleBuffers particleBuffers,
            SpatialGridBuffers spatialGridBuffers, int particleCount, float pointSize, boolean fixedParticleScreenSize,
            int colorMode, int groupCount, float maxVelocity, float bounds, float interactionRange) {
        glUseProgram(renderProgram);
        glBindVertexArray(particleVao);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, particleBuffers.positionSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, particleBuffers.velocitySsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, spatialGridBuffers.countsSsbo());

        glUniformMatrix4fv(uViewProjectionLoc, false, frameViewProjection);
        if (uViewLoc != -1) {
            glUniformMatrix4fv(uViewLoc, false, viewMatrix);
        }
        glUniform1f(uPointSizeLoc, pointSize);
        if (uFixedParticleScreenSizeLoc != -1) {
            glUniform1i(uFixedParticleScreenSizeLoc, fixedParticleScreenSize ? 1 : 0);
        }
        if (uPointSizeReferenceDistanceLoc != -1) {
            glUniform1f(uPointSizeReferenceDistanceLoc, SimulationDefaults.POINT_SIZE_REFERENCE_DISTANCE);
        }
        if (uColorModeLoc != -1) {
            glUniform1i(uColorModeLoc, colorMode);
        }
        if (uGroupCountLoc != -1) {
            glUniform1i(uGroupCountLoc, groupCount);
        }
        if (uMaxVelocityLoc != -1) {
            glUniform1f(uMaxVelocityLoc, maxVelocity);
        }
        if (uBoundsLoc != -1) {
            glUniform1f(uBoundsLoc, bounds);
        }
        if (uInteractionRangeLoc != -1) {
            glUniform1f(uInteractionRangeLoc, interactionRange);
        }
        if (uGridSizeLoc != -1) {
            glUniform1i(uGridSizeLoc, SpatialGridSizing.gridSize(bounds, interactionRange));
        }
        particleTimer.begin();
        glDrawArrays(GL_POINTS, 0, particleCount);
        particleTimer.end();
    }

    private void renderTrails(int width, int height, float[] viewMatrix, ParticleBuffers particleBuffers,
            SpatialGridBuffers spatialGridBuffers, TrailHistoryBuffers trailHistoryBuffers, int particleCount,
            float pointSize, boolean fixedParticleScreenSize, int colorMode, int groupCount, float maxVelocity,
            float bounds, float interactionRange, TrailSettings trailSettings) {
        int activeSamples = Math.min(trailSettings.length(), trailHistoryBuffers.sampleCount());
        if (activeSamples < 2 || trailHistoryBuffers.historySsbo() == 0) {
            effectiveTrailParticleStride = 1;
            return;
        }

        int segmentsPerParticle = activeSamples - 1;
        long requestedSegments = (long) particleCount * segmentsPerParticle;
        effectiveTrailParticleStride = (int) Math.max(1L,
                Math.ceilDiv(requestedSegments, MAX_TRAIL_SEGMENTS));
        int renderedParticleCount = Math.ceilDiv(particleCount, effectiveTrailParticleStride);

        glUseProgram(trailProgram);
        glBindVertexArray(particleVao);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, particleBuffers.positionSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, particleBuffers.velocitySsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, spatialGridBuffers.countsSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, trailHistoryBuffers.historySsbo());

        glUniformMatrix4fv(uTrailViewProjectionLoc, false, frameViewProjection);
        glUniformMatrix4fv(uTrailViewLoc, false, viewMatrix);
        glUniform2f(uTrailViewportLoc, width, height);
        glUniform1f(uTrailPointSizeLoc, pointSize);
        glUniform1i(uTrailFixedParticleScreenSizeLoc, fixedParticleScreenSize ? 1 : 0);
        glUniform1f(uTrailPointSizeReferenceDistanceLoc, SimulationDefaults.POINT_SIZE_REFERENCE_DISTANCE);
        glUniform1f(uTrailThicknessLoc, Math.min(trailSettings.thickness(), pointSize));
        glUniform1i(uTrailParticleCountLoc, particleCount);
        glUniform1i(uTrailParticleCapacityLoc, trailHistoryBuffers.particleCapacity());
        glUniform1i(uTrailSampleCapacityLoc, trailHistoryBuffers.sampleCapacity());
        glUniform1i(uTrailNewestSampleIndexLoc, trailHistoryBuffers.newestSampleIndex());
        glUniform1i(uTrailSampleCountLoc, activeSamples);
        glUniform1i(uTrailRenderedParticleCountLoc, renderedParticleCount);
        glUniform1i(uTrailParticleStrideLoc, effectiveTrailParticleStride);
        glUniform1i(uTrailColorModeLoc, colorMode);
        glUniform1i(uTrailGroupCountLoc, groupCount);
        glUniform1f(uTrailMaxVelocityLoc, maxVelocity);
        glUniform1f(uTrailBoundsLoc, bounds);
        glUniform1f(uTrailInteractionRangeLoc, interactionRange);
        glUniform1i(uTrailGridSizeLoc, SpatialGridSizing.gridSize(bounds, interactionRange));

        int instanceCount = Math.multiplyExact(renderedParticleCount, segmentsPerParticle);
        trailTimer.begin();
        glDrawArraysInstanced(GL_TRIANGLES, 0, 6, instanceCount);
        trailTimer.end();
    }

    int effectiveTrailParticleStride() {
        return effectiveTrailParticleStride;
    }

    private void blurTo(int targetFbo, int sourceTexture, float directionX, float directionY,
            GlowSettings glowSettings) {
        glBindFramebuffer(GL_FRAMEBUFFER, targetFbo);
        glViewport(0, 0, bloomWidth, bloomHeight);
        glUseProgram(blurProgram);
        glBindVertexArray(fullscreenVao);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sourceTexture);
        glUniform1i(uBlurTextureLoc, 0);
        glUniform2f(uBlurDirectionLoc, directionX, directionY);
        glUniform1f(uBlurRadiusLoc, glowSettings.radius());
        glUniform1f(uBlurFalloffLoc, glowSettings.falloff());
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private void extractBloom() {
        glBindFramebuffer(GL_FRAMEBUFFER, pingPongFbos[0]);
        glViewport(0, 0, bloomWidth, bloomHeight);
        glUseProgram(bloomExtractProgram);
        glBindVertexArray(fullscreenVao);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sceneTexture);
        glUniform1i(uExtractSceneLoc, 0);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    private float[] viewProjection(int width, int height, float[] viewMatrix) {
        float aspect = width / (float) height;
        return Math3d.multiply(
                Math3d.perspective((float) Math.toRadians(60.0), aspect, 0.1f, 100.0f),
                viewMatrix);
    }

    private void ensureGlowTargets(int width, int height, int particleCount) {
        int desiredDivisor = particleCount <= 50_000 ? 2 : particleCount <= 250_000 ? 4 : 8;
        if (width == effectWidth && height == effectHeight && desiredDivisor == effectiveBloomDivisor
                && sceneFbo != 0) {
            return;
        }

        deleteGlowTargets();
        effectWidth = width;
        effectHeight = height;
        effectiveBloomDivisor = desiredDivisor;
        bloomWidth = Math.max(1, Math.ceilDiv(width, effectiveBloomDivisor));
        bloomHeight = Math.max(1, Math.ceilDiv(height, effectiveBloomDivisor));

        sceneTexture = createColorTexture(width, height);
        sceneFbo = createFramebuffer(sceneTexture);

        for (int i = 0; i < 2; i++) {
            pingPongTextures[i] = createColorTexture(bloomWidth, bloomHeight);
            pingPongFbos[i] = createFramebuffer(pingPongTextures[i]);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private int createColorTexture(int width, int height) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        return texture;
    }

    private int createFramebuffer(int texture) {
        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Could not create particle effect framebuffer.");
        }
        return fbo;
    }

    private void deleteGlowTargets() {
        glDeleteFramebuffers(sceneFbo);
        glDeleteTextures(sceneTexture);
        for (int i = 0; i < 2; i++) {
            glDeleteFramebuffers(pingPongFbos[i]);
            glDeleteTextures(pingPongTextures[i]);
            pingPongFbos[i] = 0;
            pingPongTextures[i] = 0;
        }
        sceneFbo = 0;
        sceneTexture = 0;
        bloomWidth = 0;
        bloomHeight = 0;
    }

    public void dispose() {
        deleteGlowTargets();
        glDeleteVertexArrays(particleVao);
        glDeleteVertexArrays(fullscreenVao);
        glDeleteProgram(renderProgram);
        glDeleteProgram(trailProgram);
        glDeleteProgram(bloomExtractProgram);
        glDeleteProgram(blurProgram);
        glDeleteProgram(glowCompositeProgram);
        disposeTimers();
    }

    long allocatedEffectBytes() {
        return ((long) effectWidth * effectHeight + (long) bloomWidth * bloomHeight * 2L)
                * 4L * Short.BYTES;
    }

    int effectiveBloomDivisor() {
        return effectiveBloomDivisor;
    }

    double particleRenderMilliseconds() {
        return particleTimer.latestMilliseconds();
    }

    double trailRenderMilliseconds() {
        return trailTimer.latestMilliseconds();
    }

    double bloomMilliseconds() {
        return bloomTimer.latestMilliseconds();
    }

    private void disposeTimers() {
        particleTimer.dispose();
        trailTimer.dispose();
        bloomTimer.dispose();
    }
}
