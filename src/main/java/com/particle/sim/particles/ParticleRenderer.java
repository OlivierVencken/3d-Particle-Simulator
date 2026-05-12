package com.particle.sim.particles;

import com.particle.sim.graphics.ShaderProgram;
import com.particle.sim.math.Math3d;
import com.particle.sim.settings.SimulationDefaults;

import static org.lwjgl.opengl.GL43C.GL_POINTS;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.glBindBufferBase;
import static org.lwjgl.opengl.GL43C.glBindVertexArray;
import static org.lwjgl.opengl.GL43C.glDeleteProgram;
import static org.lwjgl.opengl.GL43C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL43C.glDrawArrays;
import static org.lwjgl.opengl.GL43C.glGenVertexArrays;
import static org.lwjgl.opengl.GL43C.glGetUniformLocation;
import static org.lwjgl.opengl.GL43C.glUniform1f;
import static org.lwjgl.opengl.GL43C.glUniform1i;
import static org.lwjgl.opengl.GL43C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL43C.glUseProgram;

public final class ParticleRenderer {
    private int renderProgram;
    private int vao;
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
    private int uMapSizeLoc;

    public void init() {
        renderProgram = ShaderProgram.render("/shaders/particle.vert", "/shaders/particle.frag");
        vao = glGenVertexArrays();
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
        uMapSizeLoc = glGetUniformLocation(renderProgram, "uMapSize");
    }

    public void render(int width, int height, float[] viewMatrix, ParticleBuffers particleBuffers,
            SpatialGridBuffers spatialGridBuffers, int particleCount, float pointSize, boolean fixedParticleScreenSize,
            int colorMode, int groupCount, float maxVelocity, float bounds, float interactionRange, int spatialMapSize) {
        if (particleCount == 0) {
            return;
        }

        glUseProgram(renderProgram);
        glBindVertexArray(vao);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, particleBuffers.positionSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, particleBuffers.velocitySsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, spatialGridBuffers.countsSsbo());
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, spatialGridBuffers.keysSsbo());

        float aspect = width / (float) height;
        float[] viewProjection = Math3d.multiply(
                Math3d.perspective((float) Math.toRadians(60.0), aspect, 0.1f, 100.0f),
                viewMatrix);

        glUniformMatrix4fv(uViewProjectionLoc, false, viewProjection);
        if (uViewLoc != -1)
            glUniformMatrix4fv(uViewLoc, false, viewMatrix);
        glUniform1f(uPointSizeLoc, pointSize);
        if (uFixedParticleScreenSizeLoc != -1)
            glUniform1i(uFixedParticleScreenSizeLoc, fixedParticleScreenSize ? 1 : 0);
        if (uPointSizeReferenceDistanceLoc != -1)
            glUniform1f(uPointSizeReferenceDistanceLoc, SimulationDefaults.POINT_SIZE_REFERENCE_DISTANCE);
        if (uColorModeLoc != -1)
            glUniform1i(uColorModeLoc, colorMode);
        if (uGroupCountLoc != -1)
            glUniform1i(uGroupCountLoc, groupCount);
        if (uMaxVelocityLoc != -1)
            glUniform1f(uMaxVelocityLoc, maxVelocity);
        if (uBoundsLoc != -1)
            glUniform1f(uBoundsLoc, bounds);
        if (uInteractionRangeLoc != -1)
            glUniform1f(uInteractionRangeLoc, interactionRange);
        if (uMapSizeLoc != -1)
            glUniform1i(uMapSizeLoc, spatialMapSize);
        glDrawArrays(GL_POINTS, 0, particleCount);
    }

    public void dispose() {
        glDeleteVertexArrays(vao);
        glDeleteProgram(renderProgram);
    }
}
