package com.particle.sim.particles;

import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL43C.glBindBufferBase;
import static org.lwjgl.opengl.GL43C.glBindVertexArray;
import static org.lwjgl.opengl.GL43C.glDeleteProgram;
import static org.lwjgl.opengl.GL43C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL43C.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL43C.glGenVertexArrays;
import static org.lwjgl.opengl.GL43C.glGetUniformLocation;
import static org.lwjgl.opengl.GL43C.glUniform1f;
import static org.lwjgl.opengl.GL43C.glUniform1i;
import static org.lwjgl.opengl.GL43C.glUniform2f;
import static org.lwjgl.opengl.GL43C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL43C.glUseProgram;

import com.particle.sim.graphics.GpuTimerQuery;
import com.particle.sim.graphics.ShaderProgram;
import com.particle.sim.settings.SimulationDefaults;

final class TrailRenderPass {
    private static final int MAX_TRAIL_SEGMENTS = 4_000_000;
    private int program;
    private int vao;
    private int viewProjectionLocation;
    private int viewLocation;
    private int viewportLocation;
    private int pointSizeLocation;
    private int fixedParticleScreenSizeLocation;
    private int pointSizeReferenceDistanceLocation;
    private int thicknessLocation;
    private int particleCountLocation;
    private int particleCapacityLocation;
    private int sampleCapacityLocation;
    private int newestSampleIndexLocation;
    private int sampleCountLocation;
    private int renderedParticleCountLocation;
    private int particleStrideLocation;
    private int colorModeLocation;
    private int groupCountLocation;
    private int maximumVelocityLocation;
    private int boundsLocation;
    private int interactionRangeLocation;
    private int gridSizeLocation;
    private int effectiveParticleStride = 1;
    private GpuTimerQuery timer;

    void init() {
        program = ShaderProgram.render("/shaders/trail.vert", "/shaders/trail.frag");
        timer = new GpuTimerQuery();
        vao = glGenVertexArrays();

        viewProjectionLocation = glGetUniformLocation(program, "uViewProjection");
        viewLocation = glGetUniformLocation(program, "uView");
        viewportLocation = glGetUniformLocation(program, "uViewport");
        pointSizeLocation = glGetUniformLocation(program, "uPointSize");
        fixedParticleScreenSizeLocation = glGetUniformLocation(program, "uFixedParticleScreenSize");
        pointSizeReferenceDistanceLocation =
                glGetUniformLocation(program, "uPointSizeReferenceDistance");
        thicknessLocation = glGetUniformLocation(program, "uTrailThickness");
        particleCountLocation = glGetUniformLocation(program, "uParticleCount");
        particleCapacityLocation = glGetUniformLocation(program, "uParticleCapacity");
        sampleCapacityLocation = glGetUniformLocation(program, "uSampleCapacity");
        newestSampleIndexLocation = glGetUniformLocation(program, "uNewestSampleIndex");
        sampleCountLocation = glGetUniformLocation(program, "uSampleCount");
        renderedParticleCountLocation = glGetUniformLocation(program, "uRenderedParticleCount");
        particleStrideLocation = glGetUniformLocation(program, "uParticleStride");
        colorModeLocation = glGetUniformLocation(program, "uColorMode");
        groupCountLocation = glGetUniformLocation(program, "uGroupCount");
        maximumVelocityLocation = glGetUniformLocation(program, "uMaxVelocity");
        boundsLocation = glGetUniformLocation(program, "uBounds");
        interactionRangeLocation = glGetUniformLocation(program, "uInteractionRange");
        gridSizeLocation = glGetUniformLocation(program, "uGridSize");
    }

    void render(RenderFrame frame, float[] viewProjection) {
        TrailHistoryBuffers history = frame.trailHistoryBuffers();
        TrailSettings settings = frame.trailSettings();
        int activeSamples = Math.min(settings.length(), history.sampleCount());
        if (activeSamples < 2 || history.historySsbo() == 0) {
            effectiveParticleStride = 1;
            return;
        }

        int segmentsPerParticle = activeSamples - 1;
        long requestedSegments = (long) frame.particleCount() * segmentsPerParticle;
        effectiveParticleStride =
                (int) Math.max(1L, Math.ceilDiv(requestedSegments, MAX_TRAIL_SEGMENTS));
        int renderedParticleCount = Math.ceilDiv(frame.particleCount(), effectiveParticleStride);

        glUseProgram(program);
        glBindVertexArray(vao);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, frame.particleBuffers().positionSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, frame.particleBuffers().velocitySsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, frame.spatialGridBuffers().countsSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, history.historySsbo());

        glUniformMatrix4fv(viewProjectionLocation, false, viewProjection);
        glUniformMatrix4fv(viewLocation, false, frame.viewMatrix());
        glUniform2f(viewportLocation, frame.viewport().width(), frame.viewport().height());
        glUniform1f(pointSizeLocation, frame.pointSize());
        glUniform1i(fixedParticleScreenSizeLocation, frame.fixedParticleScreenSize() ? 1 : 0);
        glUniform1f(
                pointSizeReferenceDistanceLocation,
                SimulationDefaults.POINT_SIZE_REFERENCE_DISTANCE);
        glUniform1f(thicknessLocation, Math.min(settings.thickness(), frame.pointSize()));
        glUniform1i(particleCountLocation, frame.particleCount());
        glUniform1i(particleCapacityLocation, history.particleCapacity());
        glUniform1i(sampleCapacityLocation, history.sampleCapacity());
        glUniform1i(newestSampleIndexLocation, history.newestSampleIndex());
        glUniform1i(sampleCountLocation, activeSamples);
        glUniform1i(renderedParticleCountLocation, renderedParticleCount);
        glUniform1i(particleStrideLocation, effectiveParticleStride);
        glUniform1i(colorModeLocation, frame.colorMode());
        glUniform1i(groupCountLocation, frame.groupCount());
        glUniform1f(maximumVelocityLocation, frame.maximumVelocity());
        glUniform1f(boundsLocation, frame.bounds());
        glUniform1f(interactionRangeLocation, frame.interactionRange());
        glUniform1i(gridSizeLocation, frame.gridSize());

        int instanceCount = Math.multiplyExact(renderedParticleCount, segmentsPerParticle);
        timer.begin();
        glDrawArraysInstanced(GL_TRIANGLES, 0, 6, instanceCount);
        timer.end();
    }

    int effectiveParticleStride() {
        return effectiveParticleStride;
    }

    double latestMilliseconds() {
        return timer.latestMilliseconds();
    }

    void dispose() {
        glDeleteVertexArrays(vao);
        glDeleteProgram(program);
        timer.dispose();
    }
}
